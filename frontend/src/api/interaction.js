import { apiRequest } from './client.js'

export const interactionApi = {
  getStatus: (slug) => apiRequest(`/public/interactions/novels/${slug}/status`),
  getRatings: (slug) => apiRequest(`/public/interactions/novels/${slug}/ratings`),
  toggleFavorite: (slug) => apiRequest(`/interactions/novels/${slug}/favorite`, { method: 'POST' }),
  rateNovel: (slug, score, reviewText) => apiRequest(`/interactions/novels/${slug}/rate`, {
    method: 'POST',
    body: JSON.stringify({ score, reviewText })
  }),
  getMyFavorites: () => apiRequest('/interactions/my-favorites')
}
