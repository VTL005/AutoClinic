import {
    useMutation,
    useQuery,
    useQueryClient,
} from '@tanstack/react-query'
import axios from 'axios'
import {
    ArrowRight,
    CalendarCheck2,
    CalendarDays,
    CarFront,
    CheckCircle2,
    CircleAlert,
    Clock3,
    Filter,
    LoaderCircle,
    Plus,
    RefreshCw,
    Send,
    Sparkles,
    UserRound,
    X,
    XCircle,
} from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { bookingService } from '../../services/bookingService'
import { vehicleService } from '../../services/vehicleService'
import type { ErrorResponse } from '../../types/api'
import type {
    Booking,
    BookingStatus,
    CreateBookingRequest,
} from '../../types/booking'
import './CustomerAppointmentsPage.css'

type BookingForm = {
    vehicleId: string
    serviceId: string
    mechanicUserId: string
    requestedDate: string
    requestedTime: string
    customerNote: string
}

type BookingFormErrors = Partial<
    Record<keyof BookingForm, string>
>

const emptyForm: BookingForm = {
    vehicleId: '',
    serviceId: '',
    mechanicUserId: '',
    requestedDate: '',
    requestedTime: '',
    customerNote: '',
}

const statusLabels: Record<BookingStatus, string> = {
    PENDING: 'Chờ xác nhận',
    CONFIRMED: 'Đã xác nhận',
    IN_PROGRESS: 'Đang thực hiện',
    COMPLETED: 'Đã hoàn thành',
    CANCELLED: 'Đã hủy',
    REJECTED: 'Bị từ chối',
    NO_SHOW: 'Không đến',
}

const statusGroups: Array<{
    value: BookingStatus | 'ALL'
    label: string
}> = [
    { value: 'ALL', label: 'Tất cả' },
    { value: 'PENDING', label: 'Chờ xác nhận' },
    { value: 'CONFIRMED', label: 'Đã xác nhận' },
    { value: 'IN_PROGRESS', label: 'Đang thực hiện' },
    { value: 'COMPLETED', label: 'Hoàn thành' },
    { value: 'CANCELLED', label: 'Đã hủy' },
]

const today = () => new Date().toISOString().slice(0, 10)

const formatDate = (value: string) =>
    new Intl.DateTimeFormat('vi-VN', {
        weekday: 'short',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    }).format(new Date(`${value}T00:00:00`))

const formatTime = (value: string | null) =>
    value ? value.slice(0, 5) : 'Chưa xếp giờ'

const getErrorMessage = (
    error: unknown,
    fallback: string,
) => {
    if (axios.isAxiosError<ErrorResponse>(error)) {
        return error.response?.data?.message ?? fallback
    }

    return fallback
}

const canCancel = (status: BookingStatus) =>
    status === 'PENDING' || status === 'CONFIRMED'

