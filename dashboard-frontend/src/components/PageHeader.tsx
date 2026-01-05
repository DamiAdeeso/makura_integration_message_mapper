import { PageHeader as AntPageHeader, Button, Breadcrumb } from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { ReactNode } from 'react'
import './PageHeader.css'

interface PageHeaderProps {
  title: string
  subtitle?: string
  extra?: ReactNode
  onBack?: () => void
  breadcrumbs?: { title: string; path?: string }[]
}

export const PageHeader = ({
  title,
  subtitle,
  extra,
  onBack,
  breadcrumbs,
}: PageHeaderProps) => {
  const navigate = useNavigate()

  const handleBack = () => {
    if (onBack) {
      onBack()
    } else {
      navigate(-1)
    }
  }

  return (
    <div className="page-header-wrapper">
      {/* Breadcrumbs are now in BreadcrumbBar component */}
      <div className="page-header-content">
        <div className="page-header-main">
          {onBack && (
            <Button
              type="text"
              icon={<ArrowLeftOutlined />}
              onClick={handleBack}
              className="page-header-back"
            />
          )}
          <div className="page-header-title-wrapper">
            <h1 className="page-header-title">{title}</h1>
            {subtitle && <p className="page-header-subtitle">{subtitle}</p>}
          </div>
        </div>
        {extra && <div className="page-header-extra">{extra}</div>}
      </div>
    </div>
  )
}

