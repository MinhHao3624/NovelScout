import { apiRequest } from './client.js'

function queryString(params) {
  const search = new URLSearchParams()
  Object.entries(params).forEach(([key, value]) => {
    if (value !== undefined && value !== null && value !== '') search.set(key, value)
  })
  return search.toString()
}

export const catalogApi = {
  categories: () => apiRequest('/public/catalog/categories'),
  featured: (limit = 4) => apiRequest(`/public/catalog/featured?limit=${limit}`),
  novels: (params) => apiRequest(`/public/catalog/novels?${queryString(params)}`),
  novel: (slug) => apiRequest(`/public/catalog/novels/${slug}`),
  chapters: (slug) => apiRequest(`/public/catalog/novels/${slug}/chapters`),
  chapter: (slug, chapterNumber) => apiRequest(`/public/catalog/novels/${slug}/chapters/${chapterNumber}`),
  incrementView: (slug) => apiRequest(`/public/catalog/novels/${slug}/view`, { method: 'POST' }),
}

