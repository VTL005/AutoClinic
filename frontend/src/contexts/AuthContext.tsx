import {
    createContext,
    useCallback,
    useMemo,
    useState,
    type ReactNode,
} from 'react'
import { authService } from '../services/authService'
import type { LoginRequest, User } from '../types/api'
import { authStorage } from '../utils/authStorage'

interface AuthContextValue {
    user: User | null
    isAuthenticated: boolean
    isLoading: boolean
    login: (request: LoginRequest) => Promise<User>
    logout: () => Promise<void>
}

export const AuthContext =
    createContext<AuthContextValue | undefined>(
        undefined,
    )

interface AuthProviderProps {
    children: ReactNode
}

export function AuthProvider({
                                 children,
                             }: AuthProviderProps) {
    const [user, setUser] = useState<User | null>(
        () => authStorage.getUser(),
    )

    const [isLoading, setIsLoading] = useState(false)

    const login = useCallback(
        async (request: LoginRequest): Promise<User> => {
            setIsLoading(true)

            try {
                const result = await authService.login(request)

                authStorage.saveSession(
                    result.accessToken,
                    result.refreshToken,
                    result.user,
                )

                setUser(result.user)

                return result.user
            } finally {
                setIsLoading(false)
            }
        },
        [],
    )

    const logout = useCallback(async (): Promise<void> => {
        const refreshToken =
            authStorage.getRefreshToken()

        try {
            if (refreshToken) {
                await authService.logout(refreshToken)
            }
        } finally {
            authStorage.clearSession()
            setUser(null)
        }
    }, [])

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            isAuthenticated: Boolean(
                user && authStorage.getAccessToken(),
            ),
            isLoading,
            login,
            logout,
        }),
        [user, isLoading, login, logout],
    )

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    )
}