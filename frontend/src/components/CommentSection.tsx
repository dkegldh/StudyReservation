import { useEffect, useState, type FormEvent } from 'react';
import { Link } from 'react-router-dom';
import { api, errorMessage } from '../api';
import type { Comment, Me } from '../types';

interface Props {
  meetingId: number;
  hostId: number;
  user: Me | null;
  loginReturnPath: string;
}

/** 모임 상세의 문의 댓글. 댓글 목록과 작성 상태를 스스로 관리한다. */
export function CommentSection({ meetingId, hostId, user, loginReturnPath }: Props) {
  const [comments, setComments] = useState<Comment[]>([]);
  const [text, setText] = useState('');
  const [pending, setPending] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    let ignore = false;
    api.getComments(meetingId)
      .then((res) => !ignore && setComments(res))
      .catch((e) => !ignore && setError(errorMessage(e)));
    return () => {
      ignore = true;
    };
  }, [meetingId]);

  async function handleSubmit(e: FormEvent) {
    e.preventDefault();
    const content = text.trim();
    if (!content) return;
    setPending(true);
    setError(null);
    try {
      const created = await api.addComment(meetingId, content);
      setComments((prev) => [...prev, created]);
      setText('');
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setPending(false);
    }
  }

  return (
    <section className="comments" aria-labelledby="comments-title">
      <h2 id="comments-title" className="section-title">문의 {comments.length}</h2>
      {comments.length === 0 && <p className="muted">궁금한 점이 있으면 모임장에게 물어보세요.</p>}

      <ul className="comment-list">
        {comments.map((c) => (
          <li key={c.id} className={`comment${c.parentId ? ' comment--reply' : ''}`}>
            <span className="comment__author">
              {c.author.nickname}
              {c.author.id === hostId && <span className="host-tag">모임장</span>}
            </span>
            <p className="comment__body">{c.deleted ? '삭제된 댓글이에요.' : c.content}</p>
          </li>
        ))}
      </ul>

      {error && <p className="alert" role="alert">{error}</p>}

      {user ? (
        <form className="comment-form" onSubmit={handleSubmit}>
          <textarea
            value={text}
            onChange={(e) => setText(e.target.value)}
            placeholder="준비물이 따로 있나요?"
            aria-label="댓글 내용"
            rows={2}
            maxLength={500}
          />
          <button type="submit" className="btn btn--secondary" disabled={pending || !text.trim()}>
            남기기
          </button>
        </form>
      ) : (
        <p className="muted">
          <Link to="/login" state={{ from: loginReturnPath }}>로그인</Link>하면 댓글을 남길 수 있어요.
        </p>
      )}
    </section>
  );
}
