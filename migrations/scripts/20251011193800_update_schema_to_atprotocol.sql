-- Drop existing foreign key constraints
ALTER TABLE public.notification_history DROP CONSTRAINT IF EXISTS notification_history_comment_id_fkey;
ALTER TABLE public.notification_history DROP CONSTRAINT IF EXISTS notification_history_post_id_fkey;
ALTER TABLE public.notification_history DROP CONSTRAINT IF EXISTS notification_history_recipient_user_id_fkey;
ALTER TABLE public.notification_history DROP CONSTRAINT IF EXISTS notification_history_triggering_user_id_fkey;
ALTER TABLE public.post DROP CONSTRAINT IF EXISTS post_category_id_fkey;
ALTER TABLE public.post DROP CONSTRAINT IF EXISTS post_user_id_fkey;
ALTER TABLE public.vote DROP CONSTRAINT IF EXISTS vote_post_id_fkey;
ALTER TABLE public.vote DROP CONSTRAINT IF EXISTS vote_user_id_fkey;
ALTER TABLE public."comment" DROP CONSTRAINT IF EXISTS comment_post_id_fkey;
ALTER TABLE public."comment" DROP CONSTRAINT IF EXISTS comment_user_id_fkey;
ALTER TABLE public.category DROP CONSTRAINT IF EXISTS category_category_group_id_fkey;

-- Add aturi columns to tables that don't have them
ALTER TABLE public.community ADD COLUMN IF NOT EXISTS aturi TEXT;
ALTER TABLE public.notification_history ADD COLUMN IF NOT EXISTS aturi TEXT;

-- Update user table: make aturi primary key
ALTER TABLE public."user" DROP CONSTRAINT IF EXISTS users_pkey;
-- Set a simple default value for aturi since tables are empty
ALTER TABLE public."user" ALTER COLUMN aturi SET DEFAULT 'at://user/temp';
ALTER TABLE public."user" ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public."user" ADD CONSTRAINT user_pkey PRIMARY KEY (aturi);
ALTER TABLE public."user" DROP COLUMN id;

-- Rename category_group to group and update structure
ALTER TABLE public.category_group RENAME TO "group";
ALTER TABLE public."group" DROP CONSTRAINT IF EXISTS category_group_pkey;
ALTER TABLE public."group" ALTER COLUMN aturi SET DEFAULT 'at://group/temp';
ALTER TABLE public."group" ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public."group" ADD CONSTRAINT group_pkey PRIMARY KEY (aturi);
ALTER TABLE public."group" DROP COLUMN id;
ALTER TABLE public."group" DROP COLUMN created_at;
ALTER TABLE public."group" DROP COLUMN updated_at;

-- Update category table structure
ALTER TABLE public.category DROP CONSTRAINT IF EXISTS category_pkey;
ALTER TABLE public.category ALTER COLUMN aturi SET DEFAULT 'at://category/temp';
ALTER TABLE public.category ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public.category ADD CONSTRAINT category_pkey PRIMARY KEY (aturi);
ALTER TABLE public.category DROP COLUMN id;
ALTER TABLE public.category RENAME COLUMN category_group_id TO "group";
ALTER TABLE public.category ALTER COLUMN "group" TYPE TEXT;
ALTER TABLE public.category ADD COLUMN category_type TEXT;
ALTER TABLE public.category ADD COLUMN description TEXT;

-- Update post table structure
ALTER TABLE public.post DROP CONSTRAINT IF EXISTS posts_pkey;
ALTER TABLE public.post ALTER COLUMN aturi SET DEFAULT 'at://post/temp';
ALTER TABLE public.post ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public.post ADD CONSTRAINT post_pkey PRIMARY KEY (aturi);
ALTER TABLE public.post DROP COLUMN id;
ALTER TABLE public.post RENAME COLUMN category_id TO category_aturi;
ALTER TABLE public.post ALTER COLUMN category_aturi TYPE TEXT;
ALTER TABLE public.post DROP COLUMN user_id;
ALTER TABLE public.post DROP COLUMN community_id;
ALTER TABLE public.post ADD COLUMN forum TEXT NOT NULL DEFAULT '';
ALTER TABLE public.post ADD COLUMN solution TEXT;

