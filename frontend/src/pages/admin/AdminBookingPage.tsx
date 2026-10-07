import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import axios from 'axios'
import {
    ArrowLeft,
    CalendarCheck,
    CalendarDays,
    ChevronLeft,
    ChevronRight,
    CircleAlert,
    Clock3,
    CheckCircle2,
    LoaderCircle,
    RefreshCw,
    X,
} from 'lucide-react'
import { useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { adminBookingService } from '../../services/adminBookingService'
import { adminMechanicService } from '../../services/adminMechanicService'
import { adminMechanicScheduleService } from '../../services/adminMechanicScheduleService'
import { adminRepairService } from '../../services/adminRepairService'
import type { Booking, BookingStatus } from '../../types/booking'
import type { ErrorResponse } from '../../types/api'
import './AdminOperationsPages.css'

const PAGE_SIZE = 10

const statusLabels: Record<BookingStatus, string> = {
    PENDING: 'Chờ xác nhận',
    CONFIRMED: 'Đã xác nhận',
    IN_PROGRESS: 'Đang thực hiện',
    COMPLETED: 'Hoàn thành',
    CANCELLED: 'Đã hủy',
    REJECTED: 'Đã từ chối',
    NO_SHOW: 'Không đến',
}

const formatDate = (value: string | null | undefined) => {
    if (!value) return 'Chưa xếp lịch'

    return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    }).format(new Date(value))
}

const formatDateTime = (value: string | null | undefined) => {
    if (!value) return 'Chưa xếp lịch'

    return new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(new Date(value))
}

const formatRequestedTime = (value: string) => value.slice(0, 5)

const getCheckInAvailability = (booking: Booking) => {
    const start = booking.scheduledStartAt
        ? new Date(booking.scheduledStartAt)
        : null
    const end = booking.scheduledEndAt
        ? new Date(booking.scheduledEndAt)
        : null

    if (!start || Number.isNaN(start.getTime()) || !end || Number.isNaN(end.getTime())) {
        return { allowed: true, message: null }
    }

    const now = new Date()
    const checkInFrom = new Date(start.getTime() - 30 * 60 * 1000)
    const displayTime = new Intl.DateTimeFormat('vi-VN', {
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
        hour: '2-digit',
        minute: '2-digit',
    }).format(checkInFrom)

    if (now < checkInFrom) {
        return {
            allowed: false,
            message: `Chưa đến thời gian tiếp nhận. Có thể check-in từ ${displayTime}.`,
        }
    }

    if (now >= end) {
        return {
            allowed: false,
            message: 'Lịch đã qua giờ kết thúc. Hãy sắp xếp lại lịch hẹn với khách trước khi tiếp nhận xe.',
        }
    }

    return { allowed: true, message: null }
}

const getErrorMessage = (error: unknown) => {
    if (axios.isAxiosError<ErrorResponse>(error)) {
        return (
            error.response?.data?.message ??
            'Không thể tải danh sách lịch hẹn.'
        )
    }

    return 'Đã xảy ra lỗi khi tải danh sách lịch hẹn.'
}

