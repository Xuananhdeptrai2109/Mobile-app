-- Clean schema for Expense Management Backend
-- Database: moneycare_db
-- This file recreates the database structure only. It contains no seed data.

DROP DATABASE IF EXISTS `moneycare_db`;
CREATE DATABASE `moneycare_db`
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE `moneycare_db`;

SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS `group_expense_splits`;
DROP TABLE IF EXISTS `group_expenses`;
DROP TABLE IF EXISTS `group_members`;
DROP TABLE IF EXISTS `expense_groups`;
DROP TABLE IF EXISTS `transactions`;
DROP TABLE IF EXISTS `wallets`;
DROP TABLE IF EXISTS `savings_goals`;
DROP TABLE IF EXISTS `notifications`;
DROP TABLE IF EXISTS `test_connection`;
DROP TABLE IF EXISTS `categories`;
DROP TABLE IF EXISTS `users`;

SET FOREIGN_KEY_CHECKS = 1;

CREATE TABLE `users` (
  `user_id` bigint NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `password` varchar(255) NOT NULL,
  `avatar_url` varchar(500) DEFAULT NULL,
  `auth_provider` enum('LOCAL','GOOGLE','FACEBOOK','APPLE') DEFAULT 'LOCAL',
  `is_active` tinyint(1) NOT NULL DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_users_email` (`email`),
  UNIQUE KEY `uk_users_phone_number` (`phone_number`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `categories` (
  `category_id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `type` enum('INCOME','EXPENSE') NOT NULL,
  `icon_name` varchar(50) NOT NULL,
  `color_code` varchar(10) DEFAULT '#4CAF50',
  `is_system` tinyint(1) DEFAULT '1',
  PRIMARY KEY (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `test_connection` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `message` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `wallets` (
  `wallet_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `wallet_name` varchar(100) NOT NULL DEFAULT 'Vi chinh',
  `balance` decimal(15,2) NOT NULL DEFAULT '0.00',
  `currency` varchar(10) NOT NULL DEFAULT 'VND',
  `is_default` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`wallet_id`),
  KEY `idx_wallets_user` (`user_id`),
  CONSTRAINT `fk_wallets_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transactions` (
  `transaction_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `wallet_id` bigint NOT NULL,
  `category_id` int NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `type` enum('INCOME','EXPENSE') NOT NULL,
  `note` varchar(255) DEFAULT NULL,
  `image_receipt_url` varchar(500) DEFAULT NULL,
  `transaction_time` datetime NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`transaction_id`),
  KEY `idx_transactions_wallet` (`wallet_id`),
  KEY `idx_transactions_category` (`category_id`),
  KEY `idx_transactions_user_time` (`user_id`,`transaction_time` DESC),
  CONSTRAINT `fk_transactions_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_transactions_wallet`
    FOREIGN KEY (`wallet_id`) REFERENCES `wallets` (`wallet_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_transactions_category`
    FOREIGN KEY (`category_id`) REFERENCES `categories` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `expense_groups` (
  `group_id` bigint NOT NULL AUTO_INCREMENT,
  `name` varchar(120) NOT NULL,
  `description` varchar(500) DEFAULT NULL,
  `image_base64` longtext DEFAULT NULL,
  `budget` decimal(15,2) NOT NULL,
  `created_by_user_id` bigint NOT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`group_id`),
  KEY `idx_expense_groups_created_by` (`created_by_user_id`),
  CONSTRAINT `fk_expense_groups_created_by`
    FOREIGN KEY (`created_by_user_id`) REFERENCES `users` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `group_members` (
  `member_id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `user_id` bigint DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `role` varchar(255) NOT NULL,
  `joined_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`member_id`),
  UNIQUE KEY `uk_group_members_group_name` (`group_id`, `name`),
  KEY `idx_group_members_group` (`group_id`),
  KEY `idx_group_members_user` (`user_id`),
  CONSTRAINT `fk_group_members_group`
    FOREIGN KEY (`group_id`) REFERENCES `expense_groups` (`group_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_group_members_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `group_expenses` (
  `group_expense_id` bigint NOT NULL AUTO_INCREMENT,
  `group_id` bigint NOT NULL,
  `name` varchar(160) NOT NULL,
  `amount` decimal(15,2) NOT NULL,
  `paid_by_member_id` bigint NOT NULL,
  `note` varchar(500) DEFAULT NULL,
  `expense_date` date NOT NULL,
  `receipt_image_url` varchar(1000) DEFAULT NULL,
  `created_at` datetime(6) NOT NULL,
  `updated_at` datetime(6) NOT NULL,
  PRIMARY KEY (`group_expense_id`),
  KEY `idx_group_expenses_group_date` (`group_id`,`expense_date` DESC),
  KEY `idx_group_expenses_paid_by` (`paid_by_member_id`),
  CONSTRAINT `fk_group_expenses_group`
    FOREIGN KEY (`group_id`) REFERENCES `expense_groups` (`group_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_group_expenses_paid_by`
    FOREIGN KEY (`paid_by_member_id`) REFERENCES `group_members` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `group_expense_splits` (
  `split_id` bigint NOT NULL AUTO_INCREMENT,
  `group_expense_id` bigint NOT NULL,
  `group_member_id` bigint NOT NULL,
  `split_amount` decimal(15,2) NOT NULL,
  PRIMARY KEY (`split_id`),
  UNIQUE KEY `uk_group_expense_splits_expense_member` (`group_expense_id`,`group_member_id`),
  KEY `idx_group_expense_splits_member` (`group_member_id`),
  CONSTRAINT `fk_group_expense_splits_expense`
    FOREIGN KEY (`group_expense_id`) REFERENCES `group_expenses` (`group_expense_id`)
    ON DELETE CASCADE,
  CONSTRAINT `fk_group_expense_splits_member`
    FOREIGN KEY (`group_member_id`) REFERENCES `group_members` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notifications` (
  `notification_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(150) NOT NULL,
  `content` text NOT NULL,
  `is_read` tinyint(1) DEFAULT '0',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`notification_id`),
  KEY `idx_notifications_user` (`user_id`),
  CONSTRAINT `fk_notifications_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `savings_goals` (
  `goal_id` bigint NOT NULL AUTO_INCREMENT,
  `user_id` bigint NOT NULL,
  `title` varchar(150) NOT NULL,
  `target_amount` decimal(15,2) NOT NULL,
  `current_amount` decimal(15,2) DEFAULT '0.00',
  `icon_url` varchar(500) DEFAULT NULL,
  `status` enum('IN_PROGRESS','COMPLETED') DEFAULT 'IN_PROGRESS',
  `target_date` date DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`goal_id`),
  KEY `idx_savings_goals_user` (`user_id`),
  CONSTRAINT `fk_savings_goals_user`
    FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
