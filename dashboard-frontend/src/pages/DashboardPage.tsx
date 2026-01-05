import { Card, Row, Col, Statistic, Tag, Space, Typography } from 'antd'
import {
  ApiOutlined,
  UserOutlined,
  SafetyOutlined,
  NodeIndexOutlined,
  CheckCircleOutlined,
} from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import { PageHeader } from '../components/PageHeader'
import { routesApi, RouteDTO } from '../api/routesApi'
import { usersApi } from '../api/usersApi'
import { rolesApi } from '../api/rolesApi'
import { metricsApi } from '../api/metricsApi'
import { useAuthStore } from '../store/authStore'
import './DashboardPage.css'

const { Text: TypographyText } = Typography

export const DashboardPage = () => {
  const { user } = useAuthStore()

  const { data: routes = [] } = useQuery<RouteDTO[]>({
    queryKey: ['routes'],
    queryFn: () => routesApi.getAll(),
  })

  const { data: users = [] } = useQuery({
    queryKey: ['users'],
    queryFn: usersApi.getAll,
  })

  const { data: roles = [] } = useQuery({
    queryKey: ['roles'],
    queryFn: rolesApi.getAll,
  })

  // Mappings are not directly available, use routes count as proxy
  const mappingsCount = routes.length

  const { data: health } = useQuery({
    queryKey: ['health'],
    queryFn: metricsApi.getHealth,
    refetchInterval: 30000,
  })

  const activeRoutes = (routes as RouteDTO[]).filter((r: RouteDTO) => r.active).length
  const activeUsers = users.filter((u) => u.active).length

  return (
    <div className="dashboard-page">
      <PageHeader
        title={`Welcome back, ${user?.username}!`}
        subtitle="ISO 20022 Translation Dashboard"
        breadcrumbs={[{ title: 'Dashboard' }]}
      />

      <div className="dashboard-content">
        {/* Statistics Cards */}
        <Row gutter={[16, 16]} className="stats-row">
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card stat-card-routes">
              <Statistic
                title="Routes"
                value={routes.length}
                prefix={<ApiOutlined />}
                suffix={
                  <TypographyText type="secondary" style={{ fontSize: 14 }}>
                    ({activeRoutes} active)
                  </TypographyText>
                }
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card stat-card-mappings">
              <Statistic
                title="Field Mappings"
                value={mappingsCount}
                prefix={<NodeIndexOutlined />}
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card stat-card-users">
              <Statistic
                title="Users"
                value={users.length}
                prefix={<UserOutlined />}
                suffix={
                  <TypographyText type="secondary" style={{ fontSize: 14 }}>
                    ({activeUsers} active)
                  </TypographyText>
                }
              />
            </Card>
          </Col>
          <Col xs={24} sm={12} lg={6}>
            <Card className="stat-card stat-card-roles">
              <Statistic
                title="Roles"
                value={roles.length}
                prefix={<SafetyOutlined />}
              />
            </Card>
          </Col>
        </Row>

        {/* System Status */}
        <Row gutter={[16, 16]}>
          <Col xs={24} lg={12}>
            <Card title="System Health">
              <Space direction="vertical" style={{ width: '100%' }}>
                <div className="health-item">
                  <Space>
                    <CheckCircleOutlined
                      style={{
                        fontSize: 24,
                        color: health?.status === 'UP' ? '#52c41a' : '#ff4d4f',
                      }}
                    />
                    <div>
                      <TypographyText strong>Runtime Service</TypographyText>
                      <br />
                      <TypographyText type="secondary" style={{ fontSize: 12 }}>
                        ISO 20022 Translation Service
                      </TypographyText>
                      <br />
                      <Tag color={health?.status === 'UP' ? 'success' : 'error'}>
                        {health?.status || 'Unknown'}
                      </Tag>
                    </div>
                  </Space>
                </div>
              </Space>
            </Card>
          </Col>

          <Col xs={24} lg={12}>
            <Card title="Quick Actions">
              <Space direction="vertical" style={{ width: '100%' }}>
                <a href="/routes">→ Manage Routes</a>
                <a href="/mappings">→ Configure Field Mappings</a>
                <a href="/metrics">→ View Performance Metrics</a>
                <a href="/users">→ Manage Users</a>
              </Space>
            </Card>
          </Col>
        </Row>

        {/* Recent Activity */}
        <Card title="Recent Routes">
          <div className="recent-routes">
            {(routes as RouteDTO[]).slice(0, 5).map((route: RouteDTO) => (
              <div key={route.id} className="route-item">
                <Space>
                  <ApiOutlined style={{ color: '#1890ff' }} />
                  <div>
                    <TypographyText strong>{route.routeId}</TypographyText>
                    <br />
                    <TypographyText type="secondary" style={{ fontSize: 12 }}>
                      {route.name}
                    </TypographyText>
                  </div>
                </Space>
                <Space>
                  <Tag color={route.mode === 'ACTIVE' ? 'green' : 'blue'}>
                    {route.mode}
                  </Tag>
                  <Tag color={route.active ? 'success' : 'default'}>
                    {route.active ? 'Active' : 'Inactive'}
                  </Tag>
                </Space>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  )
}

