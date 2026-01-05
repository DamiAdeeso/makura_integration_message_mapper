import apiClient from './apiClient'

export interface ApiKeyDTO {
  id: number
  routeId: string
  maskedKey: string
  description?: string
  validFrom: string
  validUntil: string
  active: boolean
  expired: boolean
  expiringSoon: boolean
  createdBy?: string
  createdAt: string
  updatedAt: string
  lastUsedAt?: string
}

export interface CreateApiKeyRequest {
  routeId: string
  description?: string
  validFrom: string
  validUntil: string
}

export interface UpdateApiKeyRequest {
  description?: string
  validFrom?: string
  validUntil?: string
  active?: boolean
}

export interface ApiKeyResponse {
  id: number
  routeId: string
  apiKey: string // Full key - shown only once!
  description?: string
  validFrom: string
  validUntil: string
  createdBy?: string
  createdAt: string
  warning: string
}

export const apiKeysApi = {
  getAll: async (routeId?: string): Promise<ApiKeyDTO[]> => {
    const params = routeId ? { routeId } : {}
    const response = await apiClient.get('/api-keys', { params })
    return response.data
  },

  getById: async (id: number): Promise<ApiKeyDTO> => {
    const response = await apiClient.get(`/api-keys/${id}`)
    return response.data
  },

  create: async (data: CreateApiKeyRequest): Promise<ApiKeyResponse> => {
    const response = await apiClient.post('/api-keys', data)
    return response.data
  },

  update: async (id: number, data: UpdateApiKeyRequest): Promise<ApiKeyDTO> => {
    const response = await apiClient.put(`/api-keys/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/api-keys/${id}`)
  },

  regenerate: async (id: number): Promise<ApiKeyResponse> => {
    const response = await apiClient.post(`/api-keys/${id}/regenerate`)
    return response.data
  },
}



