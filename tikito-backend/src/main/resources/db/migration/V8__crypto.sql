alter table aggregated_historical_money_holding_value add asset_type varchar(255) not null;
update aggregated_historical_money_holding_value set asset_type = 'FIAT';