export default function CustomerAppointmentsPage() {
    const navigate = useNavigate()
    const location = useLocation()
    const queryClient = useQueryClient()
    const [statusFilter, setStatusFilter] =
        useState<BookingStatus | 'ALL'>('ALL')
    const bookingIntent = location.state as { openBooking?: boolean; serviceId?: number } | null
    const [formOpen, setFormOpen] = useState(() => Boolean(bookingIntent?.openBooking))
    const [formValues, setFormValues] =
        useState<BookingForm>(() => ({
            ...emptyForm,
            serviceId: bookingIntent?.serviceId ? String(bookingIntent.serviceId) : '',
        }))
    const [formErrors, setFormErrors] =
        useState<BookingFormErrors>({})
    const [cancelTarget, setCancelTarget] =
        useState<Booking | null>(null)
    const [cancellationReason, setCancellationReason] =
        useState('')
    const [notice, setNotice] = useState('')

    useEffect(() => {
        if (!bookingIntent?.openBooking) return
        navigate(location.pathname, { replace: true, state: null })
    }, [bookingIntent?.openBooking, location.pathname, navigate])

    const bookingsQuery = useQuery({
        queryKey: ['my-bookings'],
        queryFn: () => bookingService.getMyBookings(),
        staleTime: 30_000,
        retry: 1,
    })

    const vehiclesQuery = useQuery({
        queryKey: ['my-vehicles'],
        queryFn: vehicleService.getMyVehicles,
        staleTime: 60_000,
        retry: 1,
    })

    const servicesQuery = useQuery({
        queryKey: ['booking-services'],
        queryFn: bookingService.getServices,
        staleTime: 5 * 60_000,
        retry: 1,
    })

    const mechanicsQuery = useQuery({
        queryKey: ['booking-mechanics'],
        queryFn: bookingService.getAvailableMechanics,
        staleTime: 5 * 60_000,
    })

    const availabilityQuery = useQuery({
        queryKey: ['booking-availability', formValues.mechanicUserId, formValues.serviceId, formValues.requestedDate],
        queryFn: () => bookingService.getAvailability(
            Number(formValues.mechanicUserId), Number(formValues.serviceId), formValues.requestedDate,
        ),
        enabled: Boolean(formValues.mechanicUserId && formValues.serviceId && formValues.requestedDate),
        retry: 1,
    })

    const createMutation = useMutation({
        mutationFn: bookingService.createBooking,
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ['my-bookings'],
            })
            setFormOpen(false)
            setFormValues(emptyForm)
            setNotice(
                'Đặt lịch thành công. Trung tâm sẽ sớm xác nhận.',
            )
        },
    })

    const cancelMutation = useMutation({
        mutationFn: ({
            bookingId,
            reason,
        }: {
            bookingId: number
            reason: string
        }) => bookingService.cancelBooking(bookingId, reason),
        onSuccess: async () => {
            await queryClient.invalidateQueries({
                queryKey: ['my-bookings'],
            })
            setCancelTarget(null)
            setCancellationReason('')
            setNotice('Lịch hẹn đã được hủy.')
        },
    })

    const bookings = useMemo(
        () => bookingsQuery.data?.content ?? [],
        [bookingsQuery.data?.content],
    )
    const vehicles = vehiclesQuery.data ?? []
    const services = servicesQuery.data ?? []
    const mechanics = mechanicsQuery.data ?? []

    const visibleBookings = useMemo(
        () =>
            statusFilter === 'ALL'
                ? bookings
                : bookings.filter(
                      (booking) => booking.status === statusFilter,
                  ),
        [bookings, statusFilter],
    )

    const upcomingCount = bookings.filter((booking) =>
        ['PENDING', 'CONFIRMED', 'IN_PROGRESS'].includes(
            booking.status,
        ),
    ).length
    const completedCount = bookings.filter(
        (booking) => booking.status === 'COMPLETED',
    ).length

    const openForm = () => {
        createMutation.reset()
        setFormErrors({})
        void mechanicsQuery.refetch()
        setFormValues({
            ...emptyForm,
            vehicleId:
                vehicles.length === 1 ? String(vehicles[0].id) : '',
            serviceId:
                services.length === 1 ? String(services[0].id) : '',
            mechanicUserId:
                mechanics.length === 1 ? String(mechanics[0].userId) : '',
        })
        setFormOpen(true)
    }

    const updateField = <K extends keyof BookingForm>(
        field: K,
        value: BookingForm[K],
    ) => {
        setFormValues((current) => ({
            ...current,
            [field]: value,
        }))
        setFormErrors((current) => ({
            ...current,
            [field]: undefined,
        }))
    }

    const submitBooking = (
        event: React.FormEvent<HTMLFormElement>,
    ) => {
        event.preventDefault()
        const errors: BookingFormErrors = {}

        if (!formValues.vehicleId) {
            errors.vehicleId = 'Vui lòng chọn phương tiện.'
        }
        if (!formValues.serviceId) {
            errors.serviceId = 'Vui lòng chọn dịch vụ.'
        }
        if (!formValues.mechanicUserId) {
            errors.mechanicUserId = 'Vui lòng chọn kỹ thuật viên để kiểm tra giờ trống.'
        }
        if (!formValues.requestedDate) {
            errors.requestedDate = 'Vui lòng chọn ngày.'
        } else if (formValues.requestedDate < today()) {
            errors.requestedDate = 'Ngày hẹn không thể ở quá khứ.'
        }
        if (!formValues.requestedTime) {
            errors.requestedTime = 'Vui lòng chọn giờ.'
        }

        const hasSelectedSlot = availabilityQuery.data?.slots.some((slot) =>
            new Date(slot.startAt).toTimeString().slice(0, 5) === formValues.requestedTime,
        )
        if (availabilityQuery.data && !hasSelectedSlot) {
            errors.requestedTime = 'Vui lòng chọn một giờ đang trống.'
        }
        if (formValues.customerNote.length > 1000) {
            errors.customerNote =
                'Ghi chú không được vượt quá 1000 ký tự.'
        }

        setFormErrors(errors)
        if (Object.keys(errors).length > 0) return

        const selectedService = services.find(
            (service) => service.id === Number(formValues.serviceId),
        )
        if (!selectedService) return

        const request: CreateBookingRequest = {
            vehicleId: Number(formValues.vehicleId),
            serviceId: selectedService.id,
            serviceType: selectedService.name,
            requestedDate: formValues.requestedDate,
            requestedTime: formValues.requestedTime,
            customerNote: formValues.customerNote.trim(),
        }

        createMutation.mutate(request)
    }

    const confirmCancel = () => {
        if (!cancelTarget || !cancellationReason.trim()) return

        cancelMutation.mutate({
            bookingId: cancelTarget.id,
            reason: cancellationReason.trim(),
        })
    }

    return (
        <div className="customer-appointments-page">
            <header className="appointments-heading">
                <div>
                    <span>Service Booking</span>
                    <h1>Lịch hẹn dịch vụ</h1>
                    <p>
                        Chủ động sắp xếp thời gian bảo dưỡng và theo dõi
                        tiến độ xác nhận từ trung tâm.
                    </p>
                </div>
                <button type="button" onClick={openForm}>
                    <Plus size={19} />
                    Đặt lịch mới
                </button>
            </header>

            {notice && (
                <div className="booking-notice" role="status">
                    <CheckCircle2 size={18} />
                    {notice}
                    <button
                        type="button"
                        onClick={() => setNotice('')}
                    >
                        <X size={16} />
                    </button>
                </div>
            )}

            <section className="booking-summary-grid">
                <article>
                    <span className="booking-summary-icon blue">
                        <CalendarDays size={22} />
                    </span>
                    <div>
                        <small>Tổng lịch hẹn</small>
                        <strong>
                            {bookingsQuery.isLoading
                                ? '—'
                                : bookings.length}
                        </strong>
                    </div>
                </article>
                <article>
                    <span className="booking-summary-icon amber">
                        <Clock3 size={22} />
                    </span>
                    <div>
                        <small>Đang xử lý</small>
                        <strong>
                            {bookingsQuery.isLoading
                                ? '—'
                                : upcomingCount}
                        </strong>
                    </div>
                </article>
                <article>
                    <span className="booking-summary-icon green">
                        <CalendarCheck2 size={22} />
                    </span>
                    <div>
                        <small>Đã hoàn thành</small>
                        <strong>
                            {bookingsQuery.isLoading
                                ? '—'
                                : completedCount}
                        </strong>
                    </div>
                </article>
            </section>

            <section className="booking-list-panel">
                <div className="booking-list-toolbar">
                    <div>
                        <span>Lịch sử đặt lịch</span>
                        <h2>Hoạt động gần đây</h2>
                    </div>
                    <label>
                        <Filter size={16} />
                        <select
                            value={statusFilter}
                            onChange={(event) =>
                                setStatusFilter(
                                    event.target.value as
                                        | BookingStatus
                                        | 'ALL',
                                )
                            }
                        >
                            {statusGroups.map((status) => (
                                <option
                                    key={status.value}
                                    value={status.value}
                                >
                                    {status.label}
                                </option>
                            ))}
                        </select>
                    </label>
                </div>

                {bookingsQuery.isLoading && (
                    <BookingState
                        icon={
                            <LoaderCircle
                                className="booking-spin"
                                size={32}
                            />
                        }
                        title="Đang tải lịch hẹn"
                        description="Đồng bộ dữ liệu từ Booking Service..."
                    />
                )}

                {bookingsQuery.isError && (
                    <BookingState
                        error
                        icon={<CircleAlert size={32} />}
                        title="Không thể tải lịch hẹn"
                        description={getErrorMessage(
                            bookingsQuery.error,
                            'Vui lòng kiểm tra Booking Service.',
                        )}
                        action={
                            <button
                                type="button"
                                onClick={() =>
                                    void bookingsQuery.refetch()
                                }
                            >
                                <RefreshCw size={16} />
                                Thử lại
                            </button>
                        }
                    />
                )}

                {!bookingsQuery.isLoading &&
                    !bookingsQuery.isError &&
                    visibleBookings.length === 0 && (
                        <BookingState
                            icon={<CalendarDays size={35} />}
                            title={
                                statusFilter === 'ALL'
                                    ? 'Bạn chưa có lịch hẹn'
                                    : 'Không có lịch hẹn phù hợp'
                            }
                            description={
                                statusFilter === 'ALL'
                                    ? 'Đặt lịch đầu tiên để trung tâm chuẩn bị dịch vụ chu đáo hơn.'
                                    : 'Hãy chọn trạng thái khác để xem lịch hẹn.'
                            }
                            action={
                                statusFilter === 'ALL' ? (
                                    <button
                                        type="button"
                                        onClick={openForm}
                                    >
                                        <Plus size={16} />
                                        Đặt lịch mới
                                    </button>
                                ) : undefined
                            }
                        />
                    )}

                {!bookingsQuery.isLoading &&
                    !bookingsQuery.isError &&
                    visibleBookings.length > 0 && (
                        <div className="customer-booking-list">
                            {visibleBookings.map((booking) => {
                                const vehicle = vehicles.find(
                                    (item) => item.id === booking.vehicleId,
                                )

                                return (
                                    <article
                                        className="customer-booking-card"
                                        key={booking.id}
                                    >
                                        <div className="booking-date-block">
                                            <small>
                                                {new Intl.DateTimeFormat(
                                                    'vi-VN',
                                                    { month: 'short' },
                                                ).format(
                                                    new Date(
                                                        `${booking.requestedDate}T00:00:00`,
                                                    ),
                                                )}
                                            </small>
                                            <strong>
                                                {booking.requestedDate.slice(
                                                    8,
                                                    10,
                                                )}
                                            </strong>
                                            <span>
                                                {formatTime(
                                                    booking.requestedTime,
                                                )}
                                            </span>
                                        </div>

                                        <div className="booking-card-content">
                                            <div className="booking-card-title">
                                                <div>
                                                    <span>
                                                        {booking.bookingCode}
                                                    </span>
                                                    <h3>
                                                        {booking.serviceType}
                                                    </h3>
                                                </div>
                                                <span
                                                    className={`booking-status ${booking.status.toLowerCase()}`}
                                                >
                                                    <i />
                                                    {
                                                        statusLabels[
                                                            booking.status
                                                        ]
                                                    }
                                                </span>
                                            </div>

                                            <div className="booking-card-meta">
                                                <span>
                                                    <CalendarDays size={15} />
                                                    {formatDate(
                                                        booking.requestedDate,
                                                    )}
                                                </span>
                                                <span>
                                                    <CarFront size={15} />
                                                    {vehicle
                                                        ? `${vehicle.manufacturer} ${vehicle.model}`
                                                        : `Phương tiện #${booking.vehicleId}`}
                                                </span>
                                                <span>
                                                    <Clock3 size={15} />
                                                    {booking.serviceDurationMinutes
                                                        ? `${booking.serviceDurationMinutes} phút`
                                                        : 'Thời lượng đang cập nhật'}
                                                </span>
                                                {booking.mechanicUserId && (
                                                    <span>
                                                        <UserRound size={15} />
                                                        Kỹ thuật viên #{
                                                            booking.mechanicUserId
                                                        }
                                                    </span>
                                                )}
                                            </div>

                                            {booking.customerNote && (
                                                <p>{booking.customerNote}</p>
                                            )}

                                            {canCancel(booking.status) && (
                                                <button
                                                    type="button"
                                                    className="cancel-booking-button"
                                                    onClick={() => {
                                                        cancelMutation.reset()
                                                        setCancellationReason('')
                                                        setCancelTarget(booking)
                                                    }}
                                                >
                                                    <XCircle size={16} />
                                                    Hủy lịch
                                                </button>
                                            )}
                                        </div>
                                    </article>
                                )
                            })}
                        </div>
                    )}
            </section>

            {formOpen && (
                <div className="booking-form-layer">
                    <button
                        type="button"
                        className="booking-form-overlay"
                        onClick={() =>
                            !createMutation.isPending &&
                            setFormOpen(false)
                        }
                    />
                    <aside
                        className="booking-form-drawer"
                        role="dialog"
                        aria-modal="true"
                    >
                        <header>
                            <div>
                                <span>Đặt lịch trực tuyến</span>
                                <h2>Lịch hẹn mới</h2>
                            </div>
                            <button
                                type="button"
                                onClick={() => setFormOpen(false)}
                                disabled={createMutation.isPending}
                            >
                                <X size={20} />
                            </button>
                        </header>

                        <form onSubmit={submitBooking}>
                            <div className="booking-form-banner">
                                <span>
                                    <Sparkles size={24} />
                                </span>
                                <div>
                                    <strong>Chăm sóc xe chủ động</strong>
                                    <small>
                                        Trung tâm sẽ xác nhận kỹ thuật viên
                                        và thời gian chính thức sau khi nhận
                                        yêu cầu.
                                    </small>
                                </div>
                            </div>

                            {vehicles.length === 0 ? (
                                <div className="booking-missing-data">
                                    <CarFront size={25} />
                                    <div>
                                        <strong>Chưa có phương tiện</strong>
                                        <span>
                                            Bạn cần thêm xe trước khi đặt
                                            lịch dịch vụ.
                                        </span>
                                    </div>
                                    <button
                                        type="button"
                                        onClick={() => navigate('/vehicles')}
                                    >
                                        Thêm xe <ArrowRight size={15} />
                                    </button>
                                </div>
                            ) : (
                                <div className="booking-form-grid">
                                    <FormField
                                        label="Phương tiện *"
                                        error={formErrors.vehicleId}
                                        full
                                    >
                                        <select
                                            value={formValues.vehicleId}
                                            onChange={(event) =>
                                                updateField(
                                                    'vehicleId',
                                                    event.target.value,
                                                )
                                            }
                                        >
                                            <option value="">
                                                Chọn phương tiện
                                            </option>
                                            {vehicles.map((vehicle) => (
                                                <option
                                                    key={vehicle.id}
                                                    value={vehicle.id}
                                                >
                                                    {vehicle.manufacturer}{' '}
                                                    {vehicle.model} ·{' '}
                                                    {vehicle.licensePlate ||
                                                        vehicle.vin}
                                                </option>
                                            ))}
                                        </select>
                                    </FormField>

                                    <FormField
                                        label="Dịch vụ *"
                                        error={formErrors.serviceId}
                                        full
                                    >
                                        <select
                                            value={formValues.serviceId}
                                            onChange={(event) =>
                                                updateField(
                                                    'serviceId',
                                                    event.target.value,
                                                )
                                            }
                                            disabled={servicesQuery.isLoading}
                                        >
                                            <option value="">
                                                {servicesQuery.isLoading
                                                    ? 'Đang tải dịch vụ...'
                                                    : 'Chọn dịch vụ'}
                                            </option>
                                            {services.map((service) => (
                                                <option
                                                    key={service.id}
                                                    value={service.id}
                                                >
                                                    {service.name} ·{' '}
                                                    {service.durationMinutes}{' '}
                                                    phút
                                                </option>
                                            ))}
                                        </select>
                                    </FormField>

                                    <FormField
                                        label="Kỹ thuật viên để kiểm tra giờ trống *"
                                        error={formErrors.mechanicUserId}
                                        full
                                    >
                                        <select
                                            value={formValues.mechanicUserId}
                                            onChange={(event) => updateField('mechanicUserId', event.target.value)}
                                            disabled={mechanicsQuery.isLoading}
                                        >
                                            <option value="">{mechanicsQuery.isLoading ? 'Đang tải kỹ thuật viên...' : 'Chọn kỹ thuật viên'}</option>
                                            {mechanics.map((mechanic) => (
                                                <option key={mechanic.userId} value={mechanic.userId}>
                                                    {mechanic.fullName} · {mechanic.specialization || mechanic.skillLevel}
                                                </option>
                                            ))}
                                        </select>
                                        <small className="booking-slot-hint">Giờ trống được tính theo ca đã thiết lập; trung tâm sẽ xác nhận phân công cuối cùng.</small>
                                        {mechanicsQuery.isError && (
                                            <small className="booking-slot-error">
                                                {getErrorMessage(mechanicsQuery.error, 'Không thể tải danh sách kỹ thuật viên. Hãy kiểm tra Identity Service.')}
                                            </small>
                                        )}
                                        {!mechanicsQuery.isLoading && !mechanicsQuery.isError && mechanics.length === 0 && (
                                            <small className="booking-slot-error">
                                                Chưa có kỹ thuật viên đang hoạt động. Quản trị viên cần tạo hoặc kích hoạt hồ sơ kỹ thuật viên trước.
                                            </small>
                                        )}
                                    </FormField>

                                    <FormField
                                        label="Ngày mong muốn *"
                                        error={formErrors.requestedDate}
                                    >
                                        <input
                                            type="date"
                                            min={today()}
                                            value={formValues.requestedDate}
                                            onChange={(event) =>
                                                updateField(
                                                    'requestedDate',
                                                    event.target.value,
                                                )
                                            }
                                        />
                                    </FormField>

                                    <FormField
                                        label="Giờ mong muốn *"
                                        error={formErrors.requestedTime}
                                    >
                                        {availabilityQuery.isLoading ? <span className="booking-slot-hint">Đang kiểm tra giờ trống...</span>
                                            : availabilityQuery.data ? <select value={formValues.requestedTime} onChange={(event) => updateField('requestedTime', event.target.value)}>
                                                <option value="">Chọn giờ trống</option>
                                                {availabilityQuery.data.slots.map((slot) => {
                                                    const time = new Date(slot.startAt).toTimeString().slice(0, 5)
                                                    return <option key={slot.startAt} value={time}>{time} – {new Date(slot.endAt).toTimeString().slice(0, 5)}</option>
                                                })}
                                            </select>
                                            : <input type="time" step="900" value={formValues.requestedTime} disabled={!formValues.mechanicUserId || !formValues.serviceId || !formValues.requestedDate} onChange={(event) => updateField('requestedTime', event.target.value)} />}
                                        {availabilityQuery.isError && <small className="booking-slot-error">Không thể tải giờ trống. Hãy kiểm tra ca làm của kỹ thuật viên.</small>}
                                    </FormField>

                                    <FormField
                                        label="Ghi chú cho trung tâm"
                                        error={formErrors.customerNote}
                                        full
                                    >
                                        <textarea
                                            value={formValues.customerNote}
                                            onChange={(event) =>
                                                updateField(
                                                    'customerNote',
                                                    event.target.value,
                                                )
                                            }
                                            maxLength={1000}
                                            placeholder="Mô tả tình trạng xe hoặc yêu cầu đặc biệt..."
                                        />
                                        <small className="booking-char-count">
                                            {formValues.customerNote.length}
                                            /1000
                                        </small>
                                    </FormField>
                                </div>
                            )}

                            {createMutation.isError && (
                                <div className="booking-form-error">
                                    <CircleAlert size={17} />
                                    {getErrorMessage(
                                        createMutation.error,
                                        'Không thể đặt lịch. Vui lòng thử lại.',
                                    )}
                                </div>
                            )}

                            <footer>
                                <button
                                    type="button"
                                    onClick={() => setFormOpen(false)}
                                    disabled={createMutation.isPending}
                                >
                                    Hủy
                                </button>
                                <button
                                    type="submit"
                                    className="primary"
                                    disabled={
                                        createMutation.isPending ||
                                        vehicles.length === 0 ||
                                        services.length === 0
                                    }
                                >
                                    {createMutation.isPending ? (
                                        <LoaderCircle
                                            className="booking-spin"
                                            size={17}
                                        />
                                    ) : (
                                        <Send size={17} />
                                    )}
                                    {createMutation.isPending
                                        ? 'Đang gửi...'
                                        : 'Gửi yêu cầu'}
                                </button>
                            </footer>
                        </form>
                    </aside>
                </div>
            )}

            {cancelTarget && (
                <div className="cancel-dialog-layer">
                    <button
                        type="button"
                        className="booking-form-overlay"
                        onClick={() =>
                            !cancelMutation.isPending &&
                            setCancelTarget(null)
                        }
                    />
                    <section className="cancel-booking-dialog">
                        <span>
                            <XCircle size={25} />
                        </span>
                        <h2>Hủy lịch hẹn?</h2>
                        <p>
                            Vui lòng cho trung tâm biết lý do bạn muốn
                            hủy lịch <strong>{cancelTarget.bookingCode}</strong>.
                        </p>
                        <textarea
                            value={cancellationReason}
                            onChange={(event) =>
                                setCancellationReason(event.target.value)
                            }
                            maxLength={500}
                            placeholder="Nhập lý do hủy lịch..."
                            autoFocus
                        />
                        {cancelMutation.isError && (
                            <div className="cancel-dialog-error">
                                {getErrorMessage(
                                    cancelMutation.error,
                                    'Không thể hủy lịch hẹn.',
                                )}
                            </div>
                        )}
                        <div>
                            <button
                                type="button"
                                onClick={() => setCancelTarget(null)}
                                disabled={cancelMutation.isPending}
                            >
                                Giữ lịch
                            </button>
                            <button
                                type="button"
                                className="danger"
                                onClick={confirmCancel}
                                disabled={
                                    cancelMutation.isPending ||
                                    !cancellationReason.trim()
                                }
                            >
                                {cancelMutation.isPending ? (
                                    <LoaderCircle
                                        className="booking-spin"
                                        size={17}
                                    />
                                ) : (
                                    <XCircle size={17} />
                                )}
                                Xác nhận hủy
                            </button>
                        </div>
                    </section>
                </div>
            )}
        </div>
    )
}

function BookingState({
    icon,
    title,
    description,
    action,
    error = false,
}: {
    icon: React.ReactNode
    title: string
    description: string
    action?: React.ReactNode
    error?: boolean
}) {
    return (
        <div className={error ? 'booking-state error' : 'booking-state'}>
            {icon}
            <strong>{title}</strong>
            <span>{description}</span>
            {action}
        </div>
    )
}

function FormField({
    label,
    error,
    full = false,
    children,
}: {
    label: string
    error?: string
    full?: boolean
    children: React.ReactNode
}) {
    return (
        <label className={full ? 'booking-field full' : 'booking-field'}>
            <span>{label}</span>
            {children}
            {error && <em>{error}</em>}
        </label>
    )
}
