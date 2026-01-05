import { useState, useEffect } from 'react'
import { Outlet, useNavigate, useLocation } from 'react-router-dom'
import { Layout, Menu, Avatar, Dropdown, Space, Typography, Switch, Button } from 'antd'
import {
  DashboardOutlined,
  ApiOutlined,
  UserOutlined,
  SafetyOutlined,
  NodeIndexOutlined,
  BarChartOutlined,
  LogoutOutlined,
  SettingOutlined,
  BulbOutlined,
  MoonOutlined,
  SearchOutlined,
  CodeOutlined,
} from '@ant-design/icons'
import { useAuthStore } from '../store/authStore'
import { useTheme } from '../contexts/ThemeContext'
import { CommandPalette } from './CommandPalette'
import { BreadcrumbBar } from './BreadcrumbBar'
import './MainLayout.css'

const { Header, Sider, Content } = Layout
const { Text: TypographyText } = Typography

export const MainLayout = () => {
  const [collapsed, setCollapsed] = useState(false)
  const [commandPaletteVisible, setCommandPaletteVisible] = useState(false)
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuthStore()
  const { theme, toggleTheme } = useTheme()

  // Global keyboard shortcut handler
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Cmd+K or Ctrl+K to open command palette
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault()
        setCommandPaletteVisible(true)
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  const menuItems = [
    {
      key: '/',
      icon: <DashboardOutlined />,
      label: 'Dashboard',
    },
    {
      key: '/routes',
      icon: <ApiOutlined />,
      label: 'Routes',
    },
    {
      key: '/mappings',
      icon: <NodeIndexOutlined />,
      label: 'Mappings',
    },
    {
      key: '/metrics',
      icon: <BarChartOutlined />,
      label: 'Metrics',
    },
    {
      key: '/developers',
      icon: <CodeOutlined />,
      label: 'Developers',
    },
    {
      key: '/users',
      icon: <UserOutlined />,
      label: 'Users',
    },
    {
      key: '/settings',
      icon: <SettingOutlined />,
      label: 'Settings',
    },
  ]

  const userMenuItems = [
    {
      key: 'profile',
      label: (
        <div>
          <strong>{user?.username}</strong>
          <br />
                <TypographyText type="secondary" style={{ fontSize: 12 }}>
                  {user?.email}
                </TypographyText>
        </div>
      ),
      disabled: true,
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'theme',
      label: (
        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Space>
            {theme === 'dark' ? <MoonOutlined /> : <BulbOutlined />}
            <span>Dark Mode</span>
          </Space>
          <Switch
            checked={theme === 'dark'}
            onChange={toggleTheme}
            size="small"
          />
        </div>
      ),
      onClick: (e: any) => {
        e.domEvent?.stopPropagation()
        // toggleTheme is already handled by Switch onChange
      },
    },
    {
      key: 'settings',
      icon: <SettingOutlined />,
      label: 'Settings & Preferences',
      onClick: () => navigate('/settings'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'roles',
      icon: <SafetyOutlined />,
      label: 'Roles & Permissions',
      onClick: () => navigate('/roles'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: 'Logout',
      onClick: () => {
        logout()
        navigate('/login')
      },
    },
  ]

  return (
    <Layout className="main-layout">
      <Sider
        trigger={null}
        collapsible
        collapsed={collapsed}
        breakpoint="lg"
        onBreakpoint={(broken) => {
          if (broken) setCollapsed(true)
        }}
        className="main-sider"
      >
        <div className="logo">
          <img src="/logo.png" alt="Makura" className="logo-image" />
        </div>
        <div className="sidebar-content">
          <div className="sidebar-menu-wrapper">
            <Menu
              theme={theme === 'dark' ? 'dark' : 'light'}
              mode="inline"
              selectedKeys={[location.pathname]}
              items={menuItems}
              onClick={({ key }) => navigate(key)}
              style={{
                background: theme === 'dark' ? '#001529' : '#ffffff',
                borderRight: 'none',
                height: '100%',
              }}
            />
          </div>
        </div>
        <div className="sidebar-footer">
          <TypographyText className="copyright-text">
            © 2025 Makura Systems
          </TypographyText>
        </div>
      </Sider>
      <Layout>
        <Header className="main-header">
          <Space className="header-left">
            <Button
              type="text"
              icon={<SearchOutlined />}
              className="command-palette-trigger"
              onClick={() => setCommandPaletteVisible(true)}
            >
              <span className="search-text">Search...</span>
              <span className="search-shortcut">
                {navigator.platform.toUpperCase().indexOf('MAC') >= 0 ? '⌘' : 'Ctrl'}K
              </span>
            </Button>
          </Space>
          <Space className="header-right">
            <Dropdown menu={{ items: userMenuItems }} placement="bottomRight">
              <Space className="user-info">
                <Avatar icon={<UserOutlined />} style={{ background: 'linear-gradient(135deg, #0B4F6C 0%, #1F7FBF 100%)' }}>
                  {user?.username.charAt(0).toUpperCase()}
                </Avatar>
                <TypographyText>{user?.username}</TypographyText>
              </Space>
            </Dropdown>
          </Space>
          <CommandPalette
            visible={commandPaletteVisible}
            onClose={() => setCommandPaletteVisible(false)}
          />
        </Header>
        <BreadcrumbBar
          collapsed={collapsed}
          onToggleCollapse={() => setCollapsed(!collapsed)}
        />
        <Content className="main-content">
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}

