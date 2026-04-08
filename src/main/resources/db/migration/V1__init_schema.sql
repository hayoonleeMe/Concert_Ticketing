-- =============================================================
-- Concert Ticketing — Phase 2: 도메인 스키마 초기화
-- =============================================================

CREATE TABLE users (
    id          BIGINT          NOT NULL AUTO_INCREMENT,
    email       VARCHAR(100)    NOT NULL,
    name        VARCHAR(50)     NOT NULL,
    provider    VARCHAR(20)     NOT NULL,                         -- enum STRING 저장: 'KAKAO', 'GOOGLE', 'LOCAL'
    provider_id VARCHAR(255),                                     -- nullable: LOCAL 로그인은 providerId 없음
    role        VARCHAR(20)     NOT NULL DEFAULT 'USER',          -- DB 레벨 기본값; Java 외부 INSERT 시에도 보장
    created_at  DATETIME(6)     NOT NULL,                        -- DATETIME(6): 마이크로초 정밀도; JPA Auditing과 일치
    updated_at  DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uq_user_email UNIQUE (email)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci; -- utf8mb4: 이모지 포함 유니코드 전체 지원

CREATE TABLE venues (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    name            VARCHAR(200)    NOT NULL,
    address         VARCHAR(500)    NOT NULL,
    total_capacity  INT             NOT NULL,                     -- INT: 공연장 좌석 수는 21억 미만이므로 BIGINT 불필요
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE events (
    id              BIGINT          NOT NULL AUTO_INCREMENT,
    venue_id        BIGINT          NOT NULL,
    title           VARCHAR(200)    NOT NULL,
    artist          VARCHAR(100)    NOT NULL,
    genre           VARCHAR(50),
    description     TEXT,                                        -- TEXT: nullable 허용; VARCHAR(255) 초과 내용 대비
    ticket_open_at  DATETIME(6)     NOT NULL,                   -- Phase 3 오픈 스케줄러가 이 값과 NOW()를 비교
    created_at      DATETIME(6)     NOT NULL,
    updated_at      DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_event_venue FOREIGN KEY (venue_id) REFERENCES venues (id)  -- FK: 실수로 venues 삭제 시 DB가 거부
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE event_schedules (
    id                      BIGINT      NOT NULL AUTO_INCREMENT,
    event_id                BIGINT      NOT NULL,
    start_at                DATETIME(6) NOT NULL,
    end_at                  DATETIME(6) NOT NULL,
    total_seat_count        INT         NOT NULL,
    remaining_seat_count    INT         NOT NULL,                -- 별도 카운터: COUNT(*) 없이 잔여석 O(1) 조회
    created_at              DATETIME(6) NOT NULL,
    updated_at              DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_event FOREIGN KEY (event_id) REFERENCES events (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE seats (
    id                  BIGINT          NOT NULL AUTO_INCREMENT,
    event_schedule_id   BIGINT          NOT NULL,
    seat_number         VARCHAR(20)     NOT NULL,
    price               DECIMAL(10, 2)  NOT NULL,               -- DECIMAL: float/double 부동소수점 오차 없이 금액 정확 저장
    status              VARCHAR(20)     NOT NULL DEFAULT 'AVAILABLE',  -- DEFAULT: Seat.create() 팩토리 초기값과 동기화
    version             BIGINT          NOT NULL DEFAULT 0,     -- DEFAULT 0: @Version 초기값과 반드시 일치; 불일치 시 OptimisticLockException
    created_at          DATETIME(6)     NOT NULL,
    updated_at          DATETIME(6)     NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_seat_schedule FOREIGN KEY (event_schedule_id) REFERENCES event_schedules (id)  -- FK: event_schedules 삭제 전 seats 있으면 DB가 거부
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- =============================================================
-- 인덱스
-- =============================================================

CREATE INDEX idx_seat_schedule_status ON seats (event_schedule_id, status);  -- 복합 인덱스: event_schedule_id(선행) → 특정 회차의 AVAILABLE 좌석 조회 최적
CREATE INDEX idx_user_provider ON users (provider, provider_id);             -- 소셜 로그인 조회: findByProviderAndProviderId() 쿼리에 대응