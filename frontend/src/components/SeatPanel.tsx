import { Link } from 'react-router-dom';
import type { Me, MeetingDetail } from '../types';
import { deadlineLabel, isOpen, remainingSeats } from '../utils/format';
import { SeatDots } from './SeatDots';

interface Props {
  meeting: MeetingDetail;
  user: Me | null;
  pending: boolean;
  error: string | null;
  newSeat: number | null;
  loginReturnPath: string;
  onApply: () => void;
  onCancel: () => void;
  onToggleBookmark: () => void;
}

/** 상세 화면 오른쪽의 참여 현황 + 신청 버튼 패널 */
export function SeatPanel({
  meeting, user, pending, error, newSeat, loginReturnPath, onApply, onCancel, onToggleBookmark,
}: Props) {
  const open = isOpen(meeting);
  const left = remainingSeats(meeting);

  const status = meeting.participating
    ? '신청했어요. 모임에서 만나요.'
    : open
      ? `${left}자리 남았어요. ${deadlineLabel(meeting.recruitDeadline)}`
      : deadlineLabel(meeting.recruitDeadline);

  return (
    <div className="panel">
      <div className="panel__head">
        <span className="muted">참여 현황</span>
        <span className="panel__count">{meeting.currentParticipants} / {meeting.maxParticipants}</span>
      </div>

      <SeatDots
        max={meeting.maxParticipants}
        taken={meeting.currentParticipants}
        size="lg"
        closed={!open && !meeting.participating}
        highlight={newSeat}
      />

      <p className="muted" aria-live="polite">{status}</p>
      {error && <p className="alert" role="alert">{error}</p>}

      <PrimaryAction
        meeting={meeting}
        user={user}
        open={open}
        left={left}
        pending={pending}
        loginReturnPath={loginReturnPath}
        onApply={onApply}
        onCancel={onCancel}
      />

      {user && (
        <button
          type="button"
          className="btn btn--ghost btn--block"
          onClick={onToggleBookmark}
          disabled={pending}
          aria-pressed={meeting.bookmarked}
        >
          {meeting.bookmarked ? '북마크 해제' : '북마크'}
        </button>
      )}
    </div>
  );
}

interface ActionProps {
  meeting: MeetingDetail;
  user: Me | null;
  open: boolean;
  left: number;
  pending: boolean;
  loginReturnPath: string;
  onApply: () => void;
  onCancel: () => void;
}

/** 로그인 여부, 모임장 여부, 신청 여부, 마감 여부에 따라 버튼이 달라진다 */
function PrimaryAction({ meeting, user, open, left, pending, loginReturnPath, onApply, onCancel }: ActionProps) {
  if (!user) {
    return (
      <Link to="/login" state={{ from: loginReturnPath }} className="btn btn--block">
        로그인하고 신청하기
      </Link>
    );
  }
  if (user.id === meeting.host.id) {
    return <p className="panel__note">내가 만든 모임이에요.</p>;
  }
  if (meeting.participating) {
    return (
      <button type="button" className="btn btn--secondary btn--block" onClick={onCancel} disabled={pending}>
        신청 취소하기
      </button>
    );
  }
  if (!open) {
    return <p className="panel__note">{left === 0 ? '정원이 모두 찼어요.' : '모집이 마감됐어요.'}</p>;
  }
  return (
    <button type="button" className="btn btn--block" onClick={onApply} disabled={pending}>
      {pending ? '신청하는 중…' : '신청하기'}
    </button>
  );
}
