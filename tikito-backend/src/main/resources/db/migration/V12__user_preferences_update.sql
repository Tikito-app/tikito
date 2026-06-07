delete from user_preference where value_key = 'MONEY_SHOW_OTHER';
update user_preference set value_key = 'MONEY_DATE_RANGE' where value_key = 'DATE_RANGE';
update user_preference set value_key = 'MONEY_ACCOUNT_IDS' where value_key = 'ACCOUNT_IDS';
update user_preference set value_key = 'MONEY_START_DATE' where value_key = 'START_DATE';
update user_preference set value_key = 'MONEY_END_DATE' where value_key = 'END_DATE';