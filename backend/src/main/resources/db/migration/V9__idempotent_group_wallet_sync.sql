ALTER TABLE personal_transactions
    ADD COLUMN source_group_id BIGINT NULL AFTER receipt_image_url,
    ADD CONSTRAINT fk_personal_transactions_source_group
        FOREIGN KEY (source_group_id) REFERENCES expense_groups (group_id)
        ON DELETE SET NULL,
    ADD CONSTRAINT uk_personal_transactions_user_source_group
        UNIQUE (user_id, source_group_id);
