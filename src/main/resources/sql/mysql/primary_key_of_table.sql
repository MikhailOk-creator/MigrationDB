SELECT DISTINCT
    c.COLUMN_NAME as column_name, c.DATA_TYPE
FROM
    INFORMATION_SCHEMA.TABLE_CONSTRAINTS tc
        JOIN information_schema.KEY_COLUMN_USAGE AS ku USING (TABLE_SCHEMA, CONSTRAINT_NAME)
        JOIN information_schema.COLUMNS AS c ON c.TABLE_SCHEMA = tc.TABLE_SCHEMA
        AND tc.TABLE_NAME = c.TABLE_NAME
        AND ku.COLUMN_NAME = c.COLUMN_NAME
WHERE
    CONSTRAINT_TYPE = 'PRIMARY KEY' AND tc.TABLE_NAME = '%s';