import { useCallback, useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import { api, errorMessage } from '../api';
import { useAuth } from '../auth/AuthContext';
import { CommentSection } from '../components/CommentSection';
import { SeatPanel } from '../components/SeatPanel';
import type { MeetingDetail } from '../types';
import { CATEGORY_LABEL, formatDateTime } from '../utils/format';

export function MeetingDetailPage() {
  const meetingId = Number(useParams().id);
  const { user } = useAuth();
  const { pathname } = useLocation();

  const [meeting, setMeeting] = useState<MeetingDetail | null>(null);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [actionError, setActionError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);
  const [newSeat, setNewSeat] = useState<number | null>(null);

  const reload = useCallback(async () => {
    const m = await api.getMeeting(meetingId);
    setMeeting(m);
    return m;
  }, [meetingId]);

  // 로그인 상태가 바뀌면 participating, bookmarked 값이 달라지므로 다시 불러온다
  useEffect(() => {
    setLoadError(null);
    reload().catch((e) => setLoadError(errorMessage(e)));
  }, [reload, user?.id]);

  /** 신청/취소/북마크 공통 처리: 중복 클릭 방지, 에러 표시, 실패 시 최신 현황으로 갱신 */
  async function runAction(action: () => Promise<void>) {
    setPending(true);
    setActionError(null);
    try {
      await action();
    } catch (e) {
      setActionError(errorMessage(e));
      reload().catch(() => undefined); // 실패 원인이 "방금 정원 초과"일 수 있음
    } finally {
      setPending(false);
    }
  }

  const handleApply = () =>
    runAction(async () => {
      await api.apply(meetingId);
      const updated = await reload();
      setNewSeat(updated.currentParticipants - 1);
    });

  const handleCancel = () => {
    if (!window.confirm('신청을 취소할까요? 취소하면 다른 사람이 자리를 가져갈 수 있어요.')) return;
    runAction(async () => {
      await api.cancel(meetingId);
      setNewSeat(null);
      await reload();
    });
  };

  const handleToggleBookmark = () =>
    runAction(async () => {
      if (!meeting) return;
      await (meeting.bookmarked ? api.unbookmark(meetingId) : api.bookmark(meetingId));
      setMeeting({ ...meeting, bookmarked: !meeting.bookmarked });
    });

  if (loadError) {
    return (
      <div className="empty">
        <p className="empty__title">{loadError}</p>
        <Link to="/" className="btn btn--secondary">목록으로 가기</Link>
      </div>
    );
  }
  if (!meeting) return <p className="muted">모임을 불러오는 중…</p>;

  return (
    <div className="detail">
      <div className="detail__main">
        <Link to="/" className="back">목록으로</Link>
        <p className="card__category">{CATEGORY_LABEL[meeting.category]}</p>
        <h1 className="page-title">{meeting.title}</h1>
        <p className="muted">모임장 {meeting.host.nickname}</p>

        <p className="description">{meeting.description}</p>

        <dl className="info">
          <dt>일시</dt>
          <dd>{formatDateTime(meeting.meetingAt)}</dd>
          <dt>장소</dt>
          <dd>{meeting.location}</dd>
          <dt>모집 마감</dt>
          <dd>{formatDateTime(meeting.recruitDeadline)}</dd>
        </dl>

        <CommentSection meetingId={meetingId} hostId={meeting.host.id} user={user} loginReturnPath={pathname} />
      </div>

      <aside className="detail__aside">
        <SeatPanel
          meeting={meeting}
          user={user}
          pending={pending}
          error={actionError}
          newSeat={newSeat}
          loginReturnPath={pathname}
          onApply={handleApply}
          onCancel={handleCancel}
          onToggleBookmark={handleToggleBookmark}
        />
      </aside>
    </div>
  );
}
