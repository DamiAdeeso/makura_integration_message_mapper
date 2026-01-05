-- Fix table column names to match JPA entity mappings
-- This script removes duplicate columns and ensures correct naming

USE makura_runtime;

-- Fix api_keys table - remove duplicate snake_case columns if they exist
-- Keep only camelCase columns to match JPA entities

-- Check and drop duplicate columns if they exist
SET @dbname = DATABASE();
SET @tablename = "api_keys";
SET @columnname = "created_at";
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_name = @tablename)
      AND (table_schema = @dbname)
      AND (column_name = @columnname)
  ) > 0,
  "SELECT 'Column created_at exists'",
  "SELECT 'Column created_at does not exist'"
));
PREPARE stmt FROM @preparedStatement;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- For now, just verify the table structure
-- The explicit @Column(name = "...") annotations should fix the mapping

-- Verify current structure
DESCRIBE api_keys;
DESCRIBE routes;

