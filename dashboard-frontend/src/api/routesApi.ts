import apiClient from './apiClient'

export interface RouteDTO {
  id: number
  routeId: string
  name: string
  description?: string
  mode: 'ACTIVE' | 'PASSIVE'
  inboundFormat: string
  outboundFormat: string
  endpoint?: string
  encryptionType?: string
  encryptionKeyRef?: string
  yamlContent?: string
  yamlVersion?: number
  active: boolean
  published: boolean
  createdAt: string
  updatedAt: string
  publishedAt?: string
}

export interface CreateRouteRequest {
  routeId: string
  name: string
  description?: string
  mode: string
  inboundFormat: string
  outboundFormat: string
  endpoint?: string
  encryptionType?: string
  encryptionKeyRef?: string
  yamlContent?: string
  active?: boolean
}

export interface UpdateRouteRequest {
  name?: string
  description?: string
  mode?: string
  inboundFormat?: string
  outboundFormat?: string
  endpoint?: string
  encryptionType?: string
  encryptionKeyRef?: string
  yamlContent?: string
  active?: boolean
}

export const routesApi = {
  getAll: async (activeOnly?: boolean): Promise<RouteDTO[]> => {
    const params = activeOnly ? { activeOnly: true } : {}
    const response = await apiClient.get('/routes', { params })
    return response.data
  },

  getById: async (id: number): Promise<RouteDTO> => {
    const response = await apiClient.get(`/routes/${id}`)
    return response.data
  },

  getByRouteId: async (routeId: string): Promise<RouteDTO> => {
    const response = await apiClient.get(`/routes/route/${routeId}`)
    return response.data
  },

  create: async (data: CreateRouteRequest): Promise<RouteDTO> => {
    const response = await apiClient.post('/routes', data)
    return response.data
  },

  update: async (id: number, data: UpdateRouteRequest): Promise<RouteDTO> => {
    const response = await apiClient.put(`/routes/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/routes/${id}`)
  },

  toggle: async (id: number): Promise<RouteDTO> => {
    const response = await apiClient.post(`/routes/${id}/toggle`)
    return response.data
  },
}

