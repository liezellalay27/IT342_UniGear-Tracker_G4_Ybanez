-- Normalize category labels: lowercase, replace non-alnum with space, trim, Title Case
-- This updates both equipment and equipment_requests tables.

-- Normalize equipment categories
UPDATE equipment
SET category = initcap(trim(regexp_replace(lower(category), '[^a-z0-9]+', ' ', 'g')))
WHERE category IS NOT NULL AND trim(category) <> '';

-- Normalize equipment_requests categories
UPDATE equipment_requests
SET category = initcap(trim(regexp_replace(lower(category), '[^a-z0-9]+', ' ', 'g')))
WHERE category IS NOT NULL AND trim(category) <> '';

-- After migration, duplicates may exist if multiple categories normalized to the same label.
-- Consider manual review for de-duplicating equipment rows that now share the same category name.
