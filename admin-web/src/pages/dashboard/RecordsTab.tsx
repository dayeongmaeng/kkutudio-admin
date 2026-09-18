import { useQuery } from '@tanstack/react-query'
import { Card, Col, Row, Statistic, Typography } from 'antd'
import {
  Area,
  AreaChart,
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  Line,
  LineChart,
  Pie,
  PieChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'
import { fetchDashboardRecords, type DashboardUnit } from '../../api/statistics'
import { ApiError } from '../../api/client'

const COLORS = ['#1677ff', '#52c41a', '#faad14']

interface Props {
  appCode: string
  from: string
  to: string
  unit: DashboardUnit
}

function toChartData(map: Record<string, number> | undefined) {
  return Object.entries(map ?? {}).map(([name, value]) => ({ name, value }))
}

export default function RecordsTab({ appCode, from, to, unit }: Props) {
  const query = useQuery({
    queryKey: ['statistics', appCode, 'records', from, to, unit],
    queryFn: () => fetchDashboardRecords(appCode, { from, to, unit }),
    enabled: Boolean(appCode),
  })

  if (query.isError) {
    return <Typography.Paragraph type="danger">{(query.error as ApiError).message}</Typography.Paragraph>
  }

  const data = query.data
  const recordTrend = (data?.photoTrend ?? []).map((point, idx) => ({
    bucketStart: point.bucketStart,
    photo: point.value,
    log: data?.logTrend[idx]?.value ?? 0,
  }))
  const engagementChartData = (data?.engagementTrend ?? []).map((point) => ({
    bucketStart: point.bucketStart,
    ratePercent: Number((point.rate * 100).toFixed(1)),
  }))
  const streakDistribution = toChartData(data?.streak.distribution)
  const featureCombination = toChartData(data?.featureCombination)

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card loading={query.isLoading}>
            <Statistic
              title="평균 연속 기록일(streak)"
              value={data ? data.streak.avgCurrentStreakDays.toFixed(1) : 0}
              suffix="일"
            />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={query.isLoading}>
            <Statistic title="최대 연속 기록일(streak)" value={data?.streak.maxCurrentStreakDays ?? 0} suffix="일" />
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card title="사진·일지 일별 건수" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={recordTrend}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="bucketStart" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="photo" name="사진" stroke="#1677ff" />
                <Line type="monotone" dataKey="log" name="일지" stroke="#52c41a" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="참여율 추이" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={engagementChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="bucketStart" />
                <YAxis unit="%" />
                <Tooltip />
                <Line type="monotone" dataKey="ratePercent" name="참여율" stroke="#faad14" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card title="누적 기록 수(조회 기간 내)" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <AreaChart data={data?.cumulativeTrend ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="bucketStart" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Area type="monotone" dataKey="value" stroke="#722ed1" fill="#722ed1" fillOpacity={0.2} />
              </AreaChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="streak 분포" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={streakDistribution}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#722ed1" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
      <Row gutter={16}>
        <Col span={12}>
          <Card title="기능 조합 비율" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={featureCombination} dataKey="value" nameKey="name" outerRadius={90} label>
                  {featureCombination.map((entry, index) => (
                    <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
