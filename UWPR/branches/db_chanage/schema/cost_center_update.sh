#! /usr/bin/sh

# use the appropriate username and password for the commands below

mysql -uroot pr < cost_center_tables.sql

mysql -uroot pr < pr.rateType.sql

# mysql -uroot pr < pr.timeBlock.sql
mysql -uroot pr < pr.populate_timeBlock.sql

# mysql -uroot pr < pr.instrumentRate.sql
mysql -uroot pr < pr.populate_instrumentRate.sql

mysql -uroot pr < instrumentUsage_update_for_costcenter.sql
