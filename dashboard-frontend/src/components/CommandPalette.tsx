import { useState, useEffect, useMemo } from 'react'
import { Modal, Input, List, Typography, Space, Tag, Divider } from 'antd'
import {
  DashboardOutlined,
  ApiOutlined,
  UserOutlined,
  SafetyOutlined,
  NodeIndexOutlined,
  BarChartOutlined,
  SettingOutlined,
  SearchOutlined,
  ThunderboltOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useTheme } from '../contexts/ThemeContext'
import { commandRegistry } from '../utils/commandRegistry'
import './CommandPalette.css'

const { Text: TypographyText } = Typography

export interface CommandAction {
  id: string
  title: string
  description?: string
  icon?: React.ReactNode
  category: string
  keywords?: string[]
  action: () => void
  shortcut?: string
  badge?: string
}

interface CommandPaletteProps {
  visible: boolean
  onClose: () => void
}

export const CommandPalette = ({ visible, onClose }: CommandPaletteProps) => {
  const [searchQuery, setSearchQuery] = useState('')
  const navigate = useNavigate()
  const { theme } = useTheme()

  // Core navigation actions
  const coreActions: CommandAction[] = [
    {
      id: 'dashboard',
      title: 'Go to Dashboard',
      description: 'View the main dashboard',
      icon: <DashboardOutlined />,
      category: 'Navigation',
      keywords: ['home', 'main', 'overview'],
      action: () => {
        navigate('/')
        onClose()
      },
      shortcut: 'G D',
    },
    {
      id: 'routes',
      title: 'Manage Routes',
      description: 'Configure ISO 20022 translation routes',
      icon: <ApiOutlined />,
      category: 'Navigation',
      keywords: ['route', 'translation', 'api'],
      action: () => {
        navigate('/routes')
        onClose()
      },
      shortcut: 'G R',
    },
    {
      id: 'mappings',
      title: 'Field Mappings',
      description: 'Configure field mapping rules',
      icon: <NodeIndexOutlined />,
      category: 'Navigation',
      keywords: ['mapping', 'field', 'transform'],
      action: () => {
        navigate('/mappings')
        onClose()
      },
      shortcut: 'G M',
    },
    {
      id: 'metrics',
      title: 'View Metrics',
      description: 'Monitor system performance and analytics',
      icon: <BarChartOutlined />,
      category: 'Navigation',
      keywords: ['metrics', 'analytics', 'performance', 'monitor'],
      action: () => {
        navigate('/metrics')
        onClose()
      },
      shortcut: 'G T',
    },
    {
      id: 'users',
      title: 'Manage Users',
      description: 'View and manage user accounts',
      icon: <UserOutlined />,
      category: 'Navigation',
      keywords: ['user', 'account', 'people'],
      action: () => {
        navigate('/users')
        onClose()
      },
      shortcut: 'G U',
    },
    {
      id: 'roles',
      title: 'Roles & Permissions',
      description: 'Configure roles and permissions',
      icon: <SafetyOutlined />,
      category: 'Navigation',
      keywords: ['role', 'permission', 'access', 'security'],
      action: () => {
        navigate('/roles')
        onClose()
      },
      shortcut: 'G P',
    },
    {
      id: 'settings',
      title: 'Settings',
      description: 'Application settings and preferences',
      icon: <SettingOutlined />,
      category: 'Navigation',
      keywords: ['settings', 'preferences', 'config'],
      action: () => {
        navigate('/settings')
        onClose()
      },
      shortcut: 'G S',
    },
  ]

  // Quick actions
  const quickActions: CommandAction[] = [
    {
      id: 'create-route',
      title: 'Create New Route',
      description: 'Add a new translation route',
      icon: <ThunderboltOutlined />,
      category: 'Actions',
      keywords: ['new', 'add', 'create', 'route'],
      action: () => {
        navigate('/routes')
        onClose()
        // Trigger create modal (would need to be handled by parent)
        setTimeout(() => {
          const event = new CustomEvent('command:create-route')
          window.dispatchEvent(event)
        }, 100)
      },
      shortcut: 'C R',
    },
    {
      id: 'create-user',
      title: 'Create New User',
      description: 'Add a new user account',
      icon: <ThunderboltOutlined />,
      category: 'Actions',
      keywords: ['new', 'add', 'create', 'user'],
      action: () => {
        navigate('/users')
        onClose()
        setTimeout(() => {
          const event = new CustomEvent('command:create-user')
          window.dispatchEvent(event)
        }, 100)
      },
      shortcut: 'C U',
    },
    {
      id: 'create-mapping',
      title: 'Create New Mapping',
      description: 'Add a new field mapping',
      icon: <ThunderboltOutlined />,
      category: 'Actions',
      keywords: ['new', 'add', 'create', 'mapping'],
      action: () => {
        navigate('/mappings')
        onClose()
        setTimeout(() => {
          const event = new CustomEvent('command:create-mapping')
          window.dispatchEvent(event)
        }, 100)
      },
      shortcut: 'C M',
    },
  ]

  // Get registered commands from registry
  const registeredCommands = useMemo(() => commandRegistry.getAll(), [])

  // Combine all actions (core + quick + registered)
  const allActions = useMemo(
    () => [...coreActions, ...quickActions, ...registeredCommands],
    [registeredCommands]
  )

  // Filter actions based on search query
  const filteredActions = useMemo(() => {
    if (!searchQuery.trim()) {
      return allActions
    }

    const query = searchQuery.toLowerCase()
    return allActions.filter((action) => {
      const titleMatch = action.title.toLowerCase().includes(query)
      const descMatch = action.description?.toLowerCase().includes(query)
      const keywordMatch = action.keywords?.some((k) => k.toLowerCase().includes(query))
      const categoryMatch = action.category.toLowerCase().includes(query)

      return titleMatch || descMatch || keywordMatch || categoryMatch
    })
  }, [searchQuery, allActions])

  // Group by category
  const groupedActions = useMemo(() => {
    const groups: Record<string, CommandAction[]> = {}
    filteredActions.forEach((action) => {
      if (!groups[action.category]) {
        groups[action.category] = []
      }
      groups[action.category].push(action)
    })
    return groups
  }, [filteredActions])

  // Handle keyboard shortcuts
  useEffect(() => {
    const handleKeyDown = (e: KeyboardEvent) => {
      // Cmd+K or Ctrl+K to open
      if ((e.metaKey || e.ctrlKey) && e.key === 'k') {
        e.preventDefault()
        // This will be handled by parent component
      }

      // Escape to close
      if (visible && e.key === 'Escape') {
        onClose()
      }

      // Arrow keys for navigation (if needed)
      if (visible && (e.key === 'ArrowDown' || e.key === 'ArrowUp')) {
        e.preventDefault()
        // Could implement keyboard navigation here
      }

      // Enter to select
      if (visible && e.key === 'Enter' && filteredActions.length > 0) {
        e.preventDefault()
        filteredActions[0].action()
      }
    }

    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [visible, filteredActions, onClose])

  // Focus input when modal opens
  useEffect(() => {
    if (visible) {
      setTimeout(() => {
        const input = document.querySelector('.command-palette-input input') as HTMLInputElement
        input?.focus()
      }, 100)
      setSearchQuery('')
    }
  }, [visible])

  const isMac = navigator.platform.toUpperCase().indexOf('MAC') >= 0
  const modifierKey = isMac ? '⌘' : 'Ctrl'

  return (
    <Modal
      open={visible}
      onCancel={onClose}
      footer={null}
      closable={false}
      width={600}
      className="command-palette-modal"
      styles={{
        body: { padding: 0 },
      }}
    >
      <div className="command-palette">
        <div className="command-palette-header">
          <SearchOutlined className="search-icon" />
          <Input
            className="command-palette-input"
            placeholder="Search commands, actions, and pages..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            bordered={false}
            autoFocus
          />
          <div className="shortcut-hint">
            <kbd>{modifierKey}</kbd>
            <kbd>K</kbd>
          </div>
        </div>

        <Divider style={{ margin: 0 }} />

        <div className="command-palette-body">
          {filteredActions.length === 0 ? (
            <div className="no-results">
              <TypographyText type="secondary">No results found for "{searchQuery}"</TypographyText>
            </div>
          ) : (
            Object.entries(groupedActions).map(([category, actions]) => (
              <div key={category} className="command-group">
                <div className="command-group-header">
                  <TypographyText type="secondary" strong>
                    {category}
                  </TypographyText>
                </div>
                <List
                  dataSource={actions}
                  renderItem={(action) => (
                    <List.Item
                      className="command-item"
                      onClick={action.action}
                      style={{ cursor: 'pointer' }}
                    >
                      <List.Item.Meta
                        avatar={
                          <div className="command-icon" style={{ color: 'var(--brand-medium-blue)' }}>
                            {action.icon}
                          </div>
                        }
                        title={
                          <Space>
                            <TypographyText strong>{action.title}</TypographyText>
                            {action.badge && (
                              <Tag color="blue" size="small">
                                {action.badge}
                              </Tag>
                            )}
                          </Space>
                        }
                        description={
                          <Space direction="vertical" size={0}>
                            {action.description && (
                              <TypographyText type="secondary" style={{ fontSize: 12 }}>
                                {action.description}
                              </TypographyText>
                            )}
                            {action.shortcut && (
                              <div className="command-shortcut">
                                {action.shortcut.split(' ').map((key, i) => (
                                  <kbd key={i}>{key}</kbd>
                                ))}
                              </div>
                            )}
                          </Space>
                        }
                      />
                    </List.Item>
                  )}
                />
              </div>
            ))
          )}
        </div>

        <Divider style={{ margin: 0 }} />

        <div className="command-palette-footer">
          <Space split={<Divider type="vertical" />}>
            <TypographyText type="secondary" style={{ fontSize: 11 }}>
              <kbd>↑</kbd> <kbd>↓</kbd> Navigate
            </TypographyText>
            <TypographyText type="secondary" style={{ fontSize: 11 }}>
              <kbd>Enter</kbd> Select
            </TypographyText>
            <TypographyText type="secondary" style={{ fontSize: 11 }}>
              <kbd>Esc</kbd> Close
            </TypographyText>
          </Space>
        </div>
      </div>
    </Modal>
  )
}

