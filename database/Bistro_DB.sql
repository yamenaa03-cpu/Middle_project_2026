

DROP DATABASE IF EXISTS bistro_db;
CREATE DATABASE bistro_db ;
USE bistro_db;


SET FOREIGN_KEY_CHECKS = 0;

DROP VIEW IF EXISTS v_table_occupancy;

DROP TABLE IF EXISTS invoice_items;
DROP TABLE IF EXISTS invoices;
DROP TABLE IF EXISTS dining_sessions;
DROP TABLE IF EXISTS bookings;
DROP TABLE IF EXISTS staff_credentials;
DROP TABLE IF EXISTS members;
DROP TABLE IF EXISTS special_opening_hours;
DROP TABLE IF EXISTS weekly_opening_hours;
DROP TABLE IF EXISTS reports;
DROP TABLE IF EXISTS restaurant_tables;
DROP TABLE IF EXISTS people;

SET FOREIGN_KEY_CHECKS = 1;

-- ------------------------------------------
-- people (customers + staff)
-- ------------------------------------------
CREATE TABLE people (
  person_id     INT NOT NULL AUTO_INCREMENT,
  phone         VARCHAR(20)  DEFAULT NULL,
  email         VARCHAR(100) DEFAULT NULL,
  person_role   ENUM('GUEST','MEMBER','EMPLOYEE','MANAGER') NOT NULL,
  PRIMARY KEY (person_id),
  UNIQUE KEY uq_people_phone (phone),
  UNIQUE KEY uq_people_email (email),
  CONSTRAINT chk_people_contact CHECK (phone IS NOT NULL OR email IS NOT NULL)
) ENGINE=InnoDB;

