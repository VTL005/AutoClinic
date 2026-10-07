import apiClient from '../api/client'
import type { AccountStatus, ApiResponse, Role } from '../types/api'
import type { AdminUser } from '../types/adminUser'

const ADMIN_USER_ENDPOINT = '/api/v1/admin/users'

export interface AdminUserPage {
    content: AdminUser[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

interface AdminUserFilters {
    keyword?: string
    role?: Role
    accountStatus?: AccountStatus | ''
    page: number
    size: number
}

export const adminUserService = {
    async createWalkInCustomer(input: { fullName: string; phone: string; email?: string | null }): Promise<AdminUser> {
        const response = await apiClient.post<ApiResponse<AdminUser>>(
            `${ADMIN_USER_ENDPOINT}/customers/walk-in`, input,
        )
        return response.data.data
    },
    async getUsers(filters: AdminUserFilters): Promise<AdminUserPage> {
        const response = await apiClient.get<ApiResponse<AdminUserPage>>(
            ADMIN_USER_ENDPOINT,
            {
                params: {
                    keyword: filters.keyword || undefined,
                    role: filters.role,
                    accountStatus: filters.accountStatus || undefined,
                    page: filters.page,
                    size: filters.size,
                    sort: 'createdAt,desc',
                },
            },
        )

        return response.data.data
    },

    async getUserById(userId: number): Promise<AdminUser> {
        const response = await apiClient.get<ApiResponse<AdminUser>>(
            `${ADMIN_USER_ENDPOINT}/${userId}`,
        )

        return response.data.data
    },

    async updateAccountStatus(userId: number, accountStatus: AccountStatus): Promise<AdminUser> {
        const response = await apiClient.patch<ApiResponse<AdminUser>>(
            `${ADMIN_USER_ENDPOINT}/${userId}/status`, { accountStatus },
        )
        return response.data.data
    },

    async resetPassword(userId: number, newPassword: string, confirmPassword: string): Promise<AdminUser> {
        const response = await apiClient.patch<ApiResponse<AdminUser>>(
            `${ADMIN_USER_ENDPOINT}/${userId}/password`, { newPassword, confirmPassword },
        )
        return response.data.data
    },
}
