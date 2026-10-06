import { useState, type FormEvent } from 'react';
import { useNavigate } from 'react-router-dom';
import { api, errorMessage } from '../api';
import { SeatDots } from '../components/SeatDots';
import type { Category } from '../types';
import { CATEGORIES, CATEGORY_LABEL } from '../utils/format';

const MIN_SEATS = 2;
const MAX_SEATS = 30;

export function MeetingCreatePage() {
  const navigate = useNavigate();
  const [title, setTitle] = useState('');
  const [category, setCategory] = useState<Category>('STUDY');
  const [description, setDescription] = useState('');
  const [maxParticipants, setMaxParticipants] = useState(6);
  const [location, setLocation] = useState('');
  const [meetingAt, setMeetingAt] = useState('');
  const [recruitDeadline, setRecruitDeadline] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [pending, setPending] = useState(false);

  function validate(): string | null {
    if (!title.trim() || !description.trim() || !location.trim()) return '모임 이름, 소개, 장소를 모두 입력해 주세요.';
    if (!meetingAt || !recruitDeadline) return '모임 일시와 모집 마감 시각을 정해 주세요.';
    if (new Date(meetingAt) <= new Date()) return '모임 일시는 지금 이후여야 해요.';
    if (new Date(recruitDeadline) > new Date(meetingAt)) return '모집 마감은 모임 시작 전이어야 해요.';
    if (maxParticipants < MIN_SEATS || maxParticipants > MAX_SEATS) return `정원은 ${MIN_SEATS}명에서 ${MAX_SEATS}명 사이로 정해 주세요.`;
    return null;
  }

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const problem = validate();
    if (problem) {
      setError(problem);
      return;
    }
    setPending(true);
    setError(null);
    try {
      const { id } = await api.createMeeting({
        title: title.trim(),
        category,
        description: description.trim(),
        maxParticipants,
        location: location.trim(),
        meetingAt,
        recruitDeadline,
      });
      navigate(`/meetings/${id}`, { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setPending(false);
    }
  }

  return (
    <div className="narrow">
      <h1 className="page-title">모임 만들기</h1>
      <form className="form" onSubmit={handleSubmit} noValidate>
        <label className="field">
          <span>모임 이름</span>
          <input value={title} onChange={(e) => setTitle(e.target.value)} placeholder="토요일 아침 탄천 러닝 5km" maxLength={50} />
        </label>

        <label className="field">
          <span>카테고리</span>
          <select value={category} onChange={(e) => setCategory(e.target.value as Category)}>
            {CATEGORIES.map((c) => <option key={c} value={c}>{CATEGORY_LABEL[c]}</option>)}
          </select>
        </label>

        <label className="field">
          <span>소개</span>
          <textarea
            value={description}
            onChange={(e) => setDescription(e.target.value)}
            placeholder="어떤 사람과 무엇을 하는 모임인지, 준비물이 있는지 적어 주세요."
            rows={5}
          />
        </label>

        <div className="field">
          <label htmlFor="seats">정원</label>
          <div className="seat-picker">
            <input
              id="seats"
              type="number"
              min={MIN_SEATS}
              max={MAX_SEATS}
              value={maxParticipants}
              onChange={(e) => setMaxParticipants(Number(e.target.value))}
            />
            <span>명</span>
          </div>
          <SeatDots max={Math.min(Math.max(maxParticipants || 0, 0), MAX_SEATS)} taken={0} />
        </div>

        <label className="field">
          <span>장소</span>
          <input value={location} onChange={(e) => setLocation(e.target.value)} placeholder="판교역 스터디카페" />
        </label>

        <div className="field-row">
          <label className="field">
            <span>모임 일시</span>
            <input type="datetime-local" value={meetingAt} onChange={(e) => setMeetingAt(e.target.value)} />
          </label>
          <label className="field">
            <span>모집 마감</span>
            <input type="datetime-local" value={recruitDeadline} onChange={(e) => setRecruitDeadline(e.target.value)} />
          </label>
        </div>

        {error && <p className="alert" role="alert">{error}</p>}

        <button type="submit" className="btn btn--block" disabled={pending}>
          {pending ? '만드는 중…' : '모임 만들기'}
        </button>
      </form>
    </div>
  );
}
