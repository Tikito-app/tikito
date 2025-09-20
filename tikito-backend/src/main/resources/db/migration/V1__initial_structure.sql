create table account (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    name varchar(255) not null,
    account_number varchar(255) not null,
    account_type varchar(255) not null,
    currency_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table isin (
    isin varchar(40) not null primary key,
    valid_from timestamp null,
    valid_to timestamp null,
    security_id bigint not null,
    symbol varchar(40) null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table job(
    id bigint not null primary key auto_increment,
    timestamp timestamp not null,
    job_type varchar(255) not null,
    user_id bigint null,
    account_id bigint null,
    security_id bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table security_holding(
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    security_id bigint not null,
    security_type varchar(255) not null,
    currency_id bigint not null,
    amount int not null,
    price double(20, 5) not null default 0,
    total_dividend double(20, 5) not null default 0,
    total_administrative_costs double(20, 5) not null default 0,
    total_taxes double(20, 5) not null default 0,
    total_transaction_costs double(20, 5) not null default 0,
    total_cash_invested double(20, 5) not null default 0,
    total_cash_withdrawn double(20, 5) not null default 0,
    worth double(20, 5) not null default 0,
    max_cash_invested double(20, 5) not null default 0,
    cash_on_hand double(20, 5) not null default 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table security_holding_account(
    id bigint not null primary key auto_increment,
    security_holding_id bigint not null,
    account_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table aggregated_historical_money_holding_account(
    id bigint not null primary key auto_increment,
    aggregated_historical_money_holding_id bigint not null,
    account_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table aggregated_historical_security_holding_account(
    id bigint not null primary key auto_increment,
    aggregated_historical_security_holding_id bigint not null,
    account_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table historical_security_holding_account(
    id bigint not null primary key auto_increment,
    historical_security_holding_id bigint not null,
    account_id bigint not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table security_transaction (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    security_id bigint null,
    isin varchar(40) null,
    account_id bigint not null,
    currency_id bigint not null,
    amount int null,
    price double(20, 5) not null,
    description text null,
    `timestamp` timestamp not null,
    transaction_type varchar(255) not null,
    cash double(20, 5) null,
    exchange_rate double(20, 5) null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table security_price (
    id bigint not null primary key auto_increment,
    security_id bigint not null,
    `date` date not null,
    price double(20, 5) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table security (
    id bigint not null primary key auto_increment,
    security_type varchar(255) not null,
    name varchar(255) not null,
    currency_id bigint null,
    sector varchar(255) null,
    industry varchar(255) null,
    exchange varchar(255) null,
    image_url varchar(255) null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table historical_security_holding_value (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    security_holding_id bigint not null,
    security_id bigint not null,
    `date` varchar(40) not null,
    currency_id bigint not null,
    currency_multiplier double(20, 5) not null,
    amount int not null default 0,
    price double(20, 5) not null default 0,
    total_dividend double(20, 5) not null default 0,
    total_administrative_costs double(20, 5) not null default 0,
    total_taxes double(20, 5) not null default 0,
    total_transaction_costs double(20, 5) not null default 0,
    total_cash_invested double(20, 5) not null default 0,
    total_cash_withdrawn double(20, 5) not null default 0,
    worth double(20, 5) not null default 0,
    max_cash_invested double(20, 5) not null default 0,
    cash_on_hand double(20, 5) not null default 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table aggregated_historical_security_holding_value (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    `date` varchar(40) not null,
    position_value double(20, 5) not null default 0,
    total_dividend double(20, 5) not null default 0,
    total_administrative_costs double(20, 5) not null default 0,
    total_taxes double(20, 5) not null default 0,
    total_transaction_costs double(20, 5) not null default 0,
    total_cash_invested double(20, 5) not null default 0,
    total_cash_withdrawn double(20, 5) not null default 0,
    worth double(20, 5) not null default 0,
    max_cash_invested double(20, 5) not null default 0,
    cash_on_hand double(20, 5) not null default 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table money_transaction (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    account_id bigint not null,
    counterpart_account_name varchar(255) null,
    counterpart_account_number varchar(255) null,
    counterpart_account_id bigint null,
    `timestamp` timestamp not null,
    amount double(20, 5) not null,
    exchange_rate double(20, 5) not null,
    final_balance double(20, 5) null,
    description text null,
    currency_id bigint not null,
    group_id bigint null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table money_transaction_group (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    name varchar(255) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table money_transaction_group_qualifier (
    id bigint not null primary key auto_increment,
    group_id bigint not null,
    qualifier_type varchar(255) not null,
    qualifier text not null,
    transaction_field varchar(100) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table budget (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    name varchar(255) not null,
    date_range varchar(255) not null,
    amount double(20, 5) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table budget_groups (
  `budget_id` bigint(20) NOT NULL,
  `groups_id` bigint(20) NOT NULL,
  UNIQUE KEY `budget_group_id` (budget_id, groups_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COLLATE=latin1_swedish_ci;

create table historical_budget (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    budget_id bigint not null,
    `date` date not null,
    budgeted double(20, 5) not null,
    spent double(20, 5) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

create table user_preference (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    `value_key` varchar(255) not null,
    `value` varchar(255) null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `user_account` (
    id bigint not null primary key auto_increment,
    `email` varchar(255) NOT NULL,
    `password` varchar(255) NULL,
    `activated` bit(1) NOT NULL,
    `activation_code` varchar(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `historical_money_holding_value` (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    account_id bigint not null,
    `date` date not null,
    currency_id bigint not null,
    currency_multiplier double(20, 5) not null,
    amount double(20, 5) not null default 0,
    manually_set bit(1) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `aggregated_historical_money_holding_value` (
    id bigint not null primary key auto_increment,
    user_id bigint not null,
    `date` date not null,
    amount double(20, 5) not null default 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;