-- Rename comment to reply and update structure
ALTER TABLE public."comment" RENAME TO reply;
ALTER TABLE public.reply DROP CONSTRAINT IF EXISTS comments_pkey;
ALTER TABLE public.reply ALTER COLUMN aturi SET DEFAULT 'at://reply/temp';
ALTER TABLE public.reply ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public.reply ADD CONSTRAINT reply_pkey PRIMARY KEY (aturi);
ALTER TABLE public.reply DROP COLUMN id;
ALTER TABLE public.reply DROP COLUMN user_id;
ALTER TABLE public.reply RENAME COLUMN post_id TO root;
ALTER TABLE public.reply ALTER COLUMN root TYPE TEXT;

-- Update vote table structure
ALTER TABLE public.vote DROP CONSTRAINT IF EXISTS vote_pkey;
ALTER TABLE public.vote ALTER COLUMN aturi SET DEFAULT 'at://vote/temp';
ALTER TABLE public.vote ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public.vote ADD CONSTRAINT vote_pkey PRIMARY KEY (aturi);
ALTER TABLE public.vote DROP COLUMN id;
ALTER TABLE public.vote DROP COLUMN user_id;
ALTER TABLE public.vote RENAME COLUMN post_id TO root;
ALTER TABLE public.vote ALTER COLUMN root TYPE TEXT;
ALTER TABLE public.vote RENAME COLUMN vote_type TO "value";
ALTER TABLE public.vote ADD COLUMN created_at TIMESTAMPTZ DEFAULT now();

-- Update community table to use aturi as primary key
ALTER TABLE public.community DROP CONSTRAINT IF EXISTS community_pkey;
ALTER TABLE public.community ALTER COLUMN aturi SET DEFAULT 'at://community/temp';
ALTER TABLE public.community ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public.community ADD CONSTRAINT community_pkey PRIMARY KEY (aturi);
ALTER TABLE public.community DROP COLUMN id;

-- Update notification_history table and rename to notification
ALTER TABLE public.notification_history RENAME TO notification;
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_history_pkey;
ALTER TABLE public.notification ALTER COLUMN aturi SET DEFAULT 'at://notification/temp';
ALTER TABLE public.notification ALTER COLUMN aturi SET NOT NULL;
ALTER TABLE public.notification ADD CONSTRAINT notification_pkey PRIMARY KEY (aturi);
ALTER TABLE public.notification DROP COLUMN id;
ALTER TABLE public.notification RENAME COLUMN recipient_user_id TO recipient_user_aturi;
ALTER TABLE public.notification RENAME COLUMN triggering_user_id TO triggering_user_aturi;
ALTER TABLE public.notification RENAME COLUMN post_id TO post_aturi;
ALTER TABLE public.notification RENAME COLUMN comment_id TO reply_aturi;
ALTER TABLE public.notification ALTER COLUMN recipient_user_aturi TYPE TEXT;
ALTER TABLE public.notification ALTER COLUMN triggering_user_aturi TYPE TEXT;
ALTER TABLE public.notification ALTER COLUMN post_aturi TYPE TEXT;
ALTER TABLE public.notification ALTER COLUMN reply_aturi TYPE TEXT;

-- Add foreign key constraints with aturi references
ALTER TABLE public.category ADD CONSTRAINT category_group_fkey FOREIGN KEY ("group") REFERENCES public."group"(aturi) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.post ADD CONSTRAINT post_category_fkey FOREIGN KEY (category_aturi) REFERENCES public.category(aturi) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.reply ADD CONSTRAINT reply_root_fkey FOREIGN KEY (root) REFERENCES public.post(aturi) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.vote ADD CONSTRAINT vote_root_fkey FOREIGN KEY (root) REFERENCES public.post(aturi) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.notification ADD CONSTRAINT notification_recipient_user_fkey FOREIGN KEY (recipient_user_aturi) REFERENCES public."user"(aturi) ON DELETE CASCADE;
ALTER TABLE public.notification ADD CONSTRAINT notification_triggering_user_fkey FOREIGN KEY (triggering_user_aturi) REFERENCES public."user"(aturi) ON DELETE CASCADE;
ALTER TABLE public.notification ADD CONSTRAINT notification_post_fkey FOREIGN KEY (post_aturi) REFERENCES public.post(aturi) ON DELETE CASCADE;
ALTER TABLE public.notification ADD CONSTRAINT notification_reply_fkey FOREIGN KEY (reply_aturi) REFERENCES public.reply(aturi) ON DELETE CASCADE;

