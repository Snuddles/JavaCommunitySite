CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    username TEXT UNIQUE NOT NULL,
    first_name TEXT NOT NULL,
    last_name TEXT NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE community (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT UNIQUE NOT NULL,
    description TEXT NOT NULL DEFAULT '',
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE category (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name TEXT UNIQUE NOT NULL
);

CREATE TABLE "post" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES "user"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    community_id UUID REFERENCES community(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    tags JSONB NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE INDEX idx_post_tags ON "post" USING GIN (tags);

CREATE TABLE post_category (
    post_id UUID REFERENCES "post"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    category_id UUID REFERENCES category(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    PRIMARY KEY (post_id, category_id)
);

CREATE TABLE "comment" (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES "user"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    post_id UUID REFERENCES "post"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    updated_at TIMESTAMPTZ DEFAULT now()
);

CREATE TABLE vote (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    user_id UUID REFERENCES "user"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    post_id UUID REFERENCES "post"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    vote_type SMALLINT CHECK (vote_type IN (-1, 1)),
    UNIQUE (user_id, post_id)
);

CREATE TYPE notification_type AS ENUM ('NEW_COMMENT', 'NEW_VOTE', 'USER_MENTION');

CREATE TABLE notification_history (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    recipient_user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    triggering_user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    post_id UUID NOT NULL REFERENCES "post"(id) ON DELETE CASCADE,
    comment_id UUID REFERENCES "comment"(id) ON DELETE CASCADE,
    type notification_type NOT NULL,
    read_at TIMESTAMPTZ DEFAULT NULL,
    created_at TIMESTAMPTZ DEFAULT now(),
    UNIQUE (recipient_user_id, triggering_user_id, post_id, comment_id)
);

CREATE INDEX idx_notification_history_recipient_user_id ON notification_history(recipient_user_id);
