import { Link, Route, Routes } from 'react-router-dom';
import { RequireAuth } from './auth/RequireAuth';
import { Header } from './components/Header';
import { LoginPage } from './pages/LoginPage';
import { MeetingCreatePage } from './pages/MeetingCreatePage';
import { MeetingDetailPage } from './pages/MeetingDetailPage';
import { MeetingListPage } from './pages/MeetingListPage';
import { MyPage } from './pages/MyPage';
import { OAuthCallbackPage } from './pages/OAuthCallbackPage';

export default function App() {
  return (
    <>
      <Header />
      <main className="container page">
        <Routes>
          <Route path="/" element={<MeetingListPage />} />
          <Route path="/meetings/new" element={<RequireAuth><MeetingCreatePage /></RequireAuth>} />
          <Route path="/meetings/:id" element={<MeetingDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/oauth/callback" element={<OAuthCallbackPage />} />
          <Route path="/me" element={<RequireAuth><MyPage /></RequireAuth>} />
          <Route
            path="*"
            element={
              <div className="empty">
                <p className="empty__title">없는 페이지예요</p>
                <Link to="/" className="btn btn--secondary">모임 목록으로 가기</Link>
              </div>
            }
          />
        </Routes>
      </main>
    </>
  );
}
