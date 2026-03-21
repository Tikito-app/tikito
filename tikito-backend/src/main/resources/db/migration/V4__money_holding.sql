create table money_holding(
   id bigint not null primary key auto_increment,
   user_id bigint not null,
   account_id bigint not null,
   currency_id bigint not null,
   current_balance double(20, 5) not null,
   starting_balance double(20, 5) not null
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

insert into money_holding (user_id, account_id, currency_id, current_balance, starting_balance)
    select a.user_id, a.id, a.currency_id, 0, 0
    from account a
    where a.account_type = 'DEBIT' or a.account_type = 'CREDIT';