-- Update constraints and indexes
ALTER TABLE public.category DROP CONSTRAINT IF EXISTS category_name_key;
ALTER TABLE public."group" DROP CONSTRAINT IF EXISTS category_group_name_key;
ALTER TABLE public.vote DROP CONSTRAINT IF EXISTS vote_user_id_post_id_key;
ALTER TABLE public.vote DROP CONSTRAINT IF EXISTS vote_vote_type_check;
ALTER TABLE public.vote ADD CONSTRAINT vote_value_check CHECK (("value" = ANY (ARRAY[-1, 1])));
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_history_recipient_user_id_triggering_user_id_p_key;
DROP INDEX IF EXISTS idx_notification_history_recipient_user_id;
CREATE INDEX idx_notification_recipient_user_aturi ON public.notification USING btree (recipient_user_aturi);

-- //@UNDO

-- Drop new foreign key constraints
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_reply_fkey;
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_post_fkey;
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_triggering_user_fkey;
ALTER TABLE public.notification DROP CONSTRAINT IF EXISTS notification_recipient_user_fkey;
ALTER TABLE public.vote DROP CONSTRAINT IF EXISTS vote_root_fkey;
ALTER TABLE public.reply DROP CONSTRAINT IF EXISTS reply_root_fkey;
ALTER TABLE public.post DROP CONSTRAINT IF EXISTS post_category_fkey;
ALTER TABLE public.category DROP CONSTRAINT IF EXISTS category_group_fkey;

-- Revert community table structure
ALTER TABLE public.community ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public.community DROP CONSTRAINT community_pkey;
ALTER TABLE public.community ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public.community DROP COLUMN aturi;
ALTER TABLE public.community ADD CONSTRAINT community_pkey PRIMARY KEY (id);

-- Revert notification table structure (rename back to notification_history)
DROP INDEX IF EXISTS idx_notification_recipient_user_aturi;
ALTER TABLE public.notification ALTER COLUMN reply_aturi TYPE uuid USING CASE WHEN reply_aturi IS NULL THEN NULL ELSE NULL END;
ALTER TABLE public.notification ALTER COLUMN post_aturi TYPE uuid USING CASE WHEN post_aturi IS NULL THEN NULL ELSE NULL END;
ALTER TABLE public.notification ALTER COLUMN triggering_user_aturi TYPE uuid USING CASE WHEN triggering_user_aturi IS NULL THEN NULL ELSE NULL END;
ALTER TABLE public.notification ALTER COLUMN recipient_user_aturi TYPE uuid USING CASE WHEN recipient_user_aturi IS NULL THEN NULL ELSE NULL END;
ALTER TABLE public.notification RENAME COLUMN reply_aturi TO comment_id;
ALTER TABLE public.notification RENAME COLUMN post_aturi TO post_id;
ALTER TABLE public.notification RENAME COLUMN triggering_user_aturi TO triggering_user_id;
ALTER TABLE public.notification RENAME COLUMN recipient_user_aturi TO recipient_user_id;
ALTER TABLE public.notification ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public.notification DROP CONSTRAINT notification_pkey;
ALTER TABLE public.notification ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public.notification DROP COLUMN aturi;
ALTER TABLE public.notification ADD CONSTRAINT notification_history_pkey PRIMARY KEY (id);
ALTER TABLE public.notification ADD CONSTRAINT notification_history_recipient_user_id_triggering_user_id_p_key UNIQUE (recipient_user_id, triggering_user_id, post_id, comment_id);
CREATE INDEX idx_notification_history_recipient_user_id ON public.notification USING btree (recipient_user_id);
ALTER TABLE public.notification RENAME TO notification_history;

-- Revert vote table structure
ALTER TABLE public.vote DROP COLUMN created_at;
ALTER TABLE public.vote RENAME COLUMN "value" TO vote_type;
ALTER TABLE public.vote ALTER COLUMN root TYPE uuid USING NULL;
ALTER TABLE public.vote RENAME COLUMN root TO post_id;
ALTER TABLE public.vote ADD COLUMN user_id uuid;
ALTER TABLE public.vote ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public.vote DROP CONSTRAINT vote_pkey;
ALTER TABLE public.vote ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public.vote ADD CONSTRAINT vote_pkey PRIMARY KEY (id);

