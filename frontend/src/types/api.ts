export type Role = 'ADMIN' | 'MECHANIC' | 'CUSTOMER'

export type AccountStatus =
    | 'PENDING_ACTIVATION'
    | 'ACTIVE'
    | 'LOCKED'
    | 'DISABLED'

export interface User {
    id: number
    username: string
    fullName: string
    phone: string
    email: string | null
    role: Role
    accountStatus: AccountStatus
    createdAt: string
}

export interface ApiResponse<T> {
    success: boolean
    message: string
    data: T
}

export interface ErrorResponse {
    success: false
    errorCode: string
    message: string
    fieldErrors: Record<string, string>
    path: string
    traceId: string
    timestamp: string
}

export interface LoginRequest {
    username: string
    password: string
}

export interface AuthTokenData {
    accessToken: string
    refreshToken: string
    tokenType: string
    expiresInSeconds: number
    user: User
}