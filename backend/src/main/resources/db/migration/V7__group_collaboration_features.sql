ALTER TABLE group_expenses
    ADD COLUMN split_method VARCHAR(20) NOT NULL DEFAULT 'EQUAL' AFTER paid_by_member_id;

CREATE TABLE group_activities (
    activity_id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    actor_user_id BIGINT NULL,
    activity_type VARCHAR(40) NOT NULL,
    title VARCHAR(160) NOT NULL,
    description VARCHAR(500) NULL,
    related_entity_type VARCHAR(40) NULL,
    related_entity_id BIGINT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (activity_id),
    KEY idx_group_activities_group_created (group_id, created_at),
    KEY idx_group_activities_actor (actor_user_id),
    CONSTRAINT fk_group_activities_group
        FOREIGN KEY (group_id) REFERENCES expense_groups (group_id) ON DELETE CASCADE,
    CONSTRAINT fk_group_activities_actor
        FOREIGN KEY (actor_user_id) REFERENCES users (user_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
