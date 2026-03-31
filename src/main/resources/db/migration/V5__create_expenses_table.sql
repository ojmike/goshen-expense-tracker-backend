CREATE TABLE expenses (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES expense_categories(id),
    name VARCHAR(200) NOT NULL,
    amount NUMERIC(19, 4) NOT NULL CHECK (amount > 0),
    expense_type VARCHAR(20) NOT NULL CHECK (expense_type IN ('RECURRING', 'ONE_TIME')),
    expense_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_expenses_user_id ON expenses(user_id);
CREATE INDEX idx_expenses_user_date ON expenses(user_id, expense_date);
CREATE INDEX idx_expenses_category_id ON expenses(category_id);
