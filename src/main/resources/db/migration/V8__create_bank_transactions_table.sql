CREATE TABLE bank_transactions (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT          NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    linked_account_id   BIGINT          NOT NULL REFERENCES linked_accounts(id) ON DELETE CASCADE,
    category_id         BIGINT          REFERENCES expense_categories(id) ON DELETE SET NULL,
    plaid_transaction_id VARCHAR(200)   NOT NULL UNIQUE,
    name                VARCHAR(500)    NOT NULL,
    amount              NUMERIC(19, 4)  NOT NULL,
    transaction_date    DATE            NOT NULL,
    plaid_category      VARCHAR(500),
    reviewed            BOOLEAN         NOT NULL DEFAULT FALSE,
    created_at          TIMESTAMP       NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_bank_transactions_user_id ON bank_transactions(user_id);
CREATE INDEX idx_bank_transactions_linked_account_id ON bank_transactions(linked_account_id);
CREATE INDEX idx_bank_transactions_date ON bank_transactions(transaction_date);
