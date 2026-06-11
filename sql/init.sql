CREATE DATABASE IF NOT EXISTS oa_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE oa_system;

DROP TABLE IF EXISTS sys_role_permission;
DROP TABLE IF EXISTS sys_user_role;
DROP TABLE IF EXISTS sys_permission;
DROP TABLE IF EXISTS sys_role;
DROP TABLE IF EXISTS sys_user;
DROP TABLE IF EXISTS oa_leave_apply;
DROP TABLE IF EXISTS oa_trip_apply;
DROP TABLE IF EXISTS oa_reimbursement_apply;
DROP TABLE IF EXISTS oa_attendance;
DROP TABLE IF EXISTS oa_salary;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL UNIQUE,
  password VARCHAR(120) NOT NULL,
  real_name VARCHAR(64),
  phone VARCHAR(32),
  email VARCHAR(128),
  department VARCHAR(64),
  enabled TINYINT(1) NOT NULL DEFAULT 1,
  create_time DATETIME,
  update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_code VARCHAR(64) NOT NULL UNIQUE,
  role_name VARCHAR(64) NOT NULL,
  remark VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  permission_code VARCHAR(64) NOT NULL UNIQUE,
  permission_name VARCHAR(64) NOT NULL,
  path VARCHAR(255),
  type VARCHAR(32),
  create_time DATETIME,
  update_time DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_user_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  UNIQUE KEY uk_user_role (user_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE sys_role_permission (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  role_id BIGINT NOT NULL,
  permission_id BIGINT NOT NULL,
  UNIQUE KEY uk_role_permission (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oa_leave_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  username VARCHAR(64),
  reason VARCHAR(500),
  start_date DATE,
  end_date DATE,
  day_count DECIMAL(8,2),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  audit_comment VARCHAR(500),
  approve_time DATETIME,
  create_time DATETIME,
  update_time DATETIME,
  KEY idx_leave_user (user_id),
  KEY idx_leave_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oa_trip_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  username VARCHAR(64),
  destination VARCHAR(128),
  reason VARCHAR(500),
  start_date DATE,
  end_date DATE,
  budget DECIMAL(12,2),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  audit_comment VARCHAR(500),
  approve_time DATETIME,
  create_time DATETIME,
  update_time DATETIME,
  KEY idx_trip_user (user_id),
  KEY idx_trip_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oa_reimbursement_apply (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  username VARCHAR(64),
  title VARCHAR(128),
  amount DECIMAL(12,2),
  detail VARCHAR(500),
  status VARCHAR(32) NOT NULL DEFAULT 'PENDING',
  audit_comment VARCHAR(500),
  approve_time DATETIME,
  create_time DATETIME,
  update_time DATETIME,
  KEY idx_reimbursement_user (user_id),
  KEY idx_reimbursement_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oa_attendance (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  username VARCHAR(64),
  work_date DATE NOT NULL,
  clock_in_time DATETIME,
  clock_out_time DATETIME,
  status VARCHAR(32) NOT NULL DEFAULT 'NORMAL',
  remark VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  UNIQUE KEY uk_attendance_user_date (user_id, work_date),
  KEY idx_attendance_date (work_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE oa_salary (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  username VARCHAR(64),
  salary_month VARCHAR(16) NOT NULL,
  base_salary DECIMAL(12,2) DEFAULT 0,
  bonus DECIMAL(12,2) DEFAULT 0,
  deduction DECIMAL(12,2) DEFAULT 0,
  total_salary DECIMAL(12,2) DEFAULT 0,
  remark VARCHAR(255),
  create_time DATETIME,
  update_time DATETIME,
  UNIQUE KEY uk_salary_user_month (user_id, salary_month),
  KEY idx_salary_month (salary_month)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

