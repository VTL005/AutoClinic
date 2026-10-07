export type RepairOrderStatus =
    | 'RECEIVED'
    | 'DIAGNOSING'
    | 'WAITING_APPROVAL'
    | 'IN_PROGRESS'
    | 'COMPLETED'
    | 'DELIVERED'
    | 'CANCELLED'

export interface RepairTask {
    id: number
    taskName: string
    description: string | null
    status: string
    laborHours: number | null
    laborCost: number | null
    approvalStatus: string | null
    approvalNote?: string | null
    mandatoryReason?: string | null
    mandatory: boolean
}

export interface RepairOrder {
    id: number
    repairCode: string
    bookingId: number
    customerUserId: number
    vehicleId: number
    mechanicUserId: number | null
    createdByUserId: number
    serviceType: string
    customerComplaint: string | null
    diagnosis: string | null
    technicianNote: string | null
    odometer: number | null
    status: RepairOrderStatus
    receivedAt: string
    estimatedCompletionAt: string | null
    startedAt: string | null
    completedAt: string | null
    deliveredAt: string | null
    createdAt: string
    updatedAt: string
    tasks: RepairTask[]
}
