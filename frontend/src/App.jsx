import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './auth/ProtectedRoute.jsx'
import PublicLayout from './layouts/PublicLayout.jsx'
import HomePage from './pages/HomePage.jsx'
import SearchPage from './pages/SearchPage.jsx'
import ChapterReaderPage from './pages/ChapterReaderPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import NovelDetailPage from './pages/NovelDetailPage.jsx'
import PlaceholderPage from './pages/PlaceholderPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'
import './App.css'

export default function App() {
  return (
    <Routes>
      <Route element={<PublicLayout />}>
        <Route index element={<HomePage />} />
        <Route path="tim-kiem" element={<SearchPage />} />
        <Route path="truyen/:slug" element={<NovelDetailPage />} />
        <Route path="truyen/:slug/chuong/:chapterNumber" element={<ChapterReaderPage />} />
        <Route path="truyen/:slug/:chapterPath" element={<ChapterReaderPage />} />
        <Route path="dang-nhap" element={<LoginPage />} />

        <Route path="dang-ky" element={<RegisterPage />} />
        <Route element={<ProtectedRoute />}>
          <Route path="ho-so" element={<ProfilePage />} />
          <Route path="tu-sach" element={<PlaceholderPage title="Tủ sách" />} />
          <Route path="goi-y" element={<PlaceholderPage title="Gợi ý cho bạn" />} />
        </Route>
        <Route path="*" element={<NotFoundPage />} />
      </Route>
    </Routes>
  )
}

