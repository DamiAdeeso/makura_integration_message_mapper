import { useState } from 'react'
import {
  Table,
  Button,
  Space,
  Tag,
  Input,
  Modal,
  message,
  Tooltip,
  Popconfirm,
  Card,
  Descriptions,
  Drawer,
  Dropdown,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  SafetyOutlined,
  EyeOutlined,
  MoreOutlined,
} from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { PageHeader } from '../components/PageHeader'
import { PermissionGuard } from '../components/PermissionGuard'
import { rolesApi, RoleDTO } from '../api/rolesApi'
import { CreateRoleModal } from '../components/roles/CreateRoleModal'
import { EditRoleModal } from '../components/roles/EditRoleModal'
import './RolesPage.css'

const { Search } = Input

export const RolesPage = () => {
  const [searchText, setSearchText] = useState('')
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [detailsDrawerVisible, setDetailsDrawerVisible] = useState(false)
  const [selectedRole, setSelectedRole] = useState<RoleDTO | null>(null)

  const queryClient = useQueryClient()

  // Fetch roles
  const { data: roles = [], isLoading, refetch } = useQuery({
    queryKey: ['roles'],
    queryFn: rolesApi.getAll,
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: rolesApi.delete,
    onSuccess: () => {
      message.success('Role deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['roles'] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to delete role')
    },
  })

  // Filter roles
  const filteredRoles = roles.filter((role) =>
    role.name.toLowerCase().includes(searchText.toLowerCase()) ||
    role.description?.toLowerCase().includes(searchText.toLowerCase())
  )

  const handleEdit = (role: RoleDTO) => {
    setSelectedRole(role)
    setEditModalVisible(true)
  }

  const handleViewDetails = (role: RoleDTO) => {
    setSelectedRole(role)
    setDetailsDrawerVisible(true)
  }

  const handleDelete = (id: number) => {
    deleteMutation.mutate(id)
  }

  const columns = [
    {
      title: 'Role Name',
      dataIndex: 'name',
      key: 'name',
      width: 200,
      sorter: (a: RoleDTO, b: RoleDTO) => a.name.localeCompare(b.name),
      render: (text: string, record: RoleDTO) => (
        <Space>
          <SafetyOutlined style={{ color: '#1890ff' }} />
          <strong>{text}</strong>
          {record.systemRole && (
            <Tag color="gold">System</Tag>
          )}
        </Space>
      ),
    },
    {
      title: 'Description',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
    },
    {
      title: 'Permissions',
      dataIndex: 'permissions',
      key: 'permissions',
      width: 150,
      render: (permissions: string[]) => (
        <Tooltip
          title={permissions.slice(0, 10).join(', ') + (permissions.length > 10 ? '...' : '')}
        >
          <Tag color="blue">{permissions.length} permissions</Tag>
        </Tooltip>
      ),
    },
    {
      title: 'Users',
      dataIndex: 'userCount',
      key: 'userCount',
      width: 100,
      render: (count: number) => <Tag>{count || 0} users</Tag>,
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 80,
      fixed: 'right' as const,
      render: (_: any, record: RoleDTO) => {
        const menuItems = [
          {
            key: 'view',
            label: 'View Details',
            icon: <EyeOutlined />,
            onClick: () => handleViewDetails(record),
          },
          {
            key: 'edit',
            label: 'Edit',
            icon: <EditOutlined />,
            onClick: () => handleEdit(record),
            disabled: record.systemRole || !can('roles:update'),
          },
          {
            type: 'divider' as const,
          },
          {
            key: 'delete',
            label: record.systemRole ? 'Cannot Delete System Role' : 'Delete',
            icon: <DeleteOutlined />,
            danger: true,
            onClick: () => {
              if (!record.systemRole) {
                Modal.confirm({
                  title: 'Delete Role',
                  content: 'Are you sure? Users with this role will lose access.',
                  okText: 'Delete',
                  okType: 'danger',
                  cancelText: 'Cancel',
                  onOk: () => handleDelete(record.id),
                })
              }
            },
            disabled: record.systemRole || !can('roles:delete'),
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
    <div className="roles-page">
      <PageHeader
        title="Roles & Permissions"
        subtitle="Manage role-based access control"
        breadcrumbs={[{ title: 'Home', path: '/' }, { title: 'Roles' }]}
        extra={
          <Space>
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              Refresh
            </Button>
            <PermissionGuard permission="roles:create">
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                Create Role
              </Button>
            </PermissionGuard>
          </Space>
        }
      />

      <div className="roles-content">
        <div className="roles-filters">
          <Search
            placeholder="Search by role name or description"
            allowClear
            prefix={<SearchOutlined />}
            style={{ width: 400 }}
            value={searchText}
            onChange={(e) => setSearchText(e.target.value)}
          />
        </div>

        <Table
          columns={columns}
          dataSource={filteredRoles}
          rowKey="id"
          loading={isLoading}
          pagination={{
            total: filteredRoles.length,
            pageSize: 10,
            showSizeChanger: true,
            showTotal: (total) => `Total ${total} roles`,
          }}
        />
      </div>

      {createModalVisible && (
        <CreateRoleModal
          visible={createModalVisible}
          onClose={() => setCreateModalVisible(false)}
          onSuccess={() => {
            setCreateModalVisible(false)
            queryClient.invalidateQueries({ queryKey: ['roles'] })
          }}
        />
      )}

      {editModalVisible && selectedRole && (
        <EditRoleModal
          visible={editModalVisible}
          role={selectedRole}
          onClose={() => {
            setEditModalVisible(false)
            setSelectedRole(null)
          }}
          onSuccess={() => {
            setEditModalVisible(false)
            setSelectedRole(null)
            queryClient.invalidateQueries({ queryKey: ['roles'] })
          }}
        />
      )}

      <Drawer
        title={`Role Details: ${selectedRole?.name}`}
        placement="right"
        width={600}
        open={detailsDrawerVisible}
        onClose={() => {
          setDetailsDrawerVisible(false)
          setSelectedRole(null)
        }}
      >
        {selectedRole && (
          <div>
            <Descriptions column={1} bordered>
              <Descriptions.Item label="Role Name">
                {selectedRole.name}
              </Descriptions.Item>
              <Descriptions.Item label="Description">
                {selectedRole.description || 'N/A'}
              </Descriptions.Item>
              <Descriptions.Item label="System Role">
                {selectedRole.systemRole ? (
                  <Tag color="gold">Yes (Cannot be deleted)</Tag>
                ) : (
                  <Tag>No</Tag>
                )}
              </Descriptions.Item>
              <Descriptions.Item label="Users Assigned">
                {selectedRole.userCount || 0}
              </Descriptions.Item>
            </Descriptions>

            <Card
              title={`Permissions (${selectedRole.permissions.length})`}
              style={{ marginTop: 24 }}
            >
              <div className="permissions-grid">
                {selectedRole.permissions.map((permission) => (
                  <Tag key={permission} color="blue" style={{ margin: 4 }}>
                    {permission}
                  </Tag>
                ))}
              </div>
            </Card>
          </div>
        )}
      </Drawer>
    </div>
  )
}

