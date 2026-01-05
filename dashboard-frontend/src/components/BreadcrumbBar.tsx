import { Breadcrumb } from 'antd'
import { MenuFoldOutlined, MenuUnfoldOutlined, CaretRightOutlined } from '@ant-design/icons'
import { useNavigate, useLocation } from 'react-router-dom'
import './BreadcrumbBar.css'

interface BreadcrumbBarProps {
  collapsed: boolean
  onToggleCollapse: () => void
}

// Map routes to breadcrumb labels
const routeLabels: Record<string, string> = {
  '/': 'Dashboard',
  '/routes': 'Routes',
  '/users': 'Users',
  '/roles': 'Roles & Permissions',
  '/mappings': 'Mappings',
  '/metrics': 'Metrics',
  '/settings': 'Settings',
}

export const BreadcrumbBar = ({ collapsed, onToggleCollapse }: BreadcrumbBarProps) => {
  const navigate = useNavigate()
  const location = useLocation()

  // Generate breadcrumbs from current route
  const generateBreadcrumbs = () => {
    const pathSegments = location.pathname.split('/').filter(Boolean)
    const breadcrumbs: { title: string; path?: string }[] = []

    // Always start with Home
    breadcrumbs.push({ title: 'Home', path: '/' })

    // Build path incrementally
    let currentPath = ''
    pathSegments.forEach((segment) => {
      currentPath += `/${segment}`
      const label = routeLabels[currentPath] || segment.charAt(0).toUpperCase() + segment.slice(1)
      breadcrumbs.push({
        title: label,
        path: currentPath,
      })
    })

    return breadcrumbs
  }

  const breadcrumbs = generateBreadcrumbs()

  return (
    <div className={`breadcrumb-bar ${collapsed ? 'sidebar-collapsed' : ''}`}>
      <div className="breadcrumb-bar-content">
        <div
          className="breadcrumb-collapse-trigger"
          onClick={onToggleCollapse}
          title={collapsed ? 'Expand sidebar' : 'Collapse sidebar'}
        >
          {collapsed ? (
            <MenuUnfoldOutlined className="collapse-icon" />
          ) : (
            <MenuFoldOutlined className="collapse-icon" />
          )}
        </div>
        <Breadcrumb 
          className="breadcrumb-nav"
          separator={<CaretRightOutlined className="breadcrumb-separator-icon" />}
        >
          {breadcrumbs.map((crumb, index) => {
            const isLast = index === breadcrumbs.length - 1
            return (
              <Breadcrumb.Item
                key={index}
                onClick={() => !isLast && crumb.path && navigate(crumb.path)}
                className={isLast ? 'breadcrumb-current' : 'breadcrumb-link'}
              >
                {crumb.title}
              </Breadcrumb.Item>
            )
          })}
        </Breadcrumb>
      </div>
    </div>
  )
}

