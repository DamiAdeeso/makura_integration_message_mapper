import { CodeExamplesTab } from '../components/developers/CodeExamplesTab'
import { PageHeader } from '../components/PageHeader'

export const CodeExamplesPage = () => {
  return (
    <div>
      <PageHeader
        title="Code Examples"
        subtitle="Ready-to-use code snippets in multiple programming languages"
        breadcrumbs={[
          { title: 'Developers', path: '/developers' },
          { title: 'Code Examples' },
        ]}
      />
      <div style={{ padding: '24px', background: 'var(--bg-secondary)' }}>
        <CodeExamplesTab />
      </div>
    </div>
  )
}



