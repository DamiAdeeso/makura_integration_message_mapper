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
  DatePicker,
  Form,
  Typography,
  Alert,
  Tooltip,
  Switch,
  Card,
} from 'antd'
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  CopyOutlined,
  MoreOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ExclamationCircleOutlined,
} from '@ant-design/icons'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { PermissionGuard, usePermissions } from '../PermissionGuard'
import { apiKeysApi, ApiKeyDTO, CreateApiKeyRequest, UpdateApiKeyRequest } from '../../api/apiKeysApi'
import { routesApi, RouteDTO } from '../../api/routesApi'
import dayjs, { Dayjs } from 'dayjs'
import './ApiKeysTab.css'

const { Search } = Input
const { Text: TypographyText } = Typography

export const ApiKeysTab = () => {
  const [searchText, setSearchText] = useState('')
  const [routeFilter, setRouteFilter] = useState<string>('all')
  const [statusFilter, setStatusFilter] = useState<string>('all')
  const [createModalVisible, setCreateModalVisible] = useState(false)
  const [editModalVisible, setEditModalVisible] = useState(false)
  const [newKeyModalVisible, setNewKeyModalVisible] = useState(false)
  const [newKeyData, setNewKeyData] = useState<{ apiKey: string; warning: string } | null>(null)
  const [selectedKey, setSelectedKey] = useState<ApiKeyDTO | null>(null)

  const queryClient = useQueryClient()
  const { can } = usePermissions()
  const [form] = Form.useForm()
  const [editForm] = Form.useForm()

  // Fetch API keys
  const { data: apiKeys = [], isLoading, refetch } = useQuery({
    queryKey: ['api-keys'],
    queryFn: () => apiKeysApi.getAll(),
  })

  // Fetch routes for dropdown
  const { data: routes = [] } = useQuery({
    queryKey: ['routes'],
    queryFn: () => routesApi.getAll(),
  })

  // Create mutation
  const createMutation = useMutation({
    mutationFn: apiKeysApi.create,
    onSuccess: (data) => {
      message.success('API key created successfully')
      queryClient.invalidateQueries({ queryKey: ['api-keys'] })
      setCreateModalVisible(false)
      form.resetFields()
      setNewKeyData({ apiKey: data.apiKey, warning: data.warning })
      setNewKeyModalVisible(true)
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to create API key')
    },
  })

  // Update mutation
  const updateMutation = useMutation({
    mutationFn: ({ id, data }: { id: number; data: UpdateApiKeyRequest }) =>
      apiKeysApi.update(id, data),
    onSuccess: () => {
      message.success('API key updated successfully')
      queryClient.invalidateQueries({ queryKey: ['api-keys'] })
      setEditModalVisible(false)
      editForm.resetFields()
      setSelectedKey(null)
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to update API key')
    },
  })

  // Delete mutation
  const deleteMutation = useMutation({
    mutationFn: apiKeysApi.delete,
    onSuccess: () => {
      message.success('API key deleted successfully')
      queryClient.invalidateQueries({ queryKey: ['api-keys'] })
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to delete API key')
    },
  })

  // Regenerate mutation
  const regenerateMutation = useMutation({
    mutationFn: apiKeysApi.regenerate,
    onSuccess: (data) => {
      message.success('API key regenerated successfully')
      queryClient.invalidateQueries({ queryKey: ['api-keys'] })
      setNewKeyData({ apiKey: data.apiKey, warning: data.warning })
      setNewKeyModalVisible(true)
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to regenerate API key')
    },
  })

  // Filter API keys
  const filteredKeys = apiKeys.filter((key) => {
    const matchesSearch =
      key.routeId.toLowerCase().includes(searchText.toLowerCase()) ||
      key.maskedKey.toLowerCase().includes(searchText.toLowerCase()) ||
      (key.description && key.description.toLowerCase().includes(searchText.toLowerCase()))

    const matchesRoute = routeFilter === 'all' || key.routeId === routeFilter
    const matchesStatus =
      statusFilter === 'all' ||
      (statusFilter === 'active' && key.active && !key.expired) ||
      (statusFilter === 'inactive' && !key.active) ||
      (statusFilter === 'expired' && key.expired) ||
      (statusFilter === 'expiring' && key.expiringSoon)

    return matchesSearch && matchesRoute && matchesStatus
  })

  const handleCreate = (values: any) => {
    const request: CreateApiKeyRequest = {
      routeId: values.routeId,
      description: values.description,
      validFrom: values.validFrom.toISOString(),
      validUntil: values.validUntil.toISOString(),
    }
    createMutation.mutate(request)
  }

  const handleEdit = (key: ApiKeyDTO) => {
    setSelectedKey(key)
    editForm.setFieldsValue({
      description: key.description,
      validFrom: dayjs(key.validFrom),
      validUntil: dayjs(key.validUntil),
      active: key.active,
    })
    setEditModalVisible(true)
  }

  const handleUpdate = (values: any) => {
    if (!selectedKey) return
    const request: UpdateApiKeyRequest = {
      description: values.description,
      validFrom: values.validFrom?.toISOString(),
      validUntil: values.validUntil?.toISOString(),
      active: values.active,
    }
    updateMutation.mutate({ id: selectedKey.id, data: request })
  }

  const handleDelete = (id: number) => {
    Modal.confirm({
      title: 'Delete API Key',
      content: 'Are you sure you want to delete this API key? This action cannot be undone.',
      okText: 'Delete',
      okType: 'danger',
      onOk: () => deleteMutation.mutate(id),
    })
  }

  const handleRegenerate = (id: number) => {
    Modal.confirm({
      title: 'Regenerate API Key',
      content: 'This will create a new API key and deactivate the old one. Continue?',
      okText: 'Regenerate',
      onOk: () => regenerateMutation.mutate(id),
    })
  }

  const copyToClipboard = (text: string) => {
    navigator.clipboard.writeText(text)
    message.success('Copied to clipboard!')
  }

  const getStatusTag = (key: ApiKeyDTO) => {
    if (key.expired) {
      return <Tag color="red" icon={<CloseCircleOutlined />}>Expired</Tag>
    }
    if (key.expiringSoon) {
      return <Tag color="orange" icon={<ExclamationCircleOutlined />}>Expiring Soon</Tag>
    }
    if (key.active) {
      return <Tag color="green" icon={<CheckCircleOutlined />}>Active</Tag>
    }
    return <Tag color="default">Inactive</Tag>
  }

  const actionMenuItems = (key: ApiKeyDTO) => [
    {
      key: 'edit',
      label: 'Edit',
      icon: <EditOutlined />,
      onClick: () => handleEdit(key),
      disabled: !can('api-keys:update'),
    },
    {
      key: 'regenerate',
      label: 'Regenerate',
      icon: <ReloadOutlined />,
      onClick: () => handleRegenerate(key.id),
      disabled: !can('api-keys:create'),
    },
    {
      type: 'divider' as const,
    },
    {
      key: 'delete',
      label: 'Delete',
      icon: <DeleteOutlined />,
      danger: true,
      onClick: () => handleDelete(key.id),
      disabled: !can('api-keys:delete'),
    },
  ]

  const columns = [
    {
      title: 'Route ID',
      dataIndex: 'routeId',
      key: 'routeId',
      sorter: (a: ApiKeyDTO, b: ApiKeyDTO) => a.routeId.localeCompare(b.routeId),
    },
    {
      title: 'API Key',
      dataIndex: 'maskedKey',
      key: 'maskedKey',
      render: (text: string) => (
        <Space>
          <TypographyText code>{text}</TypographyText>
          <Tooltip title="Copy masked key">
            <Button
              type="text"
              size="small"
              icon={<CopyOutlined />}
              onClick={() => copyToClipboard(text)}
            />
          </Tooltip>
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
      title: 'Status',
      key: 'status',
      render: (_: any, record: ApiKeyDTO) => getStatusTag(record),
    },
    {
      title: 'Valid Until',
      dataIndex: 'validUntil',
      key: 'validUntil',
      render: (text: string) => dayjs(text).format('YYYY-MM-DD HH:mm'),
      sorter: (a: ApiKeyDTO, b: ApiKeyDTO) =>
        dayjs(a.validUntil).unix() - dayjs(b.validUntil).unix(),
    },
    {
      title: 'Created By',
      dataIndex: 'createdBy',
      key: 'createdBy',
    },
    {
      title: 'Actions',
      key: 'actions',
      width: 80,
      render: (_: any, record: ApiKeyDTO) => (
        <PermissionGuard permission="api-keys:update">
          <Dropdown menu={{ items: actionMenuItems(record) }} trigger={['click']}>
            <Button type="text" icon={<MoreOutlined />} />
          </Dropdown>
        </PermissionGuard>
      ),
    },
  ]

  return (
    <div className="api-keys-tab">
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* Filters */}
        <Card>
          <Space wrap>
            <Search
              placeholder="Search by route, key, or description"
              allowClear
              style={{ width: 300 }}
              value={searchText}
              onChange={(e) => setSearchText(e.target.value)}
              prefix={<SearchOutlined />}
            />
            <Select
              placeholder="Filter by route"
              style={{ width: 200 }}
              value={routeFilter}
              onChange={setRouteFilter}
            >
              <Select.Option value="all">All Routes</Select.Option>
              {routes.map((route: RouteDTO) => (
                <Select.Option key={route.routeId} value={route.routeId}>
                  {route.routeId}
                </Select.Option>
              ))}
            </Select>
            <Select
              placeholder="Filter by status"
              style={{ width: 150 }}
              value={statusFilter}
              onChange={setStatusFilter}
            >
              <Select.Option value="all">All Status</Select.Option>
              <Select.Option value="active">Active</Select.Option>
              <Select.Option value="inactive">Inactive</Select.Option>
              <Select.Option value="expired">Expired</Select.Option>
              <Select.Option value="expiring">Expiring Soon</Select.Option>
            </Select>
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              Refresh
            </Button>
            <PermissionGuard permission="api-keys:create">
              <Button
                type="primary"
                icon={<PlusOutlined />}
                onClick={() => setCreateModalVisible(true)}
              >
                Generate API Key
              </Button>
            </PermissionGuard>
          </Space>
        </Card>

        {/* Table */}
        <Card>
          <Table
            columns={columns}
            dataSource={filteredKeys}
            rowKey="id"
            loading={isLoading}
            pagination={{
              pageSize: 10,
              showSizeChanger: true,
              showTotal: (total) => `Total ${total} API keys`,
            }}
          />
        </Card>
      </Space>

      {/* Create Modal */}
      <Modal
        title="Generate New API Key"
        open={createModalVisible}
        onCancel={() => {
          setCreateModalVisible(false)
          form.resetFields()
        }}
        footer={null}
        width={600}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={handleCreate}
          initialValues={{
            validFrom: dayjs(),
            validUntil: dayjs().add(1, 'year'),
          }}
        >
          <Form.Item
            name="routeId"
            label="Route"
            rules={[{ required: true, message: 'Please select a route' }]}
          >
            <Select placeholder="Select route">
              {routes.map((route: RouteDTO) => (
                <Select.Option key={route.routeId} value={route.routeId}>
                  {route.routeId} - {route.name}
                </Select.Option>
              ))}
            </Select>
          </Form.Item>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} placeholder="Optional description for this API key" />
          </Form.Item>
          <Form.Item
            name="validFrom"
            label="Valid From"
            rules={[{ required: true, message: 'Please select valid from date' }]}
          >
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item
            name="validUntil"
            label="Valid Until"
            rules={[{ required: true, message: 'Please select valid until date' }]}
          >
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={createMutation.isPending}>
                Generate
              </Button>
              <Button onClick={() => setCreateModalVisible(false)}>Cancel</Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>

      {/* New Key Modal - Shows full key once */}
      <Modal
        title="API Key Generated"
        open={newKeyModalVisible}
        onCancel={() => {
          setNewKeyModalVisible(false)
          setNewKeyData(null)
        }}
        footer={[
          <Button key="copy" type="primary" icon={<CopyOutlined />} onClick={() => {
            if (newKeyData) copyToClipboard(newKeyData.apiKey)
          }}>
            Copy Key
          </Button>,
          <Button key="close" onClick={() => {
            setNewKeyModalVisible(false)
            setNewKeyData(null)
          }}>
            I've Copied It
          </Button>,
        ]}
        width={700}
      >
        {newKeyData && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Alert
              message="Important"
              description={newKeyData.warning}
              type="warning"
              showIcon
            />
            <div>
              <TypographyText strong>Your API Key:</TypographyText>
              <div style={{ marginTop: 8, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
                <TypographyText code style={{ fontSize: 14 }}>{newKeyData.apiKey}</TypographyText>
              </div>
            </div>
          </Space>
        )}
      </Modal>

      {/* Edit Modal */}
      <Modal
        title="Edit API Key"
        open={editModalVisible}
        onCancel={() => {
          setEditModalVisible(false)
          editForm.resetFields()
          setSelectedKey(null)
        }}
        footer={null}
        width={600}
      >
        <Form form={editForm} layout="vertical" onFinish={handleUpdate}>
          <Form.Item name="description" label="Description">
            <Input.TextArea rows={3} placeholder="Optional description" />
          </Form.Item>
          <Form.Item name="validFrom" label="Valid From">
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="validUntil" label="Valid Until">
            <DatePicker showTime style={{ width: '100%' }} />
          </Form.Item>
          <Form.Item name="active" valuePropName="checked" label="Active">
            <Switch />
          </Form.Item>
          <Form.Item>
            <Space>
              <Button type="primary" htmlType="submit" loading={updateMutation.isPending}>
                Update
              </Button>
              <Button onClick={() => {
                setEditModalVisible(false)
                editForm.resetFields()
                setSelectedKey(null)
              }}>
                Cancel
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Modal>
    </div>
  )
}

