import { Card, Space, Typography, Button, Divider, List } from 'antd'
import {
  BookOutlined,
  LinkOutlined,
  ApiOutlined,
  FileTextOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons'
import './DocumentationTab.css'

const { Title, Text: TypographyText, Paragraph } = Typography

export const DocumentationTab = () => {
  const apiBaseUrl = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8081/dashboard-api'
  const swaggerUrl = `${apiBaseUrl.replace('/dashboard-api', '')}/swagger-ui.html`

  const quickStartSteps = [
    {
      title: 'Get an API Key',
      description: 'Generate an API key from the API Keys tab and copy it securely.',
    },
    {
      title: 'Choose a Route',
      description: 'Select the route you want to use (e.g., CREDIT_TRANSFER_PACS008).',
    },
    {
      title: 'Make Your First Request',
      description: 'Send a POST request to the route endpoint with your API key in the X-API-Key header.',
    },
    {
      title: 'Handle the Response',
      description: 'Receive the translated ISO 20022 message in the specified format.',
    },
  ]

  return (
    <div className="documentation-tab">
      <Space direction="vertical" size="large" style={{ width: '100%' }}>
        {/* Quick Start */}
        <Card>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Title level={3}>
              <BookOutlined /> Quick Start Guide
            </Title>
            <Paragraph>
              Get started with the ISO 20022 Translation API in minutes. Follow these simple steps
              to make your first API call.
            </Paragraph>
            <List
              dataSource={quickStartSteps}
              renderItem={(item, index) => (
                <List.Item>
                  <Space>
                    <div className="step-number">{index + 1}</div>
                    <div>
                      <TypographyText strong>{item.title}</TypographyText>
                      <br />
                      <TypographyText type="secondary">{item.description}</TypographyText>
                    </div>
                  </Space>
                </List.Item>
              )}
            />
          </Space>
        </Card>

        {/* API Documentation */}
        <Card>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Title level={3}>
              <ApiOutlined /> Interactive API Documentation
            </Title>
            <Paragraph>
              Explore all available endpoints, request/response schemas, and try out API calls
              directly from your browser using Swagger UI.
            </Paragraph>
            <Button
              type="primary"
              icon={<LinkOutlined />}
              size="large"
              href={swaggerUrl}
              target="_blank"
            >
              Open Swagger UI
            </Button>
            <Divider />
            <div>
              <TypographyText strong>Features:</TypographyText>
              <ul style={{ marginTop: 8, paddingLeft: 20 }}>
                <li>Complete API reference with all endpoints</li>
                <li>Request/response examples</li>
                <li>Try-it-out functionality</li>
                <li>Authentication testing</li>
                <li>Schema definitions</li>
              </ul>
            </div>
          </Space>
        </Card>

        {/* API Overview */}
        <Card>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Title level={3}>
              <FileTextOutlined /> API Overview
            </Title>
            <div>
              <TypographyText strong>Base URL:</TypographyText>
              <div style={{ marginTop: 8, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
                <TypographyText code>{apiBaseUrl}</TypographyText>
              </div>
            </div>
            <Divider />
            <div>
              <TypographyText strong>Authentication:</TypographyText>
              <Paragraph>
                All API requests require authentication using an API key. Include your API key in
                the request header:
              </Paragraph>
              <div style={{ marginTop: 8, padding: 12, background: '#f5f5f5', borderRadius: 4 }}>
                <TypographyText code>X-API-Key: your-api-key-here</TypographyText>
              </div>
            </div>
            <Divider />
            <div>
              <TypographyText strong>Available Routes:</TypographyText>
              <ul style={{ marginTop: 8, paddingLeft: 20 }}>
                <li>
                  <TypographyText code>CREDIT_TRANSFER_PACS008</TypographyText> - Credit transfer
                  messages
                </li>
                <li>
                  <TypographyText code>PAYMENT_STATUS_PACS002</TypographyText> - Payment status
                  reports
                </li>
                <li>
                  <TypographyText code>ACCOUNT_STATEMENT_CAMT053</TypographyText> - Account
                  statements
                </li>
                <li>
                  <TypographyText code>BALANCE_REPORT_CAMT052</TypographyText> - Balance reports
                </li>
              </ul>
            </div>
          </Space>
        </Card>

        {/* Best Practices */}
        <Card>
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Title level={3}>
              <CheckCircleOutlined /> Best Practices
            </Title>
            <List
              dataSource={[
                'Store API keys securely and never commit them to version control',
                'Use environment variables for API keys in your applications',
                'Rotate API keys regularly for enhanced security',
                'Monitor API key usage and revoke unused keys',
                'Handle errors gracefully and implement retry logic',
                'Respect rate limits and implement proper throttling',
              ]}
              renderItem={(item) => (
                <List.Item>
                  <CheckCircleOutlined style={{ color: '#52c41a', marginRight: 8 }} />
                  {item}
                </List.Item>
              )}
            />
          </Space>
        </Card>
      </Space>
    </div>
  )
}



