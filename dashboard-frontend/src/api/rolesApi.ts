import apiClient from './apiClient'

export interface RoleDTO {
  id: number
  name: string
  description: string
  permissions: string[]
  systemRole: boolean
  userCount?: number
}

export interface CreateRoleRequest {
  name: string
  description?: string
  permissions: string[]
}

export interface UpdateRoleRequest {
  description?: string
  permissions: string[]
}

export const rolesApi = {
  // Get all roles
  getAll: async (): Promise<RoleDTO[]> => {
    const response = await apiClient.get('/roles')
    return response.data
  },

  // Get role by ID
  getById: async (id: number): Promise<RoleDTO> => {
    const response = await apiClient.get(`/roles/${id}`)
    return response.data
  },

  // Create new role
  create: async (data: CreateRoleRequest): Promise<RoleDTO> => {
    const response = await apiClient.post('/roles', data)
    return response.data
  },

  // Update role
  update: async (id: number, data: UpdateRoleRequest): Promise<RoleDTO> => {
    const response = await apiClient.put(`/roles/${id}`, data)
    return response.data
  },

  // Delete role
  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/roles/${id}`)
  },
}



