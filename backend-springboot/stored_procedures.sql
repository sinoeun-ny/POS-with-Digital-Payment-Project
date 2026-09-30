-- =============================================================================
-- SMARTFOOD_DB: PRODUCTION STORED PROCEDURES
-- =============================================================================
-- In a Spring Boot + JPA architecture, basic CRUD is handled by Spring Data JPA.
-- These stored procedures are reserved for atomic, multi-table transactions
-- and high-volume batch financial processing.
-- =============================================================================

USE smartfood_db;

DELIMITER $$

-- -----------------------------------------------------------------------------
-- 1. PROCEDURE: sp_process_daily_settlement
-- Purpose: Batch-processes all 'SETTLED' orders and transitions them to 'PAID_OUT'.
-- Can be run for a single merchant or across all merchants (pass NULL for all).
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_process_daily_settlement$$

CREATE PROCEDURE sp_process_daily_settlement(
    IN p_merchant_id BIGINT
)
proc_label: BEGIN
    DECLARE v_settled_count INT DEFAULT 0;
    DECLARE v_total_gross DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_total_commission DECIMAL(10,2) DEFAULT 0.00;
    DECLARE v_total_payout DECIMAL(10,2) DEFAULT 0.00;

    -- Error handler: rollback on any SQL exception
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SELECT 'FAILED' AS result_status, 'Transaction rolled back due to an error' AS message;
    END;

    START TRANSACTION;

    -- Count eligible records
    IF p_merchant_id IS NOT NULL THEN
        SELECT 
            COUNT(*), 
            COALESCE(SUM(gross_amount), 0.00),
            COALESCE(SUM(platform_fee), 0.00),
            COALESCE(SUM(merchant_payout), 0.00)
        INTO 
            v_settled_count, v_total_gross, v_total_commission, v_total_payout
        FROM settlements
        WHERE status = 'SETTLED' AND merchant_id = p_merchant_id;

        -- Update to PAID_OUT
        UPDATE settlements
        SET status = 'PAID_OUT'
        WHERE status = 'SETTLED' AND merchant_id = p_merchant_id;
    ELSE
        SELECT 
            COUNT(*), 
            COALESCE(SUM(gross_amount), 0.00),
            COALESCE(SUM(platform_fee), 0.00),
            COALESCE(SUM(merchant_payout), 0.00)
        INTO 
            v_settled_count, v_total_gross, v_total_commission, v_total_payout
        FROM settlements
        WHERE status = 'SETTLED';

        -- Update to PAID_OUT
        UPDATE settlements
        SET status = 'PAID_OUT'
        WHERE status = 'SETTLED';
    END IF;

    COMMIT;

    -- Return execution summary
    SELECT 
        'SUCCESS' AS result_status,
        v_settled_count AS orders_processed,
        v_total_gross AS total_gross_sales,
        v_total_commission AS platform_fee_retained,
        v_total_payout AS total_merchant_payout_disbursed,
        NOW() AS processed_at;
END proc_label$$


-- -----------------------------------------------------------------------------
-- 2. PROCEDURE: sp_cancel_order_with_refund
-- Purpose: Atomically cancels an order and reverses its settlement in one transaction.
-- Prevents cancellation if food preparation has already begun or delivered.
-- -----------------------------------------------------------------------------
DROP PROCEDURE IF EXISTS sp_cancel_order_with_refund$$

CREATE PROCEDURE sp_cancel_order_with_refund(
    IN  p_order_id   BIGINT,
    IN  p_reason     VARCHAR(255),
    OUT p_status     VARCHAR(50),
    OUT p_message    VARCHAR(255)
)
proc_label: BEGIN
    DECLARE v_current_status VARCHAR(30);
    DECLARE v_total_amount   DECIMAL(10,2);

    -- Error handler: rollback on exception
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        SET p_status = 'ERROR';
        SET p_message = 'Database error occurred during cancellation. Rolled back.';
    END;

    -- Check if order exists
    SELECT status, total_amount INTO v_current_status, v_total_amount
    FROM orders
    WHERE id = p_order_id;

    IF v_current_status IS NULL THEN
        SET p_status = 'NOT_FOUND';
        SET p_message = CONCAT('Order #', p_order_id, ' does not exist.');
        LEAVE proc_label;
    END IF;

    -- Reject cancellation if food is already being cooked or delivered
    IF v_current_status IN ('PREPARING', 'READY_FOR_PICKUP', 'PICKED_UP', 'OUT_FOR_DELIVERY', 'DELIVERED') THEN
        SET p_status = 'REJECTED';
        SET p_message = CONCAT('Cannot cancel order in status: ', v_current_status, '. Kitchen has already started preparing or dispatched.');
        LEAVE proc_label;
    END IF;

    IF v_current_status = 'CANCELLED' THEN
        SET p_status = 'ALREADY_CANCELLED';
        SET p_message = CONCAT('Order #', p_order_id, ' is already cancelled.');
        LEAVE proc_label;
    END IF;

    START TRANSACTION;

    -- 1. Update Order Status
    UPDATE orders
    SET status = 'CANCELLED'
    WHERE id = p_order_id;

    -- 2. Cancel the financial settlement (zero out payouts)
    UPDATE settlements
    SET 
        status = 'CANCELLED',
        merchant_payout = 0.00,
        driver_payout = 0.00,
        platform_fee = 0.00
    WHERE order_id = p_order_id;

    COMMIT;

    SET p_status = 'SUCCESS';
    SET p_message = CONCAT('Order #', p_order_id, ' cancelled. Refund of $', v_total_amount, ' queued. Reason: ', COALESCE(p_reason, 'Customer Request'));
END proc_label$$

DELIMITER ;
