import { useNavigate } from 'react-router-dom'
import { Card, Row, Col, Space, Typography } from 'antd'
import { KeyOutlined, BookOutlined, CodeOutlined } from '@ant-design/icons'
import { PageHeader } from '../components/PageHeader'
import './DevelopersPage.css'

const { Title, Text: TypographyText, Paragraph } = Typography

export const DevelopersPage = () => {
  const navigate = useNavigate()

  const developerCards = [
    {
      key: 'api-keys',
      title: 'API Keys',
      icon: <KeyOutlined style={{ fontSize: 48, color: '#1F7FBF' }} />,
      description: 'Generate, manage, and monitor API keys for your integrations',
      route: '/developers/api-keys',
    },
    {
      key: 'documentation',
      title: 'Documentation',
      icon: <BookOutlined style={{ fontSize: 48, color: '#1F7FBF' }} />,
      description: 'API reference, guides, and interactive Swagger documentation',
      route: '/developers/documentation',
    },
    {
      key: 'examples',
      title: 'Code Examples',
      icon: <CodeOutlined style={{ fontSize: 48, color: '#1F7FBF' }} />,
      description: 'Ready-to-use code snippets in multiple programming languages',
      route: '/developers/examples',
    },
  ]

  return (
    <div className="developers-page">
      <PageHeader
        title="Developers"
        subtitle="API Keys, Documentation, and Integration Resources"
        breadcrumbs={[{ title: 'Developers' }]}
      />

      <div className="developers-content">
        <Row gutter={[24, 24]}>
          {developerCards.map((card) => (
            <Col xs={24} sm={12} lg={8} key={card.key}>
              <Card
                className="developer-card"
                hoverable
                onClick={() => navigate(card.route)}
              >
                <Space direction="vertical" size="large" style={{ width: '100%', textAlign: 'center' }}>
                  <div className="developer-card-icon">{card.icon}</div>
                  <Title level={4} style={{ margin: 0 }}>
                    {card.title}
                  </Title>
                  <Paragraph type="secondary" style={{ margin: 0 }}>
                    {card.description}
                  </Paragraph>
                </Space>
              </Card>
            </Col>
          ))}
        </Row>
      </div>
    </div>
  )
}