-- ------------------------------------------
-- members (extra details for MEMBER people)
-- ------------------------------------------
CREATE TABLE members (
  person_id     INT NOT NULL,
  member_code   VARCHAR(50) NOT NULL,
  first_name    VARCHAR(50) NOT NULL,
  last_name     VARCHAR(50) NOT NULL,
  address       VARCHAR(150) NOT NULL,
  PRIMARY KEY (person_id),
  UNIQUE KEY uq_members_code (member_code),
  CONSTRAINT fk_members_person
    FOREIGN KEY (person_id) REFERENCES people(person_id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------
-- staff_credentials (login for employees/managers)
-- ------------------------------------------
CREATE TABLE staff_credentials (
  person_id      INT NOT NULL,
  username       VARCHAR(50)  NOT NULL,
  password_hash  VARCHAR(255) NOT NULL,
  PRIMARY KEY (person_id),
  UNIQUE KEY uq_staff_username (username),
  CONSTRAINT fk_staff_person
    FOREIGN KEY (person_id) REFERENCES people(person_id)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------
-- restaurant_tables
-- ------------------------------------------
CREATE TABLE restaurant_tables (
  table_id     INT NOT NULL,
  seats        INT NOT NULL,
  PRIMARY KEY (table_id),
  CONSTRAINT chk_tables_seats CHECK (seats > 0)
) ENGINE=InnoDB;

-- ------------------------------------------
-- weekly_opening_hours (1=Mon .. 7=Sun, choose and keep consistent in code)
-- ------------------------------------------
CREATE TABLE weekly_opening_hours (
  weekday      TINYINT NOT NULL,
  open_time    TIME NOT NULL,
  close_time   TIME NOT NULL,
  PRIMARY KEY (weekday),
  CONSTRAINT chk_weekday_range CHECK (weekday BETWEEN 1 AND 7),
  CONSTRAINT chk_week_hours CHECK (open_time < close_time)
) ENGINE=InnoDB;

-- ------------------------------------------
-- special_opening_hours (exceptions by date)
-- ------------------------------------------
CREATE TABLE special_opening_hours (
  calendar_date  DATE NOT NULL,
  is_closed      TINYINT NOT NULL DEFAULT 0,
  open_time      TIME DEFAULT NULL,
  close_time     TIME DEFAULT NULL,
  note           VARCHAR(255) DEFAULT NULL,
  PRIMARY KEY (calendar_date),
  CONSTRAINT chk_special_hours CHECK (
    (is_closed = 1 AND open_time IS NULL AND close_time IS NULL)
    OR
    (is_closed = 0 AND open_time IS NOT NULL AND close_time IS NOT NULL AND open_time < close_time)
  )
) ENGINE=InnoDB;

-- ------------------------------------------
-- bookings (reservation + waitlist combined)
-- - booking_type: RESERVATION vs WAITLIST
-- - for WAITLIST: booking_date/time must be NULL
-- - for RESERVATION: booking_date/time must be valid + time is on :00 or :30
-- ------------------------------------------
CREATE TABLE bookings (
  booking_id        INT NOT NULL AUTO_INCREMENT,
  confirmation_code INT NOT NULL,

  person_id         INT NOT NULL,
  party_size        INT NOT NULL,

  booking_date      DATE DEFAULT NULL,
  booking_time      TIME DEFAULT NULL,

  created_at        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

  booking_type      ENUM('RESERVATION','WAITLIST') NOT NULL,
  status            ENUM('PENDING','NOTIFIED','SEATED','CANCELLED','NO_SHOW','COMPLETED')
                   NOT NULL DEFAULT 'PENDING',

  notified_at       DATETIME DEFAULT NULL,
  cancelled_at      DATETIME DEFAULT NULL,

  PRIMARY KEY (booking_id),
  UNIQUE KEY uq_booking_confirmation (confirmation_code),

  KEY idx_booking_person (person_id),
  KEY idx_booking_slot (booking_date, booking_time),
  KEY idx_booking_status (status),

  CONSTRAINT fk_booking_person
    FOREIGN KEY (person_id) REFERENCES people(person_id),

  CONSTRAINT chk_party_size CHECK (party_size > 0),

  CONSTRAINT chk_booking_slot_rules CHECK (
    (booking_type = 'WAITLIST' AND booking_date IS NULL AND booking_time IS NULL)
    OR
    (booking_type = 'RESERVATION'
      AND booking_date IS NOT NULL
      AND booking_time IS NOT NULL
      AND SECOND(booking_time) = 0
      AND MINUTE(booking_time) IN (0,30)
    )
  )
) ENGINE=InnoDB;

-- ------------------------------------------
-- dining_sessions (actual seating / visit)
-- one session per booking
-- left_at NULL means still seated
-- ------------------------------------------
CREATE TABLE dining_sessions (
  session_id      INT NOT NULL AUTO_INCREMENT,
  booking_id      INT NOT NULL,
  table_id        INT NOT NULL,

  seated_at       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expected_end_at DATETIME DEFAULT NULL,
  left_at         DATETIME DEFAULT NULL,

  end_reason      ENUM('PAID','LEFT','NO_SHOW') DEFAULT NULL,

  PRIMARY KEY (session_id),
  UNIQUE KEY uq_session_booking (booking_id),
  KEY idx_sessions_open (table_id, left_at),

  CONSTRAINT fk_session_booking
    FOREIGN KEY (booking_id) REFERENCES bookings(booking_id)
    ON DELETE CASCADE,

  CONSTRAINT fk_session_table
    FOREIGN KEY (table_id) REFERENCES restaurant_tables(table_id)
    ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------
-- invoices (payment per session)
-- ------------------------------------------
CREATE TABLE invoices (
  invoice_id          INT NOT NULL AUTO_INCREMENT,
  session_id          INT NOT NULL,

  total_amount        DECIMAL(10,2) NOT NULL,        -- final total (after discount)
  subtotal_amount     DECIMAL(10,2) NOT NULL DEFAULT 0.00,
  discount_percent    DECIMAL(5,2)  NOT NULL DEFAULT 0.00,

  created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  paid_at             DATETIME DEFAULT NULL,

  payment_method      ENUM('CASH','CREDIT') DEFAULT NULL,
  payment_status      ENUM('UNPAID','PAID') NOT NULL DEFAULT 'UNPAID',

  PRIMARY KEY (invoice_id),
  UNIQUE KEY uq_invoice_session (session_id),

  CONSTRAINT fk_invoice_session
    FOREIGN KEY (session_id) REFERENCES dining_sessions(session_id)
    ON DELETE CASCADE,

  CONSTRAINT chk_invoice_amounts CHECK (
    subtotal_amount >= 0
    AND discount_percent >= 0
    AND total_amount >= 0
  )
) ENGINE=InnoDB;

-- ------------------------------------------
-- invoice_items (line items per invoice)
-- ------------------------------------------
CREATE TABLE invoice_items (
  invoice_item_id  INT NOT NULL AUTO_INCREMENT,
  invoice_id       INT NOT NULL,
  item_name        VARCHAR(100) NOT NULL,
  quantity         INT NOT NULL DEFAULT 1,
  unit_price       DECIMAL(10,2) NOT NULL DEFAULT 0.00,

  PRIMARY KEY (invoice_item_id),
  KEY idx_invoice_items_invoice (invoice_id),

  CONSTRAINT fk_invoice_items_invoice
    FOREIGN KEY (invoice_id) REFERENCES invoices(invoice_id)
    ON DELETE CASCADE,

  CONSTRAINT chk_item_price CHECK (unit_price >= 0),
  CONSTRAINT chk_item_qty CHECK (quantity > 0)
) ENGINE=InnoDB;

-- ------------------------------------------
-- reports (metadata only, same as your friend)
-- ------------------------------------------
CREATE TABLE reports (
  report_id    INT NOT NULL AUTO_INCREMENT,
  report_name  VARCHAR(45) NOT NULL,
  report_date  DATE NOT NULL,
  PRIMARY KEY (report_id),
  UNIQUE KEY uq_report_name_date (report_name, report_date)
) ENGINE=InnoDB;

-- ------------------------------------------
-- View: current table occupancy
-- occupied_now = 1 if there is an open session (left_at IS NULL)
-- ------------------------------------------
CREATE VIEW v_table_occupancy AS
SELECT
  t.table_id,
  t.seats,
  CASE
    WHEN EXISTS (
      SELECT 1
      FROM dining_sessions s
      WHERE s.table_id = t.table_id
        AND s.left_at IS NULL
    )
    THEN 1 ELSE 0
  END AS occupied_now
FROM restaurant_tables t;
