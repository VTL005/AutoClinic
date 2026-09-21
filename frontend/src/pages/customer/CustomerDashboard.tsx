import { useQuery } from '@tanstack/react-query'
import axios from 'axios'
import {
    ArrowRight,
    CalendarDays,
    CarFront,
    CheckCircle2,
    Clock3,
    Gauge,
    LoaderCircle,
    MapPin,
    RefreshCw,
    Search,
    ShieldCheck,
    Sparkles,
    TriangleAlert,
    Wrench,
} from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { vehicleService } from '../../services/vehicleService'
import type { ErrorResponse } from '../../types/api'
import type { Vehicle } from '../../types/vehicle'
import './CustomerDashboard.css'

const formatCurrentDate = () =>
    new Intl.DateTimeFormat('vi-VN', {
        weekday: 'long',
        day: '2-digit',
        month: '2-digit',
        year: 'numeric',
    }).format(new Date())

const formatOdometer = (
    odometerKm: number | null | undefined,
) => {
    if (odometerKm === null || odometerKm === undefined) {
        return '0'
    }

    return new Intl.NumberFormat('vi-VN').format(
        odometerKm,
    )
}

const formatVehicleCount = (count: number) =>
    count.toString().padStart(2, '0')

const getVehicleStatusLabel = (
    vehicle: Vehicle | undefined,
) => {
    if (!vehicle) {
        return 'Chưa có xe'
    }

    switch (vehicle.status) {
        case 'ACTIVE':
            return 'Ổn định'

        case 'UNDER_MAINTENANCE':
            return 'Đang bảo dưỡng'

        case 'INACTIVE':
            return 'Tạm ngừng'

        default:
            return vehicle.status
    }
}

const getErrorMessage = (error: unknown) => {
    if (axios.isAxiosError<ErrorResponse>(error)) {
        return (
            error.response?.data?.message ??
            'Không thể tải dữ liệu phương tiện.'
        )
    }

    return 'Đã xảy ra lỗi khi tải dữ liệu phương tiện.'
}

