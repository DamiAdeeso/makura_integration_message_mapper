import { useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Spin } from 'antd'
import { useAuthStore } from '../store/authStore'

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const navigate = useNavigate()
  const location = useLocation()
  const { isAuthenticated, isLoading, checkAuth } = useAuthStore()

  useEffect(() => {
    checkAuth()
  }, [checkAuth])

  useEffect(() => {
    if (!isLoading) {
      if (!isAuthenticated && location.pathname !== '/login') {
        navigate('/login', { replace: true })
      } else if (isAuthenticated && location.pathname === '/login') {
        navigate('/', { replace: true })
      }
    }
  }, [isAuthenticated, isLoading, navigate, location])

  if (isLoading) {
    return (
      <div
        style={{
          display: 'flex',
          justifyContent: 'center',
          alignItems: 'center',
          height: '100vh',
        }}
      >
        <Spin size="large" tip="Loading..." />
      </div>
    )
  }

  return <>{children}</>
}



