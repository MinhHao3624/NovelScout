import { apiRequest } from './client.js';

export function getPersonalizedRecommendations(limit = 12) {
  return apiRequest(`/public/recommendations/personalized?limit=${limit}`);
}

export function getSimilarNovels(slug, limit = 6) {
  return apiRequest(`/public/recommendations/similar/${slug}?limit=${limit}`);
}
