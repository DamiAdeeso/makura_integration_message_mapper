import { ApiKeysTab } from '../components/developers/ApiKeysTab'
import { PageHeader } from '../components/PageHeader'

export const ApiKeysPage = () => {
  return (
    <div>
      <PageHeader
        title="API Keys"
        subtitle="Generate, manage, and monitor API keys for your integrations"
        breadcrumbs={[
          { title: 'Developers', path: '/developers' },
          { title: 'API Keys' },
        ]}
      />
      <div style={{ padding: '24px', background: 'var(--bg-secondary)' }}>
        <ApiKeysTab />
      </div>
    </div>
  )
}



