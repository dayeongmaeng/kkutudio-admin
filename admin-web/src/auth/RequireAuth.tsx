import type { ReactElement } from 'react'
import { Navigate } from 'react-router-dom'
import { Flex, Spin } from 'antd'
import { useAuth } from './AuthContext'

export default function RequireAuth({ children }: { children: ReactElement }) {
  const { admin, isLoading } = useAuth()

  if (isLoading) {
    return (
      <Flex align="center" justify="center" style={{ minHeight: '100vh' }}>
        <Spin size="large" />
      </Flex>
    )
  }

  if (!admin) {
    return <Navigate to="/login" replace />
  }

  return children
}
