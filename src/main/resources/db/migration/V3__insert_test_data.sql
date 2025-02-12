-- Insert test transactions with different dates and amounts
INSERT INTO transactions (customer_id, amount, transaction_date, transaction_type, processed)
VALUES
-- Customer 1001 (Regular shopper with mixed purchases)
(1001, 100.00, CURRENT_TIMESTAMP - INTERVAL '30 days', 'PURCHASE', false),
(1001, 250.50, CURRENT_TIMESTAMP - INTERVAL '25 days', 'PURCHASE', false),
(1001, 75.25, CURRENT_TIMESTAMP - INTERVAL '20 days', 'PURCHASE', false),
(1001, 1000.00, CURRENT_TIMESTAMP - INTERVAL '15 days', 'PURCHASE', false),
(1001, 50.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'PURCHASE', false),

-- Customer 1002 (Big spender)
(1002, 2500.00, CURRENT_TIMESTAMP - INTERVAL '28 days', 'PURCHASE', false),
(1002, 3000.00, CURRENT_TIMESTAMP - INTERVAL '21 days', 'PURCHASE', false),
(1002, 1500.00, CURRENT_TIMESTAMP - INTERVAL '14 days', 'PURCHASE', false),
(1002, 4000.00, CURRENT_TIMESTAMP - INTERVAL '7 days', 'PURCHASE', false),
(1002, 5000.00, CURRENT_TIMESTAMP - INTERVAL '1 day', 'PURCHASE', false),

-- Customer 1003 (Occasional shopper)
(1003, 150.00, CURRENT_TIMESTAMP - INTERVAL '60 days', 'PURCHASE', false),
(1003, 200.00, CURRENT_TIMESTAMP - INTERVAL '45 days', 'PURCHASE', false),
(1003, 75.00, CURRENT_TIMESTAMP - INTERVAL '30 days', 'PURCHASE', false),

-- Customer 1004 (New customer with recent purchases)
(1004, 500.00, CURRENT_TIMESTAMP - INTERVAL '5 days', 'PURCHASE', false),
(1004, 750.00, CURRENT_TIMESTAMP - INTERVAL '3 days', 'PURCHASE', false),
(1004, 1200.00, CURRENT_TIMESTAMP - INTERVAL '1 day', 'PURCHASE', false),

-- Customer 1005 (Luxury shopper)
(1005, 10000.00, CURRENT_TIMESTAMP - INTERVAL '15 days', 'PURCHASE', false),
(1005, 15000.00, CURRENT_TIMESTAMP - INTERVAL '10 days', 'PURCHASE', false),
(1005, 8000.00, CURRENT_TIMESTAMP - INTERVAL '5 days', 'PURCHASE', false),

-- Customer 1006 (Small, frequent purchases)
(1006, 25.00, CURRENT_TIMESTAMP - INTERVAL '5 days', 'PURCHASE', false),
(1006, 30.00, CURRENT_TIMESTAMP - INTERVAL '4 days', 'PURCHASE', false),
(1006, 45.00, CURRENT_TIMESTAMP - INTERVAL '3 days', 'PURCHASE', false),
(1006, 35.00, CURRENT_TIMESTAMP - INTERVAL '2 days', 'PURCHASE', false),
(1006, 40.00, CURRENT_TIMESTAMP - INTERVAL '1 day', 'PURCHASE', false);

-- Insert initial customer rewards
INSERT INTO customer_rewards (customer_id, loyalty_points, tier, total_spent)
VALUES
(1001, 0, 'BRONZE', 0.00),  -- Regular customer starting point
(1002, 0, 'BRONZE', 0.00),  -- Future VIP customer
(1003, 0, 'BRONZE', 0.00),  -- Occasional shopper
(1004, 0, 'BRONZE', 0.00),  -- New customer
(1005, 0, 'BRONZE', 0.00),  -- Luxury customer
(1006, 0, 'BRONZE', 0.00);  -- Frequent small purchaser

-- Add some processed transactions for history
INSERT INTO transactions (customer_id, amount, transaction_date, transaction_type, processed)
VALUES
(1001, 120.00, CURRENT_TIMESTAMP - INTERVAL '90 days', 'PURCHASE', true),
(1001, 180.00, CURRENT_TIMESTAMP - INTERVAL '85 days', 'PURCHASE', true),
(1002, 2000.00, CURRENT_TIMESTAMP - INTERVAL '95 days', 'PURCHASE', true),
(1003, 300.00, CURRENT_TIMESTAMP - INTERVAL '100 days', 'PURCHASE', true);