--- No need to mark old rates as obsolete.  Rates are effective 11/1/18, and the month of October has already been billed.

-- Add rates for new instrument TSQ-Altis added by Priska
------ INSERT new instrument rates
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- -- TSQ - Altis ----
-- UW
(15, 26, 1, 12.00, NOW(), 1),
-- UW-FFS
(15, 26, 4, 24.00, NOW(), 1),
-- Non Profit
(15, 26, 5, 35.75, NOW(), 1),
-- Commercial
(15, 26, 6, 94.52, NOW(), 1);

---------------------------------------------------------------------------------------------
-- UPDATE INSTRUMENT RATES (External and External + FFS)
-- TSQ - Vantage
UPDATE mainDb.instrumentRate set fee = 41.1 WHERE instrumentID = 10 and rateTypeID = 5 and blockID = 26 and isCurrent = 1;
UPDATE mainDb.instrumentRate set fee = 94.52 WHERE instrumentID = 10 and rateTypeID = 6 and blockID = 26 and isCurrent = 1;

-- Orbitrap - 1
UPDATE mainDb.instrumentRate set fee = 35.75 WHERE instrumentID = 4 and rateTypeID = 5 and blockID = 26 and isCurrent = 1;
UPDATE mainDb.instrumentRate set fee = 94.52 WHERE instrumentID = 4 and rateTypeID = 6 and blockID = 26 and isCurrent = 1;

-- Elite
UPDATE mainDb.instrumentRate set fee = 60.99 WHERE instrumentID = 14 and rateTypeID = 5 and blockID = 26 and isCurrent = 1;
UPDATE mainDb.instrumentRate set fee = 114.42 WHERE instrumentID = 14 and rateTypeID = 6 and blockID = 26 and isCurrent = 1;


-- QE+
UPDATE mainDb.instrumentRate set fee = 36.86 WHERE instrumentID = 11 and rateTypeID = 5 and blockID = 26 and isCurrent = 1;
UPDATE mainDb.instrumentRate set fee = 167.21 WHERE instrumentID = 11 and rateTypeID = 6 and blockID = 26 and isCurrent = 1;

-- Fusion
UPDATE mainDb.instrumentRate set fee = 44.34 WHERE instrumentID = 12 and rateTypeID = 5 and blockID = 26 and isCurrent = 1;
UPDATE mainDb.instrumentRate set fee = 167.21 WHERE instrumentID = 12 and rateTypeID = 6 and blockID = 26 and isCurrent = 1;

-- Lumos
UPDATE mainDb.instrumentRate set fee = 44.34 WHERE instrumentID = 13 and rateTypeID = 5 and blockID = 26 and isCurrent = 1;
UPDATE mainDb.instrumentRate set fee = 167.21 WHERE instrumentID = 13 and rateTypeID = 6 and blockID = 26 and isCurrent = 1;
---------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------
-- UPDATE SETUP FEE
UPDATE rateType SET setupFee = 71.51 WHERE id IN (5,6)

