SELECT
    COLUMN_NAME AS column_name,
    TABLE_SCHEMA AS table_schema,
    ORDINAL_POSITION AS ordinal_position,
    IS_NULLABLE AS is_nullable,
    COLUMN_TYPE AS udt_name,
    COLUMN_KEY AS is_identity,
    EXTRA AS identity_generation,
    COLUMN_DEFAULT AS column_default
FROM
    INFORMATION_SCHEMA.COLUMNS
WHERE
    TABLE_NAME = '%s';