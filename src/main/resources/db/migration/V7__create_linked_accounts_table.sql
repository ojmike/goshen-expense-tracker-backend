CREATE TABLE linked_accounts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT       NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token    VARCHAR(500) NOT NULL,
    item_id         VARCHAR(200) NOT NULL UNIQUE,
    institution_name VARCHAR(200),
    account_name    VARCHAR(200),
    account_mask    VARCHAR(10),
    cursor          TEXT,
    created_at      TIMESTAMP    NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_linked_accounts_user_id ON linked_accounts(user_id);
