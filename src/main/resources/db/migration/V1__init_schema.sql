CREATE TABLE IF NOT EXISTS transactions (
    id BIGSERIAL PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    transaction_type VARCHAR(50),
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS customer_rewards (
    customer_id BIGINT PRIMARY KEY,
    loyalty_points INTEGER NOT NULL DEFAULT 0,
    tier VARCHAR(20) NOT NULL,
    total_spent DECIMAL(19,2) NOT NULL DEFAULT 0.00,
    version BIGINT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_transaction_processed ON transactions(processed);
CREATE INDEX IF NOT EXISTS idx_transaction_date ON transactions(transaction_date);