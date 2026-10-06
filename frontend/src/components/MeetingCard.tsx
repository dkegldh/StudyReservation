import { Link } from 'react-router-dom';
import type { MeetingSummary } from '../types';
import { CATEGORY_LABEL, formatDateTime, isClosingSoon, isOpen, remainingSeats } from '../utils/format';
import { SeatDots } from './SeatDots';

export function MeetingCard({ meeting }: { meeting: MeetingSummary }) {
  const open = isOpen(meeting);
  const left = remainingSeats(meeting);

  return (
    <Link to={`/meetings/${meeting.id}`} className={`card${open ? '' : ' card--closed'}`}>
      <div className="card__top">
        <span className="card__category">{CATEGORY_LABEL[meeting.category]}</span>
        {isClosingSoon(meeting) && <span className="badge badge--hot">마감 임박</span>}
        {!open && <span className="badge badge--closed">모집 마감</span>}
      </div>
      <h2 className="card__title">{meeting.title}</h2>
      <p className="card__meta">
        {formatDateTime(meeting.meetingAt)}
        <br />
        {meeting.location}
      </p>
      <div className="card__seats">
        <SeatDots max={meeting.maxParticipants} taken={meeting.currentParticipants} closed={!open} />
        <span className="card__remaining">{open ? `${left}자리 남음` : '마감'}</span>
      </div>
    </Link>
  );
}
