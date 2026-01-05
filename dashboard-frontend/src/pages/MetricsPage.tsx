import { useState } from 'react'
import {
  Card,
  Row,
  Col,
  Statistic,
  Select,
  Space,
  Button,
  Spin,
  Typography,
} from 'antd'
import {
  ArrowUpOutlined,
  ArrowDownOutlined,
  ReloadOutlined,
  ThunderboltOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  ClockCircleOutlined,
} from '@ant-design/icons'
import { useQuery } from '@tanstack/react-query'
import {
  LineChart,
  Line,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
} from 'recharts'
import { PageHeader } from '../components/PageHeader'
import { metricsApi } from '../api/metricsApi'
import './MetricsPage.css'

const { Text: TypographyText } = Typography

const COLORS = ['#52c41a', '#ff4d4f', '#faad14', '#1890ff']

export const MetricsPage = () => {
  const [timeRange, setTimeRange] = useState('1h')

  const { data: metrics, isLoading, refetch } = useQuery({
    queryKey: ['metrics', timeRange],
    queryFn: () => metricsApi.getSummary(timeRange),
    refetchInterval: 30000, // Auto-refresh every 30s
  })

  const { data: health, isLoading: healthLoading } = useQuery({
    queryKey: ['health'],
    queryFn: metricsApi.getHealth,
    refetchInterval: 10000, // Check health every 10s
  })

  const { data: performanceData } = useQuery({
    queryKey: ['performance', timeRange],
    queryFn: () => metricsApi.getPerformance(timeRange),
    refetchInterval: 30000,
  })

  // Transform data for charts
  const statusData = metrics
    ? [
        { name: 'Success', value: metrics.successCount, color: '#52c41a' },
        { name: 'Failed', value: metrics.failedCount, color: '#ff4d4f' },
        { name: 'Pending', value: metrics.pendingCount, color: '#faad14' },
      ]
    : []

  const routeData = metrics?.topRoutes?.map((route) => ({
    name: route.routeId,
    requests: route.count,
    avgTime: route.avgResponseTime,
  })) || []

  return (
    <div className="metrics-page">
      <PageHeader
        title="Metrics & Analytics"
        subtitle="Real-time monitoring and performance analytics"
        breadcrumbs={[{ title: 'Home', path: '/' }, { title: 'Metrics' }]}
        extra={
          <Space>
            <Select
              value={timeRange}
              onChange={setTimeRange}
              style={{ width: 150 }}
              options={[
                { label: 'Last Hour', value: '1h' },
                { label: 'Last 24 Hours', value: '24h' },
                { label: 'Last 7 Days', value: '7d' },
                { label: 'Last 30 Days', value: '30d' },
              ]}
            />
            <Button icon={<ReloadOutlined />} onClick={() => refetch()}>
              Refresh
            </Button>
          </Space>
        }
      />

      <div className="metrics-content">
        {isLoading ? (
          <div style={{ textAlign: 'center', padding: 100 }}>
            <Spin size="large" />
          </div>
        ) : (
          <>
            {/* System Health Status */}
            <Row gutter={[16, 16]} className="health-row">
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="Runtime Service Status"
                    value={health?.status || 'Unknown'}
                    valueStyle={{
                      color: health?.status === 'UP' ? '#52c41a' : '#ff4d4f',
                    }}
                    prefix={
                      health?.status === 'UP' ? (
                        <CheckCircleOutlined />
                      ) : (
                        <CloseCircleOutlined />
                      )
                    }
                    loading={healthLoading}
                  />
                  <TypographyText type="secondary" style={{ fontSize: 12 }}>
                    ISO 20022 Translation Service
                  </TypographyText>
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="Total Requests"
                    value={metrics?.totalRequests || 0}
                    prefix={<ThunderboltOutlined />}
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="Success Rate"
                    value={metrics?.successRate || 0}
                    precision={2}
                    suffix="%"
                    valueStyle={{
                      color:
                        (metrics?.successRate || 0) > 95 ? '#52c41a' : '#faad14',
                    }}
                    prefix={
                      (metrics?.successRate || 0) > 95 ? (
                        <ArrowUpOutlined />
                      ) : (
                        <ArrowDownOutlined />
                      )
                    }
                  />
                </Card>
              </Col>
              <Col xs={24} sm={12} md={6}>
                <Card>
                  <Statistic
                    title="Avg Response Time"
                    value={metrics?.avgResponseTime || 0}
                    precision={0}
                    suffix="ms"
                    prefix={<ClockCircleOutlined />}
                  />
                </Card>
              </Col>
            </Row>

            {/* Charts Row */}
            <Row gutter={[16, 16]} className="charts-row">
              {/* Request Status Distribution */}
              <Col xs={24} lg={12}>
                <Card title="Request Status Distribution" className="chart-card">
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={statusData}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }) =>
                          `${name}: ${(percent * 100).toFixed(0)}%`
                        }
                        outerRadius={100}
                        fill="#8884d8"
                        dataKey="value"
                      >
                        {statusData.map((entry, index) => (
                          <Cell
                            key={`cell-${index}`}
                            fill={entry.color}
                          />
                        ))}
                      </Pie>
                      <Tooltip />
                    </PieChart>
                  </ResponsiveContainer>
                </Card>
              </Col>

              {/* Top Routes by Request Count */}
              <Col xs={24} lg={12}>
                <Card title="Top Routes by Volume" className="chart-card">
                  <ResponsiveContainer width="100%" height={300}>
                    <BarChart data={routeData}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="name" />
                      <YAxis />
                      <Tooltip />
                      <Legend />
                      <Bar dataKey="requests" fill="#1890ff" name="Requests" />
                    </BarChart>
                  </ResponsiveContainer>
                </Card>
              </Col>
            </Row>

            {/* Performance Over Time */}
            <Row gutter={[16, 16]}>
              <Col xs={24}>
                <Card title="Performance Over Time" className="chart-card">
                  <ResponsiveContainer width="100%" height={350}>
                    <LineChart data={performanceData || []}>
                      <CartesianGrid strokeDasharray="3 3" />
                      <XAxis dataKey="timestamp" />
                      <YAxis yAxisId="left" />
                      <YAxis yAxisId="right" orientation="right" />
                      <Tooltip />
                      <Legend />
                      <Line
                        yAxisId="left"
                        type="monotone"
                        dataKey="requestCount"
                        stroke="#1890ff"
                        name="Requests"
                        strokeWidth={2}
                      />
                      <Line
                        yAxisId="right"
                        type="monotone"
                        dataKey="avgResponseTime"
                        stroke="#52c41a"
                        name="Avg Response Time (ms)"
                        strokeWidth={2}
                      />
                    </LineChart>
                  </ResponsiveContainer>
                </Card>
              </Col>
            </Row>

            {/* Route Performance Table */}
            <Row gutter={[16, 16]}>
              <Col xs={24}>
                <Card title="Route Performance Details" className="chart-card">
                  <div className="route-performance-table">
                    <table>
                      <thead>
                        <tr>
                          <th>Route ID</th>
                          <th>Total Requests</th>
                          <th>Success Rate</th>
                          <th>Avg Response Time</th>
                          <th>P95 Response Time</th>
                        </tr>
                      </thead>
                      <tbody>
                        {metrics?.topRoutes?.map((route) => (
                          <tr key={route.routeId}>
                            <td>
                              <strong>{route.routeId}</strong>
                            </td>
                            <td>{route.count.toLocaleString()}</td>
                            <td>
                              <span
                                style={{
                                  color:
                                    route.successRate > 95
                                      ? '#52c41a'
                                      : '#faad14',
                                }}
                              >
                                {route.successRate.toFixed(2)}%
                              </span>
                            </td>
                            <td>{route.avgResponseTime.toFixed(0)} ms</td>
                            <td>{route.p95ResponseTime?.toFixed(0) || 'N/A'} ms</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </Card>
              </Col>
            </Row>
          </>
        )}
      </div>
    </div>
  )
}
