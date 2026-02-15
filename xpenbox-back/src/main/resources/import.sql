-- This file allow to write SQL commands that will be emitted in test and dev.

-- 1. User
CREATE TABLE IF NOT EXISTS tbl_user (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `email` VARCHAR(250) NOT NULL UNIQUE,
    `password` VARCHAR(250) NOT NULL,
	`currency` varchar(10) NOT NULL,
    `state` TINYINT(1) NOT NULL DEFAULT 1,
    `verified` TINYINT(1) NOT NULL DEFAULT 0,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_token (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `access_token` VARCHAR(1000) NOT NULL,
    `refresh_token` VARCHAR(250) NOT NULL,
    `access_token_expires_at` DATETIME NOT NULL,
    `refresh_token_expires_at` DATETIME NULL,
    `persistent_session` TINYINT(1) NOT NULL DEFAULT 0,
    `revoked` TINYINT(1) NOT NULL DEFAULT 0,
    `issued_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `last_used_at` DATETIME NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_token_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tbl_user_token (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `user_id` BIGINT NOT NULL,
    `token` VARCHAR(250) NOT NULL,
    `token_type` ENUM('EMAIL_VERIFICATION', 'PASSWORD_RESET') NOT NULL,
    `expires_at` DATETIME NOT NULL,
    `used` TINYINT(1) NOT NULL DEFAULT 0,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_token_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 2. Income
CREATE TABLE IF NOT EXISTS tbl_income (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `concept` VARCHAR(150) NOT NULL,
    `income_date` DATETIME NOT NULL,
    `total_amount` DECIMAL(13,2) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_income_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 3. Account (Cost Center)
CREATE TABLE IF NOT EXISTS tbl_account (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `balance` DECIMAL(13,2) NOT NULL DEFAULT 0.00,
    `initial_balance` DECIMAL(13,2) NOT NULL DEFAULT 0.00,
    `last_used_date` DATETIME NULL,
    `usage_count` BIGINT NOT NULL DEFAULT 0,
    `state` TINYINT(1) NOT NULL DEFAULT 1,
	`closing_date` DATETIME NULL,
    `user_id` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 4. Credit Card
CREATE TABLE IF NOT EXISTS tbl_credit_card (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `credit_limit` DECIMAL(13,2) NOT NULL,
    `current_balance` DECIMAL(13,2) NOT NULL DEFAULT 0.00, 
    `last_used_date` DATETIME NULL,
    `usage_count` BIGINT NOT NULL DEFAULT 0,
    `state` TINYINT(1) NOT NULL DEFAULT 1,
    `billing_day` TINYINT NOT NULL,
    `payment_day` TINYINT NOT NULL,
	`closing_date` DATETIME NULL,
    `user_id` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 5. Category
CREATE TABLE IF NOT EXISTS tbl_category (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `color` VARCHAR(20) NOT NULL,
    `last_used_date` DATETIME NULL,
    `usage_count` BIGINT NOT NULL DEFAULT 0,
    `state` TINYINT(1) NOT NULL DEFAULT 1,
    `user_id` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 6. Transactions
CREATE TABLE IF NOT EXISTS tbl_transaction (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `description` VARCHAR(500) NULL,
    `transaction_type` ENUM('INCOME', 'EXPENSE', 'TRANSFER', 'CREDIT_PAYMENT') NOT NULL,
    `amount` DECIMAL(13,2) NOT NULL,
    `latitude` DECIMAL(10,8) NULL,
    `longitude` DECIMAL(11,8) NULL,
    `transaction_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `category_id` BIGINT NULL,
	`income_id` BIGINT NULL,
    `account_id` BIGINT NULL,
    `credit_card_id` BIGINT NULL,
    `destination_account_id` BIGINT NULL,
    `user_id` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_trans_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE,
	CONSTRAINT fk_trans_income FOREIGN KEY (income_id) REFERENCES tbl_income(id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_cat FOREIGN KEY (category_id) REFERENCES tbl_category(id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_acc FOREIGN KEY (account_id) REFERENCES tbl_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_acc_dest FOREIGN KEY (destination_account_id) REFERENCES tbl_account(id) ON DELETE CASCADE,
    CONSTRAINT fk_trans_card FOREIGN KEY (credit_card_id) REFERENCES tbl_credit_card(id) ON DELETE CASCADE
);

-- 7. Subscription
CREATE TABLE IF NOT EXISTS tbl_plan (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `name` VARCHAR(100) NOT NULL,
    `description` VARCHAR(500) NULL,
    `price` DECIMAL(13,2) NOT NULL,
    `currency` CHAR(3) NOT NULL DEFAULT 'USD',
    `billing_cycle` ENUM('MONTHLY', 'ANNUALLY') NOT NULL,
    `status` ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO tbl_plan (resource_code, name, description, price, currency, billing_cycle) VALUES
('plan_free', 'XpenBox Free', 'Access to basic features on a monthly basis', 0.00, 'PEN', 'ANNUALLY'),
('plan_betatester', 'XpenBox Beta Tester', 'Access to all and beta features free', 0.00, 'PEN', 'ANNUALLY'),
('plan_pro_monthly', 'XpenBox Pro', 'Access to all features on a monthly basis with a discount', 9.99, 'PEN', 'MONTHLY');

CREATE TABLE IF NOT EXISTS tbl_subscription (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `plan_id` BIGINT NOT NULL,
    `user_id` BIGINT NOT NULL,
    `plan_price` DECIMAL(13,2) NOT NULL,
    `plan_currency` CHAR(3) NOT NULL DEFAULT 'USD',
    `start_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `end_date` DATETIME NULL,
    `next_billing_date` DATETIME NULL,
    `provider` VARCHAR(30) NOT NULL,
    `provider_plan_id` VARCHAR(100) NOT NULL,
    `provider_subscription_id` VARCHAR(100) NOT NULL,
    `status` ENUM('PENDING', 'ACTIVE', 'PAST_DUE', 'CANCELLED', 'EXPIRED', 'TRIAL') NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_provider_subscription (provider, provider_plan_id, provider_subscription_id),
    CONSTRAINT fk_subs_plan FOREIGN KEY (plan_id) REFERENCES tbl_plan(id) ON DELETE CASCADE,
    CONSTRAINT fk_subs_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS tbl_subscription_payment (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(100) NOT NULL UNIQUE,
    `subscription_id` BIGINT NOT NULL,
    `provider` VARCHAR(30) NOT NULL,
    `provider_payment_id` VARCHAR(100) NOT NULL,
    `amount` DECIMAL(13,2) NOT NULL,
    `currency` CHAR(3) NOT NULL DEFAULT 'USD',
    `payment_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `status` ENUM('APPROVED', 'REJECTED', 'PENDING', 'REFUNDED') NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    UNIQUE KEY uk_provider_payment (provider, provider_payment_id),
    CONSTRAINT fk_subs_pay_subscription FOREIGN KEY (subscription_id) REFERENCES tbl_subscription(id) ON DELETE CASCADE
);

CREATE INDEX idx_token_user
ON tbl_token(user_id);

CREATE INDEX idx_token_valid
ON tbl_token(user_id, revoked, refresh_token_expires_at);

CREATE INDEX idx_transaction_user_date 
ON tbl_transaction(user_id, transaction_date);

CREATE INDEX idx_transaction_account 
ON tbl_transaction(user_id, account_id);

CREATE INDEX idx_transaction_card 
ON tbl_transaction(user_id, credit_card_id);

CREATE UNIQUE INDEX idx_user_token_token 
ON tbl_user_token(token);

CREATE INDEX idx_subscription_user_status ON tbl_subscription(user_id, status);
CREATE INDEX idx_subscription_status ON tbl_subscription(status);
CREATE INDEX idx_subscription_next_billing ON tbl_subscription(next_billing_date);

CREATE INDEX idx_sub_payment_subscription ON tbl_subscription_payment(subscription_id);
CREATE INDEX idx_sub_payment_provider ON tbl_subscription_payment(provider_payment_id);