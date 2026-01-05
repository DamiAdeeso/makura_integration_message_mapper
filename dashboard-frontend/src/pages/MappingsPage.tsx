import { useState } from 'react'
import {
  Table,
  Button,
  Space,
  Tag,
  Input,
  Select,
  Modal,
  message,
  Tooltip,
  Popconfirm,
  Tabs,
  Dropdown,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  CodeOutlined,
  AppstoreOutlined,
  MoreOutlined,
} from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '../components/PageHeader'
import { PermissionGuard, usePermissions } from '../components/PermissionGuard'
import { mappingsApi, FieldMappingDTO } from '../api/mappingsApi'
import { CreateMappingModal } from '../components/mappings/CreateMappingModal'
import { EditMappingModal } from '../components/mappings/EditMappingModal'
import { VisualMappingBuilder } from '../components/mappings/VisualMappingBuilder'
import './MappingsPage.css'

const { Search } = Input

export const MappingsPage = () => {
  const [searchText, setSearchText] = useState('')
  const [routeFilter, setRouteFilter] = useState<string>('all')
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [builderVisible, setBuilderVisible] = useState(false)
  const [selectedMapping, setSelectedMapping] = useState<FieldMappingDTO | null>(null)
  const [activeTab, setActiveTab] = useState('list')

  const queryClient = useQueryClient()
  const { can } = usePermissions()

  // Fetch mappings
  const { data: mappings = [], isLoading, refetch } = useQuery({
    queryKey: ['mappings'],
    queryFn: mappingsApi.getAll,
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: mappingsApi.delete,
    onSuccess: () => {
      message.success('Mapping deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['mappings'] })
    },
    onError: (error: any) => {
      message.error(error.response?.data || 'Failed to delete mapping')
    },
  })

  // Filter mappings
  const filteredMappings = mappings.filter((mapping) => {
    const sourcePath = mapping.sourcePath || (mapping as any).sourceField || ''
    const targetPath = mapping.targetPath || (mapping as any).targetField || ''
    
    const matchesSearch =
      sourcePath.toLowerCase().includes(searchText.toLowerCase()) ||
      targetPath.toLowerCase().includes(searchText.toLowerCase())

    const routeId = mapping.routeId || ''
    const matchesRoute =
      routeFilter === 'all' || routeId === routeFilter

    return matchesSearch && matchesRoute
  })

  // Get unique route IDs
  const uniqueRoutes = Array.from(new Set(mappings.map((m) => m.routeId).filter(Boolean)))

  const handleEdit = (mapping: FieldMappingDTO) => {
    setSelectedMapping(mapping)
    setEditModalVisible(true)
  }

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id)
  }

  const handleOpenBuilder = (mapping?: FieldMappingDTO) => {
    setSelectedMapping(mapping || null)
    setBuilderVisible(true)
  }

  const columns = [
    {
      title: 'Route ID',
      key: 'routeId',
      width: 180,
      sorter: (a: FieldMappingDTO, b: FieldMappingDTO) => {
        const aRoute = a.routeId || ''
        const bRoute = b.routeId || ''
        return aRoute.localeCompare(bRoute)
      },
      render: (_: any, record: FieldMappingDTO) => {
        const routeId = record.routeId
        return routeId ? <Tag color="blue">{routeId}</Tag> : <Tag>-</Tag>
      },
    },
    {
      title: 'Source Field',
      key: 'sourcePath',
      width: 280,
      render: (_: any, record: FieldMappingDTO) => {
        const sourcePath = record.sourcePath || (record as any).sourceField || ''
        const isConstant = sourcePath.startsWith('constant:')
        return (
          <code style={{ color: isConstant ? '#1890ff' : undefined }}>
            {sourcePath}
            {isConstant && <Tag color="blue" style={{ marginLeft: 8 }}>Constant</Tag>}
          </code>
        )
      },
    },
    {
      title: 'Target Field',
      key: 'targetPath',
      width: 280,
      render: (_: any, record: FieldMappingDTO) => {
        const targetPath = record.targetPath || (record as any).targetField || ''
        return <code>{targetPath}</code>
      },
    },
    {
      title: 'Transformation',
      key: 'transform',
      width: 200,
      render: (_: any, record: FieldMappingDTO) => {
        const transform = record.transform || record.transformation || ''
        if (!transform) {
          return <Tag color="default">None</Tag>
        }
        // Show first 30 chars of transform expression
        const display = transform.length > 30 ? transform.substring(0, 30) + '...' : transform
        return (
          <Tooltip title={transform}>
            <Tag color="purple">{display}</Tag>
          </Tooltip>
        )
      },
    },
    {
      title: 'Default Value',
      key: 'defaultValue',
      width: 120,
      render: (_: any, record: FieldMappingDTO) => {
        const defaultValue = record.defaultValue
        if (!defaultValue) {
          return <Tag color="default">-</Tag>
        }
        return <Tag color="green">{defaultValue}</Tag>
      },
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 80,
      fixed: 'right' as const,
      render: (_: any, record: FieldMappingDTO) => {
        const menuItems = [
          {
            key: 'edit',
            label: 'Edit',
            icon: <EditOutlined />,
            onClick: () => handleEdit(record),
            disabled: !can('mappings:update'),
          },
          {
            key: 'visual',
            label: 'Visual Editor',
            icon: <AppstoreOutlined />,
            onClick: () => handleOpenBuilder(record),
            disabled: !can('mappings:update'),
          },
          {
            type: 'divider' as const,
          },
          {
            key: 'delete',
            label: 'Delete',
            icon: <DeleteOutlined />,
            danger: true,
            onClick: () => {
              Modal.confirm({
                title: 'Delete Mapping',
                content: 'Are you sure you want to delete this mapping?',
                okText: 'Delete',
                okType: 'danger',
                cancelText: 'Cancel',
                onOk: () => handleDelete(record.id),
              })
            },
            disabled: !can('mappings:delete'),
          },
        ]

        return (
          <Dropdown menu={{ items: menuItems }} trigger={['click']}>
            <Button
              type="text"
              icon={<MoreOutlined style={{ fontSize: 20 }} />}
              onClick={(e) => e.stopPropagation()}
            />
          </Dropdown>
        )
      },
    },
  ]

  return (
    <div className="mappings-page">
      <PageHeader
        title="Field Mappings"
        subtitle="Manage field transformation mappings"
        breadcrumbs={[{ title: 'Home', path: '/' }, { title: 'Mappings' }]}
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              Refresh
            </Button>
            <PermissionGuard permission="mappings:create">
              <Button
                icon={<AppstoreOutlined />}
                onClick={() => handleOpenBuilder()}
              >
                Visual Builder
              </Button>
            </PermissionGuard>
            <PermissionGuard permission="mappings:create">
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                Create Mapping
              </Button>
            </PermissionGuard>
          </Space>
        }
      />

      <div className="mappings-content">
        <Tabs
          activeKey={activeTab}
          onChange={setActiveTab}
          items={[
            {
              key: 'list',
              label: (
                <span>
                  <CodeOutlined /> Mappings List
                </span>
              ),
              children: (
                <>
                  <div className="mappings-filters">
                    <Search
                      placeholder="Search by source path, target path, or transformation"
                      allowClear
                      prefix={<SearchOutlined />}
                      style={{ width: 400 }}
                      value={searchText}
                      onChange={(e) => setSearchText(e.target.value)}
                    />
                    <Select
                      placeholder="Route"
                      style={{ width: 200 }}
                      value={routeFilter}
                      onChange={setRouteFilter}
                    >
                      <Select.Option value="all">All Routes</Select.Option>
                      {uniqueRoutes.map((route) => (
                        <Select.Option key={route} value={route}>
                          {route}
                        </Select.Option>
                      ))}
                    </Select>
                  </div>

                  <Table
                    columns={columns}
                    dataSource={filteredMappings}
                    rowKey="id"
                    loading={isLoading}
                    pagination={{
                      total: filteredMappings.length,
                      pageSize: 10,
                      showSizeChanger: true,
                      showTotal: (total) => `Total ${total} mappings`,
                    }}
                    scroll={{ x: 1200 }}
                  />
                </>
              ),
            },
            {
              key: 'builder',
              label: (
                <span>
                  <AppstoreOutlined /> Visual Builder
                </span>
              ),
              children: <VisualMappingBuilder />,
            },
          ]}
        />
      </div>

      {createModalVisible && (
        <CreateMappingModal
          visible={createModalVisible}
          onClose={() => setCreateModalVisible(false)}
          onSuccess={() => {
            setCreateModalVisible(false)
            queryClient.invalidateQueries({ queryKey: ['mappings'] })
          }}
        />
      )}

      {editModalVisible && selectedMapping && (
        <EditMappingModal
          visible={editModalVisible}
          mapping={selectedMapping}
          onClose={() => {
            setEditModalVisible(false)
            setSelectedMapping(null)
          }}
          onSuccess={() => {
            setEditModalVisible(false)
            setSelectedMapping(null)
            queryClient.invalidateQueries({ queryKey: ['mappings'] })
          }}
        />
      )}

      {builderVisible && (
        <Modal
          title="Visual Mapping Builder"
          open={builderVisible}
          onCancel={() => {
            setBuilderVisible(false)
            setSelectedMapping(null)
          }}
          footer={null}
          width={1200}
          style={{ top: 20 }}
        >
          <VisualMappingBuilder mapping={selectedMapping} />
        </Modal>
      )}
    </div>
  )
}

