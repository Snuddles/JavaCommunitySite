-- public.atproto_log definition

-- Drop table

-- DROP TABLE public.atproto_log;

CREATE TABLE public.atproto_log (
	id uuid DEFAULT gen_random_uuid() NOT NULL,
	aturi text NULL,
	collection_type text NULL,
	record_key text NULL,
	operation text NOT NULL,
	record jsonb NULL,
	attempts int4 DEFAULT 0 NOT NULL,
	max_attempts int4 DEFAULT 10 NOT NULL,
	last_error text NULL,
	processed bool DEFAULT false NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT atproto_log_pkey PRIMARY KEY (id)
);
CREATE INDEX idx_atproto_log_aturi ON public.atproto_log USING btree (aturi);
CREATE INDEX idx_atproto_log_pending ON public.atproto_log USING btree (created_at) WHERE (processed = false);


-- public.changelog definition

-- Drop table

-- DROP TABLE public.changelog;

CREATE TABLE public.changelog (
	id numeric(20) NOT NULL,
	applied_at varchar(25) NOT NULL,
	description varchar(255) NOT NULL,
	CONSTRAINT pk_changelog PRIMARY KEY (id)
);


-- public."role" definition

-- Drop table

-- DROP TABLE public."role";

CREATE TABLE public."role" (
	id serial4 NOT NULL,
	"name" text NOT NULL,
	description text NULL,
	CONSTRAINT role_name_check CHECK ((name = ANY (ARRAY['superadmin'::text, 'admin'::text, 'user'::text]))),
	CONSTRAINT role_name_key UNIQUE (name),
	CONSTRAINT role_pkey PRIMARY KEY (id)
);


-- public."user" definition

-- Drop table

-- DROP TABLE public."user";

CREATE TABLE public."user" (
	did text NOT NULL,
	handle text NOT NULL,
	first_name text NULL,
	last_name text NULL,
	display_name text NULL,
	description text NULL,
	avatar_bloburl text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz NULL,
	CONSTRAINT user_pkey PRIMARY KEY (did)
);

-- Table Triggers

create trigger trg_assign_default_user_role after
insert
    on
    public."user" for each row execute function assign_default_user_role();


-- public.hidden_user definition

-- Drop table

-- DROP TABLE public.hidden_user;

CREATE TABLE public.hidden_user (
	aturi text NOT NULL,
	target_did text NOT NULL,
	hidden_by text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT hidden_user_pkey PRIMARY KEY (aturi),
	CONSTRAINT hidden_user_target_did_key UNIQUE (target_did),
	CONSTRAINT hidden_user_hidden_by_fkey FOREIGN KEY (hidden_by) REFERENCES public."user"(did) ON DELETE CASCADE,
	CONSTRAINT hidden_user_target_did_fkey FOREIGN KEY (target_did) REFERENCES public."user"(did) ON DELETE CASCADE
);


-- public.post definition

-- Drop table

-- DROP TABLE public.post;

CREATE TABLE public.post (
	aturi text NOT NULL,
	title text NOT NULL,
	"content" text NOT NULL,
	tags json NULL,
	is_hidden bool DEFAULT false NULL,
	is_open bool DEFAULT true NULL,
	status text NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz NULL,
	owner_did text NULL,
	CONSTRAINT post_pkey PRIMARY KEY (aturi),
	CONSTRAINT post_status_check CHECK ((status = ANY (ARRAY['new'::text, 'in progress'::text, 'solved'::text]))),
	CONSTRAINT post_owner_did_fkey FOREIGN KEY (owner_did) REFERENCES public."user"(did)
);


-- public.reply definition

-- Drop table

-- DROP TABLE public.reply;

CREATE TABLE public.reply (
	aturi text NOT NULL,
	root_post_aturi text NOT NULL,
	"content" text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	updated_at timestamptz NULL,
	owner_did text NOT NULL,
	CONSTRAINT reply_pkey PRIMARY KEY (aturi),
	CONSTRAINT reply_owner_did_fkey FOREIGN KEY (owner_did) REFERENCES public."user"(did),
	CONSTRAINT reply_root_post_aturi_fkey FOREIGN KEY (root_post_aturi) REFERENCES public.post(aturi)
);


-- public.tags definition

-- Drop table

-- DROP TABLE public.tags;

