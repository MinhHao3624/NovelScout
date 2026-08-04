import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from './authContext.js'

export default function ProtectedRoute() {
  const { user, loading } = useAuth()
  const location = useLocation()

  if (loading) return <div className="route-loader" aria-label="Đang tải" />
  if (!user) return <Navigate to="/dang-nhap" replace state={{ from: location.pathname }} />
  return <Outlet />
}
