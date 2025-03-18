CREATE TABLE IF NOT EXISTS public.currency_rates
(
    id            SERIAL PRIMARY KEY,
    currency_type VARCHAR(10)    NOT NULL,
    currency      VARCHAR(10)    NOT NULL,
    rate          DECIMAL(20, 10) NOT NULL,
    last_updated TIMESTAMP default NOW() NOT NULL,
    UNIQUE (currency_type, currency)
);