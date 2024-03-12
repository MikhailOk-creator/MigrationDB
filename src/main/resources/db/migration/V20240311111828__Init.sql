CREATE TABLE IF NOT EXISTS migration_t (
    id int8 not null primary key,
    source_host varchar(255) not null,
    source_port int4 not null,
    source_db varchar(255) not null,
    target_host varchar(255) not null,
    target_port int4 not null,
    target_db varchar(255) not null,
    status varchar(255) not null,
    start_time timestamp not null,
    end_time timestamp not null,
    duration int8 not null,
    error_message text
);

CREATE TABLE IF NOT EXISTS migration_detail_t (
    id int8 not null primary key,
    migration_id int8 not null,
    source_table varchar(255) not null,
    target_table varchar(255) not null,
    status varchar(255) not null,
    start_time timestamp not null,
    end_time timestamp not null,
    duration int8 not null,
    error_message text,
    foreign key (migration_id) references migration_t(id)
);