import { apiRequest } from './client.js'

export const interactionApi = {
  getStatus: (slug) => apiRequest(`/public/interactions/novels/${slug}/status`),
  getRatings: (slug) => apiRequest(`/public/interactions/novels/${slug}/ratings`),
  toggleFavorite: (slug) => apiRequest(`/interactions/novels/${slug}/favorite`, { method: 'POST' }),
  rateNovel: (slug, score, reviewText) => apiRequest(`/interactions/novels/${slug}/rate`, {
    method: 'POST',
    body: JSON.stringify({ score, reviewText })
  }),
  getMyFavorites: () => apiRequest('/interactions/my-favorites'),
  
  // Comments API
  getNovelComments: (novelId) => apiRequest(`/public/novels/${novelId}/comments`),
  getChapterComments: (chapterId) => apiRequest(`/public/chapters/${chapterId}/comments`),
  postComment: (novelId, data) => apiRequest(`/novels/${novelId}/comments`, { method: 'POST', body: JSON.stringify(data) }),
  deleteComment: (commentId) => apiRequest(`/comments/${commentId}`, { method: 'DELETE' })
}
