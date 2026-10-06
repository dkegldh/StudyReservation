import { Link, NavLink } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { isMock } from '../api';

export function Header() {
  const { user, loading } = useAuth();

  return (
    <header className="header">
      <div className="container header__inner">
        <Link to="/" className="logo">자리있음</Link>
        {isMock && <span className="mock-flag">목 데이터</span>}
        <nav className="header__nav">
          <Link to="/meetings/new" className="btn btn--small">모임 만들기</Link>
          {!loading && (user ? (
            <NavLink to="/me" className="avatar" aria-label="내 모임">
              {user.nickname.slice(0, 1)}
            </NavLink>
          ) : (
            <Link to="/login" className="btn btn--small btn--secondary">로그인</Link>
          ))}
        </nav>
      </div>
    </header>
  );
}
