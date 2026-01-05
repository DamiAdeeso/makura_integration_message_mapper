import { Modal, Form, Input, Select, Switch, message } from 'antd'
import { useMutation } from '@tanstack/react-query'
import { usersApi, UserDTO, UpdateUserRequest } from '../../api/usersApi'
import { useEffect } from 'react'

interface EditUserModalProps {
  visible: boolean
  user: UserDTO
  onClose: () => void
  onSuccess: () => void
}

export const EditUserModal = ({
  visible,
  user,
  onClose,
  onSuccess,
}: EditUserModalProps) => {
  const [form] = Form.useForm()

  useEffect(() => {
    if (user) {
      form.setFieldsValue({
        email: user.email,
        fullName: user.fullName,
        roles: user.roles,
        active: user.active,
      })
    }
  }, [user, form])

  const updateMutation = useMutation({
    mutationFn: (data: UpdateUserRequest) => usersApi.update(user.id, data),
    onSuccess: () => {
      message.success('User updated successfully')
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to update user')
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

  return (
    <Modal
      title={`Edit User: ${user.username}`}
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={updateMutation.isPending}
      width={600}
      okText="Update"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="email"
          label="Email"
          rules={[
            { required: true, message: 'Please enter email' },
            { type: 'email', message: 'Please enter valid email' },
          ]}
        >
          <Input placeholder="john@example.com" />
        </Form.Item>

        <Form.Item name="fullName" label="Full Name">
          <Input placeholder="John Doe" />
        </Form.Item>

        <Form.Item name="password" label="Password" tooltip="Leave empty to keep current password">
          <Input.Password placeholder="********" />
        </Form.Item>

        <Form.Item
          name="roles"
          label="Roles"
          rules={[{ required: true, message: 'Please select at least one role' }]}
          tooltip="Users can have multiple roles"
        >
          <Select
            mode="multiple"
            placeholder="Select roles"
            options={[
              { label: 'Admin - Full access', value: 'ADMIN' },
              {
                label: 'Operator - Manage routes and mappings',
                value: 'OPERATOR',
              },
              { label: 'Viewer - Read-only access', value: 'VIEWER' },
            ]}
          />
        </Form.Item>

        <Form.Item name="active" label="Active" valuePropName="checked">
          <Switch />
        </Form.Item>
      </Form>
    </Modal>
  )
}



