import type { Category, MeetingSummary } from '../types';

export const CATEGORY_LABEL: Record<Category, string> = {
  STUDY: '스터디',
  HOBBY: '취미',
  SPORTS: '운동',
  SOCIAL: '친목',
};

export const CATEGORIES = Object.keys(CATEGORY_LABEL) as Category[];

const dateTime = new Intl.DateTimeFormat('ko-KR', {
  month: 'long', day: 'numeric', weekday: 'short', hour: 'numeric', minute: '2-digit',
});

export function formatDateTime(iso: string): string {
  return dateTime.format(new Date(iso));
}

const HOUR = 60 * 60 * 1000;

export function remainingSeats(m: MeetingSummary): number {
  return Math.max(0, m.maxParticipants - m.currentParticipants);
}

export function isOpen(m: MeetingSummary): boolean {
  return m.status === 'RECRUITING' && remainingSeats(m) > 0 && new Date(m.recruitDeadline).getTime() > Date.now();
}

/** 남은 자리가 2개 이하이거나 마감까지 24시간이 안 남은 모집 중 모임 */
export function isClosingSoon(m: MeetingSummary): boolean {
  if (!isOpen(m)) return false;
  const left = new Date(m.recruitDeadline).getTime() - Date.now();
  return remainingSeats(m) <= 2 || left < 24 * HOUR;
}

export function deadlineLabel(iso: string): string {
  const left = new Date(iso).getTime() - Date.now();
  if (left <= 0) return '모집이 마감됐어요';
  if (left < HOUR) return '마감까지 1시간도 안 남았어요';
  if (left < 24 * HOUR) return `마감까지 ${Math.floor(left / HOUR)}시간`;
  return `마감까지 D-${Math.ceil(left / (24 * HOUR))}`;
}
