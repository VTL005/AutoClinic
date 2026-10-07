import type { AccountStatus } from './api'

export type SkillLevel = 'JUNIOR' | 'SENIOR' | 'EXPERT'
export type EmploymentStatus = 'ACTIVE' | 'ON_LEAVE' | 'INACTIVE'

export interface Mechanic {
    profileId: number
    userId: number
    username: string
    fullName: string
    phone: string
    email: string | null
    accountStatus: AccountStatus
    skillLevel: SkillLevel
    specialization: string | null
    hourlyRate: number
    employmentStatus: EmploymentStatus
    createdAt: string
    updatedAt: string
}

export interface CreateMechanicRequest {
    username: string
    password: string
    fullName: string
    phone: string
    email: string | null
    skillLevel: SkillLevel
    specialization: string | null
    hourlyRate: number
}
