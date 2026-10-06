import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api';
import { MeetingCard } from '../components/MeetingCard';
import type { Category, MeetingSummary } from '../types';
import { CATEGORIES, CATEGORY_LABEL } from '../utils/format';

export function MeetingListPage() {
  const [category, setCategory] = useState<Category | undefined>();
  const [recruitingOnly, setRecruitingOnly] = useState(true);
  const [keywordInput, setKeywordInput] = useState('');
  const [keyword, setKeyword] = useState('');

  const [items, setItems] = useState<MeetingSummary[]>([]);
  const [page, setPage] = useState(0);
  const [last, setLast] = useState(true);
  const [loading, setLoading] = useState(true);
  const [loadingMore, setLoadingMore] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    setLoading(true);
    setError(null);
    api.searchMeetings({ category, recruitingOnly, keyword, page: 0 })
      .then((res) => {
        if (ignore) return;
        setItems(res.content);
        setPage(res.number);
        setLast(res.last);
      })
      .catch((e) => !ignore && setError(errorMessage(e)))
      .finally(() => !ignore && setLoading(false));
    return () => {
      ignore = true;
    };
  }, [category, recruitingOnly, keyword]);

  async function loadMore() {
    setLoadingMore(true);
    try {
      const res = await api.searchMeetings({ category, recruitingOnly, keyword, page: page + 1 });
      setItems((prev) => [...prev, ...res.content]);
      setPage(res.number);
      setLast(res.last);
    } catch (e) {
      setError(errorMessage(e));
    } finally {
      setLoadingMore(false);
    }
  }

  function handleSearch(e: FormEvent) {
    e.preventDefault();
    setKeyword(keywordInput.trim());
  }

  const filtered = Boolean(category || keyword);

  return (
    <>
      <h1 className="page-title">지금 자리 있는 모임</h1>

      <form className="search" onSubmit={handleSearch} role="search">
        <input
          type="search"
          value={keywordInput}
          onChange={(e) => setKeywordInput(e.target.value)}
          placeholder="모임 이름이나 장소로 찾기"
          aria-label="모임 검색"
        />
        <button type="submit" className="btn btn--secondary">검색</button>
      </form>

      <div className="filters">
        <div className="chips" role="group" aria-label="카테고리">
          <button type="button" className="chip" aria-pressed={!category} onClick={() => setCategory(undefined)}>
            전체
          </button>
          {CATEGORIES.map((c) => (
            <button key={c} type="button" className="chip" aria-pressed={category === c} onClick={() => setCategory(c)}>
              {CATEGORY_LABEL[c]}
            </button>
          ))}
        </div>
        <label className="toggle">
          <input type="checkbox" checked={recruitingOnly} onChange={(e) => setRecruitingOnly(e.target.checked)} />
          모집 중만 보기
        </label>
      </div>

      {error && <p className="alert" role="alert">{error}</p>}

      {loading ? (
        <p className="muted">모임을 불러오는 중…</p>
      ) : items.length === 0 ? (
        <div className="empty">
          <p className="empty__title">{filtered ? '조건에 맞는 모임이 없어요' : '아직 열린 모임이 없어요'}</p>
          <p className="muted">원하는 모임이 없다면 직접 열어서 사람을 모아 보세요.</p>
          <Link to="/meetings/new" className="btn">모임 만들기</Link>
        </div>
      ) : (
        <>
          <div className="grid">
            {items.map((m) => <MeetingCard key={m.id} meeting={m} />)}
          </div>
          {!last && (
            <div className="more">
              <button type="button" className="btn btn--secondary" onClick={loadMore} disabled={loadingMore}>
                {loadingMore ? '불러오는 중…' : '더 보기'}
              </button>
            </div>
          )}
        </>
      )}
    </>
  );
}
