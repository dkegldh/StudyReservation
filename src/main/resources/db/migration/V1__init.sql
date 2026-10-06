CREATE TABLE users
(
    id                BIGSERIAL PRIMARY KEY,
    provider          VARCHAR(20)  NOT NULL,
    provider_id       VARCHAR(100) NOT NULL,
    nickname          VARCHAR(50)  NOT NULL,
    profile_image_url VARCHAR(500),
    created_at        TIMESTAMP    NOT NULL,
    updated_at        TIMESTAMP    NOT NULL,
    CONSTRAINT uk_users_provider_provider_id UNIQUE (provider, provider_id)
);

CREATE TABLE meetings
(
    id                   BIGSERIAL PRIMARY KEY,
    host_id              BIGINT       NOT NULL REFERENCES users (id),
    title                VARCHAR(100) NOT NULL,
    description          TEXT         NOT NULL,
    category             VARCHAR(20)  NOT NULL,
    location             VARCHAR(200) NOT NULL,
    meeting_at           TIMESTAMP    NOT NULL,
    recruit_deadline     TIMESTAMP    NOT NULL,
    max_participants     INT          NOT NULL,
    current_participants INT          NOT NULL DEFAULT 0,
    status               VARCHAR(20)  NOT NULL,
    -- 낙관적 락 비교 실험용. 기본 전략(분산 락)에서는 엔티티에 매핑하지 않는다
    version              BIGINT       NOT NULL DEFAULT 0,
    created_at           TIMESTAMP    NOT NULL,
    updated_at           TIMESTAMP    NOT NULL,
    CONSTRAINT ck_meetings_participants CHECK (current_participants >= 0 AND current_participants <= max_participants)
);

CREATE INDEX idx_meetings_status_meeting_at ON meetings (status, meeting_at);
CREATE INDEX idx_meetings_host_id ON meetings (host_id);

CREATE TABLE participations
(
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT    NOT NULL REFERENCES meetings (id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    -- 중복 신청의 최종 방어선
    CONSTRAINT uk_participations_meeting_user UNIQUE (meeting_id, user_id)
);

CREATE INDEX idx_participations_user_id ON participations (user_id);

CREATE TABLE comments
(
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT        NOT NULL REFERENCES meetings (id) ON DELETE CASCADE,
    author_id  BIGINT        NOT NULL REFERENCES users (id),
    parent_id  BIGINT REFERENCES comments (id) ON DELETE CASCADE,
    content    VARCHAR(1000) NOT NULL,
    deleted    BOOLEAN       NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP     NOT NULL,
    updated_at TIMESTAMP     NOT NULL
);

CREATE INDEX idx_comments_meeting_id ON comments (meeting_id);

CREATE TABLE bookmarks
(
    id         BIGSERIAL PRIMARY KEY,
    meeting_id BIGINT    NOT NULL REFERENCES meetings (id) ON DELETE CASCADE,
    user_id    BIGINT    NOT NULL REFERENCES users (id),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uk_bookmarks_user_meeting UNIQUE (user_id, meeting_id)
);
