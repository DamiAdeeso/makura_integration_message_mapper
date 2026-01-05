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
  Dropdown,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  CheckCircleOutlined,
  StopOutlined,
  MoreOutlined,
} from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '../components/PageHeader'
import { PermissionGuard, usePermissions } from '../components/PermissionGuard'
import { routesApi, RouteDTO } from '../api/routesApi'
import { CreateRouteModal } from '../components/routes/CreateRouteModal'
import { EditRouteModal } from '../components/routes/EditRouteModal'
import './RoutesPage.css'

const { Search } = Input

export const RoutesPage = () => {
  const [searchText, setSearchText] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [modeFilter, setModeFilter] = useState<string>('all')
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [selectedRoute, setSelectedRoute] = useState<RouteDTO | null>(null)

  const queryClient = useQueryClient()
  const { can } = usePermissions()

  // Fetch routes
  const { data: routes = [], isLoading, refetch } = useQuery({
    queryKey: ['routes'],
    queryFn: () => routesApi.getAll(),
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: routesApi.delete,
    onSuccess: () => {
      message.success('Route deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['routes'] })
    },
    onError: () => {
      message.error('Failed to delete route')
    },
  })

  // Toggle status mutation
  const toggleMutation = useMutation({
    mutationFn: routesApi.toggle,
    onSuccess: () => {
      message.success('Route status updated')
      queryClient.invalidateQueries({ queryKey: ['routes'] })
    },
    onError: () => {
      message.error('Failed to update route status')
    },
  })

  // Filter routes
  const filteredRoutes = routes.filter((route) => {
    const matchesSearch =
      route.routeId.toLowerCase().includes(searchText.toLowerCase()) ||
      route.name.toLowerCase().includes(searchText.toLowerCase())
    
    const matchesStatus =
      statusFilter === 'all' ||
      (statusFilter === 'active' && route.active) ||
      (statusFilter === 'inactive' && !route.active)
    
    const matchesMode =
      modeFilter === 'all' || route.mode === modeFilter

    return matchesSearch && matchesStatus && matchesMode
  })

  const handleEdit = (route: RouteDTO) => {
    setSelectedRoute(route)
    setEditModalVisible(true)
  }

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id)
  }

  const handleToggle = (id: number) => {
    toggleMutation.mutate(id)
  }

  const columns = [
    {
      title: 'Route ID',
      dataIndex: 'routeId',
      key: 'routeId',
      width: 180,
      sorter: (a: RouteDTO, b: RouteDTO) => a.routeId.localeCompare(b.routeId),
      render: (text: string) => <strong>{text}</strong>,
    },
    {
      title: 'Name',
      dataIndex: 'name',
      key: 'name',
      sorter: (a: RouteDTO, b: RouteDTO) => a.name.localeCompare(b.name),
    },
    {
      title: 'Mode',
      dataIndex: 'mode',
      key: 'mode',
      width: 100,
      render: (mode: string) => (
        <Tag color={mode === 'ACTIVE' ? 'green' : 'blue'}>{mode}</Tag>
      ),
    },
    {
      title: 'Format',
      key: 'format',
      width: 150,
      render: (_: any, record: RouteDTO) => (
        <span>
          {record.inboundFormat} → {record.outboundFormat}
        </span>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      width: 100,
      render: (active: boolean) => (
        <Tag color={active ? 'success' : 'default'} icon={active ? <CheckCircleOutlined /> : <StopOutlined />}>
          {active ? 'Active' : 'Inactive'}
        </Tag>
      ),
    },
    {
      title: 'Published',
      dataIndex: 'published',
      key: 'published',
      width: 100,
      render: (published: boolean) => (
        <Tag color={published ? 'cyan' : 'default'}>
          {published ? 'Yes' : 'No'}
        </Tag>
      ),
    },
    {
      title: 'Updated',
      dataIndex: 'updatedAt',
      key: 'updatedAt',
      width: 180,
      sorter: (a: RouteDTO, b: RouteDTO) =>
        new Date(a.updatedAt).getTime() - new Date(b.updatedAt).getTime(),
      render: (date: string) => new Date(date).toLocaleString(),
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 80,
      fixed: 'right' as const,
      render: (_: any, record: RouteDTO) => {
        const menuItems = [
          {
            key: 'edit',
            label: 'Edit',
            icon: <EditOutlined />,
            onClick: () => handleEdit(record),
            disabled: !can('routes:update'),
          },
          {
            key: 'toggle',
            label: record.active ? 'Deactivate' : 'Activate',
            icon: record.active ? <StopOutlined /> : <CheckCircleOutlined />,
            onClick: () => handleToggle(record.id),
            disabled: !can('routes:toggle'),
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
                title: 'Delete Route',
                content: 'Are you sure you want to delete this route?',
                okText: 'Delete',
                okType: 'danger',
                cancelText: 'Cancel',
                onOk: () => handleDelete(record.id),
              })
            },
            disabled: !can('routes:delete'),
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
    <div className="routes-page">
      <PageHeader
        title="Routes"
        subtitle="Manage ISO 20022 translation routes"
        breadcrumbs={[{ title: 'Home', path: '/' }, { title: 'Routes' }]}
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              Refresh
            </Button>
            <PermissionGuard permission="routes:create">
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                Create Route
              </Button>
            </PermissionGuard>
          </Space>
        }
      />

      <div className="routes-content">
        <div className="routes-filters">
          <Search
            placeholder="Search by Route ID or Name"
            allowClear
            prefix={<SearchOutlined />}
            style={{ width: 300 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
          <Select
            placeholder="Status"
            style={{ width: 150 }}
            value={statusFilter}
            onChange={setStatusFilter}
            options={[
              { label: 'All Status', value: 'all' },
              { label: 'Active', value: 'active' },
              { label: 'Inactive', value: 'inactive' },
            ]}
          />
          <Select
            placeholder="Mode"
            style={{ width: 150 }}
            value={modeFilter}
            onChange={setModeFilter}
            options={[
              { label: 'All Modes', value: 'all' },
              { label: 'Active', value: 'ACTIVE' },
              { label: 'Passive', value: 'PASSIVE' },
            ]}
          />
        </div>

        <Table
          columns={columns}
          dataSource={filteredRoutes}
          rowKey="id"
          loading={isLoading}
          pagination={{
            total: filteredRoutes.length,
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} routes`,
          }}
          scroll={{ x: 1200 }}
        />
      </div>

      {createModalVisible && (
        <CreateRouteModal
          visible={createModalVisible}
          onClose={() => setCreateModalVisible(false)}
          onSuccess={() => {
            setCreateModalVisible(false)
            queryClient.invalidateQueries({ queryKey: ['routes'] })
          }}
        />
      )}

      {editModalVisible && selectedRoute && (
        <EditRouteModal
          visible={editModalVisible}
          route={selectedRoute}
          onClose={() => {
            setEditModalVisible(false)
            setSelectedRoute(null)
          }}
          onSuccess={() => {
            setEditModalVisible(false)
            setSelectedRoute(null)
            queryClient.invalidateQueries({ queryKey: ['routes'] })
          }}
        />
      )}
    </div>
  )
}
