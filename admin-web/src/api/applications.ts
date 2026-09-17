import { apiFetch } from './client'

export interface ManagedAppResponse {
  id: number
  appCode: string
  name: string
  description?: string
  iconUrl?: string
  status: 'DEVELOPMENT' | 'OPERATING' | 'SUSPENDED' | 'CLOSED'
}

export function fetchApplications() {
  return apiFetch<ManagedAppResponse[]>('/applications')
}
