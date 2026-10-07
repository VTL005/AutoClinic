import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'
import type {
    EmploymentStatus,
    CreateMechanicRequest,
    Mechanic,
    SkillLevel,
} from '../types/mechanic'

const ADMIN_MECHANIC_ENDPOINT = '/api/v1/admin/mechanics'

export interface MechanicPage {
    content: Mechanic[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

interface MechanicFilters {
    keyword?: string
    skillLevel?: SkillLevel | ''
    employmentStatus?: EmploymentStatus | ''
    page: number
    size: number
}

export const adminMechanicService = {
    async createMechanic(
        request: CreateMechanicRequest,
    ): Promise<Mechanic> {
        const response = await apiClient.post<ApiResponse<Mechanic>>(
            ADMIN_MECHANIC_ENDPOINT,
            request,
        )
        return response.data.data
    },

    async getMechanics(filters: MechanicFilters): Promise<MechanicPage> {
        const response = await apiClient.get<ApiResponse<MechanicPage>>(
            ADMIN_MECHANIC_ENDPOINT,
            {
                params: {
                    keyword: filters.keyword || undefined,
                    skillLevel: filters.skillLevel || undefined,
                    employmentStatus:
                        filters.employmentStatus || undefined,
                    page: filters.page,
                    size: filters.size,
                    sort: 'createdAt,desc',
                },
            },
        )

        return response.data.data
    },

    async updateMechanic(profileId: number, input: {
        skillLevel: SkillLevel
        specialization: string | null
        hourlyRate: number
        employmentStatus: EmploymentStatus
    }): Promise<Mechanic> {
        const response = await apiClient.put<ApiResponse<Mechanic>>(
            `${ADMIN_MECHANIC_ENDPOINT}/${profileId}`, input,
        )
        return response.data.data
    },
}
