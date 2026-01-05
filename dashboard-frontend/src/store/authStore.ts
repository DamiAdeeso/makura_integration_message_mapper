import { create } from 'zustand'
import { persist } from 'zustand/middleware'

export interface User {
  id: number
  username: string
  email: string
  fullName?: string
  roles: string[]
  permissions: string[]
  active: boolean
  lastLogin?: string
  createdAt?: string
  updatedAt?: string
}

interface AuthState {
  user: User | null
  token: string | null
  isAuthenticated: boolean
  isLoading: boolean
  login: (user: User, token: string) => void
  logout: () => void
  setAuth: (token: string, user: User) => void
  checkAuth: () => void
  hasPermission: (permission: string) => boolean
  hasAnyPermission: (permissions: string[]) => boolean
  hasAllPermissions: (permissions: string[]) => boolean
  hasRole: (role: string) => boolean
}

export const useAuthStore = create<AuthState>()(
  persist(
    (set, get) => ({
      user: null,
      token: null,
      isAuthenticated: false,
      isLoading: true,
      login: (user, token) => {
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        set({
          user,
          token,
          isAuthenticated: true,
          isLoading: false,
        })
      },
      logout: () => {
        localStorage.removeItem('token')
        localStorage.removeItem('user')
        set({
          user: null,
          token: null,
          isAuthenticated: false,
          isLoading: false,
        })
      },
      setAuth: (token, user) => {
        localStorage.setItem('token', token)
        localStorage.setItem('user', JSON.stringify(user))
        set({
          token,
          user,
          isAuthenticated: true,
          isLoading: false,
        })
      },
      checkAuth: () => {
        const token = localStorage.getItem('token')
        const userStr = localStorage.getItem('user')
        
        if (token && userStr) {
          try {
            const user = JSON.parse(userStr)
            set({
              token,
              user,
              isAuthenticated: true,
              isLoading: false,
            })
          } catch (error) {
            console.error('Failed to parse user from localStorage', error)
            localStorage.removeItem('token')
            localStorage.removeItem('user')
            set({
              user: null,
              token: null,
              isAuthenticated: false,
              isLoading: false,
            })
          }
        } else {
          set({
            isLoading: false,
          })
        }
      },
      hasPermission: (permission: string) => {
        const { user } = get()
        return user?.permissions?.includes(permission) ?? false
      },
      hasAnyPermission: (permissions: string[]) => {
        const { user } = get()
        return permissions.some(p => user?.permissions?.includes(p)) ?? false
      },
      hasAllPermissions: (permissions: string[]) => {
        const { user } = get()
        return permissions.every(p => user?.permissions?.includes(p)) ?? false
      },
      hasRole: (role: string) => {
        const { user } = get()
        return user?.roles?.includes(role) ?? false
      },
    }),
    {
      name: 'makura-auth-storage',
    }
  )
)


