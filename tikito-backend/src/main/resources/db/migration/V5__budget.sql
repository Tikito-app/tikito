alter table budget add date_range_amount int not null;

create table historical_budget_value(
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    budget_id bigint not null,
    date date not null,
    budgeted double(20, 5) not null,
    spent double(20, 5) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

drop table budget_groups;
drop table budget;
drop table budget_account;
alter table money_transaction drop column budget_id;
alter table money_transaction_group drop column budget_id;
delete from money_transaction_group_group_type where group_type = 1;
update money_transaction_group_group_type set group_type = 1 where group_type = 2;
truncate historical_budget_value;
alter table historical_budget_value drop column budget_id;
alter table historical_budget_value add column group_id bigint not null;

alter table money_transaction_group add date_range varchar(255) null;
alter table money_transaction_group add budgeted double(20, 5) null;
alter table money_transaction_group add date_range_amount int null;
alter table money_transaction_group add start_date date null;
alter table money_transaction_group add end_date date null;