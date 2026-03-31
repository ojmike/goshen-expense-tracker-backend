CREATE TABLE loans (
    id         BIGSERIAL PRIMARY KEY,
    user_id    BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    original_amount NUMERIC(19, 4) NOT NULL CHECK (original_amount > 0),
    created_at TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_loans_user_id ON loans(user_id);

CREATE TABLE loan_payments (
    id           BIGSERIAL PRIMARY KEY,
    loan_id      BIGINT        NOT NULL REFERENCES loans(id) ON DELETE CASCADE,
    amount       NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    payment_date DATE          NOT NULL,
    note         VARCHAR(200),
    created_at   TIMESTAMP     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_loan_payments_loan_id ON loan_payments(loan_id);
CREATE INDEX idx_loan_payments_loan_date ON loan_payments(loan_id, payment_date);
