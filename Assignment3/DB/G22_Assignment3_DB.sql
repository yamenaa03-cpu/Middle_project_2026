-- =========================================================
-- Bistro DB Full Installer (Schema + Seed Data for Tests)
-- - Friday closed
-- - Saturday closes early
-- - date_override on a specific date closes early
-- - Seeds cover: users, subscriber/non-subscriber, tables,
--   advance reservations, no-show, walk-in waiting flow
--   (WAITING/NOTIFIED/IN_PROGRESS/COMPLETED), bills, reports
-- =========================================================

DROP DATABASE IF EXISTS bistrodb;
CREATE DATABASE bistrodb
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_0900_ai_ci;
USE bistrodb;

-- For clean install
SET FOREIGN_KEY_CHECKS = 0;

-- =========================
-- Tables (order matters)
-- =========================

CREATE TABLE `customer` (
  `customer_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) DEFAULT NULL,
  `phone` varchar(30) DEFAULT NULL,
  `email` varchar(120) DEFAULT NULL,
  `is_subscribed` tinyint(1) NOT NULL DEFAULT '0',
  `subscription_code` varchar(50) DEFAULT NULL,
  PRIMARY KEY (`customer_id`),
  UNIQUE KEY `uq_customer_subscription_code` (`subscription_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `employee` (
  `employee_id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(100) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password` varchar(255) NOT NULL,
  `role` enum('REP','MANAGER') NOT NULL,
  `active` tinyint(1) NOT NULL DEFAULT '1',
  PRIMARY KEY (`employee_id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `restaurant_table` (
  `table_id` int NOT NULL AUTO_INCREMENT,
  `capacity` int NOT NULL,
  PRIMARY KEY (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `opening_hours` (
  `day_of_week` int NOT NULL,
  `open_time` time DEFAULT NULL,
  `close_time` time DEFAULT NULL,
  `is_closed` tinyint(1) DEFAULT '0',
  PRIMARY KEY (`day_of_week`),
  CONSTRAINT `opening_hours_chk_1` CHECK ((`day_of_week` between 1 and 7))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `date_override` (
  `override_id` int NOT NULL AUTO_INCREMENT,
  `override_date` date NOT NULL,
  `open_time` time DEFAULT NULL,
  `close_time` time DEFAULT NULL,
  `is_closed` tinyint(1) DEFAULT '0',
  `reason` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`override_id`),
  UNIQUE KEY `override_date` (`override_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `reservation` (
  `reservation_id` int NOT NULL AUTO_INCREMENT,
  `reservation_datetime` datetime DEFAULT NULL,
  `number_of_guests` int DEFAULT NULL,
  `confirmation_code` int NOT NULL,
  `customer_id` int DEFAULT NULL,
  `table_id` int DEFAULT NULL,
  `created_at` datetime NOT NULL,
  `status` varchar(20) NOT NULL DEFAULT 'ACTIVE',
  `reminder_sent` tinyint(1) NOT NULL DEFAULT '0',
  `type` enum('ADVANCE','WALKIN') DEFAULT 'ADVANCE',
  `checked_in_at` datetime DEFAULT NULL COMMENT 'When customer received table (IN_PROGRESS)',
  `checked_out_at` datetime DEFAULT NULL COMMENT 'When customer paid and left (COMPLETED)',
  PRIMARY KEY (`reservation_id`),
  UNIQUE KEY `confirmation_code` (`confirmation_code`),
  KEY `fk_res_customer` (`customer_id`),
  KEY `fk_res_table` (`table_id`),
  CONSTRAINT `fk_res_customer` FOREIGN KEY (`customer_id`) REFERENCES `customer` (`customer_id`),
  CONSTRAINT `fk_res_table` FOREIGN KEY (`table_id`) REFERENCES `restaurant_table` (`table_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `bill` (
  `bill_id` int NOT NULL AUTO_INCREMENT,
  `reservation_id` int NOT NULL,
  `amount_before_discount` double NOT NULL,
  `final_amount` double NOT NULL,
  `paid` tinyint(1) NOT NULL DEFAULT '0',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `paid_at` datetime DEFAULT NULL,
  PRIMARY KEY (`bill_id`),
  UNIQUE KEY `reservation_id` (`reservation_id`),
  UNIQUE KEY `reservation_id_2` (`reservation_id`),
  CONSTRAINT `fk_bill_reservation` FOREIGN KEY (`reservation_id`)
    REFERENCES `reservation` (`reservation_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `subscriber_report` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `report_year` int NOT NULL,
  `report_month` int NOT NULL,
  `customer_id` int DEFAULT NULL,
  `customer_name` varchar(100) DEFAULT NULL,
  `subscription_code` varchar(50) DEFAULT NULL,
  `total_reservations` int DEFAULT NULL,
  `completed_reservations` int DEFAULT NULL,
  `cancelled_reservations` int DEFAULT NULL,
  `waitlist_entries` int DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `report_year` (`report_year`,`report_month`,`customer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `time_report` (
  `report_id` int NOT NULL AUTO_INCREMENT,
  `report_year` int NOT NULL,
  `report_month` int NOT NULL,
  `reservation_id` int DEFAULT NULL,
  `scheduled_time` datetime DEFAULT NULL,
  `checked_in_at` datetime DEFAULT NULL,
  `checked_out_at` datetime DEFAULT NULL,
  `arrival_delay_minutes` int DEFAULT NULL,
  `session_duration_minutes` int DEFAULT NULL,
  `customer_name` varchar(100) DEFAULT NULL,
  `is_subscriber` tinyint(1) DEFAULT NULL,
  PRIMARY KEY (`report_id`),
  UNIQUE KEY `report_year` (`report_year`,`report_month`,`reservation_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =========================
-- Seed Data
-- =========================

-- Employees (manager + rep)
INSERT INTO employee (full_name, username, password, role, active) VALUES
('Manager One', 'manager', 'manager123', 'MANAGER', 1),
('Rep One',     'rep',     'rep123',     'REP',     1);

-- Customers: 2 subscribers + 2 non-subscribers (for tests)
INSERT INTO customer (full_name, phone, email, is_subscribed, subscription_code) VALUES
('Sub Customer A', '0500000001', 'subA@test.com', 1, 'SUB-1111'),
('Sub Customer B', '0500000004', 'subB@test.com', 1, 'SUB-2222'),
('Walkin Customer', '0500000002', 'walk@test.com', 0, NULL),
('Regular Customer', '0500000003', 'reg@test.com', 0, NULL);

-- Tables (varied capacities)
INSERT INTO restaurant_table (capacity) VALUES
(2),(2),(4),(4),(6),(6),(8),(10);

-- Opening hours:
-- DayOfWeek mapping assumption: MON=1 ... SUN=7
-- Friday(5) closed, Saturday(6) closes early
INSERT INTO opening_hours (day_of_week, open_time, close_time, is_closed) VALUES
(1,'10:00:00','22:00:00',0),  -- Monday
(2,'10:00:00','22:00:00',0),  -- Tuesday
(3,'10:00:00','22:00:00',0),  -- Wednesday
(4,'10:00:00','22:00:00',0),  -- Thursday
(5, NULL,       NULL,      1),-- Friday closed
(6,'10:00:00','18:00:00',0),  -- Saturday close early
(7,'10:00:00','22:00:00',0);  -- Sunday

-- Date overrides:
-- (1) specific fixed date closes early (requested)
INSERT INTO date_override (override_date, open_time, close_time, is_closed, reason)
VALUES ('2026-02-10', '12:00:00', '16:00:00', 0, 'Special early closing');

-- (2) optional: tomorrow closed (useful for "canceled date / not available" scenarios)
INSERT INTO date_override (override_date, open_time, close_time, is_closed, reason)
VALUES (CURDATE() + INTERVAL 1 DAY, NULL, NULL, 1, 'Holiday');

-- =========================================================
-- Reservations seed (cover tests)
-- Status values used here:
-- ACTIVE / IN_PROGRESS / COMPLETED / CANCELED / WAITING / NOTIFIED
-- =========================================================

-- (ADV1) Future ACTIVE advance reservation (for viewing future availability / reminders)
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() + INTERVAL 3 HOUR, 4, 111111, 1, 3, NOW() - INTERVAL 1 DAY, 'ACTIVE', 0, 'ADVANCE', NULL, NULL);

-- (ADV2) No-show candidate: scheduled 20 minutes ago, still ACTIVE, not checked in
-- should be auto-canceled after 15 minutes if your scheduler checks it
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() - INTERVAL 20 MINUTE, 2, 222222, 3, 1, NOW() - INTERVAL 2 HOUR, 'ACTIVE', 1, 'ADVANCE', NULL, NULL);

-- (WALK1) Walk-in immediate table available: goes directly IN_PROGRESS (no NOTIFIED => no email/sms)
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW(), 2, 333331, 3, 2, NOW() - INTERVAL 2 MINUTE, 'IN_PROGRESS', 0, 'WALKIN', NOW() - INTERVAL 2 MINUTE, NULL);

-- (WL1) WAITING: entered waitlist 5 minutes ago, not notified yet
-- reservation_datetime stays NULL while waiting
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NULL, 2, 555551, 3, NULL, NOW() - INTERVAL 5 MINUTE, 'WAITING', 0, 'WALKIN', NULL, NULL);

-- (WL2) NOTIFIED recently: table became available, notified 3 minutes ago
-- We encode "notified time" into reservation_datetime (since you said it was NULL until table frees)
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() - INTERVAL 3 MINUTE, 4, 555552, 4, NULL, NOW() - INTERVAL 20 MINUTE, 'NOTIFIED', 0, 'WALKIN', NULL, NULL);

-- (WL3) NOTIFIED 오래: notified 20 minutes ago -> should be auto-canceled by "15 min after notified" rule
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() - INTERVAL 20 MINUTE, 2, 555553, 2, NULL, NOW() - INTERVAL 40 MINUTE, 'NOTIFIED', 0, 'WALKIN', NULL, NULL);

