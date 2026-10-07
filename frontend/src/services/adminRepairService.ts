import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'
import type { RepairOrder, RepairOrderStatus } from '../types/repair'

const REPAIR_ENDPOINT = '/api/v1/repair-orders'

interface SpringPage<T> {
    content: T[]
    number: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface RepairOrderPage {
    content: RepairOrder[]
    page: number
    size: number
    totalElements: number
    totalPages: number
    first: boolean
    last: boolean
}

export interface CreateRepairOrderRequest {
    bookingId: number
    odometer?: number | null
    estimatedCompletionAt?: string | null
    technicianNote?: string | null
}

export interface AddRepairTaskRequest {
    taskName: string
    description?: string | null
    laborHours?: number | null
    laborCost?: number | null
    mandatory: boolean
    mandatoryReason?: string | null
}

export interface UpdateRepairOrderDetailsRequest {
    diagnosis?: string | null
    technicianNote?: string | null
    odometer?: number | null
    estimatedCompletionAt?: string | null
}

export interface RepairTaskPart {
    id: number
    inventoryPartId: number
    partCode: string
    partName: string
    unit: string
    quantity: number
    unitPriceSnapshot: number
    lineTotal: number
    reservationStatus: string
}

export interface RepairIntegrationJob {
    id: number
    status: string
    attempt_count: number
    next_attempt_at: string | null
    last_error: string | null
    completed_at: string | null
    booking_id?: number
    target_status?: string
}

export const adminRepairService = {
    async getBookingSyncJobs(orderId: number): Promise<RepairIntegrationJob[]> {
        const response = await apiClient.get<ApiResponse<RepairIntegrationJob[]>>(
            `${REPAIR_ENDPOINT}/${orderId}/booking-sync`,
        )
        return response.data.data
    },

    async retryBookingSync(orderId: number): Promise<number> {
        const response = await apiClient.post<ApiResponse<number>>(
            `${REPAIR_ENDPOINT}/${orderId}/booking-sync/retry`,
        )
        return response.data.data
    },

    async getInventoryReleaseJobs(orderId: number): Promise<RepairIntegrationJob[]> {
        const response = await apiClient.get<ApiResponse<RepairIntegrationJob[]>>(
            `${REPAIR_ENDPOINT}/${orderId}/inventory-release`,
        )
        return response.data.data
    },

    async retryInventoryRelease(orderId: number): Promise<number> {
        const response = await apiClient.post<ApiResponse<number>>(
            `${REPAIR_ENDPOINT}/${orderId}/inventory-release/retry`,
        )
        return response.data.data
    },

    async addTaskPart(orderId: number, taskId: number, partId: number, quantity: number): Promise<RepairTaskPart> {
        const response = await apiClient.post<ApiResponse<RepairTaskPart>>(
            `${REPAIR_ENDPOINT}/${orderId}/tasks/${taskId}/parts`, { partId, quantity },
        )
        return response.data.data
    },

    async decideAtCounter(orderId: number, taskId: number, approved: boolean, note: string): Promise<RepairOrder> {
        const response = await apiClient.patch<ApiResponse<RepairOrder>>(
            `${REPAIR_ENDPOINT}/${orderId}/tasks/${taskId}/counter-approval`, { approved, note },
        )
        return response.data.data
    },
    async getRepairOrders(filters: {
        status: RepairOrderStatus | ''
        page: number
        size: number
    }): Promise<RepairOrderPage> {
        const response = await apiClient.get<
            ApiResponse<SpringPage<RepairOrder>>
        >(REPAIR_ENDPOINT, {
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

    async getRepairOrder(orderId: number): Promise<RepairOrder> {
        const response = await apiClient.get<ApiResponse<RepairOrder>>(
            `${REPAIR_ENDPOINT}/${orderId}`,
        )
        return response.data.data
    },

    async createRepairOrder(
        request: CreateRepairOrderRequest,
    ): Promise<RepairOrder> {
        const response = await apiClient.post<ApiResponse<RepairOrder>>(
            REPAIR_ENDPOINT,
            request,
        )
        return response.data.data
    },

    async addRepairTask(
        orderId: number,
        request: AddRepairTaskRequest,
    ): Promise<RepairOrder> {
        const response = await apiClient.post<ApiResponse<RepairOrder>>(
            `${REPAIR_ENDPOINT}/${orderId}/tasks`,
            request,
        )
        return response.data.data
    },

    async updateDetails(
        orderId: number,
        request: UpdateRepairOrderDetailsRequest,
    ): Promise<RepairOrder> {
        const response = await apiClient.patch<ApiResponse<RepairOrder>>(
            `${REPAIR_ENDPOINT}/${orderId}/details`,
            request,
        )
        return response.data.data
    },

    async updateStatus(
        orderId: number,
        status: RepairOrderStatus,
        note?: string,
    ): Promise<RepairOrder> {
        const response = await apiClient.patch<ApiResponse<RepairOrder>>(
            `${REPAIR_ENDPOINT}/${orderId}/status`,
            { status, note: note || null },
        )
        return response.data.data
    },
}
