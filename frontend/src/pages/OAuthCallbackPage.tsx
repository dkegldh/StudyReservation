import { useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../auth/AuthContext';
import { REDIRECT_KEY } from './LoginPage';

/**
 * 백엔드 OAuth2 로그인 성공 핸들러가 Refresh Token을 HttpOnly 쿠키로 심은 뒤
 * 이 페이지(/oauth/callback)로 리다이렉트한다. 여기서 Access Token을 재발급받는다.
 */
export function OAuthCallbackPage() {
  const { refresh } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    refresh().then((me) => {
      const to = sessionStorage.getItem(REDIRECT_KEY) ?? '/';
      sessionStorage.removeItem(REDIRECT_KEY);
      navigate(me ? to : '/login?error', { replace: true });
    });
  }, [refresh, navigate]);

  return <p className="muted">로그인하는 중…</p>;
}
