import { useState } from 'react'
import { Form, Input, Button, message, Typography, Space } from 'antd'
import {
  UserOutlined,
  LockOutlined,
  SafetyOutlined,
  ThunderboltOutlined,
  GlobalOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons'
import { useNavigate } from 'react-router-dom'
import { useAuthStore } from '../store/authStore'
import { authApi } from '../api/authApi'
import './LoginPage.css'

const { Title, Text: TypographyText, Paragraph } = Typography

export const LoginPage = () => {
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()
  const { setAuth } = useAuthStore()

  const handleLogin = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      const response = await authApi.login(values)
      setAuth(response.token, response.user)
      message.success('Login successful')
      navigate('/')
    } catch (error: any) {
      message.error(error.response?.data?.message || 'Login failed')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="login-page">
      {/* Left Side - Branding */}
      <div className="login-branding">
        <div className="branding-content">
          <div className="brand-header">
            <img src="/logo.png" alt="Makura" className="brand-logo" />
            <Title level={1} className="brand-title">
              MAKURA
            </Title>
            <Paragraph className="brand-subtitle">
              ISO 20022 Translation Platform
            </Paragraph>
          </div>

          <div className="features-list">
            <div className="feature-item">
              <SafetyOutlined className="feature-icon" />
              <div>
                <TypographyText strong className="feature-title">
                  Enterprise Security
                </TypographyText>
                <TypographyText className="feature-desc">
                  Bank-grade encryption and compliance
                </TypographyText>
              </div>
            </div>

            <div className="feature-item">
              <ThunderboltOutlined className="feature-icon" />
              <div>
                <TypographyText strong className="feature-title">
                  Lightning Fast
                </TypographyText>
                <TypographyText className="feature-desc">
                  Process thousands of transactions per second
                </TypographyText>
              </div>
            </div>

            <div className="feature-item">
              <GlobalOutlined className="feature-icon" />
              <div>
                <TypographyText strong className="feature-title">
                  Global Standards
                </TypographyText>
                <TypographyText className="feature-desc">
                  Full ISO 20022 compliance and support
                </TypographyText>
              </div>
            </div>

            <div className="feature-item">
              <CheckCircleOutlined className="feature-icon" />
              <div>
                <TypographyText strong className="feature-title">
                  99.9% Uptime
                </TypographyText>
                <TypographyText className="feature-desc">
                  Reliable and production-ready infrastructure
                </TypographyText>
              </div>
            </div>
          </div>

          <div className="branding-footer">
            <TypographyText className="copyright">
              © 2025 Makura Systems. All rights reserved.
            </TypographyText>
          </div>
        </div>
      </div>

      {/* Right Side - Login Form */}
      <div className="login-form-container">
        <div className="login-form-content">
          <div className="form-header">
            <Title level={2}>Welcome Back</Title>
            <TypographyText type="secondary">
              Sign in to access your dashboard
            </TypographyText>
          </div>

          <Form
            name="login"
            onFinish={handleLogin}
            autoComplete="off"
            layout="vertical"
            size="large"
            className="login-form"
          >
            <Form.Item
              name="username"
              label="Username"
              rules={[{ required: true, message: 'Please enter your username' }]}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="Enter your username"
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="Password"
              rules={[{ required: true, message: 'Please enter your password' }]}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="Enter your password"
              />
            </Form.Item>

            <Form.Item>
              <Button
                type="primary"
                htmlType="submit"
                block
                size="large"
                loading={loading}
                className="login-button"
              >
                Sign In
              </Button>
            </Form.Item>
          </Form>

          <div className="form-footer">
            <TypographyText type="secondary" className="demo-creds">
              Demo: <strong>admin</strong> / <strong>admin123</strong>
            </TypographyText>
          </div>
        </div>
      </div>
    </div>
  )
}
