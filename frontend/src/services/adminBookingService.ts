import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'
import type { Booking, BookingStatus } from '../types/booking'

const ADMIN_BOOKING_ENDPOINT = '/api/v1/admin/bookings'

interface SpringPage<T> {
    content: T[]
    number: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface BookingPage {
    content: Booking[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

interface BookingFilters {
    status?: BookingStatus | ''
    page: number
    size: number
}

export interface ConfirmBookingRequest {
    mechanicUserId: number
    scheduledStartAt: string
    scheduledEndAt?: string | null
    internalNote?: string | null
}

export interface CreateWalkInBookingRequest {
    customerUserId: number
    vehicleId: number
    mechanicUserId: number
    serviceId: number
    customerNote?: string | null
    internalNote?: string | null
}

export const adminBookingService = {
    async createWalkInBooking(request: CreateWalkInBookingRequest): Promise<Booking> {
        const response = await apiClient.post<ApiResponse<Booking>>(
            `${ADMIN_BOOKING_ENDPOINT}/walk-in`, request,
            { headers: { 'Idempotency-Key': crypto.randomUUID() } },
        )
        return response.data.data
    },
    async getBookings(filters: BookingFilters): Promise<BookingPage> {
        const response = await apiClient.get<
            ApiResponse<SpringPage<Booking>>
        >(ADMIN_BOOKING_ENDPOINT, {
            params: {
                status: filters.status || undefined,
                page: filters.page,
                size: filters.size,
                sort: 'createdAt,desc',
            },
        })

        const result = response.data.data

        return {
            content: result.content,
            page: result.number,
            size: result.size,
            totalElements: result.totalElements,
            totalPages: result.totalPages,
            first: result.first,
            last: result.last,
        }
    },

    async getBookingById(bookingId: number): Promise<Booking> {
        const response = await apiClient.get<ApiResponse<Booking>>(
            `${ADMIN_BOOKING_ENDPOINT}/${bookingId}`,
        )

        return response.data.data
    },

    async confirmBooking(
        bookingId: number,
        request: ConfirmBookingRequest,
    ): Promise<Booking> {
        const response = await apiClient.patch<ApiResponse<Booking>>(
            `${ADMIN_BOOKING_ENDPOINT}/${bookingId}/confirm`,
            request,
        )
        return response.data.data
    },

    async checkInBooking(bookingId: number): Promise<Booking> {
        const response = await apiClient.patch<ApiResponse<Booking>>(
            `${ADMIN_BOOKING_ENDPOINT}/${bookingId}/check-in`,
        )
        return response.data.data
    },

    async rejectBooking(bookingId: number, reason: string): Promise<Booking> {
        const response = await apiClient.patch<ApiResponse<Booking>>(
            `${ADMIN_BOOKING_ENDPOINT}/${bookingId}/reject`, { reason },
        )
        return response.data.data
    },

    async updateStatus(bookingId: number, status: BookingStatus, internalNote?: string | null): Promise<Booking> {
        const response = await apiClient.patch<ApiResponse<Booking>>(
            `${ADMIN_BOOKING_ENDPOINT}/${bookingId}/status`, { status, internalNote: internalNote || null },
        )
        return response.data.data
    },

    async getCalendar(from: string, to: string, mechanicUserId?: number): Promise<Booking[]> {
        const response = await apiClient.get<ApiResponse<Booking[]>>(
            `${ADMIN_BOOKING_ENDPOINT}/calendar`,
            { params: { from, to, mechanicUserId } },
        )
        return response.data.data
    },
}
