import apiClient from './apiClient'

export interface MetricsDTO {
  routeId: string
  totalRequests: number
  successfulRequests: number
  failedRequests: number
  averageDuration: number
  minDuration: number
  maxDuration: number
  additionalMetrics?: Record<string, any>
}

export interface MetricsSummary {
  totalRoutes: number
  activeRoutes: number
  totalRequests: number
  successRate: number
  averageResponseTime: number
}

export interface HealthStatus {
  status: string
  [key: string]: any
}

export const metricsApi = {
  getAll: async (): Promise<Record<string, any>> => {
    const response = await apiClient.get('/metrics')
    return response.data
  },

  getForRoute: async (routeId: string): Promise<MetricsDTO> => {
    const response = await apiClient.get(`/metrics/route/${routeId}`)
    return response.data
  },

  getSummary: async (timeRange?: string): Promise<any> => {
    const response = await apiClient.get('/metrics/summary', {
      params: timeRange ? { timeRange } : undefined
    })
    return response.data
  },

  getHealth: async (): Promise<HealthStatus> => {
    const response = await apiClient.get('/metrics/health')
    return response.data
  },

  getPerformance: async (timeRange?: string): Promise<any[]> => {
    // Mock performance data for now - would need actual time-series endpoint
    const now = Date.now()
    const interval = timeRange === '1h' ? 5 * 60 * 1000 : 60 * 60 * 1000
    const points = timeRange === '1h' ? 12 : 24
    
    return Array.from({ length: points }, (_, i) => ({
      timestamp: new Date(now - (points - i) * interval).toLocaleTimeString(),
      requestCount: Math.floor(Math.random() * 100) + 50,
      avgResponseTime: Math.floor(Math.random() * 200) + 100,
    }))
  },
}

