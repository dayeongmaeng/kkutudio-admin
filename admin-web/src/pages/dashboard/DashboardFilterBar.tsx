import { Segmented, Space, Typography } from 'antd'
import type { DashboardUnit } from '../../api/statistics'

const UNIT_OPTIONS: { label: string; value: DashboardUnit }[] = [
  { label: '일', value: 'DAY' },
  { label: '주', value: 'WEEK' },
  { label: '월', value: 'MONTH' },
]

interface Props {
  unit: DashboardUnit
  from: string
  to: string
  onUnitChange: (unit: DashboardUnit) => void
  onFromChange: (from: string) => void
  onToChange: (to: string) => void
}

export default function DashboardFilterBar({ unit, from, to, onUnitChange, onFromChange, onToChange }: Props) {
  return (
    <Space size="middle" style={{ marginBottom: 16 }} wrap>
      <Segmented options={UNIT_OPTIONS} value={unit} onChange={(value) => onUnitChange(value as DashboardUnit)} />
      <Space size="small">
        <Typography.Text type="secondary">기간</Typography.Text>
        <input type="date" value={from} max={to} onChange={(e) => onFromChange(e.target.value)} />
        <Typography.Text type="secondary">~</Typography.Text>
        <input type="date" value={to} min={from} onChange={(e) => onToChange(e.target.value)} />
      </Space>
    </Space>
  )
}
