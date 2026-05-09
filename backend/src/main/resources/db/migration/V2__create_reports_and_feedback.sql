create table traffic_reports (
    id uuid primary key,
    type varchar(32) not null,
    latitude double precision not null,
    longitude double precision not null,
    location_label varchar(160),
    description varchar(500),
    submitted_at timestamp with time zone not null,
    default_expires_at timestamp with time zone not null,
    status varchar(24) not null,
    initial_credibility integer not null,
    confidence_score integer not null,
    submitter_id uuid not null references app_users(id),
    hidden_reason varchar(300),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone not null
);

create index idx_traffic_reports_map
    on traffic_reports (status, type, latitude, longitude);

create table report_feedback (
    id uuid primary key,
    report_id uuid not null references traffic_reports(id),
    user_id uuid not null references app_users(id),
    feedback_type varchar(32) not null,
    weight_snapshot double precision not null,
    created_at timestamp with time zone not null,
    constraint uq_report_feedback_user unique (report_id, user_id)
);

create table reputation_events (
    id uuid primary key,
    user_id uuid not null references app_users(id),
    source_type varchar(40) not null,
    source_id uuid not null,
    points_delta integer not null,
    reputation_delta integer not null,
    reason_code varchar(64) not null,
    created_at timestamp with time zone not null
);

create index idx_reputation_events_user
    on reputation_events (user_id, created_at desc);
