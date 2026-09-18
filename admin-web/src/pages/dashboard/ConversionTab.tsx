import { useQuery } from '@tanstack/react-query'
import { Card, Col, Row, Statistic, Table, Typography } from 'antd'
import type { TableColumnsType } from 'antd'
import { fetchDashboardConversion, type CohortRetention, type DashboardUnit } from '../../api/statistics'
import { ApiError } from '../../api/client'

interface Props {
  appCode: string
  from: string
  to: string
  unit: DashboardUnit
}

function formatRate(rate: number | null) {
  return rate === null ? '-' : `${(rate * 100).toFixed(1)}%`
}

export default function ConversionTab({ appCode, from, to, unit }: Props) {
  const query = useQuery({
    queryKey: ['statistics', appCode, 'conversion', from, to, unit],
    queryFn: () => fetchDashboardConversion(appCode, { from, to, unit }),
    enabled: Boolean(appCode),
  })

  if (query.isError) {
    return <Typography.Paragraph type="danger">{(query.error as ApiError).message}</Typography.Paragraph>
  }

  const data = query.data

  const columns: TableColumnsType<CohortRetention> = [
    { title: '가입 주(월요일 시작)', dataIndex: 'cohortWeekStart' },
    { title: '코호트 크기', dataIndex: 'cohortSize' },
    { title: 'W1', render: (_, record) => formatRate(record.w1) },
    { title: 'W2', render: (_, record) => formatRate(record.w2) },
    { title: 'W4', render: (_, record) => formatRate(record.w4) },
    { title: 'W8', render: (_, record) => formatRate(record.w8) },
  ]

  return (
    <div>
      <Row gutter={16} style={{ marginBottom: 24 }}>
        <Col span={8}>
          <Card title="가입 → 첫 펫 등록" loading={query.isLoading}>
            <Statistic title="평균" value={data?.signupToFirstPet.avgHours.toFixed(1) ?? 0} suffix="시간" />
            <Statistic
              title="중앙값"
              value={data?.signupToFirstPet.medianHours.toFixed(1) ?? 0}
              suffix="시간"
              style={{ marginTop: 12 }}
            />
            <Typography.Text type="secondary">표본 {data?.signupToFirstPet.sampleCount ?? 0}건</Typography.Text>
          </Card>
        </Col>
        <Col span={8}>
          <Card title="펫 등록 → 첫 일지/사진" loading={query.isLoading}>
            <Statistic title="평균" value={data?.petToFirstRecord.avgHours.toFixed(1) ?? 0} suffix="시간" />
            <Statistic
              title="중앙값"
              value={data?.petToFirstRecord.medianHours.toFixed(1) ?? 0}
              suffix="시간"
              style={{ marginTop: 12 }}
            />
            <Typography.Text type="secondary">표본 {data?.petToFirstRecord.sampleCount ?? 0}건</Typography.Text>
          </Card>
        </Col>
      </Row>
      <Card title="코호트 리텐션 (가입 주 기준)">
        <Table<CohortRetention>
          rowKey="cohortWeekStart"
          columns={columns}
          dataSource={data?.retention ?? []}
          loading={query.isLoading}
          pagination={false}
        />
      </Card>
    </div>
  )
}
