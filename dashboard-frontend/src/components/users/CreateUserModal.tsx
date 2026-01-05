import { Modal, Form, Input, Select, Switch, message } from 'antd'
import { useMutation } from '@tanstack/react-query'
import { usersApi, CreateUserRequest } from '../../api/usersApi'

interface CreateUserModalProps {
  visible: boolean
  onClose: () => void
  onSuccess: () => void
}

export const CreateUserModal = ({
  visible,
  onClose,
  onSuccess,
}: CreateUserModalProps) => {
  const [form] = Form.useForm()

  const createMutation = useMutation({
    mutationFn: usersApi.create,
    onSuccess: () => {
      message.success('User created successfully')
      form.resetFields()
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to create user')
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

  return (
    <Modal
      title="Create New User"
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={createMutation.isPending}
      width={600}
      okText="Create"
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          roles: ['VIEWER'],
          active: true,
        }}
      >
        <Form.Item
          name="username"
          label="Username"
          rules={[
            { required: true, message: 'Please enter username' },
            {
              pattern: /^[a-z0-9_]+$/,
              message: 'Only lowercase letters, numbers, and underscores',
            },
            { min: 3, message: 'Minimum 3 characters' },
          ]}
        >
          <Input placeholder="johndoe" />
        </Form.Item>

        <Form.Item
          name="password"
          label="Password"
          rules={[
            { required: true, message: 'Please enter password' },
            { min: 8, message: 'Minimum 8 characters' },
          ]}
        >
          <Input.Password placeholder="********" />
        </Form.Item>

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
              { label: 'Operator - Manage routes and mappings', value: 'OPERATOR' },
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



