import { useState } from 'react'
import { useParams } from 'react-router-dom'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Button, Drawer, Input, Modal, Popconfirm, Space, Table, Typography, message } from 'antd'
import type { TableColumnsType } from 'antd'
import {
  fetchMemberDetail,
  fetchMembers,
  forceLogoutMember,
  suspendMember,
  type MemberSummary,
} from '../api/members'
import { ApiError } from '../api/client'

const PAGE_SIZE = 20

export default function MembersPage() {
  const { appCode = '' } = useParams()
  const queryClient = useQueryClient()
  const [keyword, setKeyword] = useState('')
  const [page, setPage] = useState(0)
  const [detailMemberId, setDetailMemberId] = useState<string | null>(null)
  const [suspendTarget, setSuspendTarget] = useState<string | null>(null)
  const [suspendReason, setSuspendReason] = useState('')

  const membersQuery = useQuery({
    queryKey: ['members', appCode, keyword, page],
    queryFn: () => fetchMembers(appCode, { keyword: keyword || undefined, page, size: PAGE_SIZE }),
    enabled: Boolean(appCode),
  })

  const detailQuery = useQuery({
    queryKey: ['member', appCode, detailMemberId],
    queryFn: () => fetchMemberDetail(appCode, detailMemberId as string),
    enabled: Boolean(detailMemberId),
  })

  const suspendMutation = useMutation({
    mutationFn: (params: { memberId: string; reason: string }) =>
      suspendMember(appCode, params.memberId, params.reason),
    onSuccess: () => {
      message.success('회원을 정지했습니다.')
      setSuspendTarget(null)
      setSuspendReason('')
      queryClient.invalidateQueries({ queryKey: ['members', appCode] })
    },
    onError: (error: ApiError) => message.error(error.message),
  })

  const forceLogoutMutation = useMutation({
    mutationFn: (memberId: string) => forceLogoutMember(appCode, memberId),
    onSuccess: () => message.success('강제 로그아웃 처리했습니다.'),
    onError: (error: ApiError) => message.error(error.message),
  })

  const columns: TableColumnsType<MemberSummary> = [
    { title: '회원 ID', dataIndex: 'memberId' },
    { title: '닉네임', dataIndex: 'nickname' },
    { title: '이메일', dataIndex: 'email' },
    { title: '상태', dataIndex: 'status' },
    { title: '가입일', dataIndex: 'joinedAt' },
    {
      title: '작업',
      render: (_, record) => (
        <Space>
          <Button size="small" onClick={() => setDetailMemberId(record.memberId)}>
            상세
          </Button>
          <Button size="small" danger onClick={() => setSuspendTarget(record.memberId)}>
            정지
          </Button>
          <Popconfirm
            title="이 회원을 강제 로그아웃할까요?"
            onConfirm={() => forceLogoutMutation.mutate(record.memberId)}
          >
            <Button size="small">강제 로그아웃</Button>
          </Popconfirm>
        </Space>
      ),
    },
  ]

  return (
    <div>
      <Typography.Title level={3}>회원 관리</Typography.Title>
      <Input.Search
        placeholder="닉네임, 이메일 검색"
        style={{ width: 320, marginBottom: 16 }}
        allowClear
        onSearch={(value) => {
          setKeyword(value)
          setPage(0)
        }}
      />
      {membersQuery.isError && (
        <Typography.Paragraph type="danger">{(membersQuery.error as ApiError).message}</Typography.Paragraph>
      )}
      <Table<MemberSummary>
        rowKey="memberId"
        columns={columns}
        dataSource={membersQuery.data?.content ?? []}
        loading={membersQuery.isLoading}
        pagination={{
          current: page + 1,
          pageSize: PAGE_SIZE,
          total: membersQuery.data?.totalElements ?? 0,
          onChange: (nextPage) => setPage(nextPage - 1),
        }}
      />

      <Drawer title="회원 상세" open={Boolean(detailMemberId)} onClose={() => setDetailMemberId(null)}>
        {detailQuery.isLoading && <Typography.Text>불러오는 중...</Typography.Text>}
        {detailQuery.data && (
          <Space direction="vertical">
            <Typography.Text>회원 ID: {detailQuery.data.memberId}</Typography.Text>
            <Typography.Text>닉네임: {detailQuery.data.nickname}</Typography.Text>
            <Typography.Text>이메일: {detailQuery.data.email}</Typography.Text>
            <Typography.Text>상태: {detailQuery.data.status}</Typography.Text>
            <Typography.Text>가입일: {detailQuery.data.joinedAt}</Typography.Text>
            <Typography.Text>최근 활동: {detailQuery.data.lastActiveAt ?? '-'}</Typography.Text>
          </Space>
        )}
      </Drawer>

      <Modal
        title="회원 정지"
        open={Boolean(suspendTarget)}
        onCancel={() => setSuspendTarget(null)}
        onOk={() => suspendTarget && suspendMutation.mutate({ memberId: suspendTarget, reason: suspendReason })}
        confirmLoading={suspendMutation.isPending}
        okButtonProps={{ disabled: !suspendReason.trim() }}
      >
        <Input.TextArea
          placeholder="정지 사유를 입력하세요"
          value={suspendReason}
          onChange={(e) => setSuspendReason(e.target.value)}
          rows={4}
        />
      </Modal>
    </div>
  )
}
