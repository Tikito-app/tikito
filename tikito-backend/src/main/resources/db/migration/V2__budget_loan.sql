
create table budget_account(
    id bigint not null primary key auto_increment,
    budget_id bigint not null,
    account_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table money_transaction_group_group_type(
    id bigint not null primary key auto_increment,
    money_transaction_group_id bigint not null,
    group_type varchar(255) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table money_transaction_group_account_id(
    id bigint not null primary key auto_increment,
    money_transaction_account_id bigint not null,
    account_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


alter table security add current_isin varchar(40) not null;
update security s set s.current_isin = (select isin from isin i where i.security_id = s.id order by valid_to asc limit 1);
alter table money_transaction add budget_id bigint null;
alter table money_transaction add loan_id bigint null;
alter table budget add `start_date` date null;
alter table budget add `end_date` date null;
alter table money_transaction_group add budget_id bigint null;
alter table money_transaction_group add loan_id bigint null;
alter table job add loan_id bigint null;

drop table budget_groups;
create table budget_groups (
    id bigint(20) not null primary key auto_increment,
    budget_id bigint(20) NOT NULL,
    group_id bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

create table loan (
    id bigint not null primary key auto_increment,
    user_id bigint(20) NOT NULL,
    date_range varchar(255) not null,
    name varchar(255) not null
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

create table loan_part (
    id bigint not null primary key auto_increment,
    user_id bigint(20) NOT NULL,
    loan_id bigint(20) NOT NULL,
    name varchar(255) not null,
    start_date varchar(255) not null,
    end_date varchar(255) null,
    amount double(20, 5) not null,
    remaining_amount double(20, 5) not null,
    periodic_payment double(20, 5) not null,
    currency_id bigint not null,
    loan_type varchar(255) not null
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

create table loan_interest (
    id bigint not null primary key auto_increment,
    start_date varchar(255) not null,
    end_date varchar(255) null,
    amount double(20, 5) not null,
    loan_part_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

create table loan_value (
    id bigint not null primary key auto_increment,
    loan_id bigint(20) not null,
    loan_part_id bigint(20) not null,
    `date` varchar(255) not null,
    amount_remaining double(20, 5) not null,
    interest_remaining double(20, 5) not null,
    interest_paid double(20, 5) not null,
    interest_paid_this_period double(20, 5) not null,
    repayment_remaining double(20, 5) not null,
    loan_paid double(20, 5) not null,
    loan_paid_this_period double(20, 5) not null,
    simulated bit(1) not null
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;
