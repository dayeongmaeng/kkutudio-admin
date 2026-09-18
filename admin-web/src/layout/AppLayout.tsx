import { Outlet, useLocation, useNavigate, useParams } from 'react-router-dom'
import { useQuery } from '@tanstack/react-query'
import { Button, Layout, Menu, Select, Typography } from 'antd'
import { fetchApplications } from '../api/applications'
import { useAuth } from '../auth/AuthContext'

const { Header, Sider, Content } = Layout

const MENU_ITEMS = [
  { key: 'dashboard', label: '대시보드' },
  { key: 'members', label: '회원 관리' },
]
const MENU_KEYS = MENU_ITEMS.map((item) => item.key)

export default function AppLayout() {
  const { appCode } = useParams()
  const navigate = useNavigate()
  const location = useLocation()
  const { admin, logout } = useAuth()

  const { data: apps = [] } = useQuery({ queryKey: ['applications'], queryFn: fetchApplications })

  const selectedApp = appCode ?? apps[0]?.appCode
  const currentTab = location.pathname.split('/').pop()
  const selectedMenuKey = MENU_KEYS.includes(currentTab ?? '') ? (currentTab as string) : 'dashboard'

  return (
    <Layout style={{ minHeight: '100vh' }}>
      <Header style={{ display: 'flex', alignItems: 'center', gap: 16 }}>
        <Typography.Title level={4} style={{ color: '#fff', margin: 0 }}>
          꾸튜디오 Admin
        </Typography.Title>
        <Select
          value={selectedApp}
          onChange={(value) => navigate(`/apps/${value}/${selectedMenuKey}`)}
          options={apps.map((app) => ({ value: app.appCode, label: app.name }))}
          style={{ width: 160, marginLeft: 'auto' }}
        />
        <Typography.Text style={{ color: 'rgba(255,255,255,0.85)' }}>{admin?.name}</Typography.Text>
        <Button onClick={() => logout()}>로그아웃</Button>
      </Header>
      <Layout>
        <Sider width={220}>
          <Menu
            mode="inline"
            style={{ height: '100%' }}
            selectedKeys={[selectedMenuKey]}
            items={MENU_ITEMS}
            onClick={({ key }) => navigate(`/apps/${selectedApp}/${key}`)}
          />
        </Sider>
        <Content style={{ padding: 24 }}>
          <Outlet />
        </Content>
      </Layout>
    </Layout>
  )
}
