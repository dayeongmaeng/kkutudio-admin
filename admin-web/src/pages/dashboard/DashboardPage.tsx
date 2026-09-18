import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { Tabs, Typography } from 'antd'
import DashboardFilterBar from './DashboardFilterBar'
import OverviewTab from './OverviewTab'
import MembersTab from './MembersTab'
import PetsTab from './PetsTab'
import RecordsTab from './RecordsTab'
import ConversionTab from './ConversionTab'
import { defaultRangeFor } from './dateUtils'
import type { DashboardUnit } from '../../api/statistics'

export default function DashboardPage() {
  const { appCode = '' } = useParams()
  const [unit, setUnit] = useState<DashboardUnit>('DAY')
  const [range, setRange] = useState(() => defaultRangeFor('DAY'))

  const handleUnitChange = (nextUnit: DashboardUnit) => {
    setUnit(nextUnit)
    setRange(defaultRangeFor(nextUnit))
  }

  const tabProps = { appCode, from: range.from, to: range.to, unit }

  return (
    <div>
      <Typography.Title level={3}>대시보드</Typography.Title>
      <DashboardFilterBar
        unit={unit}
        from={range.from}
        to={range.to}
        onUnitChange={handleUnitChange}
        onFromChange={(from) => setRange((prev) => ({ ...prev, from }))}
        onToChange={(to) => setRange((prev) => ({ ...prev, to }))}
      />
      <Tabs
        items={[
          { key: 'overview', label: '개요', children: <OverviewTab {...tabProps} /> },
          { key: 'members', label: '회원', children: <MembersTab {...tabProps} /> },
          { key: 'pets', label: '반려동물', children: <PetsTab {...tabProps} /> },
          { key: 'records', label: '기록', children: <RecordsTab {...tabProps} /> },
          { key: 'conversion', label: '전환·리텐션', children: <ConversionTab {...tabProps} /> },
        ]}
      />
    </div>
  )
}
