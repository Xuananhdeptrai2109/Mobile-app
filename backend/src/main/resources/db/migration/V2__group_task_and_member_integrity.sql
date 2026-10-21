CREATE TABLE IF NOT EXISTS group_tasks (
    task_id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    assignee_member_id BIGINT NULL,
    due_date DATE NULL,
    description VARCHAR(500) NULL,
    is_completed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NULL,
    PRIMARY KEY (task_id),
    KEY idx_group_tasks_group (group_id),
    KEY idx_group_tasks_assignee (assignee_member_id),
    CONSTRAINT fk_group_tasks_group
        FOREIGN KEY (group_id) REFERENCES expense_groups (group_id),
    CONSTRAINT fk_group_tasks_assignee
        FOREIGN KEY (assignee_member_id) REFERENCES group_members (member_id)
        ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

UPDATE group_tasks
SET due_date = DATE_FORMAT(STR_TO_DATE(due_date, '%d/%m/%Y'), '%Y-%m-%d')
WHERE due_date IS NOT NULL
  AND CAST(due_date AS CHAR) REGEXP '^[0-9]{2}/[0-9]{2}/[0-9]{4}$';

SET @due_date_ddl = IF(
    (SELECT DATA_TYPE
     FROM information_schema.COLUMNS
     WHERE TABLE_SCHEMA = DATABASE()
       AND TABLE_NAME = 'group_tasks'
       AND COLUMN_NAME = 'due_date') = 'date',
    'SELECT 1',
    'ALTER TABLE group_tasks MODIFY COLUMN due_date DATE NULL'
);
PREPARE due_date_statement FROM @due_date_ddl;
EXECUTE due_date_statement;
DEALLOCATE PREPARE due_date_statement;

SET @member_index_ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.STATISTICS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'group_members'
          AND INDEX_NAME = 'uk_group_members_group_user'
    ),
    'SELECT 1',
    'ALTER TABLE group_members ADD CONSTRAINT uk_group_members_group_user UNIQUE (group_id, user_id)'
);
PREPARE member_index_statement FROM @member_index_ddl;
EXECUTE member_index_statement;
DEALLOCATE PREPARE member_index_statement;
