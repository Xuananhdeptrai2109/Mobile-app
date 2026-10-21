SET @split_paid_ddl = IF(
    EXISTS(
        SELECT 1
        FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
          AND TABLE_NAME = 'group_expense_splits'
          AND COLUMN_NAME = 'is_paid'
    ),
    'SELECT 1',
    'ALTER TABLE group_expense_splits ADD COLUMN is_paid BOOLEAN NOT NULL DEFAULT FALSE'
);
PREPARE split_paid_statement FROM @split_paid_ddl;
EXECUTE split_paid_statement;
DEALLOCATE PREPARE split_paid_statement;
