load data infile '/Users/vagisha/WORK/UWPR/trunk/schema/all_rates.txt' into table instrumentRate (id, instrumentID, blockID, rateTypeID, fee, isCurrent) SET createDate = CURRENT_TIMESTAMP;

load data infile '/Users/vagisha/WORK/UWPR/trunk/schema/nanomate_hplc_rates.txt' into table instrumentRate (id, instrumentID, blockID, rateTypeID, fee, isCurrent) SET createDate = CURRENT_TIMESTAMP;

load data infile '/Users/vagisha/WORK/UWPR/trunk/schema/qexactive_rates.txt' into table instrumentRate (instrumentID, blockID, rateTypeID, fee, isCurrent) SET createDate = CURRENT_TIMESTAMP;
