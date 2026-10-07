import type { AccountStatus, Role } from './api'

export interface AdminUser {
    id: number
    username: string
    fullName: string
    phone: string
    email: string | null
    role: Role
    accountStatus: AccountStatus
    failedLoginCount: number
    lockedUntil: string | null
    createdAt: string
    updatedAt: string
}
