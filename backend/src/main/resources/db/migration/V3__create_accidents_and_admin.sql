create table accident_posts (
    id uuid primary key,
    latitude double precision not null,
    longitude double precision not null,
    location_label varchar(160),
    occurred_at timestamp with time zone not null,
    description varchar(500) not null,
    created_by_user_id uuid not null references app_users(id),
    status varchar(24) not null,
    hidden_reason varchar(300),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_accident_posts_map
    on accident_posts (status, latitude, longitude, occurred_at desc);

create table contact_exchange_requests (
    id uuid primary key,
    accident_id uuid not null references accident_posts(id),
    requesting_user_id uuid not null references app_users(id),
    target_user_id uuid references app_users(id),
    status varchar(32) not null,
    requester_confirmed_at timestamp with time zone,
    target_confirmed_at timestamp with time zone,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table accident_contact_offers (
    id uuid primary key,
    exchange_request_id uuid not null references contact_exchange_requests(id),
    user_id uuid not null references app_users(id),
    contact_type varchar(24) not null,
    encrypted_contact_value varchar(500) not null,
    created_at timestamp with time zone not null,
    constraint uq_contact_offer_user unique (exchange_request_id, user_id)
);

create table moderation_flags (
    id uuid primary key,
    target_type varchar(32) not null,
    target_id uuid not null,
    created_by_user_id uuid not null references app_users(id),
    reason_type varchar(64) not null,
    details varchar(500),
    status varchar(24) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create table admin_actions (
    id uuid primary key,
    admin_user_id uuid not null references app_users(id),
    target_type varchar(32) not null,
    target_id uuid not null,
    action_type varchar(64) not null,
    reason varchar(500) not null,
    created_at timestamp with time zone not null
);

create index idx_admin_actions_created
    on admin_actions (created_at desc);
