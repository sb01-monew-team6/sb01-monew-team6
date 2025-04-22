-- 테이블 및 외래키 존재 시 삭제
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS interests CASCADE;
DROP TABLE IF EXISTS subscriptions CASCADE;
DROP TABLE IF EXISTS news_article CASCADE;
DROP TABLE IF EXISTS news_article_interest CASCADE;
DROP TABLE IF EXISTS article_view CASCADE;
DROP TABLE IF EXISTS comments CASCADE;
DROP TABLE IF EXISTS comment_like CASCADE;
DROP TABLE IF EXISTS notifications CASCADE;

-- 사용자 정보 테이블
CREATE TABLE users
(
    id         BIGSERIAL    NOT NULL,
    email      VARCHAR(255) NOT NULL,
    nickname   VARCHAR(255) NOT NULL,
    password   VARCHAR(255) NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NULL
);

-- 관심사 정보 테이블
CREATE TABLE interests
(
    id               BIGSERIAL    NOT NULL,
    name             VARCHAR(255) NOT NULL,
    keyword          TEXT[]       NULL,
    subscriber_count BIGINT       NOT NULL DEFAULT 0,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMPTZ  NULL
);

-- 구독 정보 테이블
CREATE TABLE subscriptions
(
    id          BIGSERIAL   NOT NULL,
    user_id     BIGINT      NOT NULL,
    interest_id BIGINT      NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 뉴스 기사 테이블
CREATE TABLE news_article
(
    id                     BIGSERIAL    NOT NULL,
    source                 VARCHAR(255) NOT NULL,
    source_url             VARCHAR(255) NOT NULL,
    article_title          VARCHAR(255) NOT NULL,
    article_published_date TIMESTAMPTZ  NOT NULL,
    article_summary        TEXT         NOT NULL,
    is_deleted             BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at             TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 뉴스 기사 - 관심사 연결 테이블
CREATE TABLE news_article_interest
(
    news_article_id BIGINT      NOT NULL,
    interest_id     BIGINT      NOT NULL,
    created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 뉴스 기사 조회 기록 테이블
CREATE TABLE article_view
(
    id                BIGSERIAL   NOT NULL,
    article_id        BIGINT      NOT NULL,
    user_id           BIGINT      NOT NULL,
    article_view_date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 댓글 테이블
CREATE TABLE comments
(
    id         BIGSERIAL    NOT NULL,
    article_id BIGINT       NOT NULL,
    user_id    BIGINT       NOT NULL,
    content    VARCHAR(500) NOT NULL,
    is_deleted BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ  NULL
);

-- 댓글 좋아요 테이블
CREATE TABLE comment_like
(
    id         BIGSERIAL   NOT NULL,
    comment_id BIGINT      NOT NULL,
    user_id    BIGINT      NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP -- TIMESTAMP 타입 권장
);

-- 알림 테이블
CREATE TABLE notifications
(
    id            BIGSERIAL    NOT NULL,
    user_id       BIGINT       NOT NULL,
    content       VARCHAR(255) NOT NULL,
    resource_type VARCHAR(50)  NOT NULL,
    resource_id   BIGINT       NOT NULL,
    confirmed     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMPTZ  NULL
);

-- 기본 키 (PK) 제약 조건 추가
ALTER TABLE users
    ADD CONSTRAINT pk_users PRIMARY KEY (id);
ALTER TABLE interests
    ADD CONSTRAINT pk_interests PRIMARY KEY (id);
ALTER TABLE subscriptions
    ADD CONSTRAINT pk_subscriptions PRIMARY KEY (id);
ALTER TABLE news_article
    ADD CONSTRAINT pk_news_article PRIMARY KEY (id);
ALTER TABLE news_article_interest
    ADD CONSTRAINT pk_news_article_interest PRIMARY KEY (news_article_id, interest_id);
ALTER TABLE article_view
    ADD CONSTRAINT pk_article_view PRIMARY KEY (id);
ALTER TABLE comments
    ADD CONSTRAINT pk_comments PRIMARY KEY (id);
ALTER TABLE comment_like
    ADD CONSTRAINT pk_comment_like PRIMARY KEY (id);
ALTER TABLE notifications
    ADD CONSTRAINT pk_notifications PRIMARY KEY (id);

-- 고유 (UNIQUE) 제약 조건 추가
ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);
-- ALTER TABLE users ADD CONSTRAINT uq_users_nickname UNIQUE (nickname); -- 필요시
ALTER TABLE interests
    ADD CONSTRAINT uq_interests_name UNIQUE (name);
ALTER TABLE subscriptions
    ADD CONSTRAINT uq_subscriptions_user_interest UNIQUE (user_id, interest_id);
ALTER TABLE comment_like
    ADD CONSTRAINT uq_comment_like_user_comment UNIQUE (comment_id, user_id);
-- ALTER TABLE article_view ADD CONSTRAINT uq_article_view_user_article UNIQUE (article_id, user_id); -- 필요시

-- 외래 키 (FK) 제약 조건 추가
ALTER TABLE subscriptions
    ADD CONSTRAINT fk_subscriptions_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;
ALTER TABLE subscriptions
    ADD CONSTRAINT fk_subscriptions_interest FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE CASCADE;

ALTER TABLE news_article_interest
    ADD CONSTRAINT fk_news_article_interest_article FOREIGN KEY (news_article_id) REFERENCES news_article (id) ON DELETE CASCADE;
ALTER TABLE news_article_interest
    ADD CONSTRAINT fk_news_article_interest_interest FOREIGN KEY (interest_id) REFERENCES interests (id) ON DELETE CASCADE;

ALTER TABLE article_view
    ADD CONSTRAINT fk_article_view_article FOREIGN KEY (article_id) REFERENCES news_article (id) ON DELETE CASCADE;
ALTER TABLE article_view
    ADD CONSTRAINT fk_article_view_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE comments
    ADD CONSTRAINT fk_comments_article FOREIGN KEY (article_id) REFERENCES news_article (id) ON DELETE CASCADE;
ALTER TABLE comments
    ADD CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE comment_like
    ADD CONSTRAINT fk_comment_like_comment FOREIGN KEY (comment_id) REFERENCES comments (id) ON DELETE CASCADE;
ALTER TABLE comment_like
    ADD CONSTRAINT fk_comment_like_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;

ALTER TABLE notifications
    ADD CONSTRAINT fk_notifications_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE;