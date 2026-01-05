import apiClient from './apiClient'

export interface FieldMappingDTO {
  id?: number // For existing mappings
  routeId?: string // Route this mapping belongs to
  sourcePath: string // Supports "constant:value" syntax for constant values
  targetPath: string
  defaultValue?: string // Default value if source is null
  transformation?: string // Legacy field name
  transform?: string // Transformation expression (e.g., "formatDateTime(now(), 'yyyy-MM-ddTHH:mm:ss.SSSZ')")
}

export interface MappingConfigDTO {
  routeId: string
  name: string
  description?: string
  inputFormat: string
  outputFormat: string
  mode?: string // ACTIVE or PASSIVE
  endpoint?: string // For ACTIVE mode
  mappings?: FieldMappingDTO[] // Legacy: flat list
  requestMappings?: FieldMappingDTO[] // Request direction mappings
  responseMappings?: FieldMappingDTO[] // Response direction mappings
}

export interface CreateFieldMappingRequest {
  routeId: string
  sourcePath: string
  targetPath: string
  transform?: string
  defaultValue?: string
}

export interface UpdateFieldMappingRequest {
  sourcePath?: string
  targetPath?: string
  transform?: string
  defaultValue?: string
}

export const mappingsApi = {
  getAll: async (): Promise<FieldMappingDTO[]> => {
    // Note: This endpoint may need to be implemented in the backend
    // For now, returning empty array as mappings are typically managed through routes
    const response = await apiClient.get('/mappings')
    return response.data || []
  },

  create: async (data: CreateFieldMappingRequest): Promise<FieldMappingDTO> => {
    // Note: This endpoint may need to be implemented in the backend
    const response = await apiClient.post('/mappings', data)
    return response.data
  },

  update: async (id: number, data: UpdateFieldMappingRequest): Promise<FieldMappingDTO> => {
    // Note: This endpoint may need to be implemented in the backend
    const response = await apiClient.put(`/mappings/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<void> => {
    // Note: This endpoint may need to be implemented in the backend
    await apiClient.delete(`/mappings/${id}`)
  },

  generateYaml: async (config: MappingConfigDTO): Promise<{ yaml: string }> => {
    const response = await apiClient.post('/mappings/generate-yaml', config)
    return response.data
  },

  validateYaml: async (yaml: string): Promise<{ valid: boolean; message: string }> => {
    const response = await apiClient.post('/mappings/validate-yaml', { yaml })
    return response.data
  },

  parseYaml: async (yaml: string): Promise<MappingConfigDTO> => {
    const response = await apiClient.post('/mappings/parse-yaml', { yaml })
    return response.data
  },

  downloadYaml: async (config: MappingConfigDTO): Promise<Blob> => {
    const response = await apiClient.post('/mappings/download-yaml', config, {
      responseType: 'blob',
    })
    return response.data
  },
}

