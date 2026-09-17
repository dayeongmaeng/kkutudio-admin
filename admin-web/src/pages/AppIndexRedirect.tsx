import { Navigate } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Flex, Spin, Typography } from 'antd'
import { fetchApplications } from '../api/applications'

export default function AppIndexRedirect() {
  const { data: apps, isLoading } = useQuery({ queryKey: ['applications'], queryFn: fetchApplications })

  if (isLoading) {
    return (
      <Flex align="center" justify="center" style={{ minHeight: '50vh' }}>
        <Spin size="large" />
      </Flex>
    )
  }

  if (!apps || apps.length === 0) {
    return <Typography.Text>등록된 앱이 없습니다.</Typography.Text>
  }

  return <Navigate to={`/apps/${apps[0].appCode}/members`} replace />
}
