CREATE TABLE transactions (
    id UUID PRIMARY KEY,
    order_id VARCHAR(255) UNIQUE NOT NULL,
    channel VARCHAR(20) NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    account VARCHAR(255) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'IDR',
    payment_method VARCHAR(255) NOT NULL,
    status VARCHAR(10) NOT NULL,
    corebank_reference VARCHAR(255),
    biller_reference VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT NULL
);
