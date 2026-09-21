import type { User } from '../types/api'

const ACCESS_TOKEN_KEY = 'autoservice_access_token'
const REFRESH_TOKEN_KEY = 'autoservice_refresh_token'
const USER_KEY = 'autoservice_user'

export const authStorage = {
    getAccessToken(): string | null {
        return localStorage.getItem(ACCESS_TOKEN_KEY)
    },

    getRefreshToken(): string | null {
        return localStorage.getItem(REFRESH_TOKEN_KEY)
    },

    getUser(): User | null {
        const storedUser = localStorage.getItem(USER_KEY)

        if (!storedUser) {
            return null
        }

        try {
            return JSON.parse(storedUser) as User
        } catch {
            localStorage.removeItem(USER_KEY)
            return null
        }
    },

    saveSession(
        accessToken: string,
        refreshToken: string,
        user: User,
    ): void {
        localStorage.setItem(ACCESS_TOKEN_KEY, accessToken)
        localStorage.setItem(REFRESH_TOKEN_KEY, refreshToken)
        localStorage.setItem(USER_KEY, JSON.stringify(user))
    },

    clearSession(): void {
        localStorage.removeItem(ACCESS_TOKEN_KEY)
        localStorage.removeItem(REFRESH_TOKEN_KEY)
        localStorage.removeItem(USER_KEY)
    },
}