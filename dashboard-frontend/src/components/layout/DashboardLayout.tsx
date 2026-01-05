import { ReactNode, useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Button, Avatar, Dropdown, Typography } from 'antd'
import type { MenuProps } from 'antd'
import {
  DashboardOutlined,
  ApiOutlined,
  BarChartOutlined,
  UserOutlined,
  FileTextOutlined,
  LogoutOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../../store/authStore'
import './DashboardLayout.css'

const { Header, Sider, Content } = Layout
const { Text: TypographyText } = Typography

interface DashboardLayoutProps {
  children: ReactNode
}

export default function DashboardLayout({ children }: DashboardLayoutProps) {
  const [collapsed, setCollapsed] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()

  const menuItems: MenuProps['items'] = [
    {
      key: '/routes',
      icon: <ApiOutlined />,
      label: 'Routes',
    },
    {
      key: '/metrics',
      icon: <BarChartOutlined />,
      label: 'Metrics',
    },
    ...(user?.role === 'ADMIN'
      ? [
          {
            key: '/users',
            icon: <UserOutlined />,
            label: 'Users',
          },
          {
            key: '/audit-logs',
            icon: <FileTextOutlined />,
            label: 'Audit Logs',
          },
        ]
      : []),
  ]

  const userMenuItems: MenuProps['items'] = [
    {
      key: 'profile',
      label: 'Profile',
      icon: <UserOutlined />,
    },
    {
      type: 'divider',
    },
    {
      key: 'logout',
      label: 'Logout',
      icon: <LogoutOutlined />,
      danger: true,
      onClick: () => {
        logout()
        navigate('/login')
      },
    },
  ]

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Sider
        collapsible
        collapsed={collapsed}
        onCollapse={setCollapsed}
        trigger={null}
        width={250}
      >
        <div className="logo">
          {!collapsed ? (
            <>
              <DashboardOutlined style={{ fontSize: 24 }} />
              <span>Makura</span>
            </>
          ) : (
            <DashboardOutlined style={{ fontSize: 24 }} />
          )}
        </div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={[location.pathname]}
          items={menuItems}
          onClick={({ key }) => navigate(key)}
        />
      </Sider>
      <Layout>
        <Header className="dashboard-header">
          <Button
            type="text"
            icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
            onClick={() => setCollapsed(!collapsed)}
            style={{ fontSize: '16px', color: 'white' }}
          />
          <div className="header-right">
            <TypographyText style={{ color: 'white', marginRight: 16 }}>
              ISO 20022 Dashboard
            </TypographyText>
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <div className="user-avatar">
                <Avatar icon={<UserOutlined />} />
                <TypographyText style={{ color: 'white', marginLeft: 8 }}>
                  {user?.username}
                </TypographyText>
              </div>
            </Dropdown>
          </div>
        </Header>
        <Content className="dashboard-content">{children}</Content>
      </Layout>
    </Layout>
  )
}


