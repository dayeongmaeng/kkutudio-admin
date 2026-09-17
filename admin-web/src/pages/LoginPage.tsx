import { Navigate } from 'react-router-dom'
import { Alert, Button, Card, Flex, Form, Input, Typography } from 'antd'
import { useAuth } from '../auth/AuthContext'

interface LoginFormValues {
  email: string
  password: string
}

export default function LoginPage() {
  const { admin, isLoading, login, isLoggingIn, loginError } = useAuth()

  if (!isLoading && admin) {
    return <Navigate to="/" replace />
  }

  const handleFinish = (values: LoginFormValues) => {
    login(values.email, values.password).catch(() => {
      // 에러 메시지는 loginError로 표시
    })
  }

  return (
    <Flex align="center" justify="center" style={{ minHeight: '100vh' }}>
      <Card style={{ width: 360 }}>
        <Typography.Title level={3} style={{ textAlign: 'center', marginTop: 0 }}>
          꾸튜디오 Admin
        </Typography.Title>
        <Form<LoginFormValues> layout="vertical" onFinish={handleFinish}>
          <Form.Item name="email" label="이메일" rules={[{ required: true, message: '이메일을 입력하세요' }]}>
            <Input autoComplete="username" autoFocus />
          </Form.Item>
          <Form.Item
            name="password"
            label="비밀번호"
            rules={[{ required: true, message: '비밀번호를 입력하세요' }]}
          >
            <Input.Password autoComplete="current-password" />
          </Form.Item>
          {loginError && (
            <Form.Item>
              <Alert type="error" message={loginError} showIcon />
            </Form.Item>
          )}
          <Form.Item style={{ marginBottom: 0 }}>
            <Button type="primary" htmlType="submit" block loading={isLoggingIn}>
              로그인
            </Button>
          </Form.Item>
        </Form>
      </Card>
    </Flex>
  )
}
