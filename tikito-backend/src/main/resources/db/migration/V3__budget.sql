alter table budget add date_range_amount int not null;

create table historical_budget_value(
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    budget_id bigint not null,
    date date not null,
    budgeted double(20, 5) not null,
    spent double(20, 5) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
