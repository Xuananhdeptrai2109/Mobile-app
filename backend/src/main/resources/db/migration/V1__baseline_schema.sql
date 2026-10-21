CREATE TABLE users (
    user_id BIGINT NOT NULL AUTO_INCREMENT,
    full_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    phone_number VARCHAR(255) NULL,
    password VARCHAR(255) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (user_id),
    CONSTRAINT uk_users_email UNIQUE (email),
    CONSTRAINT uk_users_phone_number UNIQUE (phone_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE test_connection (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE expense_groups (
    group_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(500) NULL,
    image_base64 LONGTEXT NULL,
    budget DECIMAL(15, 2) NOT NULL,
    created_by_user_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (group_id),
    KEY idx_expense_groups_creator (created_by_user_id),
    CONSTRAINT fk_expense_groups_creator
        FOREIGN KEY (created_by_user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE group_members (
    member_id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    user_id BIGINT NULL,
    name VARCHAR(255) NOT NULL,
    role VARCHAR(255) NOT NULL,
    joined_at DATETIME(6) NOT NULL,
    PRIMARY KEY (member_id),
    CONSTRAINT uk_group_members_group_name UNIQUE (group_id, name),
    KEY idx_group_members_user (user_id),
    CONSTRAINT fk_group_members_group
        FOREIGN KEY (group_id) REFERENCES expense_groups (group_id),
    CONSTRAINT fk_group_members_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE group_tasks (
    task_id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    assignee_member_id BIGINT NULL,
    due_date VARCHAR(255) NULL,
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

CREATE TABLE group_expenses (
    group_expense_id BIGINT NOT NULL AUTO_INCREMENT,
    group_id BIGINT NOT NULL,
    name VARCHAR(160) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    paid_by_member_id BIGINT NOT NULL,
    note VARCHAR(500) NULL,
    expense_date DATE NOT NULL,
    receipt_image_url VARCHAR(1000) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (group_expense_id),
    KEY idx_group_expenses_group (group_id),
    KEY idx_group_expenses_payer (paid_by_member_id),
    CONSTRAINT fk_group_expenses_group
        FOREIGN KEY (group_id) REFERENCES expense_groups (group_id),
    CONSTRAINT fk_group_expenses_payer
        FOREIGN KEY (paid_by_member_id) REFERENCES group_members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE group_expense_splits (
    split_id BIGINT NOT NULL AUTO_INCREMENT,
    group_expense_id BIGINT NOT NULL,
    group_member_id BIGINT NOT NULL,
    split_amount DECIMAL(15, 2) NOT NULL,
    PRIMARY KEY (split_id),
    CONSTRAINT uk_group_expense_splits_expense_member
        UNIQUE (group_expense_id, group_member_id),
    KEY idx_group_expense_splits_member (group_member_id),
    CONSTRAINT fk_group_expense_splits_expense
        FOREIGN KEY (group_expense_id) REFERENCES group_expenses (group_expense_id),
    CONSTRAINT fk_group_expense_splits_member
        FOREIGN KEY (group_member_id) REFERENCES group_members (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
