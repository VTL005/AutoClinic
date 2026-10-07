import apiClient from '../api/client'
import type { ApiResponse } from '../types/api'

export interface SaveMechanicWorkDayRequest {
  workingDay: boolean
  startTime: string
  endTime: string
  breakStart?: string | null
  breakEnd?: string | null
}

export interface MechanicWorkDay {
  mechanicUserId: number
  date: string
  workingDay: boolean
  startTime: string
  endTime: string
  breakStart: string | null
  breakEnd: string | null
}

export const adminMechanicScheduleService = {
  async getWorkDays(date: string): Promise<MechanicWorkDay[]> {
    const response = await apiClient.get<ApiResponse<MechanicWorkDay[]>>(
      '/api/v1/admin/bookings/work-days',
      { params: { date } },
    )
    return response.data.data
  },

  async getWorkDay(mechanicId: number, date: string): Promise<MechanicWorkDay> {
    const response = await apiClient.get<ApiResponse<MechanicWorkDay>>(
      `/api/v1/admin/bookings/mechanics/${mechanicId}/work-days/${date}`,
    )
    return response.data.data
  },

  async saveWorkDay(
    mechanicId: number,
    date: string,
    request: SaveMechanicWorkDayRequest,
  ): Promise<MechanicWorkDay> {
    const response = await apiClient.put<ApiResponse<MechanicWorkDay>>(
      `/api/v1/admin/bookings/mechanics/${mechanicId}/work-days/${date}`,
      request,
    )
    return response.data.data
  },
}
