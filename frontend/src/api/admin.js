import { apiRequest } from './client.js';

export function getAdminStats() {
  return apiRequest('/admin/dashboard/stats');
}

export function getAdminNovels(query = '', page = 0, size = 20) {
  const params = new URLSearchParams({ page, size });
  if (query) params.append('query', query);
  return apiRequest(`/admin/novels?${params.toString()}`);
}

export function createAdminNovel(data) {
  return apiRequest('/admin/novels', {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateAdminNovel(id, data) {
  return apiRequest(`/admin/novels/${id}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export async function uploadAdminNovelCover(id, file) {
  const formData = new FormData();
  formData.append('file', file);

  const res = await fetch(`http://localhost:8080/api/admin/novels/${id}/cover`, {
    method: 'POST',
    credentials: 'include',
    body: formData,
  });

  if (!res.ok) {
    const errorData = await res.json().catch(() => ({}));
    throw new Error(errorData.message || 'Lỗi khi tải ảnh bìa');
  }

  return res.json();
}

export function deleteAdminNovel(id) {
  return apiRequest(`/admin/novels/${id}`, {
    method: 'DELETE',
  });
}

export function getAdminChapters(novelId) {
  return apiRequest(`/admin/novels/${novelId}/chapters`);
}

export function createAdminChapter(novelId, data) {
  return apiRequest(`/admin/novels/${novelId}/chapters`, {
    method: 'POST',
    body: JSON.stringify(data),
  });
}

export function updateAdminChapter(chapterId, data) {
  return apiRequest(`/admin/chapters/${chapterId}`, {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function deleteAdminChapter(chapterId) {
  return apiRequest(`/admin/chapters/${chapterId}`, {
    method: 'DELETE',
  });
}

/* Admin Recommendation Endpoints */
export function getAdminRecommendationConfig() {
  return apiRequest('/admin/recommendations/config');
}

export function updateAdminRecommendationConfig(data) {
  return apiRequest('/admin/recommendations/config', {
    method: 'PUT',
    body: JSON.stringify(data),
  });
}

export function getAdminRecommendationMetrics() {
  return apiRequest('/admin/recommendations/metrics');
}

export function recalculateRecommendationMatrix() {
  return apiRequest('/admin/recommendations/recalculate', {
    method: 'POST',
  });
}

export function simulateRecommendationForUser(userId, limit = 10) {
  return apiRequest(`/admin/recommendations/simulate?userId=${userId}&limit=${limit}`);
}
