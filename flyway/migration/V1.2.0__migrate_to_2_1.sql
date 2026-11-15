CREATE INDEX IF NOT EXISTS idx_expense_payment_date ON expense.expense(payment_date);

CREATE INDEX IF NOT EXISTS idx_income_receive_date ON expense.income(receive_date);

CREATE INDEX IF NOT EXISTS idx_income_attribute_id ON expense.income(attribute_id);

ALTER TABLE expense.expense ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE expense.attribute ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE expense.income_attribute ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE expense.income ALTER COLUMN created_at SET DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE expense.attribute ADD CONSTRAINT uq_attribute_name_category UNIQUE (name, category);

ALTER TABLE expense.income_attribute ADD CONSTRAINT uq_income_attribute_name UNIQUE (name);

ALTER TABLE expense.income ADD CONSTRAINT ck_income_amount_positive CHECK (amount > 0);

ALTER TABLE expense.expense RENAME COLUMN price TO amount;

ALTER TABLE expense.expense ADD CONSTRAINT ck_expense_amount_positive CHECK (amount > 0);

ALTER TABLE expense.expense ADD COLUMN attribute_id CHAR(36);

UPDATE expense.expense AS e
SET attribute_id = COALESCE(
    (SELECT attribute_id FROM expense.fixed_expense WHERE expense_id = e.id),
    (SELECT attribute_id FROM expense.variable_expense WHERE expense_id = e.id)
);

ALTER TABLE expense.expense ALTER COLUMN attribute_id SET NOT NULL;

ALTER TABLE expense.expense
    ADD CONSTRAINT fk_expense_attribute FOREIGN KEY (attribute_id) REFERENCES expense.attribute (id);

CREATE INDEX idx_expense_attribute_id ON expense.expense(attribute_id);

DROP TABLE expense.fixed_expense;

DROP TABLE expense.variable_expense;

ALTER TABLE expense.attribute ADD COLUMN sort_order INTEGER;

ALTER TABLE expense.income_attribute ADD COLUMN sort_order INTEGER;

UPDATE expense.attribute
SET sort_order = ranked.position
FROM (
    SELECT id, row_number() OVER (PARTITION BY category ORDER BY created_at, id) AS position
    FROM expense.attribute
) AS ranked
WHERE attribute.id = ranked.id;

UPDATE expense.income_attribute
SET sort_order = ranked.position
FROM (
    SELECT id, row_number() OVER (ORDER BY created_at, id) AS position
    FROM expense.income_attribute
) AS ranked
WHERE income_attribute.id = ranked.id;

ALTER TABLE expense.attribute ALTER COLUMN sort_order SET NOT NULL;

ALTER TABLE expense.income_attribute ALTER COLUMN sort_order SET NOT NULL;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE FUNCTION expense.jcs_string(s TEXT) RETURNS TEXT IMMUTABLE LANGUAGE SQL AS $$
SELECT '"' || replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(replace(s, chr(92), chr(92) || chr(92)), chr(34), chr(92) || chr(34)), chr(1), '\u0001'), chr(2), '\u0002'), chr(3), '\u0003'), chr(4), '\u0004'), chr(5), '\u0005'), chr(6), '\u0006'), chr(7), '\u0007'), chr(8), '\b'), chr(9), '\t'), chr(10), '\n'), chr(11), '\u000b'), chr(12), '\f'), chr(13), '\r'), chr(14), '\u000e'), chr(15), '\u000f'), chr(16), '\u0010'), chr(17), '\u0011'), chr(18), '\u0012'), chr(19), '\u0013'), chr(20), '\u0014'), chr(21), '\u0015'), chr(22), '\u0016'), chr(23), '\u0017'), chr(24), '\u0018'), chr(25), '\u0019'), chr(26), '\u001a'), chr(27), '\u001b'), chr(28), '\u001c'), chr(29), '\u001d'), chr(30), '\u001e'), chr(31), '\u001f') || '"'
$$;

ALTER TABLE expense.attribute ADD COLUMN document_digest BYTEA;

ALTER TABLE expense.expense ADD COLUMN document_digest BYTEA;

ALTER TABLE expense.income_attribute ADD COLUMN document_digest BYTEA;

ALTER TABLE expense.income ADD COLUMN document_digest BYTEA;

UPDATE expense.attribute
SET document_digest = digest(convert_to('{"category":' || expense.jcs_string(category::TEXT) || ',"name":' || expense.jcs_string(name::TEXT) || '}', 'UTF8'), 'sha256');

UPDATE expense.expense
SET document_digest = digest(convert_to('{"amount":' || amount::TEXT || ',"attribute_id":' || expense.jcs_string(attribute_id::TEXT) || ',"description":' || expense.jcs_string(description::TEXT) || ',"payment_date":' || expense.jcs_string(payment_date::TEXT) || '}', 'UTF8'), 'sha256');

UPDATE expense.income_attribute
SET document_digest = digest(convert_to('{"name":' || expense.jcs_string(name::TEXT) || '}', 'UTF8'), 'sha256');

UPDATE expense.income
SET document_digest = digest(convert_to('{"amount":' || amount::TEXT || ',"attribute_id":' || expense.jcs_string(attribute_id::TEXT) || ',"description":' || expense.jcs_string(description::TEXT) || ',"receive_date":' || expense.jcs_string(receive_date::TEXT) || '}', 'UTF8'), 'sha256');

ALTER TABLE expense.attribute ALTER COLUMN document_digest SET NOT NULL;

ALTER TABLE expense.expense ALTER COLUMN document_digest SET NOT NULL;

ALTER TABLE expense.income_attribute ALTER COLUMN document_digest SET NOT NULL;

ALTER TABLE expense.income ALTER COLUMN document_digest SET NOT NULL;

DROP FUNCTION expense.jcs_string(TEXT);
