CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT pg_uuid_generate_v7(),
    username TEXT UNIQUE NOT NULL,
    email TEXT UNIQUE NOT NULL,
    password_hash TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Community Table
CREATE TABLE community (
    id UUID PRIMARY KEY DEFAULT pg_uuid_generate_v7(),
    name TEXT UNIQUE NOT NULL,
    description TEXT,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Category Table
CREATE TABLE category (
    id UUID PRIMARY KEY DEFAULT pg_uuid_generate_v7(),
    name TEXT UNIQUE NOT NULL
);

-- Post Table
CREATE TABLE post (
    id UUID PRIMARY KEY DEFAULT pg_uuid_generate_v7(),
    user_id UUID REFERENCES "user"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    community_id UUID REFERENCES community(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    title TEXT NOT NULL,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE post_category (
    post_id UUID REFERENCES post(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    category_id UUID REFERENCES category(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    PRIMARY KEY (post_id, category_id)
);

-- Comment Table
CREATE TABLE comment (
    id UUID PRIMARY KEY DEFAULT pg_uuid_generate_v7(),
    user_id UUID REFERENCES "user"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    post_id UUID REFERENCES post(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- Vote Table (Upvotes/Downvotes)
CREATE TABLE vote (
    id UUID PRIMARY KEY DEFAULT pg_uuid_generate_v7(),
    user_id UUID REFERENCES "user"(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    post_id UUID REFERENCES post(id) ON UPDATE RESTRICT ON DELETE RESTRICT,
    vote_type SMALLINT CHECK (vote_type IN (-1, 1)),
    UNIQUE (user_id, post_id)
);