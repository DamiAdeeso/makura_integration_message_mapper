import { ReactNode } from 'react'
import { useAuthStore } from '../store/authStore'

interface PermissionGuardProps {
  permission?: string
  permissions?: string[]
  requireAll?: boolean
  role?: string
  fallback?: ReactNode
  children: ReactNode
}

/**
 * Permission-based component guard
 * Shows children only if user has required permissions/roles
 */
export const PermissionGuard = ({
  permission,
  permissions,
  requireAll = false,
  role,
  fallback = null,
  children,
}: PermissionGuardProps) => {
  const { hasPermission, hasAnyPermission, hasAllPermissions, hasRole } = useAuthStore()

  // Check single permission
  if (permission && !hasPermission(permission)) {
    return <>{fallback}</>
  }

  // Check multiple permissions
  if (permissions) {
    const hasPerms = requireAll
      ? hasAllPermissions(permissions)
      : hasAnyPermission(permissions)
    
    if (!hasPerms) {
      return <>{fallback}</>
    }
  }

  // Check role
  if (role && !hasRole(role)) {
    return <>{fallback}</>
  }

  return <>{children}</>
}

// Hook for permission checks
export const usePermissions = () => {
  const { hasPermission, hasAnyPermission, hasAllPermissions, hasRole } = useAuthStore()
  
  return {
    can: hasPermission,
    canAny: hasAnyPermission,
    canAll: hasAllPermissions,
    hasRole,
  }
}



