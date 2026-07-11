-- V8__wms_feature_closure.sql
-- Add perishable/FEFO and batch columns to existing tables
ALTER TABLE wms_sku ADD COLUMN is_perishable TINYINT(1) DEFAULT 0;
ALTER TABLE wms_inventory ADD COLUMN manufacture_date DATETIME NULL;
ALTER TABLE wms_inventory ADD COLUMN expiry_date DATETIME NULL;

-- 1. Table for Batch Tracking
CREATE TABLE IF NOT EXISTS wms_stock_batch (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    bin_id BIGINT NULL,
    batch_number VARCHAR(255) NOT NULL,
    quantity INT NOT NULL,
    manufacture_date DATETIME NULL,
    expiry_date DATETIME NULL,
    status VARCHAR(50) NOT NULL, -- ACTIVE, NEAR_EXPIRY, EXPIRED, QUARANTINED
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_wms_stock_batch_sku FOREIGN KEY (sku_id) REFERENCES wms_sku(id) ON DELETE CASCADE,
    CONSTRAINT fk_wms_stock_batch_bin FOREIGN KEY (bin_id) REFERENCES wms_bin(id) ON DELETE SET NULL
);

-- 2. Tables for Cycle Counting & Inventory Auditing
CREATE TABLE IF NOT EXISTS wms_count_task (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    zone_id BIGINT NULL,
    bin_id BIGINT NULL,
    sku_id BIGINT NULL, -- nullable (if null, count full bin/zone)
    assigned_to BIGINT NULL,
    status VARCHAR(50) NOT NULL, -- SCHEDULED, IN_PROGRESS, COMPLETED, VARIANCE_REVIEW, CLOSED
    scheduled_date DATETIME NOT NULL,
    completed_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_wms_count_task_zone FOREIGN KEY (zone_id) REFERENCES wms_zone(id) ON DELETE SET NULL,
    CONSTRAINT fk_wms_count_task_bin FOREIGN KEY (bin_id) REFERENCES wms_bin(id) ON DELETE SET NULL,
    CONSTRAINT fk_wms_count_task_sku FOREIGN KEY (sku_id) REFERENCES wms_sku(id) ON DELETE SET NULL,
    CONSTRAINT fk_wms_count_task_user FOREIGN KEY (assigned_to) REFERENCES wms__user(id) ON DELETE SET NULL
);

CREATE TABLE IF NOT EXISTS wms_count_line (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    count_task_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    batch_number VARCHAR(255) NULL,
    expected_qty INT NOT NULL,
    counted_qty INT NOT NULL,
    variance INT NOT NULL,
    reason_code VARCHAR(50) NULL, -- DAMAGE, THEFT, MISCOUNT, SYSTEM_ERROR, FOUND_STOCK
    status VARCHAR(50) NOT NULL, -- PENDING, APPROVED, REJECTED
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_wms_count_line_task FOREIGN KEY (count_task_id) REFERENCES wms_count_task(id) ON DELETE CASCADE,
    CONSTRAINT fk_wms_count_line_sku FOREIGN KEY (sku_id) REFERENCES wms_sku(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wms_stock_adjustment (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sku_id BIGINT NOT NULL,
    bin_id BIGINT NULL,
    batch_number VARCHAR(255) NULL,
    quantity_adjusted INT NOT NULL,
    reason VARCHAR(255) NULL,
    adjusted_by BIGINT NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_wms_stock_adj_sku FOREIGN KEY (sku_id) REFERENCES wms_sku(id) ON DELETE CASCADE,
    CONSTRAINT fk_wms_stock_adj_bin FOREIGN KEY (bin_id) REFERENCES wms_bin(id) ON DELETE SET NULL,
    CONSTRAINT fk_wms_stock_adj_user FOREIGN KEY (adjusted_by) REFERENCES wms__user(id) ON DELETE SET NULL
);

-- 3. Tables for Returns / RMA Flow
CREATE TABLE IF NOT EXISTS wms_return_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    original_order_id BIGINT NOT NULL,
    customer_ref VARCHAR(255) NULL,
    status VARCHAR(50) NOT NULL, -- RETURN_REQUESTED, AWAITING_PICKUP, IN_TRANSIT, RECEIVED, INSPECTING, RESTOCKED, SCRAPPED, REJECTED, REFUND_TRIGGERED, CLOSED
    created_at DATETIME NOT NULL,
    updated_at DATETIME NOT NULL,
    CONSTRAINT fk_wms_return_order_sales FOREIGN KEY (original_order_id) REFERENCES wms_sales_order(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS wms_return_line (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    return_order_id BIGINT NOT NULL,
    sku_id BIGINT NOT NULL,
    ordered_qty INT NOT NULL,
    returned_qty INT NOT NULL,
    condition_grade VARCHAR(50) NULL, -- RESELLABLE, DAMAGED, SCRAP
    inspection_notes VARCHAR(1000) NULL,
    restocked_bin_id BIGINT NULL,
    batch_number VARCHAR(255) NULL,
    CONSTRAINT fk_wms_return_line_order FOREIGN KEY (return_order_id) REFERENCES wms_return_order(id) ON DELETE CASCADE,
    CONSTRAINT fk_wms_return_line_sku FOREIGN KEY (sku_id) REFERENCES wms_sku(id) ON DELETE CASCADE,
    CONSTRAINT fk_wms_return_line_bin FOREIGN KEY (restocked_bin_id) REFERENCES wms_bin(id) ON DELETE SET NULL
);

-- 4. Table for System Audit Logging
CREATE TABLE IF NOT EXISTS wms_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NULL,
    module VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id BIGINT NULL,
    action VARCHAR(100) NOT NULL,
    old_value_json TEXT NULL,
    new_value_json TEXT NULL,
    timestamp DATETIME NOT NULL,
    CONSTRAINT fk_wms_audit_log_user FOREIGN KEY (user_id) REFERENCES wms__user(id) ON DELETE SET NULL
);

-- 5. Table for Carrier API Requests/Retries Queue
CREATE TABLE IF NOT EXISTS wms_carrier_request_queue (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    shipment_id BIGINT NOT NULL,
    carrier_name VARCHAR(100) NOT NULL,
    request_type VARCHAR(100) NOT NULL,
    request_body TEXT NOT NULL,
    status VARCHAR(50) NOT NULL, -- PENDING, RETRYING, SUCCESS, FAILED
    error_message TEXT NULL,
    retry_count INT DEFAULT 0,
    next_run_at DATETIME NULL,
    created_at DATETIME NOT NULL,
    CONSTRAINT fk_wms_carrier_req_shipment FOREIGN KEY (shipment_id) REFERENCES wms_shipment_record(id) ON DELETE CASCADE
);

-- Add returns & cycle count permissions to roles module
INSERT IGNORE INTO wms_role_permission(role_id, permission)
SELECT id, 'CYCLE_COUNT_VIEW' FROM wms_role WHERE name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'WORKER');

INSERT IGNORE INTO wms_role_permission(role_id, permission)
SELECT id, 'CYCLE_COUNT_EXECUTE' FROM wms_role WHERE name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'WORKER');

INSERT IGNORE INTO wms_role_permission(role_id, permission)
SELECT id, 'RETURNS_VIEW' FROM wms_role WHERE name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'WORKER');

INSERT IGNORE INTO wms_role_permission(role_id, permission)
SELECT id, 'RETURNS_EXECUTE' FROM wms_role WHERE name IN ('SUPER_ADMIN', 'ADMIN', 'MANAGER', 'WORKER');
