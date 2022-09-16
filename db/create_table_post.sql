create table if not exists post (
    id serial primary key,
    link text unique,
    "name" text,
    "text" text,
    created timestamp
);