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
import { fetchDashboardPets, type DashboardUnit } from '../../api/statistics'
import { ApiError } from '../../api/client'

const COLORS = ['#1677ff', '#52c41a', '#faad14', '#f5222d']

interface Props {
  appCode: string
  from: string
  to: string
  unit: DashboardUnit
}

function toChartData(map: Record<string, number> | undefined) {
  return Object.entries(map ?? {}).map(([name, value]) => ({ name, value }))
}

export default function PetsTab({ appCode, from, to, unit }: Props) {
  const query = useQuery({
    queryKey: ['statistics', appCode, 'pets', from, to, unit],
    queryFn: () => fetchDashboardPets(appCode, { from, to, unit }),
    enabled: Boolean(appCode),
  })

  if (query.isError) {
    return <Typography.Paragraph type="danger">{(query.error as ApiError).message}</Typography.Paragraph>
  }

  const data = query.data
  const bySpecies = toChartData(data?.bySpecies)
  const distribution = toChartData(data?.petsPerMemberDistribution)

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={6}>
          <Card loading={query.isLoading}>
            <Statistic title="총 반려동물 수" value={data?.totalPets ?? 0} />
          </Card>
        </Col>
      </Row>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={24}>
          <Card title="신규 등록 추이" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <LineChart data={data?.newPetTrend ?? []}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="bucketStart" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Line type="monotone" dataKey="value" stroke="#1677ff" />
              </LineChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
      <Row gutter={16}>
        <Col span={12}>
          <Card title="종 비율" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <PieChart>
                <Pie data={bySpecies} dataKey="value" nameKey="name" outerRadius={90} label>
                  {bySpecies.map((entry, index) => (
                    <Cell key={entry.name} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip />
              </PieChart>
            </ResponsiveContainer>
          </Card>
        </Col>
        <Col span={12}>
          <Card title="회원당 펫 수 분포" loading={query.isLoading}>
            <ResponsiveContainer width="100%" height={260}>
              <BarChart data={distribution}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="name" />
                <YAxis allowDecimals={false} />
                <Tooltip />
                <Bar dataKey="value" fill="#52c41a" />
              </BarChart>
            </ResponsiveContainer>
          </Card>
        </Col>
      </Row>
    </div>
  )
}
