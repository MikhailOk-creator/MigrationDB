CREATE TABLE IF NOT EXISTS user_t (
    id UUID not null primary key,
    username varchar(255) not null,
    password varchar(255) not null,
    email varchar(255) not null,
    role varchar(255) not null
);