import { Table, Card, Typography, Tag } from 'antd'

const { Title } = Typography

export default function AuditLogPage() {
  // Mock data
  const auditLogs = [
    {
      key: '1',
      timestamp: '2025-12-20 10:30:45',
      username: 'admin',
      action: 'CREATE_ROUTE',
      resourceType: 'Route',
      resourceId: 'SYSTEM_TO_NIP',
      details: 'Created new route configuration',
    },
    {
      key: '2',
      timestamp: '2025-12-20 10:25:12',
      username: 'operator1',
      action: 'UPDATE_ROUTE',
      resourceType: 'Route',
      resourceId: 'SYSTEM_TO_NIP_PASSIVE',
      details: 'Updated route mappings',
    },
    {
      key: '3',
      timestamp: '2025-12-20 10:20:33',
      username: 'admin',
      action: 'CREATE_USER',
      resourceType: 'User',
      resourceId: 'operator1',
      details: 'Created new operator user',
    },
  ]

  const columns = [
    {
      title: 'Timestamp',
      dataIndex: 'timestamp',
      key: 'timestamp',
    },
    {
      title: 'User',
      dataIndex: 'username',
      key: 'username',
    },
    {
      title: 'Action',
      dataIndex: 'action',
      key: 'action',
      render: (action: string) => <Tag color="blue">{action}</Tag>,
    },
    {
      title: 'Resource',
      key: 'resource',
      render: (_: any, record: any) => (
        <>
          {record.resourceType}: {record.resourceId}
        </>
      ),
    },
    {
      title: 'Details',
      dataIndex: 'details',
      key: 'details',
    },
  ]

  return (
    <div>
      <Title level={2} style={{ marginBottom: 24 }}>
        Audit Logs
      </Title>

      <Card>
        <Table
          columns={columns}
          dataSource={auditLogs}
          pagination={{ pageSize: 20 }}
        />
      </Card>
    </div>
  )
}




