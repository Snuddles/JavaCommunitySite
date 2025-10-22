ALTER TABLE public."user" ADD COLUMN IF NOT EXISTS display_name TEXT;

-- //@UNDO
ALTER TABLE public."user" DROP COLUMN IF EXISTS display_name;
