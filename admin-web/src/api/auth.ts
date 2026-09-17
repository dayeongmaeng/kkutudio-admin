import { apiFetch } from './client'

export interface AdminUserResponse {
  id: number
  email: string
  name: string
}

export function login(email: string, password: string) {
  return apiFetch<AdminUserResponse>('/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password }),
  })
}

export function logout() {
  return apiFetch<void>('/auth/logout', { method: 'POST' })
}

export function fetchMe() {
  return apiFetch<AdminUserResponse>('/auth/me')
}
