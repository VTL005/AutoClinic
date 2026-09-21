import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'
import type { Vehicle } from '../types/vehicle'

const VEHICLE_ENDPOINT = '/api/v1/vehicles'

interface VehiclePage {
    content: Vehicle[]
    page?: number
    size?: number
    totalElements?: number
    totalPages?: number
    first?: boolean
    last?: boolean
}

type VehicleListData =
    | Vehicle[]
    | VehiclePage

const extractVehicles = (
    data: VehicleListData,
): Vehicle[] => {
    if (Array.isArray(data)) {
        return data
    }

    if (Array.isArray(data.content)) {
        return data.content
    }

    return []
}

export const vehicleService = {
    async getMyVehicles(): Promise<Vehicle[]> {
        const response =
            await apiClient.get<
                ApiResponse<VehicleListData>
            >(VEHICLE_ENDPOINT)

        return extractVehicles(
            response.data.data,
        )
    },

    async getVehicleById(
        vehicleId: number,
    ): Promise<Vehicle> {
        const response =
            await apiClient.get<ApiResponse<Vehicle>>(
                `${VEHICLE_ENDPOINT}/${vehicleId}`,
            )

        return response.data.data
    },
}