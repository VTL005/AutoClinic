import {
    Navigate,
    Outlet,
    useLocation,
} from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import type { Role } from '../../types/api'

interface ProtectedRouteProps {
    allowedRoles?: Role[]
}

export default function ProtectedRoute({
                                           allowedRoles,
                                       }: ProtectedRouteProps) {
    const location = useLocation()

    const {
        user,
        isAuthenticated,
    } = useAuth()

    if (!isAuthenticated || !user) {
        return (
            <Navigate
                to="/login"
                state={{ from: location.pathname }}
                replace
            />
        )
    }

    if (
        allowedRoles &&
        !allowedRoles.includes(user.role)
    ) {
        const redirectPath =
            user.role === 'ADMIN'
                ? '/admin'
                : '/dashboard'

        return (
            <Navigate
                to={redirectPath}
                replace
            />
        )
    }

    return <Outlet />
}