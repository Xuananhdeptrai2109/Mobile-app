CREATE TABLE IF NOT EXISTS notifications (
    notification_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(150) NOT NULL,
    content TEXT NOT NULL,
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    PRIMARY KEY (notification_id),
    KEY idx_notifications_user (user_id),
    CONSTRAINT fk_notifications_user
        FOREIGN KEY (user_id) REFERENCES users (user_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Older local databases already contained this table outside Flyway. Normalize
-- the shared columns so the same migration works for both legacy and fresh DBs.
ALTER TABLE notifications
    MODIFY COLUMN title VARCHAR(150) NOT NULL,
    MODIFY COLUMN content TEXT NOT NULL,
    MODIFY COLUMN is_read BOOLEAN NOT NULL DEFAULT FALSE,
    MODIFY COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6);
