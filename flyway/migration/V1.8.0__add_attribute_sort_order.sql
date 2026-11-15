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
