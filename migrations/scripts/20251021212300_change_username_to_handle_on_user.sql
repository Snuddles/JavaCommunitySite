-- //UP
ALTER TABLE public."user" RENAME COLUMN username TO handle;

ALTER TABLE public."user" DROP CONSTRAINT IF EXISTS users_username_key;

ALTER TABLE public."user" ADD CONSTRAINT users_handle_key UNIQUE (handle);

-- //@UNDO
-- Reverse the rename and constraints
ALTER TABLE public."user" DROP CONSTRAINT IF EXISTS users_handle_key;
ALTER TABLE public."user" RENAME COLUMN handle TO username;
ALTER TABLE public."user" ADD CONSTRAINT users_username_key UNIQUE (username);
