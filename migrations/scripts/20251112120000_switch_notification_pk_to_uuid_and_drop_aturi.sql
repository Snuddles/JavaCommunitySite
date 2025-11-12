-- ============================================
-- //UP
-- ============================================
-- Switch primary key from `aturi` → `id` (UUID) and drop `aturi` column.
-- Extension-free Azure-safe migration.

-- 1. Add UUID id column (nullable for now)
ALTER TABLE public.notification
  ADD COLUMN IF NOT EXISTS id UUID;

-- 2. Backfill existing rows with valid UUIDs
UPDATE public.notification
SET id = (
  (
    substr(md5(random()::text), 1, 8) || '-' ||
    substr(md5(random()::text), 1, 4) || '-' ||
    substr(md5(random()::text), 1, 4) || '-' ||
    substr(md5(random()::text), 1, 4) || '-' ||
    substr(md5(random()::text), 1, 12)
  )::uuid
)
WHERE id IS NULL;

-- 3. Make id non-nullable and assign a default UUID generator
ALTER TABLE public.notification
  ALTER COLUMN id SET NOT NULL,
  ALTER COLUMN id SET DEFAULT (
    (
      substr(md5(random()::text), 1, 8) || '-' ||
      substr(md5(random()::text), 1, 4) || '-' ||
      substr(md5(random()::text), 1, 4) || '-' ||
      substr(md5(random()::text), 1, 4) || '-' ||
      substr(md5(random()::text), 1, 12)
    )::uuid
  );

-- 4. Replace old primary key with new one
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_pkey;
ALTER TABLE public.notification ADD CONSTRAINT notification_pkey PRIMARY KEY (id);

-- 5. Drop the old `aturi` column (no longer needed)
ALTER TABLE public.notification
  DROP COLUMN IF EXISTS aturi;

-- ============================================

-- //@UNDO
-- ============================================
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_pkey;
ALTER TABLE public.notification ADD COLUMN IF NOT EXISTS aturi TEXT DEFAULT 'at://notification/temp'::text NOT NULL;
ALTER TABLE public.notification ADD CONSTRAINT notification_pkey PRIMARY KEY (aturi);
ALTER TABLE public.notification DROP COLUMN IF EXISTS id;
-- ============================================
