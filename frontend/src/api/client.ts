export class ApiError extends Error {
  constructor(
    public readonly status: number,
    public readonly code: string,
    message: string,
  ) {
    super(message);
  }
}

// Access Token은 XSS 노출을 줄이기 위해 메모리에만 둔다.
// 새로고침하면 사라지므로, HttpOnly 쿠키의 Refresh Token으로 재발급받는다.
let accessToken: string | null = null;

export const tokenStore = {
  get: () => accessToken,
  set: (token: string | null) => {
    accessToken = token;
  },
};

let refreshing: Promise<boolean> | null = null;

/** Refresh Token 쿠키로 Access Token 재발급. 동시에 여러 번 호출돼도 요청은 한 번만 보낸다. */
export function reissue(): Promise<boolean> {
  if (!refreshing) {
    refreshing = fetch('/api/v1/auth/reissue', { method: 'POST', credentials: 'include' })
      .then(async (res) => {
        if (!res.ok) {
          tokenStore.set(null);
          return false;
        }
        const body = (await res.json()) as { accessToken: string };
        tokenStore.set(body.accessToken);
        return true;
      })
      .catch(() => false)
      .finally(() => {
        refreshing = null;
      });
  }
  return refreshing;
}

export async function request<T>(path: string, options: RequestInit = {}, retry = true): Promise<T> {
  const headers = new Headers(options.headers);
  if (options.body && !headers.has('Content-Type')) {
    headers.set('Content-Type', 'application/json');
  }
  const token = tokenStore.get();
  if (token) headers.set('Authorization', `Bearer ${token}`);

  let res: Response;
  try {
    res = await fetch(path, { ...options, headers, credentials: 'include' });
  } catch {
    throw new ApiError(0, 'NETWORK_ERROR', '서버에 연결할 수 없어요. 네트워크를 확인해 주세요.');
  }

  // Access Token 만료 시 한 번만 재발급 후 재시도
  if (res.status === 401 && retry && (await reissue())) {
    return request<T>(path, options, false);
  }

  if (!res.ok) {
    let code = 'UNKNOWN';
    let message = '요청을 처리하지 못했어요. 잠시 후 다시 시도해 주세요.';
    try {
      const body = (await res.json()) as { code?: string; message?: string };
      code = body.code ?? code;
      message = body.message ?? message;
    } catch {
      // 에러 본문이 JSON이 아니면 기본 메시지 사용
    }
    throw new ApiError(res.status, code, message);
  }

  const text = await res.text();
  return (text ? JSON.parse(text) : undefined) as T;
}

export function errorMessage(error: unknown): string {
  if (error instanceof ApiError) return error.message;
  return '알 수 없는 문제가 생겼어요. 잠시 후 다시 시도해 주세요.';
}
