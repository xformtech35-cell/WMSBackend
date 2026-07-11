-- Add PURCHASE_VIEW permission to all existing roles
INSERT IGNORE INTO wms_role_permission(role_id, permission)
SELECT id, 'PURCHASE_VIEW' FROM wms_role;
