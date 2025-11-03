-- // rename_is_deleted_to_is_hidden_on_post
-- Renames the is_deleted column on the post table to is_hidden for semantic clarity.

-- ======================================
-- ========== MIGRATE UP ================
-- ======================================

ALTER TABLE public.post
RENAME COLUMN is_deleted TO is_hidden;

-- //@UNDO
-- ======================================
-- ========== MIGRATE DOWN ==============
-- ======================================

ALTER TABLE public.post
RENAME COLUMN is_hidden TO is_deleted;