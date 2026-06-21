USE oa_system;

CREATE TABLE IF NOT EXISTS oa_approval_history (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  apply_type VARCHAR(32) NOT NULL,
  apply_id BIGINT NOT NULL,
  approver_id BIGINT,
  approver_name VARCHAR(64),
  approval_level INT NOT NULL DEFAULT 1,
  result VARCHAR(32) NOT NULL,
  audit_comment VARCHAR(500),
  create_time DATETIME,
  KEY idx_approval_apply (apply_type, apply_id),
  KEY idx_approval_approver (approver_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_notice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  title VARCHAR(128) NOT NULL,
  content VARCHAR(500),
  read_flag TINYINT(1) NOT NULL DEFAULT 0,
  create_time DATETIME,
  KEY idx_notice_user_read (user_id, read_flag),
  KEY idx_notice_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sys_operation_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  operator_id BIGINT,
  operator_name VARCHAR(64),
  module_name VARCHAR(64),
  operation_type VARCHAR(32),
  target_type VARCHAR(64),
  target_id BIGINT,
  content VARCHAR(500),
  create_time DATETIME,
  KEY idx_operation_operator (operator_id),
  KEY idx_operation_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
