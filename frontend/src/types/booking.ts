export type BookingStatus =
    | 'PENDING'
    | 'CONFIRMED'
    | 'IN_PROGRESS'
    | 'COMPLETED'
    | 'CANCELLED'
    | 'REJECTED'
    | 'NO_SHOW'

export interface Booking {
    id: number
    bookingCode: string
    customerUserId: number
    vehicleId: number
    mechanicUserId: number | null
    serviceType: string
    requestedDate: string
    requestedTime: string
    scheduledStartAt: string | null
    scheduledEndAt: string | null
    customerNote: string | null
    internalNote: string | null
    status: BookingStatus
    cancellationReason: string | null
    cancelledAt: string | null
    createdAt: string
    updatedAt: string
    checkedInAt: string | null
    serviceId: number | null
    serviceDurationMinutes: number | null
}

export interface ServiceCatalogItem {
    id: number
    serviceCode: string
    name: string
    description: string | null
    durationMinutes: number
    active: boolean
}

export interface CreateBookingRequest {
    vehicleId: number
    serviceType: string
    requestedDate: string
    requestedTime: string
    customerNote: string
    serviceId: number
}

export interface BookingMechanic {
    userId: number
    fullName: string
    skillLevel: string
    specialization: string | null
}

export interface BookingAvailability {
    mechanicUserId: number
    serviceId: number
    date: string
    durationMinutes: number
    slotStepMinutes: number
    slots: Array<{ startAt: string; endAt: string }>
}

export interface BookingPage {
    content: Booking[]
    number: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}
