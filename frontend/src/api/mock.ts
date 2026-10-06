/**
 * 백엔드 없이 화면을 개발하기 위한 목 API (npm run dev:mock).
 * 실제 API와 같은 인터페이스를 가지며, 정원/중복 신청 등 도메인 규칙도 흉내 낸다.
 */
import { ApiError } from './client';
import type { Api } from './real';
import type {
  Category, Comment, CreateMeetingRequest, Me, MeetingDetail, MeetingStatus,
  MeetingSummary, UserSummary,
} from '../types';

const ME: Me = { id: 1, nickname: '민준', profileImageUrl: null, provider: 'KAKAO' };
const users: Record<number, UserSummary> = {
  1: ME,
  2: { id: 2, nickname: '서연', profileImageUrl: null },
  3: { id: 3, nickname: '도윤', profileImageUrl: null },
  4: { id: 4, nickname: '하은', profileImageUrl: null },
};

interface MockMeeting {
  id: number;
  title: string;
  description: string;
  category: Category;
  location: string;
  meetingAt: string;
  recruitDeadline: string;
  maxParticipants: number;
  others: number;           // 나를 제외한 참여자 수
  joined: boolean;
  bookmarked: boolean;
  hostId: number;
  canceled?: boolean;
}

function at(daysFromNow: number, hour: number, minute = 0): string {
  const d = new Date();
  d.setDate(d.getDate() + daysFromNow);
  d.setHours(hour, minute, 0, 0);
  const pad = (n: number) => String(n).padStart(2, '0');
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}:00`;
}

const meetings: MockMeeting[] = [
  { id: 1, title: '스프링 시큐리티 딥다이브 스터디', category: 'STUDY', location: '판교역 스터디카페', meetingAt: at(4, 14), recruitDeadline: at(3, 23, 59), maxParticipants: 6, others: 4, joined: false, bookmarked: true, hostId: 2,
    description: '공식 문서와 소스 코드를 같이 읽으면서 필터 체인부터 OAuth2 로그인까지 직접 구현해 봐요.\n매주 한 명씩 발표하고, 발표 자료는 깃허브에 모읍니다.' },
  { id: 2, title: '토요일 아침 탄천 러닝 5km', category: 'SPORTS', location: '탄천 정자교 아래', meetingAt: at(4, 7), recruitDeadline: at(3, 21), maxParticipants: 10, others: 10, joined: false, bookmarked: false, hostId: 3,
    description: '6분 30초 페이스로 천천히 뛰어요. 끝나고 근처에서 같이 아침 먹어요.' },
  { id: 3, title: '알고리즘 주 2회 모각코', category: 'STUDY', location: '온라인 디스코드', meetingAt: at(2, 20), recruitDeadline: at(1, 20), maxParticipants: 8, others: 2, joined: true, bookmarked: false, hostId: 1,
    description: '화요일, 목요일 저녁 8시에 모여서 각자 문제를 풀고 마지막 30분 동안 풀이를 공유해요.' },
  { id: 4, title: '보드게임 카페 번개', category: 'HOBBY', location: '서현역 보드게임카페', meetingAt: at(1, 19, 30), recruitDeadline: at(0, 23), maxParticipants: 5, others: 4, joined: false, bookmarked: false, hostId: 4,
    description: '처음 오시는 분도 괜찮아요. 규칙 쉬운 게임부터 시작합니다. 참가비는 각자 계산해요.' },
  { id: 5, title: '주니어 백엔드 개발자 네트워킹', category: 'SOCIAL', location: '강남역 공유오피스 라운지', meetingAt: at(9, 19), recruitDeadline: at(7, 18), maxParticipants: 20, others: 12, joined: false, bookmarked: false, hostId: 3,
    description: '1~3년 차 백엔드 개발자들이 모여 요즘 하는 일과 고민을 나눠요. 짧은 라이트닝 토크 3개가 있어요.' },
  { id: 6, title: '필름 카메라 출사', category: 'HOBBY', location: '성수동 서울숲 입구', meetingAt: at(6, 15), recruitDeadline: at(5, 12), maxParticipants: 6, others: 2, joined: false, bookmarked: true, hostId: 2,
    description: '필름 한 롤씩 들고 서울숲과 성수 골목을 걸어요. 카메라가 없으면 일회용 카메라도 좋아요.' },
  { id: 7, title: '배드민턴 초보 환영', category: 'SPORTS', location: '분당 탄천종합운동장 체육관', meetingAt: at(3, 10), recruitDeadline: at(2, 22), maxParticipants: 8, others: 7, joined: false, bookmarked: false, hostId: 4,
    description: '라켓은 여분이 있어요. 실내화만 챙겨 오세요. 2시간 동안 복식 위주로 쳐요.' },
  { id: 8, title: '영어 원서 읽기 모임', category: 'STUDY', location: '정자동 카페거리', meetingAt: at(5, 10), recruitDeadline: at(4, 20), maxParticipants: 5, others: 1, joined: false, bookmarked: false, hostId: 3,
    description: '한 달에 한 권, 매주 두 챕터씩 읽고 인상 깊은 문장을 나눠요.' },
];

const comments: Record<number, Comment[]> = {
  1: [
    { id: 1, content: '비전공자인데 따라갈 수 있을까요?', author: users[3], parentId: null, deleted: false, createdAt: at(-1, 21) },
    { id: 2, content: '네, 첫 주는 기초부터 같이 봐요!', author: users[2], parentId: 1, deleted: false, createdAt: at(-1, 22) },
  ],
};
let nextMeetingId = 100;
let nextCommentId = 100;

const LOGIN_KEY = 'mock:logged-in';
const isLoggedIn = () => sessionStorage.getItem(LOGIN_KEY) === 'true';

const delay = <T,>(value: T, ms = 250) => new Promise<T>((resolve) => setTimeout(() => resolve(value), ms));

function requireLogin() {
  if (!isLoggedIn()) throw new ApiError(401, 'UNAUTHORIZED', '로그인이 필요해요.');
}

function find(id: number): MockMeeting {
  const m = meetings.find((x) => x.id === id);
  if (!m) throw new ApiError(404, 'MEETING_NOT_FOUND', '모임을 찾을 수 없어요.');
  return m;
}

function current(m: MockMeeting) {
  return m.others + (m.joined ? 1 : 0);
}

function statusOf(m: MockMeeting): MeetingStatus {
  if (m.canceled) return 'CANCELED';
  if (current(m) >= m.maxParticipants || new Date(m.recruitDeadline) <= new Date()) return 'CLOSED';
  return 'RECRUITING';
}

function toSummary(m: MockMeeting): MeetingSummary {
  return {
    id: m.id, title: m.title, category: m.category, location: m.location,
    meetingAt: m.meetingAt, recruitDeadline: m.recruitDeadline,
    maxParticipants: m.maxParticipants, currentParticipants: current(m), status: statusOf(m),
  };
}

function toDetail(m: MockMeeting): MeetingDetail {
  const loggedIn = isLoggedIn();
  return {
    ...toSummary(m),
    description: m.description,
    host: users[m.hostId],
    participating: loggedIn && m.joined,
    bookmarked: loggedIn && m.bookmarked,
  };
}

export const mockApi: Api = {
  async restoreSession() {
    return delay(isLoggedIn() ? ME : null);
  },

  async startLogin() {
    sessionStorage.setItem(LOGIN_KEY, 'true');
    await delay(undefined, 400);
  },

  async logout() {
    sessionStorage.removeItem(LOGIN_KEY);
    await delay(undefined);
  },

  async searchMeetings({ category, recruitingOnly, keyword, page = 0 }) {
    const size = 12;
    const q = keyword?.trim().toLowerCase();
    const filtered = meetings
      .filter((m) => !m.canceled)
      .filter((m) => !category || m.category === category)
      .filter((m) => !recruitingOnly || statusOf(m) === 'RECRUITING')
      .filter((m) => !q || m.title.toLowerCase().includes(q) || m.location.toLowerCase().includes(q))
      .sort((a, b) => a.meetingAt.localeCompare(b.meetingAt));
    const totalPages = Math.max(1, Math.ceil(filtered.length / size));
    return delay({
      content: filtered.slice(page * size, (page + 1) * size).map(toSummary),
      number: page,
      totalPages,
      last: page >= totalPages - 1,
    });
  },

  async getMeeting(id) {
    return delay(toDetail(find(id)));
  },

  async createMeeting(body: CreateMeetingRequest) {
    requireLogin();
    const id = nextMeetingId++;
    meetings.push({ id, ...body, others: 0, joined: false, bookmarked: false, hostId: ME.id });
    return delay({ id });
  },

  async apply(id) {
    requireLogin();
    const m = find(id);
    if (m.hostId === ME.id) throw new ApiError(400, 'HOST_CANNOT_PARTICIPATE', '내가 만든 모임에는 신청할 수 없어요.');
    if (m.joined) throw new ApiError(409, 'ALREADY_PARTICIPATING', '이미 신청한 모임이에요.');
    if (new Date(m.recruitDeadline) <= new Date()) throw new ApiError(400, 'RECRUITMENT_CLOSED', '모집이 마감된 모임이에요.');
    if (current(m) >= m.maxParticipants) throw new ApiError(409, 'MEETING_FULL', '방금 정원이 모두 찼어요.');
    m.joined = true;
    await delay(undefined, 400);
  },

  async cancel(id) {
    requireLogin();
    const m = find(id);
    if (!m.joined) throw new ApiError(400, 'NOT_PARTICIPATING', '신청하지 않은 모임이에요.');
    m.joined = false;
    await delay(undefined);
  },

  async bookmark(id) {
    requireLogin();
    find(id).bookmarked = true;
    await delay(undefined, 150);
  },

  async unbookmark(id) {
    requireLogin();
    find(id).bookmarked = false;
    await delay(undefined, 150);
  },

  async getComments(id) {
    return delay(comments[id] ?? []);
  },

  async addComment(id, content) {
    requireLogin();
    const comment: Comment = {
      id: nextCommentId++, content, author: ME, parentId: null, deleted: false, createdAt: new Date().toISOString(),
    };
    comments[id] = [...(comments[id] ?? []), comment];
    return delay(comment);
  },

  async getMyMeetings(type) {
    requireLogin();
    const list = meetings.filter((m) =>
      type === 'HOSTED' ? m.hostId === ME.id : type === 'JOINED' ? m.joined : m.bookmarked,
    );
    return delay(list.map(toSummary));
  },
};
