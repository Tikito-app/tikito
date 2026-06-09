create table log(
    id bigint not null primary key auto_increment,
    `timestamp` timestamp not null,
    object_identifier varchar(255) null,
    message varchar(255) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;