export default function AdminBookingPage() {
    const navigate = useNavigate()
    const queryClient = useQueryClient()
    const [status, setStatus] = useState<BookingStatus | ''>('')
    const [page, setPage] = useState(0)
    const [selectedBookingId, setSelectedBookingId] = useState<
        number | null
    >(null)
    const [confirmForm, setConfirmForm] = useState({
        mechanicUserId: '',
        scheduledStartAt: '',
        scheduledEndAt: '',
        internalNote: '',
    })
    const [actionNotice, setActionNotice] = useState<string | null>(null)
    const [actionError, setActionError] = useState<string | null>(null)
    const [rejectReason, setRejectReason] = useState('')
    const [calendarOpen, setCalendarOpen] = useState(false)
    const [calendarAnchor, setCalendarAnchor] = useState(() => new Date().toISOString().slice(0, 10))
    const [shiftForm, setShiftForm] = useState({
        startTime: '08:00',
        endTime: '17:00',
        breakStart: '12:00',
        breakEnd: '13:00',
    })

    const {
        data,
        isLoading,
        isError,
        error,
        isFetching,
        refetch,
    } = useQuery({
        queryKey: ['admin-bookings', status, page],
        queryFn: () =>
            adminBookingService.getBookings({
                status,
                page,
                size: PAGE_SIZE,
            }),
        staleTime: 20_000,
        retry: 1,
    })

    const {
        data: selectedBooking,
        isLoading: isDetailLoading,
        isError: isDetailError,
    } = useQuery({
        queryKey: ['admin-booking', selectedBookingId],
        queryFn: () =>
            adminBookingService.getBookingById(
                selectedBookingId as number,
            ),
        enabled: selectedBookingId !== null,
        staleTime: 20_000,
        retry: 1,
    })

    const bookings = useMemo(
        () => data?.content ?? [],
        [data?.content],
    )

    const mechanicsQuery = useQuery({
        queryKey: ['active-mechanics-for-booking'],
        queryFn: () => adminMechanicService.getMechanics({
            employmentStatus: 'ACTIVE',
            page: 0,
            size: 100,
        }),
        enabled: selectedBooking?.status === 'PENDING',
        staleTime: 60_000,
    })

    const calendarRange = useMemo(() => {
        const anchor = new Date(`${calendarAnchor}T00:00:00`)
        const start = new Date(anchor)
        const weekday = anchor.getDay() || 7
        start.setDate(anchor.getDate() - weekday + 1)
        const end = new Date(start)
        end.setDate(start.getDate() + 6)
        return { from: start.toISOString().slice(0, 10), to: end.toISOString().slice(0, 10) }
    }, [calendarAnchor])
    const calendarQuery = useQuery({
        queryKey: ['admin-booking-calendar', calendarRange.from, calendarRange.to],
        queryFn: () => adminBookingService.getCalendar(calendarRange.from, calendarRange.to),
        enabled: calendarOpen,
    })

    const refreshBookingData = async () => {
        await Promise.all([
            queryClient.invalidateQueries({ queryKey: ['admin-bookings'] }),
            queryClient.invalidateQueries({ queryKey: ['admin-booking'] }),
            queryClient.invalidateQueries({ queryKey: ['admin-repairs'] }),
        ])
    }

    const confirmMutation = useMutation({
        mutationFn: () => adminBookingService.confirmBooking(
            selectedBookingId as number,
            {
                mechanicUserId: Number(confirmForm.mechanicUserId),
                scheduledStartAt: confirmForm.scheduledStartAt,
                scheduledEndAt: confirmForm.scheduledEndAt || null,
                internalNote: confirmForm.internalNote.trim() || null,
            },
        ),
        onSuccess: async () => {
            setActionNotice('Đã xác nhận lịch hẹn và phân công kỹ thuật viên.')
            await refreshBookingData()
        },
    })

    const checkInMutation = useMutation({
        mutationFn: () => adminBookingService.checkInBooking(
            selectedBookingId as number,
        ),
        onSuccess: async () => {
            setActionNotice('Đã tiếp nhận xe. Bạn có thể tạo lệnh sửa chữa.')
            await refreshBookingData()
        },
    })

    const rejectMutation = useMutation({
        mutationFn: () => adminBookingService.rejectBooking(selectedBookingId as number, rejectReason.trim()),
        onSuccess: async () => { setRejectReason(''); setActionNotice('Đã từ chối lịch hẹn và ghi nhận lý do cho khách hàng.'); await refreshBookingData() },
    })

    const noShowMutation = useMutation({
        mutationFn: () => adminBookingService.updateStatus(selectedBookingId as number, 'NO_SHOW', 'Khách không đến theo lịch đã xác nhận.'),
        onSuccess: async () => { setActionNotice('Đã đánh dấu khách không đến.'); await refreshBookingData() },
    })

    const createRepairMutation = useMutation({
        mutationFn: () => adminRepairService.createRepairOrder({
            bookingId: selectedBookingId as number,
            technicianNote: confirmForm.internalNote.trim() || null,
        }),
        onSuccess: async () => {
            setActionNotice('Đã tạo lệnh sửa chữa và chuyển vào Repair Service.')
            await refreshBookingData()
        },
    })

    const saveShiftMutation = useMutation({
        mutationFn: () => adminMechanicScheduleService.saveWorkDay(
            Number(confirmForm.mechanicUserId),
            confirmForm.scheduledStartAt.slice(0, 10),
            {
                workingDay: true,
                startTime: shiftForm.startTime,
                endTime: shiftForm.endTime,
                breakStart: shiftForm.breakStart || null,
                breakEnd: shiftForm.breakEnd || null,
            },
        ),
        onSuccess: () => {
            setActionNotice('Đã thiết lập ca làm cho kỹ thuật viên vào ngày đã chọn.')
        },
    })

    const submitConfirmation = (event: React.FormEvent<HTMLFormElement>) => {
        event.preventDefault()
        setActionNotice(null)
        confirmMutation.reset()
        if (!confirmForm.mechanicUserId || !confirmForm.scheduledStartAt) return
        confirmMutation.mutate()
    }
    const pendingOnPage = useMemo(
        () =>
            bookings.filter((booking) => booking.status === 'PENDING')
                .length,
        [bookings],
    )

    return (
        <div className="ops-page">
            <button
                type="button"
                className="ops-back"
                onClick={() => navigate('/admin')}
            >
                <ArrowLeft size={18} />
                Tổng quan
            </button>

            <header className="ops-heading">
                <div className="ops-heading-copy">
                    <span>Booking Service</span>
                    <h1>Quản lý lịch hẹn</h1>
                    <p>
                        Theo dõi yêu cầu đặt lịch, thời gian tiếp nhận và
                        phân công kỹ thuật viên.
                    </p>
                </div>
                <div className="ops-live-badge">
                    <span />
                    Dữ liệu trực tiếp từ backend
                </div>
                <button type="button" className="ops-secondary-action" onClick={() => setCalendarOpen(true)}>
                    <CalendarDays size={17} /> Lịch tuần
                </button>
            </header>

            <section className="ops-summary">
                <article>
                    <span className="ops-summary-icon">
                        <CalendarDays size={22} />
                    </span>
                    <span>
                        <small>Tổng lịch hẹn</small>
                        <strong>
                            {isLoading ? '—' : data?.totalElements ?? 0}
                        </strong>
                    </span>
                </article>
                <article>
                    <span className="ops-summary-icon">
                        <Clock3 size={22} />
                    </span>
                    <span>
                        <small>Chờ xác nhận trên trang</small>
                        <strong>{isLoading ? '—' : pendingOnPage}</strong>
                    </span>
                </article>
                <article>
                    <span className="ops-summary-icon">
                        <CalendarCheck size={22} />
                    </span>
                    <span>
                        <small>Đang hiển thị</small>
                        <strong>{isLoading ? '—' : bookings.length}</strong>
                    </span>
                </article>
            </section>

            <section className="ops-panel">
                <div className="ops-toolbar">
                    <select
                        className="ops-filter"
                        value={status}
                        onChange={(event) => {
                            setStatus(
                                event.target.value as BookingStatus | '',
                            )
                            setPage(0)
                        }}
                        aria-label="Lọc lịch hẹn theo trạng thái"
                    >
                        <option value="">Tất cả trạng thái</option>
                        {Object.entries(statusLabels).map(
                            ([value, label]) => (
                                <option key={value} value={value}>
                                    {label}
                                </option>
                            ),
                        )}
                    </select>

                    {status && (
                        <button
                            type="button"
                            className="ops-clear"
                            onClick={() => {
                                setStatus('')
                                setPage(0)
                            }}
                        >
                            <X size={16} />
                            Xóa bộ lọc
                        </button>
                    )}
                </div>

                {isLoading && (
                    <div className="ops-state">
                        <LoaderCircle
                            className="admin-spinning"
                            size={31}
                        />
                        <strong>Đang tải danh sách lịch hẹn</strong>
                        <span>Vui lòng chờ trong giây lát...</span>
                    </div>
                )}

                {isError && (
                    <div className="ops-state error">
                        <CircleAlert size={31} />
                        <strong>Không thể tải dữ liệu</strong>
                        <span>{getErrorMessage(error)}</span>
                        <button
                            type="button"
                            onClick={() => void refetch()}
                            disabled={isFetching}
                        >
                            <RefreshCw
                                className={
                                    isFetching ? 'admin-spinning' : ''
                                }
                                size={17}
                            />
                            Thử lại
                        </button>
                    </div>
                )}

                {!isLoading && !isError && bookings.length === 0 && (
                    <div className="ops-state empty">
                        <CalendarDays size={34} />
                        <strong>Chưa có lịch hẹn</strong>
                        <span>
                            {status
                                ? 'Không có lịch hẹn phù hợp với trạng thái đã chọn.'
                                : 'Các yêu cầu đặt lịch sẽ xuất hiện tại đây.'}
                        </span>
                    </div>
                )}

                {!isLoading && !isError && bookings.length > 0 && (
                    <>
                        <div className="ops-table-wrap">
                            <table className="ops-table">
                                <thead>
                                    <tr>
                                        <th>Mã lịch hẹn</th>
                                        <th>Dịch vụ</th>
                                        <th>Khách hàng / Xe</th>
                                        <th>Thời gian yêu cầu</th>
                                        <th>Kỹ thuật viên</th>
                                        <th>Trạng thái</th>
                                        <th aria-label="Thao tác" />
                                    </tr>
                                </thead>
                                <tbody>
                                    {bookings.map((booking) => (
                                        <tr key={booking.id}>
                                            <td>
                                                <span className="ops-code">
                                                    {booking.bookingCode}
                                                </span>
                                            </td>
                                            <td>{booking.serviceType}</td>
                                            <td>
                                                Khách #{booking.customerUserId}
                                                <span className="ops-subtext">
                                                    Xe #{booking.vehicleId}
                                                </span>
                                            </td>
                                            <td>
                                                {formatDate(
                                                    booking.requestedDate,
                                                )}
                                                <span className="ops-subtext">
                                                    {formatRequestedTime(
                                                        booking.requestedTime,
                                                    )}
                                                </span>
                                            </td>
                                            <td>
                                                {booking.mechanicUserId
                                                    ? `ID ${booking.mechanicUserId}`
                                                    : 'Chưa phân công'}
                                            </td>
                                            <td>
                                                <span
                                                    className={`ops-status ${booking.status.toLowerCase()}`}
                                                >
                                                    {
                                                        statusLabels[
                                                            booking.status
                                                        ]
                                                    }
                                                </span>
                                            </td>
                                            <td>
                                                <button
                                                    type="button"
                                                    className="ops-row-action"
                                                    onClick={() =>
                                                        {
                                                            setActionNotice(null)
                                                            setActionError(null)
                                                            setConfirmForm({
                                                                mechanicUserId: '',
                                                                scheduledStartAt: `${booking.requestedDate}T${booking.requestedTime.slice(0, 5)}`,
                                                                scheduledEndAt: '',
                                                                internalNote: '',
                                                            })
                                                            saveShiftMutation.reset()
                                                            setSelectedBookingId(booking.id)
                                                        }
                                                    }
                                                >
                                                    Chi tiết
                                                    <ChevronRight size={16} />
                                                </button>
                                            </td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                        <footer className="ops-pagination">
                            <span>
                                Hiển thị {bookings.length} trong tổng số{' '}
                                {data?.totalElements ?? 0} lịch hẹn
                            </span>
                            <div>
                                <button
                                    type="button"
                                    onClick={() =>
                                        setPage((current) => current - 1)
                                    }
                                    disabled={data?.first}
                                    aria-label="Trang trước"
                                >
                                    <ChevronLeft size={18} />
                                </button>
                                <span>
                                    Trang {(data?.page ?? 0) + 1} /{' '}
                                    {Math.max(data?.totalPages ?? 1, 1)}
                                </span>
                                <button
                                    type="button"
                                    onClick={() =>
                                        setPage((current) => current + 1)
                                    }
                                    disabled={data?.last}
                                    aria-label="Trang sau"
                                >
                                    <ChevronRight size={18} />
                                </button>
                            </div>
                        </footer>
                    </>
                )}
            </section>

            {calendarOpen && <div className="ops-detail-layer"><button className="ops-detail-overlay" onClick={() => setCalendarOpen(false)} aria-label="Đóng lịch tuần" /><aside className="ops-detail-drawer" role="dialog"><header className="ops-detail-header"><div><span>Booking Service</span><h2>Lịch vận hành tuần</h2></div><button onClick={() => setCalendarOpen(false)}><X size={21} /></button></header><div className="ops-detail-content"><section className="ops-action-card"><label>Chọn ngày trong tuần<input type="date" value={calendarAnchor} onChange={(event) => setCalendarAnchor(event.target.value)} /></label><p>Hiển thị lịch từ {formatDate(calendarRange.from)} đến {formatDate(calendarRange.to)}.</p></section><section><h4>Ca đã đặt</h4>{calendarQuery.isLoading ? <p className="ops-note">Đang tải lịch tuần…</p> : calendarQuery.isError ? <p className="ops-action-error">Không thể tải lịch tuần.</p> : calendarQuery.data?.length ? <div className="ops-task-list">{calendarQuery.data.map(booking => <button type="button" className="ops-task-item" key={booking.id} onClick={() => { setCalendarOpen(false); setSelectedBookingId(booking.id) }}><div><strong>{formatDateTime(booking.scheduledStartAt || `${booking.requestedDate}T${booking.requestedTime}`)} · {booking.serviceType}</strong><small>KTV #{booking.mechanicUserId || 'chưa phân công'} · {statusLabels[booking.status]}</small></div><span>{booking.bookingCode}</span></button>)}</div> : <p className="ops-note">Chưa có lịch xác nhận trong tuần này.</p>}</section></div></aside></div>}

            {selectedBookingId !== null && (
                <div className="ops-detail-layer">
                    <button
                        type="button"
                        className="ops-detail-overlay"
                        onClick={() => setSelectedBookingId(null)}
                        aria-label="Đóng thông tin lịch hẹn"
                    />
                    <aside
                        className="ops-detail-drawer"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="booking-detail-title"
                    >
                        <header className="ops-detail-header">
                            <div>
                                <span>Booking Service</span>
                                <h2 id="booking-detail-title">
                                    Chi tiết lịch hẹn
                                </h2>
                            </div>
                            <button
                                type="button"
                                onClick={() => setSelectedBookingId(null)}
                                aria-label="Đóng"
                            >
                                <X size={21} />
                            </button>
                        </header>

                        {isDetailLoading && (
                            <div className="ops-detail-state">
                                <LoaderCircle
                                    className="admin-spinning"
                                    size={31}
                                />
                                Đang tải lịch hẹn...
                            </div>
                        )}

                        {isDetailError && (
                            <div className="ops-detail-state">
                                <CircleAlert size={31} />
                                Không thể tải chi tiết lịch hẹn.
                            </div>
                        )}

                        {selectedBooking && (
                            <div className="ops-detail-content">
                                <div className="ops-detail-hero">
                                    <span className="ops-detail-hero-icon">
                                        <CalendarCheck size={30} />
                                    </span>
                                    <div>
                                        <small>
                                            {selectedBooking.bookingCode}
                                        </small>
                                        <h3>
                                            {selectedBooking.serviceType}
                                        </h3>
                                        <span
                                            className={`ops-status ${selectedBooking.status.toLowerCase()}`}
                                        >
                                            {
                                                statusLabels[
                                                    selectedBooking.status
                                                ]
                                            }
                                        </span>
                                    </div>
                                </div>

                                <section>
                                    <h4>Thông tin lịch hẹn</h4>
                                    <dl className="ops-detail-list">
                                        <div>
                                            <dt>Khách hàng</dt>
                                            <dd>
                                                ID{' '}
                                                {
                                                    selectedBooking.customerUserId
                                                }
                                            </dd>
                                        </div>
                                        <div>
                                            <dt>Phương tiện</dt>
                                            <dd>
                                                ID {selectedBooking.vehicleId}
                                            </dd>
                                        </div>
                                        <div>
                                            <dt>Thời gian yêu cầu</dt>
                                            <dd>
                                                {formatDate(
                                                    selectedBooking.requestedDate,
                                                )}{' '}
                                                ·{' '}
                                                {formatRequestedTime(
                                                    selectedBooking.requestedTime,
                                                )}
                                            </dd>
                                        </div>
                                        <div>
                                            <dt>Đã xếp lịch</dt>
                                            <dd>
                                                {formatDateTime(
                                                    selectedBooking.scheduledStartAt,
                                                )}
                                            </dd>
                                        </div>
                                        <div>
                                            <dt>Kỹ thuật viên</dt>
                                            <dd>
                                                {selectedBooking.mechanicUserId
                                                    ? `ID ${selectedBooking.mechanicUserId}`
                                                    : 'Chưa phân công'}
                                            </dd>
                                        </div>
                                    </dl>
                                </section>

                                <section>
                                    <h4>Ghi chú khách hàng</h4>
                                    <p className="ops-note">
                                        {selectedBooking.customerNote ||
                                            'Khách hàng không để lại ghi chú.'}
                                    </p>
                                </section>

                                {actionNotice && (
                                    <div className="ops-action-success">
                                        <CheckCircle2 size={18} />
                                        {actionNotice}
                                    </div>
                                )}

                                {actionError && (
                                    <p className="ops-action-error ops-action-error-box">
                                        {actionError}
                                    </p>
                                )}

                                {selectedBooking.status === 'PENDING' && (
                                    <section className="ops-action-card">
                                        <h4>Xác nhận & phân công</h4>
                                        <p>Chọn kỹ thuật viên và thời gian chính thức trước khi tiếp nhận xe.</p>
                                        <form onSubmit={submitConfirmation}>
                                            <label>
                                                Kỹ thuật viên
                                                <select
                                                    required
                                                    value={confirmForm.mechanicUserId}
                                                    onChange={(event) => setConfirmForm((current) => ({ ...current, mechanicUserId: event.target.value }))}
                                                >
                                                    <option value="">Chọn kỹ thuật viên đang làm việc</option>
                                                    {(mechanicsQuery.data?.content ?? []).map((mechanic) => (
                                                        <option key={mechanic.userId} value={mechanic.userId}>
                                                            {mechanic.fullName} · {mechanic.specialization || 'Tổng quát'}
                                                        </option>
                                                    ))}
                                                </select>
                                            </label>
                                            <label>
                                                Thời gian bắt đầu
                                                <input
                                                    required
                                                    type="datetime-local"
                                                    value={confirmForm.scheduledStartAt}
                                                    onChange={(event) => setConfirmForm((current) => ({ ...current, scheduledStartAt: event.target.value }))}
                                                />
                                            </label>
                                            <label>
                                                Thời gian kết thúc (không bắt buộc)
                                                <input
                                                    type="datetime-local"
                                                    value={confirmForm.scheduledEndAt}
                                                    onChange={(event) => setConfirmForm((current) => ({ ...current, scheduledEndAt: event.target.value }))}
                                                />
                                            </label>
                                            <label>
                                                Ghi chú nội bộ
                                                <textarea
                                                    rows={3}
                                                    value={confirmForm.internalNote}
                                                    onChange={(event) => setConfirmForm((current) => ({ ...current, internalNote: event.target.value }))}
                                                    placeholder="Thông tin dành cho trung tâm và kỹ thuật viên"
                                                />
                                            </label>
                                            <div className="ops-shift-setup">
                                                <div>
                                                    <strong>Thiết lập ca làm</strong>
                                                    <p>Thiết lập một lần cho kỹ thuật viên ở ngày đang chọn trước khi phân công.</p>
                                                </div>
                                                <div className="ops-shift-grid">
                                                    <label>Vào ca<input type="time" value={shiftForm.startTime} onChange={(event) => setShiftForm((current) => ({ ...current, startTime: event.target.value }))} /></label>
                                                    <label>Ra ca<input type="time" value={shiftForm.endTime} onChange={(event) => setShiftForm((current) => ({ ...current, endTime: event.target.value }))} /></label>
                                                    <label>Nghỉ từ<input type="time" value={shiftForm.breakStart} onChange={(event) => setShiftForm((current) => ({ ...current, breakStart: event.target.value }))} /></label>
                                                    <label>Đến<input type="time" value={shiftForm.breakEnd} onChange={(event) => setShiftForm((current) => ({ ...current, breakEnd: event.target.value }))} /></label>
                                                </div>
                                                {saveShiftMutation.isError && <p className="ops-action-error">{getErrorMessage(saveShiftMutation.error)}</p>}
                                                <button
                                                    type="button"
                                                    className="ops-secondary-action"
                                                    disabled={!confirmForm.mechanicUserId || !confirmForm.scheduledStartAt || saveShiftMutation.isPending}
                                                    onClick={() => { setActionNotice(null); saveShiftMutation.mutate() }}
                                                >
                                                    {saveShiftMutation.isPending ? 'Đang lưu ca...' : 'Lưu ca làm ngày này'}
                                                </button>
                                            </div>
                                            {confirmMutation.isError && <p className="ops-action-error">{getErrorMessage(confirmMutation.error)}</p>}
                                            <button type="submit" disabled={confirmMutation.isPending || mechanicsQuery.isLoading}>
                                                {confirmMutation.isPending ? 'Đang xác nhận...' : 'Xác nhận & phân công'}
                                            </button>
                                        </form>
                                    </section>
                                )}

                                {selectedBooking.status === 'PENDING' && (
                                    <section className="ops-action-card">
                                        <h4>Từ chối lịch hẹn</h4>
                                        <p>Chỉ từ chối khi trung tâm không thể đáp ứng yêu cầu; lý do sẽ được gửi cho khách hàng.</p>
                                        <form onSubmit={(event) => { event.preventDefault(); rejectMutation.reset(); if (rejectReason.trim()) rejectMutation.mutate() }}>
                                            <label>Lý do từ chối<textarea required rows={2} maxLength={500} value={rejectReason} onChange={(event) => setRejectReason(event.target.value)} placeholder="Ví dụ: Trung tâm đã kín lịch trong khung giờ này" /></label>
                                            {rejectMutation.isError && <p className="ops-action-error">{getErrorMessage(rejectMutation.error)}</p>}
                                            <button type="submit" className="ops-danger-action" disabled={rejectMutation.isPending}>{rejectMutation.isPending ? 'Đang từ chối...' : 'Từ chối lịch hẹn'}</button>
                                        </form>
                                    </section>
                                )}

                                {selectedBooking.status === 'CONFIRMED' && (
                                    <section className="ops-action-card">
                                        <h4>Tiếp nhận xe tại trung tâm</h4>
                                        <p>Khi khách mang xe đến, xác nhận tiếp nhận để chuyển lịch hẹn sang trạng thái đang thực hiện.</p>
                                        {!getCheckInAvailability(selectedBooking).allowed && (
                                            <p className="ops-checkin-hint">{getCheckInAvailability(selectedBooking).message}</p>
                                        )}
                                        {checkInMutation.isError && <p className="ops-action-error">{getErrorMessage(checkInMutation.error)}</p>}
                                        <button type="button" onClick={() => {
                                            setActionNotice(null)
                                            setActionError(null)
                                            const availability = getCheckInAvailability(selectedBooking)
                                            if (!availability.allowed) {
                                                setActionError(availability.message)
                                                return
                                            }
                                            checkInMutation.mutate()
                                        }} disabled={checkInMutation.isPending}>
                                            {checkInMutation.isPending ? 'Đang tiếp nhận...' : 'Tiếp nhận xe'}
                                        </button>
                                    </section>
                                )}

                                {selectedBooking.status === 'CONFIRMED' && (
                                    <section className="ops-action-card">
                                        <h4>Khách không đến</h4>
                                        <p>Chỉ dùng sau giờ hẹn khi khách không mang xe tới. Thao tác này kết thúc lịch hẹn.</p>
                                        {noShowMutation.isError && <p className="ops-action-error">{getErrorMessage(noShowMutation.error)}</p>}
                                        <button type="button" className="ops-danger-action" disabled={noShowMutation.isPending} onClick={() => noShowMutation.mutate()}>{noShowMutation.isPending ? 'Đang cập nhật...' : 'Đánh dấu không đến'}</button>
                                    </section>
                                )}

                                {selectedBooking.status === 'IN_PROGRESS' && (
                                    <section className="ops-action-card">
                                        <h4>Tạo lệnh sửa chữa</h4>
                                        <p>Lệnh sẽ kế thừa đúng khách hàng, phương tiện, yêu cầu dịch vụ và kỹ thuật viên đã được phân công.</p>
                                        {createRepairMutation.isError && <p className="ops-action-error">{getErrorMessage(createRepairMutation.error)}</p>}
                                        <button type="button" onClick={() => { setActionNotice(null); createRepairMutation.mutate() }} disabled={createRepairMutation.isPending}>
                                            {createRepairMutation.isPending ? 'Đang tạo lệnh...' : 'Tạo lệnh sửa chữa'}
                                        </button>
                                        {actionNotice && <button type="button" className="ops-action-link" onClick={() => navigate('/admin/repairs')}>Mở Repair Service</button>}
                                    </section>
                                )}
                            </div>
                        )}
                    </aside>
                </div>
            )}
        </div>
    )
}
