import { useQuery } from '@tanstack/react-query'
import { Card, Col, Row, Statistic, Typography } from 'antd'
import { Area, AreaChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from 'recharts'
import { fetchDashboardOverview, type DashboardUnit } from '../../api/statistics'
import { ApiError } from '../../api/client'

interface Props {
  appCode: string
  from: string
  to: string
  unit: DashboardUnit
}

export default function OverviewTab({ appCode, from, to, unit }: Props) {
  const query = useQuery({
    queryKey: ['statistics', appCode, 'overview', from, to, unit],
    queryFn: () => fetchDashboardOverview(appCode, { from, to, unit }),
    enabled: Boolean(appCode),
  })

  if (query.isError) {
    return <Typography.Paragraph type="danger">{(query.error as ApiError).message}</Typography.Paragraph>
  }

  const data = query.data

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={4}>
          <Card loading={query.isLoading}>
            <Statistic title="총 회원" value={data?.totalMembers ?? 0} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={query.isLoading}>
            <Statistic title="DAU" value={data?.dau ?? 0} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={query.isLoading}>
            <Statistic title="WAU" value={data?.wau ?? 0} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={query.isLoading}>
            <Statistic title="MAU" value={data?.mau ?? 0} />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={query.isLoading}>
            <Statistic title="고착도(DAU/MAU)" value={data ? (data.stickiness * 100).toFixed(1) : 0} suffix="%" />
          </Card>
        </Col>
        <Col span={4}>
          <Card loading={query.isLoading}>
            <Statistic title="오늘 기록 수" value={data?.todayRecordCount ?? 0} />
          </Card>
        </Col>
      </Row>
      <Card title="활성 단위 추이" loading={query.isLoading}>
        <ResponsiveContainer width="100%" height={300}>
          <AreaChart data={data?.activeUnitTrend ?? []}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="bucketStart" />
            <YAxis allowDecimals={false} />
            <Tooltip />
            <Area type="monotone" dataKey="value" stroke="#1677ff" fill="#1677ff" fillOpacity={0.2} />
          </AreaChart>
        </ResponsiveContainer>
      </Card>
    </div>
  )
}
