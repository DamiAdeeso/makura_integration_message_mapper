import { Modal, Form, Input, Select, message, Divider } from 'antd'
import { useMutation, useQuery } from '@tanstack/react-query'
import { rolesApi, CreateRoleRequest } from '../../api/rolesApi'

const { TextArea } = Input

interface CreateRoleModalProps {
  visible: boolean
  onClose: () => void
  onSuccess: () => void
}

// Available permissions grouped by resource
const PERMISSION_GROUPS = {
  Routes: ['routes:create', 'routes:view', 'routes:update', 'routes:delete', 'routes:toggle', 'routes:publish'],
  Users: ['users:create', 'users:view', 'users:update', 'users:delete'],
  Roles: ['roles:create', 'roles:view', 'roles:update', 'roles:delete'],
  Mappings: ['mappings:create', 'mappings:view', 'mappings:update', 'mappings:delete'],
  Metrics: ['metrics:view', 'metrics:export'],
  System: ['system:configure', 'system:health'],
}

export const CreateRoleModal = ({
  visible,
  onClose,
  onSuccess,
}: CreateRoleModalProps) => {
  const [form] = Form.useForm()

  const createMutation = useMutation({
    mutationFn: rolesApi.create,
    onSuccess: () => {
      message.success('Role created successfully')
      form.resetFields()
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to create role')
    },
  })

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      createMutation.mutate(values)
    } catch (error) {
      console.error('Validation failed:', error)
    }
  }

  // Flatten all permissions for the select
  const allPermissions = Object.entries(PERMISSION_GROUPS).flatMap(
    ([group, perms]) =>
      perms.map((perm) => ({
        label: `${group}: ${perm.split(':')[1]}`,
        value: perm,
        group,
      }))
  )

  return (
    <Modal
      title="Create New Role"
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={createMutation.isPending}
      width={700}
      okText="Create"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="name"
          label="Role Name"
          rules={[
            { required: true, message: 'Please enter role name' },
            {
              pattern: /^[A-Z_]+$/,
              message: 'Only uppercase letters and underscores',
            },
          ]}
          tooltip="e.g., CUSTOM_OPERATOR, DATA_ANALYST"
        >
          <Input placeholder="CUSTOM_OPERATOR" />
        </Form.Item>

        <Form.Item name="description" label="Description">
          <TextArea rows={3} placeholder="Description of this role..." />
        </Form.Item>

        <Divider orientation="left">Permissions</Divider>

        <Form.Item
          name="permissions"
          label="Assign Permissions"
          rules={[
            { required: true, message: 'Please select at least one permission' },
          ]}
          tooltip="Select all permissions this role should have"
        >
          <Select
            mode="multiple"
            placeholder="Select permissions"
            style={{ width: '100%' }}
            maxTagCount="responsive"
            options={allPermissions}
            optionFilterProp="label"
            filterOption={(input, option) =>
              (option?.label ?? '').toLowerCase().includes(input.toLowerCase())
            }
          />
        </Form.Item>

        <div style={{ marginTop: 16, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
          <strong>Quick Templates:</strong>
          <div style={{ marginTop: 8, display: 'flex', gap: 8, flexWrap: 'wrap' }}>
            <a
              onClick={() => {
                const allPerms = Object.values(PERMISSION_GROUPS).flat()
                form.setFieldsValue({ permissions: allPerms })
              }}
            >
              All Permissions
            </a>
            <a
              onClick={() => {
                const viewPerms = Object.values(PERMISSION_GROUPS)
                  .flat()
                  .filter((p) => p.includes(':view'))
                form.setFieldsValue({ permissions: viewPerms })
              }}
            >
              Read-Only
            </a>
            <a
              onClick={() => {
                form.setFieldsValue({ permissions: PERMISSION_GROUPS.Routes })
              }}
            >
              Routes Only
            </a>
          </div>
        </div>
      </Form>
    </Modal>
  )
}



