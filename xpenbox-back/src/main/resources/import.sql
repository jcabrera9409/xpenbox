-- This file allow to write SQL commands that will be emitted in test and dev.

-- 1. User
CREATE TABLE IF NOT EXISTS tbl_user (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `email` VARCHAR(250) NOT NULL UNIQUE,
    `password` VARCHAR(250) NOT NULL,
	`currency` varchar(10) NOT NULL,
    `state` TINYINT(1) NOT NULL DEFAULT 1,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS tbl_token (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(50) NOT NULL UNIQUE,
    `user_id` BIGINT NOT NULL,
    `access_token` VARCHAR(500) NOT NULL,
    `refresh_token` VARCHAR(500) NOT NULL,
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


-- 2. Income
CREATE TABLE IF NOT EXISTS tbl_income (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(50) NOT NULL UNIQUE,
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
    `resource_code` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `balance` DECIMAL(13,2) NOT NULL DEFAULT 0.00,
	`closing_date` DATETIME NULL,
    `user_id` BIGINT NOT NULL,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_date` DATETIME ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_account_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 4. Credit Card
CREATE TABLE IF NOT EXISTS tbl_credit_card (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `credit_limit` DECIMAL(13,2) NOT NULL,
    `current_balance` DECIMAL(13,2) NOT NULL DEFAULT 0.00, 
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
    `resource_code` VARCHAR(50) NOT NULL UNIQUE,
    `name` VARCHAR(150) NOT NULL,
    `color` VARCHAR(20) NOT NULL,
    `user_id` BIGINT NOT NULL,
    `state` TINYINT(1) NOT NULL DEFAULT 1,
    `created_date` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_category_user FOREIGN KEY (user_id) REFERENCES tbl_user(id) ON DELETE CASCADE
);

-- 6. Transactions
CREATE TABLE IF NOT EXISTS tbl_transaction (
    `id` BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    `resource_code` VARCHAR(50) NOT NULL UNIQUE,
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

CREATE INDEX idx_token_user
ON tbl_token(user_id);

CREATE INDEX idx_token_refresh
ON tbl_token(refresh_token);

CREATE INDEX idx_token_valid
ON tbl_token(user_id, revoked, refresh_token_expires_at);

CREATE INDEX idx_transaction_user_date 
ON tbl_transaction(user_id, transaction_date);

CREATE INDEX idx_transaction_account 
ON tbl_transaction(user_id, account_id);

CREATE INDEX idx_transaction_card 
ON tbl_transaction(user_id, credit_card_id);



