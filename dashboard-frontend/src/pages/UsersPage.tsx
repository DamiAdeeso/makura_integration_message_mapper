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
  Avatar,
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
  UserOutlined,
  LockOutlined,
  MoreOutlined,
} from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '../components/PageHeader'
import { PermissionGuard, usePermissions } from '../components/PermissionGuard'
import { usersApi, UserDTO } from '../api/usersApi'
import { CreateUserModal } from '../components/users/CreateUserModal'
import { EditUserModal } from '../components/users/EditUserModal'
import './UsersPage.css'

const { Search } = Input

export const UsersPage = () => {
  const [searchText, setSearchText] = useState('')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [roleFilter, setRoleFilter] = useState<string>('all')
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [selectedUser, setSelectedUser] = useState<UserDTO | null>(null)

  const queryClient = useQueryClient()
  const { can } = usePermissions()

  // Fetch users
  const { data: users = [], isLoading, refetch } = useQuery({
    queryKey: ['users'],
    queryFn: usersApi.getAll,
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: usersApi.delete,
    onSuccess: () => {
      message.success('User deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['users'] })
    },
    onError: (error: any) => {
      message.error(error.response?.data || 'Failed to delete user')
    },
  })

  // Toggle status mutation
  const toggleMutation = useMutation({
    mutationFn: usersApi.toggle,
    onSuccess: () => {
      message.success('User status updated')
      queryClient.invalidateQueries({ queryKey: ['users'] })
    },
    onError: () => {
      message.error('Failed to update user status')
    },
  })

  // Filter users
  const filteredUsers = users.filter((user) => {
    const matchesSearch =
      user.username.toLowerCase().includes(searchText.toLowerCase()) ||
      user.email?.toLowerCase().includes(searchText.toLowerCase()) ||
      user.fullName?.toLowerCase().includes(searchText.toLowerCase())

    const matchesStatus =
      statusFilter === 'all' ||
      (statusFilter === 'active' && user.active) ||
      (statusFilter === 'inactive' && !user.active)

    const matchesRole =
      roleFilter === 'all' || user.roles.includes(roleFilter)

    return matchesSearch && matchesStatus && matchesRole
  })

  const handleEdit = (user: UserDTO) => {
    setSelectedUser(user)
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
      title: 'User',
      key: 'user',
      width: 250,
      render: (_: any, record: UserDTO) => (
        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <Avatar icon={<UserOutlined />} style={{ backgroundColor: '#1890ff' }}>
            {record.username.charAt(0).toUpperCase()}
          </Avatar>
          <div>
            <div style={{ fontWeight: 500 }}>{record.username}</div>
            <div style={{ fontSize: 12, color: '#999' }}>{record.email}</div>
          </div>
        </div>
      ),
    },
    {
      title: 'Full Name',
      dataIndex: 'fullName',
      key: 'fullName',
      width: 200,
    },
    {
      title: 'Roles',
      dataIndex: 'roles',
      key: 'roles',
      width: 200,
      render: (roles: string[]) => (
        <>
          {roles.map((role) => (
            <Tag key={role} color="blue">
              {role}
            </Tag>
          ))}
        </>
      ),
    },
    {
      title: 'Permissions',
      dataIndex: 'permissions',
      key: 'permissions',
      width: 120,
      render: (permissions: string[]) => (
        <Tooltip title={permissions.slice(0, 5).join(', ') + (permissions.length > 5 ? '...' : '')}>
          <Tag>{permissions.length} permissions</Tag>
        </Tooltip>
      ),
    },
    {
      title: 'Status',
      dataIndex: 'active',
      key: 'active',
      width: 120,
      render: (active: boolean) => (
        <Tag
          color={active ? 'success' : 'default'}
          icon={active ? <CheckCircleOutlined /> : <StopOutlined />}
        >
          {active ? 'Active' : 'Inactive'}
        </Tag>
      ),
    },
    {
      title: 'Last Login',
      dataIndex: 'lastLogin',
      key: 'lastLogin',
      width: 180,
      render: (date: string) =>
        date ? new Date(date).toLocaleString() : 'Never',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 80,
      fixed: 'right' as const,
      render: (_: any, record: UserDTO) => {
        const menuItems = [
          {
            key: 'edit',
            label: 'Edit',
            icon: <EditOutlined />,
            onClick: () => handleEdit(record),
            disabled: !can('users:update'),
          },
          {
            key: 'toggle',
            label: record.active ? 'Deactivate' : 'Activate',
            icon: record.active ? <StopOutlined /> : <CheckCircleOutlined />,
            onClick: () => handleToggle(record.id),
            disabled: !can('users:update'),
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
                title: 'Delete User',
                content: 'Are you sure you want to delete this user?',
                okText: 'Delete',
                okType: 'danger',
                cancelText: 'Cancel',
                onOk: () => handleDelete(record.id),
              })
            },
            disabled: !can('users:delete'),
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
    <div className="users-page">
      <PageHeader
        title="Users"
        subtitle="Manage dashboard users and permissions"
        breadcrumbs={[{ title: 'Home', path: '/' }, { title: 'Users' }]}
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              Refresh
            </Button>
            <PermissionGuard permission="users:create">
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                Create User
              </Button>
            </PermissionGuard>
          </Space>
        }
      />

      <div className="users-content">
        <div className="users-filters">
          <Search
            placeholder="Search by username, email, or name"
            allowClear
            prefix={<SearchOutlined />}
            style={{ width: 350 }}
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
            placeholder="Role"
            style={{ width: 150 }}
            value={roleFilter}
            onChange={setRoleFilter}
            options={[
              { label: 'All Roles', value: 'all' },
              { label: 'Admin', value: 'ADMIN' },
              { label: 'Operator', value: 'OPERATOR' },
              { label: 'Viewer', value: 'VIEWER' },
            ]}
          />
        </div>

        <Table
          columns={columns}
          dataSource={filteredUsers}
          rowKey="id"
          loading={isLoading}
          pagination={{
            total: filteredUsers.length,
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} users`,
          }}
          scroll={{ x: 1200 }}
        />
      </div>

      {createModalVisible && (
        <CreateUserModal
          visible={createModalVisible}
          onClose={() => setCreateModalVisible(false)}
          onSuccess={() => {
            setCreateModalVisible(false)
            queryClient.invalidateQueries({ queryKey: ['users'] })
          }}
        />
      )}

      {editModalVisible && selectedUser && (
        <EditUserModal
          visible={editModalVisible}
          user={selectedUser}
          onClose={() => {
            setEditModalVisible(false)
            setSelectedUser(null)
          }}
          onSuccess={() => {
            setEditModalVisible(false)
            setSelectedUser(null)
            queryClient.invalidateQueries({ queryKey: ['users'] })
          }}
        />
      )}
    </div>
  )
}
