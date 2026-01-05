import { useState } from 'react'
import { useParams, useNavigate } from 'react-router-dom'
import {
  Card,
  Steps,
  Form,
  Input,
  Select,
  Button,
  Space,
  Typography,
  Radio,
} from 'antd'
import { ArrowLeftOutlined } from '@ant-design/icons'

const { Title } = Typography
const { Step } = Steps

export default function RouteEditorPage() {
  const { id } = useParams()
  const navigate = useNavigate()
  const [currentStep, setCurrentStep] = useState(0)

  const isEditMode = id !== 'new'

  const steps = [
    {
      title: 'Basic Info',
      content: (
        <Form layout="vertical">
          <Form.Item
            label="Route ID"
            name="routeId"
            rules={[{ required: true }]}
          >
            <Input placeholder="e.g., SYSTEM_TO_NIP" />
          </Form.Item>
          <Form.Item label="Name" name="name" rules={[{ required: true }]}>
            <Input placeholder="e.g., System to NIP" />
          </Form.Item>
          <Form.Item label="Description" name="description">
            <Input.TextArea rows={3} />
          </Form.Item>
          <Form.Item
            label="Inbound Format"
            name="inboundFormat"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value="JSON">JSON</Select.Option>
              <Select.Option value="XML">XML</Select.Option>
              <Select.Option value="SOAP">SOAP</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item
            label="Outbound Format"
            name="outboundFormat"
            rules={[{ required: true }]}
          >
            <Select>
              <Select.Option value="ISO_XML">ISO 20022 XML</Select.Option>
            </Select>
          </Form.Item>
          <Form.Item label="Mode" name="mode" rules={[{ required: true }]}>
            <Radio.Group>
              <Radio value="ACTIVE">Active (Forward to endpoint)</Radio>
              <Radio value="PASSIVE">Passive (Return ISO only)</Radio>
            </Radio.Group>
          </Form.Item>
          <Form.Item label="Endpoint URL" name="endpoint">
            <Input placeholder="https://api.example.com/endpoint" />
          </Form.Item>
          <Form.Item label="Encryption" name="encryptionType">
            <Select>
              <Select.Option value="NONE">None</Select.Option>
              <Select.Option value="AES">AES</Select.Option>
              <Select.Option value="PGP">PGP</Select.Option>
            </Select>
          </Form.Item>
        </Form>
      ),
    },
    {
      title: 'Request Mapping',
      content: <div>Request Mapping Builder (Coming Soon)</div>,
    },
    {
      title: 'Response Mapping',
      content: <div>Response Mapping Builder (Coming Soon)</div>,
    },
    {
      title: 'Review & Publish',
      content: <div>Review and Publish (Coming Soon)</div>,
    },
  ]

  return (
    <div>
      <Button
        icon={<ArrowLeftOutlined />}
        onClick={() => navigate('/routes')}
        style={{ marginBottom: 16 }}
      >
        Back to Routes
      </Button>

      <Title level={2}>
        {isEditMode ? 'Edit Route' : 'Create New Route'}
      </Title>

      <Card>
        <Steps current={currentStep} style={{ marginBottom: 32 }}>
          {steps.map((step) => (
            <Step key={step.title} title={step.title} />
          ))}
        </Steps>

        <div style={{ minHeight: 400 }}>{steps[currentStep].content}</div>

        <div style={{ marginTop: 24 }}>
          <Space>
            {currentStep > 0 && (
              <Button onClick={() => setCurrentStep(currentStep - 1)}>
                Previous
              </Button>
            )}
            {currentStep < steps.length - 1 && (
              <Button
                type="primary"
                onClick={() => setCurrentStep(currentStep + 1)}
              >
                Next
              </Button>
            )}
            {currentStep === steps.length - 1 && (
              <Button type="primary">
                {isEditMode ? 'Update' : 'Create'} & Publish
              </Button>
            )}
          </Space>
        </div>
      </Card>
    </div>
  )
}




