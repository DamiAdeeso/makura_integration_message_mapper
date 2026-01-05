import { Modal, Form, Input, Select, Switch, message } from 'antd'
import { useMutation } from '@tanstack/react-query'
import { routesApi, CreateRouteRequest } from '../../api/routesApi'

const { TextArea } = Input

interface CreateRouteModalProps {
  visible: boolean
  onClose: () => void
  onSuccess: () => void
}

export const CreateRouteModal = ({
  visible,
  onClose,
  onSuccess,
}: CreateRouteModalProps) => {
  const [form] = Form.useForm()

  const createMutation = useMutation({
    mutationFn: routesApi.create,
    onSuccess: () => {
      message.success('Route created successfully')
      form.resetFields()
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to create route')
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
      title="Create New Route"
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={createMutation.isPending}
      width={700}
      okText="Create"
    >
      <Form
        form={form}
        layout="vertical"
        initialValues={{
          mode: 'PASSIVE',
          inboundFormat: 'JSON',
          outboundFormat: 'JSON',
          encryptionType: 'NONE',
          active: true,
        }}
      >
        <Form.Item
          name="routeId"
          label="Route ID"
          rules={[
            { required: true, message: 'Please enter Route ID' },
            {
              pattern: /^[A-Z0-9_]+$/,
              message: 'Only uppercase letters, numbers, and underscores',
            },
          ]}
          tooltip="Unique identifier (e.g., SYSTEM_TO_NIP)"
        >
          <Input placeholder="SYSTEM_TO_NIP" />
        </Form.Item>

        <Form.Item
          name="name"
          label="Name"
          rules={[{ required: true, message: 'Please enter name' }]}
        >
          <Input placeholder="System to NIP Translation" />
        </Form.Item>

        <Form.Item name="description" label="Description">
          <TextArea
            rows={3}
            placeholder="Description of this route..."
          />
        </Form.Item>

        <Form.Item
          name="mode"
          label="Mode"
          rules={[{ required: true, message: 'Please select mode' }]}
          tooltip="ACTIVE: Forward to downstream | PASSIVE: Return ISO only"
        >
          <Select>
            <Select.Option value="ACTIVE">ACTIVE (Forward)</Select.Option>
            <Select.Option value="PASSIVE">PASSIVE (No Forward)</Select.Option>
          </Select>
        </Form.Item>

        <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}>
          <Form.Item
            name="inboundFormat"
            label="Inbound Format"
            rules={[{ required: true, message: 'Please select format' }]}
          >
            <Select>
              <Select.Option value="JSON">JSON</Select.Option>
              <Select.Option value="SOAP">SOAP</Select.Option>
              <Select.Option value="XML">XML</Select.Option>
            </Select>
          </Form.Item>

          <Form.Item
            name="outboundFormat"
            label="Outbound Format"
            rules={[{ required: true, message: 'Please select format' }]}
          >
            <Select>
              <Select.Option value="JSON">JSON</Select.Option>
              <Select.Option value="XML">XML</Select.Option>
              <Select.Option value="ISO_XML">ISO XML</Select.Option>
            </Select>
          </Form.Item>
        </div>

        <Form.Item
          noStyle
          shouldUpdate={(prevValues, currentValues) =>
            prevValues.mode !== currentValues.mode
          }
        >
          {({ getFieldValue }) =>
            getFieldValue('mode') === 'ACTIVE' ? (
              <Form.Item
                name="endpoint"
                label="Forward Endpoint"
                rules={[
                  {
                    required: true,
                    message: 'Endpoint required for ACTIVE mode',
                  },
                  { type: 'url', message: 'Please enter valid URL' },
                ]}
              >
                <Input placeholder="https://downstream.api/endpoint" />
              </Form.Item>
            ) : null
          }
        </Form.Item>

        <Form.Item
          name="encryptionType"
          label="Encryption"
          tooltip="Encryption for message transport"
        >
          <Select>
            <Select.Option value="NONE">None</Select.Option>
            <Select.Option value="AES">AES</Select.Option>
            <Select.Option value="PGP">PGP</Select.Option>
          </Select>
        </Form.Item>

        <Form.Item
          noStyle
          shouldUpdate={(prevValues, currentValues) =>
            prevValues.encryptionType !== currentValues.encryptionType
          }
        >
          {({ getFieldValue }) =>
            getFieldValue('encryptionType') !== 'NONE' ? (
              <Form.Item
                name="encryptionKeyRef"
                label="Encryption Key Reference"
              >
                <Input placeholder="key-ref-001" />
              </Form.Item>
            ) : null
          }
        </Form.Item>

        <Form.Item name="active" label="Active" valuePropName="checked">
          <Switch />
        </Form.Item>
      </Form>
    </Modal>
  )
}



