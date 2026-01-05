import { Modal, Form, Input, Select, message, Divider } from 'antd'
import { useMutation } from '@tanstack/react-query'
import { rolesApi, RoleDTO, UpdateRoleRequest } from '../../api/rolesApi'
import { useEffect } from 'react'

const { TextArea } = Input

interface EditRoleModalProps {
  visible: boolean
  role: RoleDTO
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

export const EditRoleModal = ({
  visible,
  role,
  onClose,
  onSuccess,
}: EditRoleModalProps) => {
  const [form] = Form.useForm()

  useEffect(() => {
    if (role) {
      form.setFieldsValue({
        description: role.description,
        permissions: role.permissions,
      })
    }
  }, [role, form])

  const updateMutation = useMutation({
    mutationFn: (data: UpdateRoleRequest) => rolesApi.update(role.id, data),
    onSuccess: () => {
      message.success('Role updated successfully')
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to update role')
    },
  })

  const handleSubmit = async () => {
    try {
      const values = await form.validateFields()
      updateMutation.mutate(values)
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
      title={`Edit Role: ${role.name}`}
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={updateMutation.isPending}
      width={700}
      okText="Update"
    >
      <Form form={form} layout="vertical">
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



