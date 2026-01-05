import apiClient from './apiClient'
import { User } from '../store/authStore'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  user: User
}

export interface PermissionsResponse {
  username: string
  roles: string[]
  permissions: string[]
}

export const authApi = {
  login: async (credentials: LoginRequest): Promise<LoginResponse> => {
    const response = await apiClient.post('/auth/login', credentials)
    return response.data
  },

  logout: async (): Promise<void> => {
    // Just clear local storage, no backend call needed
  },

  getCurrentUser: async (): Promise<User> => {
    const response = await apiClient.get('/auth/me')
    return response.data
  },

  getPermissions: async (): Promise<PermissionsResponse> => {
    const response = await apiClient.get('/auth/permissions')
    return response.data
  },
}


