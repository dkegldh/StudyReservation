import { realApi } from './real';
import { mockApi } from './mock';

export const isMock = import.meta.env.VITE_USE_MOCK === 'true';
export const api = isMock ? mockApi : realApi;
export { ApiError, errorMessage } from './client';
