import apiClient from './apiClient'

export interface UserDTO {
  id: number
  username: string
  email: string
  fullName?: string
  roles: string[]
  permissions: string[]
  active: boolean
  lastLogin?: string
  createdAt: string
  updatedAt: string
}

export interface CreateUserRequest {
  username: string
  password: string
  email: string
  fullName: string
  roleIds?: number[]
  active?: boolean
}

export interface UpdateUserRequest {
  email?: string
  fullName?: string
  roleIds?: number[]
  active?: boolean
}

export interface ChangePasswordRequest {
  currentPassword: string
  newPassword: string
}

export const usersApi = {
  getAll: async (): Promise<UserDTO[]> => {
    const response = await apiClient.get('/users')
    return response.data
  },

  getById: async (id: number): Promise<UserDTO> => {
    const response = await apiClient.get(`/users/${id}`)
    return response.data
  },

  getByUsername: async (username: string): Promise<UserDTO> => {
    const response = await apiClient.get(`/users/username/${username}`)
    return response.data
  },

  create: async (data: CreateUserRequest): Promise<UserDTO> => {
    const response = await apiClient.post('/users', data)
    return response.data
  },

  update: async (id: number, data: UpdateUserRequest): Promise<UserDTO> => {
    const response = await apiClient.put(`/users/${id}`, data)
    return response.data
  },

  delete: async (id: number): Promise<void> => {
    await apiClient.delete(`/users/${id}`)
  },

  toggle: async (id: number): Promise<UserDTO> => {
    const response = await apiClient.post(`/users/${id}/toggle`)
    return response.data
  },

  changePassword: async (id: number, data: ChangePasswordRequest): Promise<void> => {
    await apiClient.post(`/users/${id}/change-password`, data)
  },
}

