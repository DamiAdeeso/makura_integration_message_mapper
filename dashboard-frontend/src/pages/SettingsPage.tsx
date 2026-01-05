import { Card, Switch, Space, Typography, Divider, Row, Col } from 'antd'
import { BulbOutlined, MoonOutlined } from '@ant-design/icons'
import { PageHeader } from '../components/PageHeader'
import { useTheme } from '../contexts/ThemeContext'
import './SettingsPage.css'

const { Title, Text: TypographyText, Paragraph } = Typography

export const SettingsPage = () => {
  const { theme, toggleTheme, setTheme } = useTheme()

  return (
    <div className="settings-page">
      <PageHeader
        title="Settings & Preferences"
        subtitle="Manage your application preferences"
        breadcrumbs={[{ title: 'Home', path: '/' }, { title: 'Settings' }]}
      />

      <div className="settings-content">
        <Row gutter={[24, 24]}>
          {/* Appearance Section */}
          <Col xs={24} lg={16}>
            <Card title="Appearance" className="settings-card">
              <Space direction="vertical" size="large" style={{ width: '100%' }}>
                <div className="setting-item">
                  <div className="setting-info">
                    <Space>
                      {theme === 'dark' ? (
                        <MoonOutlined style={{ fontSize: 20 }} />
                      ) : (
                        <BulbOutlined style={{ fontSize: 20 }} />
                      )}
                      <div>
                        <Title level={5} style={{ margin: 0 }}>
                          Theme Mode
                        </Title>
                        <TypographyText type="secondary">
                          {theme === 'dark'
                            ? 'Dark mode is enabled. Switch to light mode for a brighter interface.'
                            : 'Light mode is enabled. Switch to dark mode for reduced eye strain.'}
                        </TypographyText>
                      </div>
                    </Space>
                  </div>
                  <Switch
                    checked={theme === 'dark'}
                    onChange={toggleTheme}
                    checkedChildren={<MoonOutlined />}
                    unCheckedChildren={<BulbOutlined />}
                    size="default"
                  />
                </div>

                <Divider />

                <div className="theme-preview">
                  <TypographyText strong>Preview:</TypographyText>
                  <div className={`preview-box ${theme}`}>
                    <div className="preview-header">
                      <TypographyText strong>Sample Card</TypographyText>
                    </div>
                    <div className="preview-content">
                      <Paragraph>
                        This is how your interface will look in{' '}
                        <TypographyText strong>{theme === 'dark' ? 'dark' : 'light'}</TypographyText> mode.
                      </Paragraph>
                    </div>
                  </div>
                </div>
              </Space>
            </Card>
          </Col>

          {/* Quick Actions */}
          <Col xs={24} lg={8}>
            <Card title="Quick Actions" className="settings-card">
              <Space direction="vertical" style={{ width: '100%' }} size="middle">
                <div className="quick-action-item">
                  <TypographyText strong>Reset to Default</TypographyText>
                  <TypographyText type="secondary" style={{ fontSize: 12 }}>
                    Reset all preferences to default values
                  </TypographyText>
                </div>
                <div className="quick-action-item">
                  <TypographyText strong>Export Settings</TypographyText>
                  <TypographyText type="secondary" style={{ fontSize: 12 }}>
                    Download your preferences as JSON
                  </TypographyText>
                </div>
                <div className="quick-action-item">
                  <TypographyText strong>Import Settings</TypographyText>
                  <TypographyText type="secondary" style={{ fontSize: 12 }}>
                    Restore preferences from a file
                  </TypographyText>
                </div>
              </Space>
            </Card>

            <Card title="About" className="settings-card" style={{ marginTop: 24 }}>
              <Space direction="vertical" size="small">
                <div>
                  <TypographyText type="secondary">Application Version</TypographyText>
                  <br />
                  <TypographyText strong>1.0.0</TypographyText>
                </div>
                <Divider style={{ margin: '12px 0' }} />
                <div>
                  <TypographyText type="secondary">Build Date</TypographyText>
                  <br />
                  <TypographyText strong>{new Date().toLocaleDateString()}</TypographyText>
                </div>
              </Space>
            </Card>
          </Col>
        </Row>
      </div>
    </div>
  )
}

