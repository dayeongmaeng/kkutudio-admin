import { apiFetch } from './client'

export type DashboardUnit = 'DAY' | 'WEEK' | 'MONTH'

export interface DashboardPeriodParams {
  from: string
  to: string
  unit: DashboardUnit
}

export interface TimeSeriesPoint {
  bucketStart: string
  value: number
}

export interface EngagementPoint {
  bucketStart: string
  activeUnits: number
  eligibleUnits: number
  rate: number
}

export interface StreakSummary {
  avgCurrentStreakDays: number
  maxCurrentStreakDays: number
  distribution: Record<string, number>
}

export interface DurationStats {
  avgHours: number
  medianHours: number
  sampleCount: number
}

export interface CohortRetention {
  cohortWeekStart: string
  cohortSize: number
  w1: number | null
  w2: number | null
  w4: number | null
  w8: number | null
}

export interface DashboardOverview {
  totalMembers: number
  dau: number
  wau: number
  mau: number
  stickiness: number
  todayRecordCount: number
  activeUnitTrend: TimeSeriesPoint[]
}

export interface DashboardMembers {
  totalMembers: number
  withdrawnMembers: number
  signupTrend: TimeSeriesPoint[]
  withdrawalTrend: TimeSeriesPoint[]
  byProvider: Record<string, number>
  byStatus: Record<string, number>
}

export interface DashboardPets {
  totalPets: number
  newPetTrend: TimeSeriesPoint[]
  bySpecies: Record<string, number>
  petsPerMemberDistribution: Record<string, number>
}

export interface DashboardRecords {
  photoTrend: TimeSeriesPoint[]
  logTrend: TimeSeriesPoint[]
  activeUnitTrend: TimeSeriesPoint[]
  engagementTrend: EngagementPoint[]
  cumulativeTrend: TimeSeriesPoint[]
  streak: StreakSummary
  featureCombination: Record<string, number>
}

export interface DashboardConversion {
  signupToFirstPet: DurationStats
  petToFirstRecord: DurationStats
  retention: CohortRetention[]
}

function toSearchParams(params: DashboardPeriodParams) {
  const search = new URLSearchParams()
  search.set('from', params.from)
  search.set('to', params.to)
  search.set('unit', params.unit)
  return search
}

export function fetchDashboardOverview(appCode: string, params: DashboardPeriodParams) {
  return apiFetch<DashboardOverview>(`/apps/${appCode}/statistics/overview?${toSearchParams(params)}`)
}

export function fetchDashboardMembers(appCode: string, params: DashboardPeriodParams) {
  return apiFetch<DashboardMembers>(`/apps/${appCode}/statistics/members?${toSearchParams(params)}`)
}

export function fetchDashboardPets(appCode: string, params: DashboardPeriodParams) {
  return apiFetch<DashboardPets>(`/apps/${appCode}/statistics/pets?${toSearchParams(params)}`)
}

export function fetchDashboardRecords(appCode: string, params: DashboardPeriodParams) {
  return apiFetch<DashboardRecords>(`/apps/${appCode}/statistics/records?${toSearchParams(params)}`)
}

export function fetchDashboardConversion(appCode: string, params: DashboardPeriodParams) {
  return apiFetch<DashboardConversion>(`/apps/${appCode}/statistics/conversion?${toSearchParams(params)}`)
}
