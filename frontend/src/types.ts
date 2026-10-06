export type Category = 'STUDY' | 'HOBBY' | 'SPORTS' | 'SOCIAL';
export type MeetingStatus = 'RECRUITING' | 'CLOSED' | 'CANCELED';
export type Provider = 'KAKAO' | 'GOOGLE';
export type MyMeetingType = 'HOSTED' | 'JOINED' | 'BOOKMARKED';

export interface UserSummary {
  id: number;
  nickname: string;
  profileImageUrl: string | null;
}

export interface Me extends UserSummary {
  provider: Provider;
}

export interface MeetingSummary {
  id: number;
  title: string;
  category: Category;
  location: string;
  meetingAt: string;        // ISO LocalDateTime, 예: 2026-10-03T14:00:00
  recruitDeadline: string;
  maxParticipants: number;
  currentParticipants: number;
  status: MeetingStatus;
}

export interface MeetingDetail extends MeetingSummary {
  description: string;
  host: UserSummary;
  participating: boolean;   // 로그인 사용자 기준
  bookmarked: boolean;
}

export interface Comment {
  id: number;
  content: string;
  author: UserSummary;
  parentId: number | null;
  deleted: boolean;
  createdAt: string;
}

/** Spring Data Page 응답 중 사용하는 필드 */
export interface Page<T> {
  content: T[];
  number: number;
  totalPages: number;
  last: boolean;
}

export interface MeetingSearch {
  category?: Category;
  recruitingOnly?: boolean;
  keyword?: string;
  page?: number;
}

export interface CreateMeetingRequest {
  title: string;
  description: string;
  category: Category;
  maxParticipants: number;
  location: string;
  meetingAt: string;
  recruitDeadline: string;
}