export default function CustomerDashboard() {
    const navigate = useNavigate()
    const { user } = useAuth()

    const {
        data: vehicles = [],
        isLoading,
        isError,
        error,
        refetch,
        isFetching,
    } = useQuery({
        queryKey: ['my-vehicles'],
        queryFn: vehicleService.getMyVehicles,
        staleTime: 60_000,
        retry: 1,
    })

    const primaryVehicle = vehicles[0]

    const displayName =
        user?.fullName?.trim() ||
        user?.username ||
        'Khách hàng'

    const vehicleName = primaryVehicle
        ? `${primaryVehicle.manufacturer} ${primaryVehicle.model}`
        : 'Chưa có phương tiện'

    const vehicleStatus =
        getVehicleStatusLabel(primaryVehicle)

    return (
        <div className="customer-dashboard">
            <header className="dashboard-heading">
                <div>
                    <span className="dashboard-date">
                        {formatCurrentDate()}
                    </span>

                    <h1>
                        Xin chào,{' '}
                        <span>{displayName}</span>
                    </h1>

                    <p>
                        Theo dõi phương tiện và chăm sóc xe của bạn
                        trên một nền tảng duy nhất.
                    </p>
                </div>

                <button
                    type="button"
                    className="dashboard-booking-button"
                    onClick={() => navigate('/appointments')}
                >
                    <CalendarDays size={19} />

                    <span>Đặt lịch bảo dưỡng</span>

                    <ArrowRight size={18} />
                </button>
            </header>

            {isLoading && (
                <section className="dashboard-api-state">
                    <LoaderCircle
                        className="dashboard-loading-icon"
                        size={32}
                    />

                    <div>
                        <strong>
                            Đang tải dữ liệu phương tiện
                        </strong>

                        <span>
                            Vui lòng chờ trong giây lát...
                        </span>
                    </div>
                </section>
            )}

            {isError && (
                <section className="dashboard-api-state dashboard-api-error">
                    <TriangleAlert size={29} />

                    <div>
                        <strong>
                            Không thể tải dữ liệu phương tiện
                        </strong>

                        <span>
                            {getErrorMessage(error)}
                        </span>
                    </div>

                    <button
                        type="button"
                        onClick={() => refetch()}
                        disabled={isFetching}
                    >
                        <RefreshCw
                            className={
                                isFetching
                                    ? 'dashboard-refreshing'
                                    : ''
                            }
                            size={17}
                        />

                        Thử lại
                    </button>
                </section>
            )}

            <section className="dashboard-hero">
                <div className="dashboard-hero-content">
                    <div className="dashboard-hero-label">
                        <Sparkles size={16} />
                        Chăm sóc xe thông minh
                    </div>

                    <h2>
                        Mọi hành trình đều xứng đáng được
                        <span> chăm sóc hoàn hảo.</span>
                    </h2>

                    <p>
                        Quản lý hồ sơ phương tiện, tra cứu thông tin
                        VIN và theo dõi lịch bảo dưỡng nhanh chóng.
                    </p>

                    <div className="dashboard-hero-actions">
                        <button
                            type="button"
                            className="hero-primary-action"
                            onClick={() => navigate('/vehicles')}
                        >
                            Xem phương tiện
                            <ArrowRight size={18} />
                        </button>

                        <button
                            type="button"
                            className="hero-secondary-action"
                            onClick={() => navigate('/vin-lookup')}
                        >
                            <Search size={18} />
                            Tra cứu VIN
                        </button>
                    </div>
                </div>

                <div className="dashboard-car-visual">
                    <div className="car-orbit car-orbit-one" />
                    <div className="car-orbit car-orbit-two" />

                    <div className="car-icon-shell">
                        <CarFront
                            size={92}
                            strokeWidth={1.25}
                        />
                    </div>

                    <div className="vehicle-status-pill">
                        <span />
                        {primaryVehicle
                            ? `Phương tiện ${vehicleStatus.toLowerCase()}`
                            : 'Sẵn sàng thêm phương tiện'}
                    </div>
                </div>
            </section>

            <section className="dashboard-stat-grid">
                <article className="dashboard-stat-card stat-blue">
                    <div className="stat-icon">
                        <CarFront size={22} />
                    </div>

                    <div>
                        <span>Phương tiện</span>

                        <strong>
                            {isLoading
                                ? '--'
                                : formatVehicleCount(
                                    vehicles.length,
                                )}
                        </strong>

                        <small>Đang được quản lý</small>
                    </div>
                </article>

                <article className="dashboard-stat-card stat-violet">
                    <div className="stat-icon">
                        <CalendarDays size={22} />
                    </div>

                    <div>
                        <span>Lịch hẹn sắp tới</span>
                        <strong>00</strong>
                        <small>Chưa có lịch hẹn mới</small>
                    </div>
                </article>

                <article className="dashboard-stat-card stat-orange">
                    <div className="stat-icon">
                        <Gauge size={22} />
                    </div>

                    <div>
                        <span>Số kilomet</span>

                        <strong>
                            {isLoading
                                ? '--'
                                : formatOdometer(
                                    primaryVehicle?.odometerKm,
                                )}
                        </strong>

                        <small>Km đã ghi nhận</small>
                    </div>
                </article>

                <article className="dashboard-stat-card stat-green">
                    <div className="stat-icon">
                        <ShieldCheck size={22} />
                    </div>

                    <div>
                        <span>Trạng thái</span>

                        <strong className="status-text">
                            {isLoading
                                ? 'Đang tải'
                                : vehicleStatus}
                        </strong>

                        <small>
                            Hồ sơ phương tiện của bạn
                        </small>
                    </div>
                </article>
            </section>

            <div className="dashboard-content-grid">
                <section className="dashboard-section vehicle-overview">
                    <div className="dashboard-section-heading">
                        <div>
                            <span>Phương tiện của bạn</span>
                            <h2>Tổng quan phương tiện</h2>
                        </div>

                        <button
                            type="button"
                            onClick={() => navigate('/vehicles')}
                        >
                            Xem tất cả
                            <ArrowRight size={16} />
                        </button>
                    </div>

                    {!isLoading &&
                    !isError &&
                    primaryVehicle ? (
                        <>
                            <article className="vehicle-preview-card">
                                <div className="vehicle-preview-icon">
                                    <CarFront
                                        size={47}
                                        strokeWidth={1.4}
                                    />
                                </div>

                                <div className="vehicle-preview-info">
                                    <div className="vehicle-name-row">
                                        <div>
                                            <span>
                                                {
                                                    primaryVehicle.manufacturer
                                                }
                                            </span>

                                            <h3>
                                                {primaryVehicle.model}
                                            </h3>
                                        </div>

                                        <div className="active-badge">
                                            <CheckCircle2 size={14} />
                                            {vehicleStatus}
                                        </div>
                                    </div>

                                    <div className="vehicle-information-grid">
                                        <div>
                                            <span>Biển số</span>

                                            <strong>
                                                {
                                                    primaryVehicle.licensePlate
                                                }
                                            </strong>
                                        </div>

                                        <div>
                                            <span>Quãng đường</span>

                                            <strong>
                                                {formatOdometer(
                                                    primaryVehicle.odometerKm,
                                                )}{' '}
                                                km
                                            </strong>
                                        </div>

                                        <div>
                                            <span>Động cơ</span>

                                            <strong>
                                                {primaryVehicle.engineType ||
                                                    'Chưa cập nhật'}
                                            </strong>
                                        </div>
                                    </div>
                                </div>
                            </article>

                            <div className="vehicle-health">
                                <div className="vehicle-health-heading">
                                    <span>
                                        Tình trạng tổng thể
                                    </span>

                                    <strong>
                                        {primaryVehicle.status ===
                                        'ACTIVE'
                                            ? '92%'
                                            : 'Đang kiểm tra'}
                                    </strong>
                                </div>

                                <div className="vehicle-health-track">
                                    <span />
                                </div>

                                <p>
                                    {vehicleName} đang được quản lý
                                    trong hệ thống AutoService.
                                </p>
                            </div>
                        </>
                    ) : null}

                    {!isLoading &&
                        !isError &&
                        vehicles.length === 0 && (
                            <div className="dashboard-empty-vehicle">
                                <div>
                                    <CarFront size={37} />
                                </div>

                                <h3>Bạn chưa có phương tiện</h3>

                                <p>
                                    Hãy thêm phương tiện đầu tiên để theo
                                    dõi và đặt lịch bảo dưỡng.
                                </p>

                                <button
                                    type="button"
                                    onClick={() => navigate('/vehicles')}
                                >
                                    Thêm phương tiện
                                    <ArrowRight size={17} />
                                </button>
                            </div>
                        )}
                </section>

                <section className="dashboard-section quick-actions">
                    <div className="dashboard-section-heading">
                        <div>
                            <span>Truy cập nhanh</span>
                            <h2>Dịch vụ tiện ích</h2>
                        </div>
                    </div>

                    <div className="quick-action-list">
                        <button
                            type="button"
                            className="quick-action quick-action-cyan"
                            onClick={() => navigate('/vin-lookup')}
                        >
                            <div>
                                <Search size={21} />
                            </div>

                            <span>
                                <strong>Tra cứu VIN</strong>
                                Kiểm tra thông tin phương tiện
                            </span>

                            <ArrowRight size={18} />
                        </button>

                        <button
                            type="button"
                            className="quick-action quick-action-violet"
                            onClick={() => navigate('/appointments')}
                        >
                            <div>
                                <CalendarDays size={21} />
                            </div>

                            <span>
                                <strong>Đặt lịch dịch vụ</strong>
                                Chọn thời gian bảo dưỡng phù hợp
                            </span>

                            <ArrowRight size={18} />
                        </button>

                        <button
                            type="button"
                            className="quick-action quick-action-gold"
                        >
                            <div>
                                <MapPin size={21} />
                            </div>

                            <span>
                                <strong>Tìm trung tâm</strong>
                                Trung tâm dịch vụ gần bạn
                            </span>

                            <ArrowRight size={18} />
                        </button>
                    </div>
                </section>
            </div>

            <section className="dashboard-service-reminder">
                <div className="reminder-icon">
                    <Wrench size={25} />
                </div>

                <div>
                    <span>Nhắc lịch bảo dưỡng</span>
                    <h3>
                        Phương tiện chưa có lịch hẹn sắp tới
                    </h3>

                    <p>
                        Đặt lịch trước để lựa chọn thời gian và kỹ
                        thuật viên phù hợp.
                    </p>
                </div>

                <div className="reminder-time">
                    <Clock3 size={17} />
                    Chỉ mất khoảng 2 phút
                </div>

                <button
                    type="button"
                    onClick={() => navigate('/appointments')}
                >
                    Đặt lịch ngay
                    <ArrowRight size={17} />
                </button>
            </section>
        </div>
    )
}