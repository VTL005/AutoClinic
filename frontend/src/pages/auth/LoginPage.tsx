import { zodResolver } from '@hookform/resolvers/zod'
import axios from 'axios'
import {
    ArrowRight,
    CarFront,
    Eye,
    EyeOff,
    Gauge,
    LockKeyhole,
    ShieldCheck,
    UserRound,
    Wrench,
} from 'lucide-react'
import { useState } from 'react'
import { useForm } from 'react-hook-form'
import { Navigate, useNavigate } from 'react-router-dom'
import { z } from 'zod'
import { useAuth } from '../../hooks/useAuth'
import type { ErrorResponse } from '../../types/api'
import './LoginPage.css'

const loginSchema = z.object({
    username: z
        .string()
        .trim()
        .min(1, 'Vui lòng nhập tên đăng nhập.'),

    password: z
        .string()
        .min(1, 'Vui lòng nhập mật khẩu.'),
})

type LoginFormValues = z.infer<typeof loginSchema>

export default function LoginPage() {
    const navigate = useNavigate()

    const {
        user,
        isAuthenticated,
        isLoading,
        login,
    } = useAuth()

    const [showPassword, setShowPassword] =
        useState(false)

    const [serverError, setServerError] =
        useState<string | null>(null)

    const {
        register,
        handleSubmit,
        formState: { errors },
    } = useForm<LoginFormValues>({
        resolver: zodResolver(loginSchema),

        defaultValues: {
            username: '',
            password: '',
        },
    })

    if (isAuthenticated && user) {
        return (
            <Navigate
                to={user.role === 'ADMIN'
                    ? '/admin'
                    : '/dashboard'}
                replace
            />
        )
    }

    const onSubmit = async (
        values: LoginFormValues,
    ) => {
        setServerError(null)

        try {
            const loggedInUser = await login(values)

            navigate(
                loggedInUser.role === 'ADMIN'
                    ? '/admin'
                    : '/dashboard',
                { replace: true },
            )
        } catch (error) {
            if (axios.isAxiosError<ErrorResponse>(error)) {
                setServerError(
                    error.response?.data?.message ??
                    'Không thể kết nối đến máy chủ.',
                )

                return
            }

            setServerError(
                'Đã xảy ra lỗi không mong muốn. Vui lòng thử lại.',
            )
        }
    }

    return (
        <main className="login-page">
            <div className="login-glow login-glow-one" />
            <div className="login-glow login-glow-two" />

            <section className="login-showcase">
                <header className="login-brand">
                    <div className="login-brand-icon">
                        <CarFront size={27} strokeWidth={1.8} />
                    </div>

                    <div>
                        <strong>AutoService</strong>
                        <span>Premium Car Care</span>
                    </div>
                </header>

                <div className="showcase-content">
                    <div className="showcase-label">
                        <span />
                        Hệ sinh thái chăm sóc ô tô
                    </div>

                    <h1>
                        Chăm sóc xế yêu
                        <span> theo tiêu chuẩn mới.</span>
                    </h1>

                    <p>
                        Quản lý phương tiện, tra cứu VIN và theo dõi
                        toàn bộ hành trình bảo dưỡng trên một nền tảng
                        an toàn, hiện đại.
                    </p>

                    <div className="showcase-features">
                        <article>
                            <div>
                                <Gauge size={21} />
                            </div>

                            <span>
                <strong>Vận hành thông minh</strong>
                Theo dõi dịch vụ tập trung
              </span>
                        </article>

                        <article>
                            <div>
                                <ShieldCheck size={21} />
                            </div>

                            <span>
                <strong>Bảo mật dữ liệu</strong>
                Xác thực an toàn bằng JWT
              </span>
                        </article>

                        <article>
                            <div>
                                <Wrench size={21} />
                            </div>

                            <span>
                <strong>Dịch vụ chuyên nghiệp</strong>
                Quy trình bảo dưỡng minh bạch
              </span>
                        </article>
                    </div>
                </div>

                <footer className="showcase-footer">
                    <span className="status-dot" />
                    Hệ thống đang hoạt động ổn định
                </footer>
            </section>

            <section className="login-panel">
                <div className="login-card">
                    <div className="mobile-brand">
                        <CarFront size={25} />

                        <strong>AutoService</strong>
                    </div>

                    <div className="login-heading">
                        <span>Chào mừng trở lại</span>
                        <h2>Đăng nhập hệ thống</h2>

                        <p>
                            Nhập thông tin tài khoản để tiếp tục.
                        </p>
                    </div>

                    {serverError && (
                        <div className="login-error" role="alert">
                            <ShieldCheck size={19} />

                            <span>{serverError}</span>
                        </div>
                    )}

                    <form
                        className="login-form"
                        onSubmit={handleSubmit(onSubmit)}
                        noValidate
                    >
                        <div className="form-group">
                            <label htmlFor="username">
                                Tên đăng nhập
                            </label>

                            <div className="input-shell">
                                <UserRound size={19} />

                                <input
                                    id="username"
                                    type="text"
                                    placeholder="Nhập tên đăng nhập"
                                    autoComplete="username"
                                    {...register('username')}
                                />
                            </div>

                            {errors.username && (
                                <small>
                                    {errors.username.message}
                                </small>
                            )}
                        </div>

                        <div className="form-group">
                            <div className="label-row">
                                <label htmlFor="password">
                                    Mật khẩu
                                </label>

                                <button
                                    type="button"
                                    className="forgot-password"
                                >
                                    Quên mật khẩu?
                                </button>
                            </div>

                            <div className="input-shell">
                                <LockKeyhole size={19} />

                                <input
                                    id="password"
                                    type={
                                        showPassword
                                            ? 'text'
                                            : 'password'
                                    }
                                    placeholder="Nhập mật khẩu"
                                    autoComplete="current-password"
                                    {...register('password')}
                                />

                                <button
                                    type="button"
                                    className="password-toggle"
                                    onClick={() =>
                                        setShowPassword((current) => !current)
                                    }
                                    aria-label={
                                        showPassword
                                            ? 'Ẩn mật khẩu'
                                            : 'Hiện mật khẩu'
                                    }
                                >
                                    {showPassword
                                        ? <EyeOff size={19} />
                                        : <Eye size={19} />}
                                </button>
                            </div>

                            {errors.password && (
                                <small>
                                    {errors.password.message}
                                </small>
                            )}
                        </div>

                        <button
                            className="login-submit"
                            type="submit"
                            disabled={isLoading}
                        >
              <span>
                {isLoading
                    ? 'Đang đăng nhập...'
                    : 'Đăng nhập'}
              </span>

                            {!isLoading && (
                                <ArrowRight size={20} />
                            )}
                        </button>
                    </form>

                    <div className="register-prompt">
                        Chưa có tài khoản?

                        <button
                            type="button"
                            onClick={() => navigate('/register')}
                        >
                            Đăng ký ngay
                        </button>
                    </div>

                    <div className="login-security">
                        <ShieldCheck size={16} />

                        Kết nối được mã hóa và bảo vệ an toàn
                    </div>
                </div>
            </section>
        </main>
    )
}