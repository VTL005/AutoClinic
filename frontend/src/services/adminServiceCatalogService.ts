import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'
import type { ServiceCatalogItem } from '../types/booking'

interface SpringPage<T> {
    content: T[]
    number: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface ServiceCatalogPage {
    content: ServiceCatalogItem[]
    page: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface SaveServiceCatalogInput {
    serviceCode: string
    name: string
    description: string | null
    durationMinutes: number
    active: boolean
}

export const adminServiceCatalogService = {
    async getServices(filters: { active: '' | 'true' | 'false'; page: number; size: number }): Promise<ServiceCatalogPage> {
        const response = await apiClient.get<ApiResponse<SpringPage<ServiceCatalogItem>>>(
            '/api/v1/admin/bookings/services', { params: { active: filters.active || undefined, page: filters.page, size: filters.size, sort: 'name,asc' } },
        )
        const result = response.data.data
        return { content: result.content, page: result.number, totalElements: result.totalElements, totalPages: result.totalPages, first: result.first, last: result.last }
    },

    async create(input: SaveServiceCatalogInput): Promise<ServiceCatalogItem> {
        const response = await apiClient.post<ApiResponse<ServiceCatalogItem>>('/api/v1/admin/bookings/services', input)
        return response.data.data
    },

    async update(id: number, input: SaveServiceCatalogInput): Promise<ServiceCatalogItem> {
        const response = await apiClient.patch<ApiResponse<ServiceCatalogItem>>(`/api/v1/admin/bookings/services/${id}`, input)
        return response.data.data
    },
}
