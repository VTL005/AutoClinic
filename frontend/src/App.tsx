import {
    Navigate,
    Route,
    Routes,
    useNavigate,
} from 'react-router-dom'
import ProtectedRoute from './components/common/ProtectedRoute'
import CustomerLayout from './components/layout/CustomerLayout'
import { useAuth } from './hooks/useAuth'
import LoginPage from './pages/auth/LoginPage'
import CustomerDashboard from './pages/customer/CustomerDashboard'

type ComingSoonProps = {
    title: string
    description: string
}

function ComingSoon({
                        title,
                        description,
                    }: ComingSoonProps) {
    const navigate = useNavigate()

    return (
        <section
            style={{
                display: 'grid',
                minHeight: 'calc(100vh - 150px)',
                placeItems: 'center',
            }}
        >
            <div
                style={{
                    width: 'min(560px, 100%)',
                    padding: '42px',
                    border:
                        '1px solid rgba(103, 211, 248, 0.16)',
                    borderRadius: '24px',
                    background:
                        'linear-gradient(145deg, rgba(19, 42, 70, 0.82), rgba(10, 25, 45, 0.74))',
                    boxShadow:
                        '0 25px 65px rgba(0, 0, 0, 0.2)',
                    textAlign: 'center',
                }}
            >
                <span
                    style={{
                        color: '#67e8f9',
                        fontSize: '11px',
                        fontWeight: 750,
                        letterSpacing: '0.14em',
                        textTransform: 'uppercase',
                    }}
                >
                    AutoService
                </span>

                <h1
                    style={{
                        margin: '12px 0',
                        color: '#f8fafc',
                        fontSize: '30px',
                    }}
                >
                    {title}
                </h1>

                <p
                    style={{
                        margin: '0 0 25px',
                        color: '#91a6bd',
                        lineHeight: 1.7,
                    }}
                >
                    {description}
                </p>

                <button
                    type="button"
                    onClick={() => navigate('/dashboard')}
                    style={{
                        minHeight: '44px',
                        padding: '0 18px',
                        border: 0,
                        borderRadius: '12px',
                        color: '#ffffff',
                        background:
                            'linear-gradient(110deg, #2563eb, #06b6d4)',
                        font: 'inherit',
                        fontWeight: 700,
                        cursor: 'pointer',
                    }}
                >
                    Quay lại tổng quan
                </button>
            </div>
        </section>
    )
}

function AdminDashboard() {
    const navigate = useNavigate()
    const { user, logout } = useAuth()

    if (!user) {
        return null
    }

    const handleLogout = async () => {
        await logout()
        navigate('/login', { replace: true })
    }

    return (
        <main className="temporary-dashboard">
            <div>
                <span>AutoService Admin</span>

                <h1>
                    Xin chào, {user.fullName}
                </h1>

                <p>
                    Bạn đã đăng nhập thành công với vai trò{' '}
                    <strong>{user.role}</strong>.
                </p>

                <button
                    type="button"
                    onClick={handleLogout}
                >
                    Đăng xuất
                </button>
            </div>
        </main>
    )
}

function App() {
    return (
        <Routes>
            <Route
                path="/login"
                element={<LoginPage />}
            />

            <Route
                path="/register"
                element={
                    <Navigate
                        to="/login"
                        replace
                    />
                }
            />

            <Route
                element={
                    <ProtectedRoute
                        allowedRoles={[
                            'CUSTOMER',
                            'MECHANIC',
                        ]}
                    />
                }
            >
                <Route element={<CustomerLayout />}>
                    <Route
                        path="/dashboard"
                        element={<CustomerDashboard />}
                    />

                    <Route
                        path="/vehicles"
                        element={
                            <ComingSoon
                                title="Quản lý phương tiện"
                                description="Chức năng quản lý phương tiện sẽ được kết nối với Vehicle Service trong bước tiếp theo."
                            />
                        }
                    />

                    <Route
                        path="/vin-lookup"
                        element={
                            <ComingSoon
                                title="Tra cứu VIN"
                                description="Chức năng tra cứu và giải mã số VIN đang được hoàn thiện."
                            />
                        }
                    />

                    <Route
                        path="/appointments"
                        element={
                            <ComingSoon
                                title="Lịch hẹn dịch vụ"
                                description="Chức năng đặt lịch sửa chữa và bảo dưỡng sẽ sớm được cập nhật."
                            />
                        }
                    />

                    <Route
                        path="/profile"
                        element={
                            <ComingSoon
                                title="Hồ sơ cá nhân"
                                description="Chức năng xem và cập nhật thông tin cá nhân đang được hoàn thiện."
                            />
                        }
                    />
                </Route>
            </Route>

            <Route
                element={
                    <ProtectedRoute
                        allowedRoles={['ADMIN']}
                    />
                }
            >
                <Route
                    path="/admin"
                    element={<AdminDashboard />}
                />
            </Route>

            <Route
                path="/"
                element={
                    <Navigate
                        to="/dashboard"
                        replace
                    />
                }
            />

            <Route
                path="*"
                element={
                    <Navigate
                        to="/login"
                        replace
                    />
                }
            />
        </Routes>
    )
}

export default App