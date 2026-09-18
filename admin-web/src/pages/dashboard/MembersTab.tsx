import { useQuery } from '@tanstack/react-query'
import { Card, Col, Row, Statistic, Typography } from 'antd'
import {
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
import { fetchDashboardMembers, type DashboardUnit } from '../../api/statistics'
import { ApiError } from '../../api/client'

const COLORS = ['#1677ff', '#52c41a', '#faad14', '#f5222d', '#722ed1']

interface Props {
  appCode: string
  from: string
  to: string
  unit: DashboardUnit
}

function toChartData(map: Record<string, number> | undefined) {
  return Object.entries(map ?? {}).map(([name, value]) => ({ name, value }))
}

export default function MembersTab({ appCode, from, to, unit }: Props) {
  const query = useQuery({
    queryKey: ['statistics', appCode, 'members', from, to, unit],
    queryFn: () => fetchDashboardMembers(appCode, { from, to, unit }),
    enabled: Boolean(appCode),
  })

  if (query.isError) {
    return <Typography.Paragraph type="danger">{(query.error as ApiError).message}</Typography.Paragraph>
  }

  const data = query.data
  const byProvider = toChartData(data?.byProvider)
  const byStatus = toChartData(data?.byStatus)

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card loading={query.isLoading}>
            <Statistic title="총 회원" value={data?.totalMembers ?? 0} />
          </Card>
        </Col>
        <Col span={6}>
          <Card loading={query.isLoading}>
            <Statistic title="탈퇴 회원" value={data?.withdrawnMembers ?? 0} />
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={12}>
          <Card title="가입 추이" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={data?.signupTrend ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="bucketStart" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="value" stroke="#1677ff" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="탈퇴 추이" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={data?.withdrawalTrend ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="bucketStart" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="value" stroke="#f5222d" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
      <Row gutter={16}>
        <Col span={12}>
          <Card title="가입경로" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={byProvider} dataKey="value" nameKey="name" outerRadius={90} label>
                  {byProvider.map((entry, index) => (
                    <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="상태별" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={byStatus}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#1677ff" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
