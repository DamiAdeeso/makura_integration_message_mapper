import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { ConfigProvider, theme as antdTheme } from 'antd'
import { ErrorBoundary } from './components/ErrorBoundary'
import { AuthProvider } from './components/AuthProvider'
import { ThemeProvider, useTheme } from './contexts/ThemeContext'
import { MainLayout } from './components/MainLayout'
import { LoginPage } from './pages/LoginPage'
import { DashboardPage } from './pages/DashboardPage'
import { RoutesPage } from './pages/RoutesPage'
import { UsersPage } from './pages/UsersPage'
import { RolesPage } from './pages/RolesPage'
import { MappingsPage } from './pages/MappingsPage'
import { MetricsPage } from './pages/MetricsPage'
import { SettingsPage } from './pages/SettingsPage'
import { DevelopersPage } from './pages/DevelopersPage'
import { ApiKeysPage } from './pages/ApiKeysPage'
import { DocumentationPage } from './pages/DocumentationPage'
import { CodeExamplesPage } from './pages/CodeExamplesPage'
import { COLORS } from './theme/colors'
import './App.css'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      refetchOnWindowFocus: false,
      retry: 1,
      staleTime: 30000,
    },
  },
})

// Inner component that uses theme context
const AppContent = () => {
  const { theme } = useTheme()

  return (
    <ConfigProvider
      theme={{
        algorithm: theme === 'dark' ? antdTheme.darkAlgorithm : antdTheme.defaultAlgorithm,
        token: {
          colorPrimary: COLORS.deepBlue,
          colorInfo: COLORS.mediumBlue,
          colorSuccess: COLORS.lightBlue,
          colorWarning: COLORS.warmOrange,
          colorLink: COLORS.mediumBlue,
          borderRadius: 6,
        },
      }}
    >
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <AuthProvider>
            <Routes>
              <Route path="/login" element={<LoginPage />} />
              <Route path="/" element={<MainLayout />}>
                <Route index element={<DashboardPage />} />
                <Route path="routes" element={<RoutesPage />} />
                <Route path="users" element={<UsersPage />} />
                <Route path="roles" element={<RolesPage />} />
                <Route path="mappings" element={<MappingsPage />} />
                <Route path="metrics" element={<MetricsPage />} />
                <Route path="developers" element={<DevelopersPage />} />
                <Route path="developers/api-keys" element={<ApiKeysPage />} />
                <Route path="developers/documentation" element={<DocumentationPage />} />
                <Route path="developers/examples" element={<CodeExamplesPage />} />
                <Route path="settings" element={<SettingsPage />} />
              </Route>
              <Route path="*" element={<Navigate to="/" replace />} />
            </Routes>
          </AuthProvider>
        </BrowserRouter>
      </QueryClientProvider>
    </ConfigProvider>
  )
}

function App() {
  return (
    <ErrorBoundary>
      <ThemeProvider>
        <AppContent />
      </ThemeProvider>
    </ErrorBoundary>
  )
}

export default App
