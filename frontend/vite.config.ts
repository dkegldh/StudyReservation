import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 로컬 개발 시 API, OAuth 요청을 Spring(8080)으로 넘겨서 운영과 같은 "같은 출처" 환경을 만든다.
// changeOrigin을 켜지 않아야 Spring이 redirect_uri를 localhost:5173 기준으로 만든다.
const backend = 'http://localhost:8080';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': backend,
      '/oauth2': backend,
      '/login/oauth2': backend,
    },
  },
});