-- Revert reply table structure (rename back to comment)
ALTER TABLE public.reply ALTER COLUMN root TYPE uuid USING NULL;
ALTER TABLE public.reply RENAME COLUMN root TO post_id;
ALTER TABLE public.reply ADD COLUMN user_id uuid;
ALTER TABLE public.reply ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public.reply DROP CONSTRAINT reply_pkey;
ALTER TABLE public.reply ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public.reply ADD CONSTRAINT comments_pkey PRIMARY KEY (id);
ALTER TABLE public.reply RENAME TO "comment";

-- Revert post table structure
ALTER TABLE public.post DROP COLUMN solution;
ALTER TABLE public.post DROP COLUMN forum;
ALTER TABLE public.post ADD COLUMN community_id uuid;
ALTER TABLE public.post ADD COLUMN user_id uuid;
ALTER TABLE public.post ALTER COLUMN category_aturi TYPE uuid USING NULL;
ALTER TABLE public.post RENAME COLUMN category_aturi TO category_id;
ALTER TABLE public.post ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public.post DROP CONSTRAINT post_pkey;
ALTER TABLE public.post ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public.post ADD CONSTRAINT posts_pkey PRIMARY KEY (id);

-- Revert category table structure
ALTER TABLE public.category DROP COLUMN description;
ALTER TABLE public.category DROP COLUMN category_type;
ALTER TABLE public.category ALTER COLUMN "group" TYPE uuid USING NULL;
ALTER TABLE public.category RENAME COLUMN "group" TO category_group_id;
ALTER TABLE public.category ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public.category DROP CONSTRAINT category_pkey;
ALTER TABLE public.category ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public.category ADD CONSTRAINT category_pkey PRIMARY KEY (id);

-- Revert group table structure (rename back to category_group)
ALTER TABLE public."group" ADD COLUMN updated_at timestamptz DEFAULT now();
ALTER TABLE public."group" ADD COLUMN created_at timestamptz DEFAULT now();
ALTER TABLE public."group" ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public."group" DROP CONSTRAINT group_pkey;
ALTER TABLE public."group" ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public."group" ADD CONSTRAINT category_group_pkey PRIMARY KEY (id);
ALTER TABLE public."group" RENAME TO category_group;

-- Revert user table structure
ALTER TABLE public."user" ADD COLUMN id uuid DEFAULT uuid_generate_v4();
ALTER TABLE public."user" DROP CONSTRAINT user_pkey;
ALTER TABLE public."user" ALTER COLUMN aturi DROP NOT NULL;
ALTER TABLE public."user" ADD CONSTRAINT users_pkey PRIMARY KEY (id);

-- Recreate original foreign key constraints
ALTER TABLE public.category ADD CONSTRAINT category_category_group_id_fkey FOREIGN KEY (category_group_id) REFERENCES public.category_group(id) ON DELETE SET NULL ON UPDATE RESTRICT;
ALTER TABLE public."comment" ADD CONSTRAINT comment_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public."comment" ADD CONSTRAINT comment_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.post(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.vote ADD CONSTRAINT vote_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.vote ADD CONSTRAINT vote_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.post(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.post ADD CONSTRAINT post_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.post ADD CONSTRAINT post_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.category(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.post ADD CONSTRAINT posts_community_id_fkey FOREIGN KEY (community_id) REFERENCES public.community(id) ON DELETE RESTRICT ON UPDATE RESTRICT;
ALTER TABLE public.notification_history ADD CONSTRAINT notification_history_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES public."comment"(id) ON DELETE CASCADE;
ALTER TABLE public.notification_history ADD CONSTRAINT notification_history_post_id_fkey FOREIGN KEY (post_id) REFERENCES public.post(id) ON DELETE CASCADE;
ALTER TABLE public.notification_history ADD CONSTRAINT notification_history_recipient_user_id_fkey FOREIGN KEY (recipient_user_id) REFERENCES public."user"(id) ON DELETE CASCADE;
ALTER TABLE public.notification_history ADD CONSTRAINT notification_history_triggering_user_id_fkey FOREIGN KEY (triggering_user_id) REFERENCES public."user"(id) ON DELETE CASCADE;

-- Recreate original constraints
ALTER TABLE public.vote ADD CONSTRAINT vote_value_check CHECK ((vote_type = ANY (ARRAY['-1'::integer, 1])));
ALTER TABLE public.vote ADD CONSTRAINT vote_user_id_post_id_key UNIQUE (user_id, post_id);
ALTER TABLE public."group" ADD CONSTRAINT category_group_name_key UNIQUE (name);
ALTER TABLE public.category ADD CONSTRAINT category_name_key UNIQUE (name);