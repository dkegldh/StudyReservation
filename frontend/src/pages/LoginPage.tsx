import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { api, isMock } from '../api';
import { useAuth } from '../auth/AuthContext';
import type { Provider } from '../types';

export const REDIRECT_KEY = 'redirectAfterLogin';

export function LoginPage() {
  const location = useLocation();
  const navigate = useNavigate();
  const { refresh } = useAuth();
  const [pending, setPending] = useState<Provider | null>(null);

  const from = (location.state as { from?: string } | null)?.from ?? '/';
  const failed = new URLSearchParams(location.search).has('error');

  async function login(provider: Provider) {
    setPending(provider);
    sessionStorage.setItem(REDIRECT_KEY, from);
    await api.startLogin(provider);
    // 실제 환경에서는 위에서 페이지가 이동한다. 목 환경에서만 아래가 실행된다.
    if (isMock) {
      await refresh();
      navigate(from, { replace: true });
    }
  }

  return (
    <div className="login">
      <h1 className="page-title">로그인하고 자리를 잡아 보세요</h1>
      <p className="muted">따로 가입할 필요 없이 쓰던 계정으로 시작할 수 있어요.</p>
      {failed && <p className="alert" role="alert">로그인하지 못했어요. 다시 시도해 주세요.</p>}
      <div className="login__buttons">
        <button type="button" className="btn btn--kakao btn--block" onClick={() => login('KAKAO')} disabled={pending !== null}>
          {pending === 'KAKAO' ? '카카오로 이동하는 중…' : '카카오로 계속하기'}
        </button>
        <button type="button" className="btn btn--google btn--block" onClick={() => login('GOOGLE')} disabled={pending !== null}>
          {pending === 'GOOGLE' ? 'Google로 이동하는 중…' : 'Google로 계속하기'}
        </button>
      </div>
    </div>
  );
}
