import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'
import type { CreateVehicleRequest, Vehicle, VehicleStatus } from '../types/vehicle'

const ADMIN_VEHICLE_ENDPOINT = '/api/v1/admin/vehicles'

export interface VehiclePage {
    content: Vehicle[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface AdminVehicleFilters {
    keyword?: string
    status?: VehicleStatus | ''
    page: number
    size: number
}

export const adminVehicleService = {
    async createForCustomer(customerId: number, input: CreateVehicleRequest): Promise<Vehicle> {
        const response = await apiClient.post<ApiResponse<Vehicle>>(
            `${ADMIN_VEHICLE_ENDPOINT}/customers/${customerId}`, input,
        )
        return response.data.data
    },
    async getVehicles(
        filters: AdminVehicleFilters,
    ): Promise<VehiclePage> {
        const response = await apiClient.get<
            ApiResponse<VehiclePage>
        >(ADMIN_VEHICLE_ENDPOINT, {
            params: {
                keyword: filters.keyword || undefined,
                status: filters.status || undefined,
                page: filters.page,
                size: filters.size,
                sort: 'createdAt,desc',
            },
        })

        return response.data.data
    },

    async getVehicleById(vehicleId: number): Promise<Vehicle> {
        const response = await apiClient.get<ApiResponse<Vehicle>>(
            `${ADMIN_VEHICLE_ENDPOINT}/${vehicleId}`,
        )

        return response.data.data
    },
}
