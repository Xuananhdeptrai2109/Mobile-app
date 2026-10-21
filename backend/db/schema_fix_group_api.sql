USE moneycare_db;

-- The previous schema contains both the old `groups` design and the newer
-- `expense_groups` design. The backend uses `expense_groups` because it stores
-- name, description, budget, and created_by_user_id.

ALTER TABLE group_members DROP FOREIGN KEY group_members_ibfk_1;

ALTER TABLE group_expenses DROP FOREIGN KEY group_expenses_ibfk_1;
ALTER TABLE group_expenses DROP FOREIGN KEY group_expenses_ibfk_2;
ALTER TABLE group_expenses DROP FOREIGN KEY group_expenses_ibfk_3;

ALTER TABLE group_expense_splits DROP FOREIGN KEY group_expense_splits_ibfk_1;

ALTER TABLE group_expense_splits DROP INDEX UK9wrf0rw2sfiogm6tw5sl908gw;
ALTER TABLE group_expense_splits DROP COLUMN expense_id;
ALTER TABLE group_expense_splits DROP COLUMN amount;
ALTER TABLE group_expense_splits DROP COLUMN is_settled;
ALTER TABLE group_expense_splits DROP COLUMN settled_at;
ALTER TABLE group_expense_splits ADD UNIQUE KEY uk_group_expense_splits_expense_user (group_expense_id, user_id);

ALTER TABLE group_expenses DROP COLUMN payer_id;
ALTER TABLE group_expenses DROP COLUMN category_id;
ALTER TABLE group_expenses DROP COLUMN title;
ALTER TABLE group_expenses DROP COLUMN total_amount;
ALTER TABLE group_expenses DROP COLUMN expense_time;

ALTER TABLE group_expense_splits
    ADD CONSTRAINT group_expense_splits_ibfk_1
    FOREIGN KEY (group_expense_id) REFERENCES group_expenses (group_expense_id)
    ON DELETE CASCADE;

DROP TABLE IF EXISTS groups;
