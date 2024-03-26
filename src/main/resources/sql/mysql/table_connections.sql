SELECT
    tc.TABLE_SCHEMA,
    tc.CONSTRAINT_NAME,
    tc.TABLE_NAME,
    kcu.COLUMN_NAME,
    kcu.REFERENCED_TABLE_SCHEMA AS foreign_table_schema,
    kcu.REFERENCED_TABLE_NAME AS foreign_table_name,
    kcu.REFERENCED_COLUMN_NAME AS foreign_column_name
FROM
    INFORMATION_SCHEMA.TABLE_CONSTRAINTS AS tc
        JOIN information_schema.KEY_COLUMN_USAGE AS kcu
             ON tc.CONSTRAINT_NAME = kcu.CONSTRAINT_NAME
                 AND tc.CONSTRAINT_SCHEMA = kcu.CONSTRAINT_SCHEMA
        JOIN information_schema.KEY_COLUMN_USAGE AS kcu2
             ON tc.CONSTRAINT_NAME = kcu2.CONSTRAINT_NAME
WHERE
    tc.CONSTRAINT_TYPE = 'FOREIGN KEY' AND
    tc.TABLE_NAME = '%s';