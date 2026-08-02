import { Route, Routes } from 'react-router-dom'
import PublicLayout from './layouts/PublicLayout.jsx'
import HomePage from './pages/HomePage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import PlaceholderPage from './pages/PlaceholderPage.jsx'
import './App.css'

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="tim-kiem" element={<PlaceholderPage title="Tìm truyện" />} />
        <Route path="dang-nhap" element={<PlaceholderPage title="Đăng nhập" />} />
        <Route path="dang-ky" element={<PlaceholderPage title="Đăng ký" />} />
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}