-- (WL4) NOTIFIED then received -> IN_PROGRESS (represents "customer receives freed table")
-- assigned a table now
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() - INTERVAL 25 MINUTE, 2, 555556, 2, 5, NOW() - INTERVAL 1 HOUR, 'IN_PROGRESS', 0, 'WALKIN',
 NOW() - INTERVAL 20 MINUTE, NULL);

-- (DONE1) COMPLETED advance reservation (has paid bill)
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() - INTERVAL 5 HOUR, 4, 444444, 1, 4, NOW() - INTERVAL 3 DAY, 'COMPLETED', 1, 'ADVANCE',
 NOW() - INTERVAL 5 HOUR + INTERVAL 5 MINUTE,
 NOW() - INTERVAL 3 HOUR);

INSERT INTO bill (reservation_id, amount_before_discount, final_amount, paid, paid_at)
SELECT r.reservation_id, 200.0, 160.0, 1, NOW() - INTERVAL 3 HOUR
FROM reservation r
WHERE r.confirmation_code = 444444;

-- (DONE2) COMPLETED walk-in subscriber paid with 10% discount scenario
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() - INTERVAL 3 HOUR, 2, 777777, 2, 6, NOW() - INTERVAL 1 DAY, 'COMPLETED', 0, 'WALKIN',
 NOW() - INTERVAL 3 HOUR + INTERVAL 2 MINUTE,
 NOW() - INTERVAL 1 HOUR);

