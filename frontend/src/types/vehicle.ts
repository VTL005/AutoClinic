export type VehicleStatus =
    | 'ACTIVE'
    | 'INACTIVE'
    | 'UNDER_MAINTENANCE'

export interface Vehicle {
    id: number
    ownerUserId: number
    vin: string
    licensePlate: string
    manufacturer: string
    model: string
    modelYear: number | null
    color: string | null
    engineType: string | null
    odometerKm: number
    status: VehicleStatus
    createdAt: string
    updatedAt: string
}