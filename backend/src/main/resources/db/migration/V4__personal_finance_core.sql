CREATE TABLE personal_categories (
    category_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    icon_name VARCHAR(50) NOT NULL,
    color_code VARCHAR(10) NOT NULL DEFAULT '#4CAF50',
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (category_id),
    CONSTRAINT uk_personal_categories_name_type UNIQUE (name, type),
    CONSTRAINT chk_personal_categories_type CHECK (type IN ('INCOME', 'EXPENSE'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE personal_wallets (
    wallet_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    wallet_name VARCHAR(100) NOT NULL,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    is_default BOOLEAN NOT NULL DEFAULT FALSE,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (wallet_id),
    CONSTRAINT uk_personal_wallets_user_name UNIQUE (user_id, wallet_name),
    KEY idx_personal_wallets_user (user_id),
    CONSTRAINT fk_personal_wallets_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE personal_transactions (
    transaction_id BIGINT NOT NULL AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    wallet_id BIGINT NOT NULL,
    category_id BIGINT NOT NULL,
    title VARCHAR(160) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    type VARCHAR(20) NOT NULL,
    note VARCHAR(500) NULL,
    receipt_image_url VARCHAR(1000) NULL,
    transaction_time DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (transaction_id),
    KEY idx_personal_transactions_user_time (user_id, transaction_time DESC),
    KEY idx_personal_transactions_wallet (wallet_id),
    KEY idx_personal_transactions_category (category_id),
    KEY idx_personal_transactions_user_type (user_id, type),
    CONSTRAINT chk_personal_transactions_amount CHECK (amount > 0),
    CONSTRAINT chk_personal_transactions_type CHECK (type IN ('INCOME', 'EXPENSE')),
    CONSTRAINT fk_personal_transactions_user
        FOREIGN KEY (user_id) REFERENCES users (user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_personal_transactions_wallet
        FOREIGN KEY (wallet_id) REFERENCES personal_wallets (wallet_id),
    CONSTRAINT fk_personal_transactions_category
        FOREIGN KEY (category_id) REFERENCES personal_categories (category_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO personal_categories (name, type, icon_name, color_code, display_order) VALUES
    ('Ăn uống', 'EXPENSE', 'fastfood', '#F97316', 10),
    ('Di chuyển', 'EXPENSE', 'directions_bus', '#3B82F6', 20),
    ('Mua sắm', 'EXPENSE', 'shopping_bag', '#EC4899', 30),
    ('Hóa đơn', 'EXPENSE', 'bolt', '#EAB308', 40),
    ('Giải trí', 'EXPENSE', 'local_cafe', '#8B5CF6', 50),
    ('Sức khỏe', 'EXPENSE', 'health_and_safety', '#EF4444', 60),
    ('Giáo dục', 'EXPENSE', 'school', '#06B6D4', 70),
    ('Chi tiêu khác', 'EXPENSE', 'receipt_long', '#64748B', 80),
    ('Lương', 'INCOME', 'payments', '#22C55E', 10),
    ('Thưởng', 'INCOME', 'redeem', '#10B981', 20),
    ('Lãi suất', 'INCOME', 'savings', '#14B8A6', 30),
    ('Thu nhập khác', 'INCOME', 'account_balance_wallet', '#64748B', 40);

INSERT INTO personal_wallets (
    user_id, wallet_name, balance, currency, is_default, created_at, updated_at
)
SELECT user_id, 'Ví chính', 0.00, 'VND', TRUE, NOW(6), NOW(6)
FROM users;
