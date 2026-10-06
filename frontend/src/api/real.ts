import { request, reissue, tokenStore } from './client';
import type {
  Comment, CreateMeetingRequest, Me, MeetingDetail, MeetingSearch,
  MeetingSummary, MyMeetingType, Page, Provider,
} from '../types';

const PAGE_SIZE = 12;

export const realApi = {
  /** 새로고침 등으로 Access Token이 없을 때 쿠키로 로그인 상태 복원 */
  async restoreSession(): Promise<Me | null> {
    if (!tokenStore.get() && !(await reissue())) return null;
    try {
      return await request<Me>('/api/v1/users/me');
    } catch {
      return null;
    }
  },

  /** 소셜 로그인 시작: Spring Security의 OAuth2 엔드포인트로 이동 */
  async startLogin(provider: Provider): Promise<void> {
    window.location.href = `/oauth2/authorization/${provider.toLowerCase()}`;
  },

  async logout(): Promise<void> {
    try {
      await request<void>('/api/v1/auth/logout', { method: 'POST' });
    } finally {
      tokenStore.set(null);
    }
  },

  searchMeetings(search: MeetingSearch): Promise<Page<MeetingSummary>> {
    const query = new URLSearchParams();
    if (search.category) query.set('category', search.category);
    if (search.recruitingOnly) query.set('status', 'RECRUITING');
    if (search.keyword) query.set('keyword', search.keyword);
    query.set('page', String(search.page ?? 0));
    query.set('size', String(PAGE_SIZE));
    return request(`/api/v1/meetings?${query}`);
  },

  getMeeting: (id: number) => request<MeetingDetail>(`/api/v1/meetings/${id}`),

  createMeeting: (body: CreateMeetingRequest) =>
    request<{ id: number }>('/api/v1/meetings', { method: 'POST', body: JSON.stringify(body) }),

  apply: (id: number) => request<void>(`/api/v1/meetings/${id}/participations`, { method: 'POST' }),
  cancel: (id: number) => request<void>(`/api/v1/meetings/${id}/participations`, { method: 'DELETE' }),

  bookmark: (id: number) => request<void>(`/api/v1/meetings/${id}/bookmarks`, { method: 'POST' }),
  unbookmark: (id: number) => request<void>(`/api/v1/meetings/${id}/bookmarks`, { method: 'DELETE' }),

  getComments: (id: number) => request<Comment[]>(`/api/v1/meetings/${id}/comments`),
  addComment: (id: number, content: string) =>
    request<Comment>(`/api/v1/meetings/${id}/comments`, { method: 'POST', body: JSON.stringify({ content }) }),

  getMyMeetings: (type: MyMeetingType) =>
    request<MeetingSummary[]>(`/api/v1/users/me/meetings?type=${type}`),
};

export type Api = typeof realApi;
