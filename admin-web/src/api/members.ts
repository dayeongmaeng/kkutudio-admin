import { apiFetch } from './client'

export interface MemberSummary {
  memberId: string
  nickname: string
  email: string
  status: string
  joinedAt: string
}

export interface MemberDetail extends MemberSummary {
  lastActiveAt?: string
}

export interface MemberPage {
  content: MemberSummary[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export function fetchMembers(
  appCode: string,
  params: { keyword?: string; page?: number; size?: number },
) {
  const search = new URLSearchParams()
  if (params.keyword) search.set('keyword', params.keyword)
  search.set('page', String(params.page ?? 0))
  search.set('size', String(params.size ?? 20))
  return apiFetch<MemberPage>(`/apps/${appCode}/members?${search.toString()}`)
}

export function fetchMemberDetail(appCode: string, memberId: string) {
  return apiFetch<MemberDetail>(`/apps/${appCode}/members/${memberId}`)
}

export function suspendMember(appCode: string, memberId: string, reason: string) {
  return apiFetch<void>(`/apps/${appCode}/members/${memberId}/suspend`, {
    method: 'POST',
    body: JSON.stringify({ reason }),
  })
}

export function forceLogoutMember(appCode: string, memberId: string) {
  return apiFetch<void>(`/apps/${appCode}/members/${memberId}/force-logout`, { method: 'POST' })
}
