import {
    CalendarDays,
    CarFront,
    ChevronRight,
    Gauge,
    LogOut,
    Menu,
    Search,
    UserRound,
    Wrench,
    X,
} from 'lucide-react'
import { useState } from 'react'
import {
    NavLink,
    Outlet,
    useNavigate,
} from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import './CustomerLayout.css'

const navigationItems = [
    {
        label: 'Tổng quan',
        path: '/dashboard',
        icon: Gauge,
    },
    {
        label: 'Phương tiện',
        path: '/vehicles',
        icon: CarFront,
    },
    {
        label: 'Tra cứu VIN',
        path: '/vin-lookup',
        icon: Search,
    },
    {
        label: 'Lịch hẹn',
        path: '/appointments',
        icon: CalendarDays,
    },
    {
        label: 'Hồ sơ cá nhân',
        path: '/profile',
        icon: UserRound,
    },
]

export default function CustomerLayout() {
    const navigate = useNavigate()
    const { user, logout } = useAuth()

    const [sidebarOpen, setSidebarOpen] =
        useState(false)

    const handleLogout = async () => {
        await logout()
        navigate('/login', { replace: true })
    }

    const closeSidebar = () => {
        setSidebarOpen(false)
    }

    const userInitial =
        user?.fullName?.trim().charAt(0).toUpperCase() ??
        'U'

    return (
        <div className="customer-shell">
            <div
                className={
                    sidebarOpen
                        ? 'sidebar-overlay visible'
                        : 'sidebar-overlay'
                }
                onClick={closeSidebar}
            />

            <aside
                className={
                    sidebarOpen
                        ? 'customer-sidebar open'
                        : 'customer-sidebar'
                }
            >
                <div className="sidebar-header">
                    <button
                        type="button"
                        className="sidebar-brand"
                        onClick={() => navigate('/dashboard')}
                    >
            <span className="sidebar-brand-icon">
              <CarFront size={24} />
            </span>

                        <span>
              <strong>AutoService</strong>
              <small>Premium Car Care</small>
            </span>
                    </button>

                    <button
                        type="button"
                        className="sidebar-close"
                        onClick={closeSidebar}
                        aria-label="Đóng menu"
                    >
                        <X size={21} />
                    </button>
                </div>

                <div className="sidebar-section-label">
                    Không gian của bạn
                </div>

                <nav className="sidebar-navigation">
                    {navigationItems.map((item) => {
                        const Icon = item.icon

                        return (
                            <NavLink
                                key={item.path}
                                to={item.path}
                                onClick={closeSidebar}
                                className={({ isActive }) =>
                                    isActive
                                        ? 'sidebar-link active'
                                        : 'sidebar-link'
                                }
                            >
                <span className="sidebar-link-icon">
                  <Icon size={19} />
                </span>

                                <span>{item.label}</span>

                                <ChevronRight
                                    className="sidebar-link-arrow"
                                    size={16}
                                />
                            </NavLink>
                        )
                    })}
                </nav>

                <div className="sidebar-service-card">
          <span className="service-card-icon">
            <Wrench size={21} />
          </span>

                    <div>
                        <strong>Cần hỗ trợ?</strong>

                        <p>
                            Đội ngũ kỹ thuật luôn sẵn sàng đồng hành
                            cùng bạn.
                        </p>
                    </div>

                    <button type="button">
                        Liên hệ cố vấn
                    </button>
                </div>

                <div className="sidebar-user">
          <span className="sidebar-avatar">
            {userInitial}
          </span>

                    <span className="sidebar-user-info">
            <strong>{user?.fullName}</strong>
            <small>Khách hàng</small>
          </span>

                    <button
                        type="button"
                        className="sidebar-logout"
                        onClick={handleLogout}
                        title="Đăng xuất"
                    >
                        <LogOut size={18} />
                    </button>
                </div>
            </aside>

            <div className="customer-workspace">
                <header className="customer-topbar">
                    <button
                        type="button"
                        className="mobile-menu-button"
                        onClick={() => setSidebarOpen(true)}
                        aria-label="Mở menu"
                    >
                        <Menu size={22} />
                    </button>

                    <div className="topbar-status">
                        <span />
                        Trung tâm dịch vụ đang hoạt động
                    </div>

                    <div className="topbar-user">
            <span>
              <small>Xin chào</small>
              <strong>{user?.fullName}</strong>
            </span>

                        <div className="topbar-avatar">
                            {userInitial}
                        </div>
                    </div>
                </header>

                <main className="customer-content">
                    <Outlet />
                </main>
            </div>
        </div>
    )
}