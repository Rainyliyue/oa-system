USE oa_system;

ALTER TABLE oa_leave_apply
  ADD COLUMN evidence_image VARCHAR(255) NULL AFTER reason;

ALTER TABLE oa_trip_apply
  ADD COLUMN evidence_image VARCHAR(255) NULL AFTER reason;

ALTER TABLE oa_reimbursement_apply
  ADD COLUMN evidence_image VARCHAR(255) NULL AFTER detail;
