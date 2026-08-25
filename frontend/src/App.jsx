import { Navigate, Route, Routes } from 'react-router-dom'
import ProtectedRoute from './auth/ProtectedRoute.jsx'
import AdminProtectedRoute from './auth/AdminProtectedRoute.jsx'
import PublicLayout from './layouts/PublicLayout.jsx'
import HomePage from './pages/HomePage.jsx'
import SearchPage from './pages/SearchPage.jsx'
import ChapterReaderPage from './pages/ChapterReaderPage.jsx'
import BookshelfPage from './pages/BookshelfPage.jsx'
import LoginPage from './pages/LoginPage.jsx'
import NotFoundPage from './pages/NotFoundPage.jsx'
import NovelDetailPage from './pages/NovelDetailPage.jsx'
import RecommendationPage from './pages/RecommendationPage.jsx'
import ProfilePage from './pages/ProfilePage.jsx'
import RegisterPage from './pages/RegisterPage.jsx'

// Admin Components
import AdminLayout from './admin/AdminLayout.jsx'
import AdminDashboardPage from './admin/AdminDashboardPage.jsx'
import AdminNovelListPage from './admin/AdminNovelListPage.jsx'
import AdminChapterListPage from './admin/AdminChapterListPage.jsx'

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
          <Route path="tu-sach" element={<BookshelfPage />} />
          <Route path="goi-y" element={<RecommendationPage />} />
        </Route>

        <Route path="*" element={<NotFoundPage />} />
      </Route>

      {/* Admin Dashboard Routes */}
      <Route element={<AdminProtectedRoute />}>
        <Route path="admin" element={<AdminLayout />}>
          <Route index element={<AdminDashboardPage />} />
          <Route path="novels" element={<AdminNovelListPage />} />
          <Route path="novels/:novelId/chapters" element={<AdminChapterListPage />} />
        </Route>
      </Route>
    </Routes>
  )
}