INSERT INTO bill (reservation_id, amount_before_discount, final_amount, paid, paid_at)
SELECT r.reservation_id, 100.0, 90.0, 1, NOW() - INTERVAL 1 HOUR
FROM reservation r
WHERE r.confirmation_code = 777777;

-- (CANCEL1) CANCELED example (useful for history reports)
INSERT INTO reservation
(reservation_datetime, number_of_guests, confirmation_code, customer_id, table_id, created_at, status, reminder_sent, type, checked_in_at, checked_out_at)
VALUES
(NOW() - INTERVAL 2 DAY, 4, 888888, 4, 3, NOW() - INTERVAL 10 DAY, 'CANCELED', 0, 'ADVANCE', NULL, NULL);

-- =========================
-- Pre-seeded reports (optional but helps tests that "view stored reports")
-- =========================

-- Subscriber report for current month (8th month mention in spec: reports by month; this works for any month)
INSERT INTO subscriber_report (report_year, report_month, customer_id, customer_name, subscription_code,
                              total_reservations, completed_reservations, cancelled_reservations, waitlist_entries)
VALUES
(YEAR(CURDATE()), MONTH(CURDATE()), 1, 'Sub Customer A', 'SUB-1111', 3, 1, 0, 1),
(YEAR(CURDATE()), MONTH(CURDATE()), 2, 'Sub Customer B', 'SUB-2222', 2, 1, 0, 1);

-- Time report entry based on completed reservation 444444
INSERT INTO time_report (report_year, report_month, reservation_id, scheduled_time, checked_in_at, checked_out_at,
                         arrival_delay_minutes, session_duration_minutes, customer_name, is_subscriber)
SELECT
YEAR(CURDATE()), MONTH(CURDATE()),
r.reservation_id,
r.reservation_datetime,
r.checked_in_at,
r.checked_out_at,
5,
120,
'Sub Customer A',
1
FROM reservation r
WHERE r.confirmation_code = 444444;

-- =========================================================
-- End
-- =========================================================
