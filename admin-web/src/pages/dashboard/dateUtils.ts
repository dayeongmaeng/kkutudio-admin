import type { DashboardUnit } from '../../api/statistics'

export function toIsoDate(date: Date): string {
  return date.toISOString().slice(0, 10)
}

function addDays(date: Date, days: number): Date {
  const d = new Date(date)
  d.setDate(d.getDate() + days)
  return d
}

function addMonths(date: Date, months: number): Date {
  const d = new Date(date)
  d.setMonth(d.getMonth() + months)
  return d
}

/** unit 변경 시 기본 조회 기간: 일 단위는 최근 30일, 주 단위는 최근 12주, 월 단위는 최근 12개월. */
export function defaultRangeFor(unit: DashboardUnit): { from: string; to: string } {
  const today = new Date()
  const to = toIsoDate(today)
  switch (unit) {
    case 'DAY':
      return { from: toIsoDate(addDays(today, -29)), to }
    case 'WEEK':
      return { from: toIsoDate(addDays(today, -83)), to }
    case 'MONTH':
      return { from: toIsoDate(addMonths(today, -11)), to }
    default:
      return { from: toIsoDate(addDays(today, -29)), to }
  }
}
