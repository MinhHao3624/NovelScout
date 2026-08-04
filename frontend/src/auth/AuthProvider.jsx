import { useCallback, useEffect, useMemo, useState } from 'react'
import { authApi } from '../api/auth.js'
import { AuthContext } from './authContext.js'

export default function AuthProvider({ children }) {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    authApi.me().then(setUser).catch((error) => {
      if (error.status !== 401) console.error(error)
    }).finally(() => setLoading(false))
  }, [])

  const login = useCallback(async (credentials) => {
    const currentUser = await authApi.login(credentials)
    setUser(currentUser)
    return currentUser
  }, [])

  const logout = useCallback(async () => {
    try { await authApi.logout() } finally { setUser(null) }
  }, [])

  const updateProfile = useCallback(async (data) => {
    const updatedUser = await authApi.updateProfile(data)
    setUser(updatedUser)
    return updatedUser
  }, [])

  const changePassword = useCallback(async (data) => {
    await authApi.changePassword(data)
    setUser(null)
  }, [])

  const value = useMemo(() => ({ user, loading, login, logout, updateProfile, changePassword }),
    [user, loading, login, logout, updateProfile, changePassword])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
