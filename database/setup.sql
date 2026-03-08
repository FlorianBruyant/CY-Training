DROP TABLE IF EXISTS account;

CREATE TABLE account(
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,

    email TEXT NOT NULL UNIQUE,

    password VARCHAR(100) NOT NULL,

    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,

    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),

    -- email format validation
    CONSTRAINT account_email_format_constraint
        CHECK (
            email ~* '^[a-z0-9._%+-]+@[a-z0-9.-]+\.[a-z]{2,}$'
        ),

    -- prevent empty names
    CONSTRAINT account_first_name_not_blank_constraint
        CHECK (length(trim(first_name)) > 0),

    CONSTRAINT account_last_name_not_blank_constraint
        CHECK (length(trim(last_name)) > 0),

    -- check password (hashed) is not weirdly short
    CONSTRAINT account_password_length_constraint
        CHECK (length(password) >= 32)
);

-- equivalent of ON UPDATE on mysql
CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_set_updated_at
BEFORE UPDATE ON account
FOR EACH ROW
EXECUTE FUNCTION set_updated_at();

-- index to speed up email lookups
CREATE UNIQUE INDEX account_email_index ON account(email);