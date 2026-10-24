CREATE TABLE group_settlements (
    settlement_id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    from_member_id BIGINT NOT NULL,
    to_member_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    settled_at DATETIME(6) NOT NULL,
    PRIMARY KEY (settlement_id),
    KEY idx_group_settlements_group_time (group_id, settled_at DESC),
    KEY idx_group_settlements_from_member (from_member_id),
    KEY idx_group_settlements_to_member (to_member_id),
    CONSTRAINT chk_group_settlements_amount CHECK (amount > 0),
    CONSTRAINT chk_group_settlements_members CHECK (from_member_id <> to_member_id),
    CONSTRAINT fk_group_settlements_group
        FOREIGN KEY (group_id) REFERENCES expense_groups (group_id) ON DELETE CASCADE,
    CONSTRAINT fk_group_settlements_from_member
        FOREIGN KEY (from_member_id) REFERENCES group_members (member_id),
    CONSTRAINT fk_group_settlements_to_member
        FOREIGN KEY (to_member_id) REFERENCES group_members (member_id),
    CONSTRAINT fk_group_settlements_created_by
        FOREIGN KEY (created_by_user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
