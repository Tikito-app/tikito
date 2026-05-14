truncate security_holding;
truncate historical_security_holding_value;
truncate security_transaction;
truncate aggregated_historical_security_holding_account;

alter table security_holding add account_id bigint not null;
alter table historical_security_holding_value add account_id bigint not null;


drop table historical_security_holding_account;
drop table security_holding_account