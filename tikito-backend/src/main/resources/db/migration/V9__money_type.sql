alter table aggregated_historical_money_holding_value rename column asset_type to money_type;
update aggregated_historical_money_holding_value set money_type = 'FIAT' where money_type = 'CASH';
