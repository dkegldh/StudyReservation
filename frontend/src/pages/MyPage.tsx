import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errorMessage } from '../api';
import { useAuth } from '../auth/AuthContext';
import { MeetingCard } from '../components/MeetingCard';
import type { MeetingSummary, MyMeetingType } from '../types';

const TABS: { type: MyMeetingType; label: string; empty: string }[] = [
  { type: 'JOINED', label: '신청한 모임', empty: '아직 신청한 모임이 없어요. 목록에서 자리가 남은 모임을 찾아보세요.' },
  { type: 'HOSTED', label: '만든 모임', empty: '직접 만든 모임이 없어요. 원하는 모임을 열어 사람을 모아 보세요.' },
  { type: 'BOOKMARKED', label: '북마크', empty: '북마크한 모임이 없어요. 관심 있는 모임을 저장해 두면 여기서 볼 수 있어요.' },
];

export function MyPage() {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [tab, setTab] = useState<MyMeetingType>('JOINED');
  const [items, setItems] = useState<MeetingSummary[] | null>(null);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    setItems(null);
    setError(null);
    api.getMyMeetings(tab)
      .then((res) => !ignore && setItems(res))
      .catch((e) => !ignore && setError(errorMessage(e)));
    return () => {
      ignore = true;
    };
  }, [tab]);

  async function handleLogout() {
    await logout();
    navigate('/', { replace: true });
  }

  const current = TABS.find((t) => t.type === tab)!;

  return (
    <>
      <div className="my-head">
        <h1 className="page-title">{user?.nickname}님의 모임</h1>
        <button type="button" className="btn btn--ghost" onClick={handleLogout}>로그아웃</button>
      </div>

      <div className="chips" role="group" aria-label="모임 종류">
        {TABS.map((t) => (
          <button key={t.type} type="button" className="chip" aria-pressed={tab === t.type} onClick={() => setTab(t.type)}>
            {t.label}
          </button>
        ))}
      </div>

      {error && <p className="alert" role="alert">{error}</p>}
      {items === null && !error && <p className="muted">불러오는 중…</p>}
      {items?.length === 0 && <p className="muted empty-inline">{current.empty}</p>}
      {items && items.length > 0 && (
        <div className="grid">
          {items.map((m) => <MeetingCard key={m.id} meeting={m} />)}
        </div>
      )}
    </>
  );
}
