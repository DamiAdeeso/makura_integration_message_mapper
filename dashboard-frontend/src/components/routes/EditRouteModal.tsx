import { Modal, Form, Input, Select, Switch, message } from 'antd'
import { useMutation } from '@tanstack/react-query'
import { routesApi, RouteDTO, UpdateRouteRequest } from '../../api/routesApi'
import { useEffect } from 'react'

const { TextArea } = Input

interface EditRouteModalProps {
  visible: boolean
  route: RouteDTO
  onClose: () => void
  onSuccess: () => void
}

export const EditRouteModal = ({
  visible,
  route,
  onClose,
  onSuccess,
}: EditRouteModalProps) => {
  const [form] = Form.useForm()

  useEffect(() => {
    if (route) {
      form.setFieldsValue({
        name: route.name,
        description: route.description,
        mode: route.mode,
        inboundFormat: route.inboundFormat,
        outboundFormat: route.outboundFormat,
        endpoint: route.endpoint,
        encryptionType: route.encryptionType || 'NONE',
        encryptionKeyRef: route.encryptionKeyRef,
        active: route.active,
      })
    }
  }, [route, form])

  const updateMutation = useMutation({
    mutationFn: (data: UpdateRouteRequest) => routesApi.update(route.id, data),
    onSuccess: () => {
      message.success('Route updated successfully')
      onSuccess()
    },
    onError: (error: any) => {
      message.error(error.response?.data?.message || 'Failed to update route')
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
      title={`Edit Route: ${route.routeId}`}
      open={visible}
      onCancel={onClose}
      onOk={handleSubmit}
      confirmLoading={updateMutation.isPending}
      width={700}
      okText="Update"
    >
      <Form form={form} layout="vertical">
        <Form.Item
          name="name"
          label="Name"
          rules={[{ required: true, message: 'Please enter name' }]}
        >
          <Input placeholder="System to NIP Translation" />
        </Form.Item>

        <Form.Item name="description" label="Description">
          <TextArea rows={3} placeholder="Description of this route..." />
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

        <div
          style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '16px' }}
        >
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



