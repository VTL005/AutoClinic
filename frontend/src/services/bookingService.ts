import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'
import type {
    Booking,
    BookingPage,
    CreateBookingRequest,
    BookingAvailability,
    BookingMechanic,
    ServiceCatalogItem,
} from '../types/booking'

interface SpringPage<T> {
    content: T[]
    number: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

const BOOKING_ENDPOINT = '/api/v1/bookings'

export const bookingService = {
    async getMyBookings(
        page = 0,
        size = 50,
    ): Promise<BookingPage> {
        const response = await apiClient.get<
            ApiResponse<SpringPage<Booking>>
        >(BOOKING_ENDPOINT, {
            params: {
                page,
                size,
                sort: 'createdAt,desc',
            },
        })

        return response.data.data
    },

    async getServices(): Promise<ServiceCatalogItem[]> {
        const response = await apiClient.get<
            ApiResponse<SpringPage<ServiceCatalogItem>>
        >(`${BOOKING_ENDPOINT}/services`, {
            params: {
                page: 0,
                size: 100,
                sort: 'name,asc',
            },
        })

        return response.data.data.content
    },

    async createBooking(
        request: CreateBookingRequest,
    ): Promise<Booking> {
        const response =
            await apiClient.post<ApiResponse<Booking>>(
                BOOKING_ENDPOINT,
                request,
            )

        return response.data.data
    },

    async getAvailableMechanics(): Promise<BookingMechanic[]> {
        const response = await apiClient.get<ApiResponse<BookingMechanic[]>>(
            '/api/v1/mechanics/available',
        )
        return response.data.data
    },

    async getAvailability(
        mechanicUserId: number,
        serviceId: number,
        date: string,
    ): Promise<BookingAvailability> {
        const response = await apiClient.get<ApiResponse<BookingAvailability>>(
            `${BOOKING_ENDPOINT}/availability`,
            { params: { mechanicUserId, serviceId, date } },
        )
        return response.data.data
    },

    async cancelBooking(
        bookingId: number,
        cancellationReason: string,
    ): Promise<Booking> {
        const response =
            await apiClient.patch<ApiResponse<Booking>>(
                `${BOOKING_ENDPOINT}/${bookingId}/cancel`,
                { cancellationReason },
            )

        return response.data.data
    },
}
