create table app_users (
    id uuid primary key,
    student_number_hash varchar(128) not null unique,
    public_code varchar(32) not null unique,
    role varchar(16) not null,
    reputation_score integer not null,
    points integer not null,
    title_code varchar(32) not null,
    posting_ban_until timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_app_users_leaderboard
    on app_users (points desc, reputation_score desc, created_at asc);
