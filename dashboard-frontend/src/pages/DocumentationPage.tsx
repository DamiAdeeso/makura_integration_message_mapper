import { DocumentationTab } from '../components/developers/DocumentationTab'
import { PageHeader } from '../components/PageHeader'

export const DocumentationPage = () => {
  return (
    <div>
      <PageHeader
        title="Documentation"
        subtitle="API reference, guides, and interactive Swagger documentation"
        breadcrumbs={[
          { title: 'Developers', path: '/developers' },
          { title: 'Documentation' },
        ]}
      />
      <div style={{ padding: '24px', background: 'var(--bg-secondary)' }}>
        <DocumentationTab />
      </div>
    </div>
  )
}



