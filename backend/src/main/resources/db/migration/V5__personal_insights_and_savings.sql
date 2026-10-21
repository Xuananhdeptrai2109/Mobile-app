CREATE TABLE personal_monthly_budgets (
    budget_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    budget_month DATE NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (budget_id),
    CONSTRAINT uk_personal_monthly_budgets_user_month UNIQUE (user_id, budget_month),
    CONSTRAINT chk_personal_monthly_budgets_amount CHECK (amount > 0),
    CONSTRAINT fk_personal_monthly_budgets_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE personal_savings_goals (
    goal_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    emoji VARCHAR(16) NOT NULL DEFAULT '🎯',
    target_amount DECIMAL(15, 2) NOT NULL,
    current_amount DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    target_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (goal_id),
    KEY idx_personal_savings_goals_user_status (user_id, status),
    CONSTRAINT chk_personal_savings_goals_target CHECK (target_amount > 0),
    CONSTRAINT chk_personal_savings_goals_current CHECK (current_amount >= 0),
    CONSTRAINT chk_personal_savings_goals_status CHECK (status IN ('ACTIVE', 'COMPLETED')),
    CONSTRAINT fk_personal_savings_goals_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
