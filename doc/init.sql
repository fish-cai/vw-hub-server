-- auto-generated definition
create table vw_hub_input
(
    id          integer
        constraint vw_hub_input_pk
        primary key autoincrement,
    file_name   VARCHAR(255),
    file_type   integer,
    gmt_create  timestamp,
    finish_time timestamp
);

-- auto-generated definition
create table vw_hub_output
(
    id               integer
        constraint vw_hub_output_pk
        primary key autoincrement,
    input_id         integer,
    output_file_name varchar(255),
    gmt_create       timestamp
);

