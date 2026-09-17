import { createContext, useContext, type ReactNode } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { fetchMe, login as apiLogin, logout as apiLogout, type AdminUserResponse } from '../api/auth'

interface AuthContextValue {
  admin?: AdminUserResponse
  isLoading: boolean
  login: (email: string, password: string) => Promise<AdminUserResponse>
  isLoggingIn: boolean
  loginError?: string
  logout: () => void
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const queryClient = useQueryClient()

  const meQuery = useQuery({
    queryKey: ['auth', 'me'],
    queryFn: fetchMe,
    retry: false,
  })

  const loginMutation = useMutation({
    mutationFn: ({ email, password }: { email: string; password: string }) => apiLogin(email, password),
    onSuccess: (admin) => {
      queryClient.setQueryData(['auth', 'me'], admin)
    },
  })

  const logoutMutation = useMutation({
    mutationFn: apiLogout,
    onSuccess: () => {
      queryClient.clear()
    },
  })

  const value: AuthContextValue = {
    admin: meQuery.data,
    isLoading: meQuery.isLoading,
    login: (email, password) => loginMutation.mutateAsync({ email, password }),
    isLoggingIn: loginMutation.isPending,
    loginError: loginMutation.error?.message,
    logout: () => logoutMutation.mutate(),
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export function useAuth() {
  const ctx = useContext(AuthContext)
  if (!ctx) {
    throw new Error('useAuth must be used within AuthProvider')
  }
  return ctx
}