CREATE TABLE public.tags (
	aturi text NOT NULL,
	tag_name text NOT NULL,
	created_by text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT tags_pkey PRIMARY KEY (aturi),
	CONSTRAINT tags_tag_name_key UNIQUE (tag_name),
	CONSTRAINT tags_created_by_fkey FOREIGN KEY (created_by) REFERENCES public."user"(did) ON DELETE CASCADE
);
CREATE INDEX idx_tags_tag_name ON public.tags USING btree (tag_name);


-- public.user_role definition

-- Drop table

-- DROP TABLE public.user_role;

CREATE TABLE public.user_role (
	user_did text NOT NULL,
	role_id int4 NOT NULL,
	CONSTRAINT user_role_pkey PRIMARY KEY (user_did, role_id),
	CONSTRAINT user_role_role_id_fkey FOREIGN KEY (role_id) REFERENCES public."role"(id) ON DELETE CASCADE,
	CONSTRAINT user_role_user_did_fkey FOREIGN KEY (user_did) REFERENCES public."user"(did) ON DELETE CASCADE
);


-- public.hidden_post definition

-- Drop table

-- DROP TABLE public.hidden_post;

CREATE TABLE public.hidden_post (
	aturi text NOT NULL,
	post_aturi text NOT NULL,
	target_owner_did text NOT NULL,
	hidden_by text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT hidden_post_pkey PRIMARY KEY (aturi),
	CONSTRAINT hidden_post_post_aturi_key UNIQUE (post_aturi),
	CONSTRAINT hidden_post_hidden_by_fkey FOREIGN KEY (hidden_by) REFERENCES public."user"(did) ON DELETE CASCADE,
	CONSTRAINT hidden_post_post_aturi_fkey FOREIGN KEY (post_aturi) REFERENCES public.post(aturi) ON DELETE CASCADE,
	CONSTRAINT hidden_post_target_owner_did_fkey FOREIGN KEY (target_owner_did) REFERENCES public."user"(did) ON DELETE CASCADE
);


-- public.hidden_reply definition

-- Drop table

-- DROP TABLE public.hidden_reply;

CREATE TABLE public.hidden_reply (
	aturi text NOT NULL,
	reply_aturi text NOT NULL,
	target_owner_did text NOT NULL,
	hidden_by text NOT NULL,
	created_at timestamptz DEFAULT now() NOT NULL,
	CONSTRAINT hidden_reply_pkey PRIMARY KEY (aturi),
	CONSTRAINT hidden_reply_reply_aturi_key UNIQUE (reply_aturi),
	CONSTRAINT hidden_reply_hidden_by_fkey FOREIGN KEY (hidden_by) REFERENCES public."user"(did) ON DELETE CASCADE,
	CONSTRAINT hidden_reply_reply_aturi_fkey FOREIGN KEY (reply_aturi) REFERENCES public.reply(aturi) ON DELETE CASCADE,
	CONSTRAINT hidden_reply_target_owner_did_fkey FOREIGN KEY (target_owner_did) REFERENCES public."user"(did) ON DELETE CASCADE
);


-- public.notification definition

-- Drop table

-- DROP TABLE public.notification;

CREATE TABLE public.notification (
	recipient_user_did text NOT NULL,
	triggering_user_did text NOT NULL,
	post_aturi text NOT NULL,
	reply_aturi text NULL,
	"type" public."notification_type" NOT NULL,
	created_at timestamptz DEFAULT now() NULL,
	read_at timestamptz NULL,
	aturi text DEFAULT 'at://notification/temp'::text NOT NULL,
	CONSTRAINT notification_pkey PRIMARY KEY (aturi),
	CONSTRAINT notification_post_fkey FOREIGN KEY (post_aturi) REFERENCES public.post(aturi) ON DELETE CASCADE,
	CONSTRAINT notification_recipient_user_fkey FOREIGN KEY (recipient_user_did) REFERENCES public."user"(did) ON DELETE CASCADE,
	CONSTRAINT notification_reply_fkey FOREIGN KEY (reply_aturi) REFERENCES public.reply(aturi) ON DELETE CASCADE,
	CONSTRAINT notification_triggering_user_fkey FOREIGN KEY (triggering_user_did) REFERENCES public."user"(did) ON DELETE CASCADE
);
CREATE INDEX idx_notification_recipient_user_did ON public.notification USING btree (recipient_user_did);