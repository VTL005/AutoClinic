import apiClient from '../api/client'
import type {
    ApiResponse,
    AuthTokenData,
    LoginRequest,
    User,
} from '../types/api'

export const authService = {
    async login(
        request: LoginRequest,
    ): Promise<AuthTokenData> {
        const response =
            await apiClient.post<ApiResponse<AuthTokenData>>(
                '/api/v1/auth/login',
                request,
            )

        return response.data.data
    },

    async getCurrentUser(): Promise<User> {
        const response =
            await apiClient.get<ApiResponse<User>>(
                '/api/v1/users/me',
            )

        return response.data.data
    },

    async logout(refreshToken: string): Promise<void> {
        await apiClient.post('/api/v1/auth/logout', {
            refreshToken,
        })
    },
}