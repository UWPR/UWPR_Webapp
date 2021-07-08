-- Mark old rates obsolete
UPDATE mainDb.instrumentRate SET isCurrent = 0 WHERE isCurrent = 1 AND instrumentID in (10, 11, 12, 13, 14, 15) AND rateTypeID in (1,4,5,6);

-- UPDATE SETUP FEE
-- |  5 | NON_PROFIT_FFS | Non-UW; non-profit Fee for Service |
UPDATE mainDb.rateType SET setupFee = 84.23 WHERE id=5;
-- |  6 | COMMERCIAL_FFS | Non-UW; Commercial Fee for Service
UPDATE mainDb.rateType SET setupFee = 110.94 WHERE id=6;

-- Add rates for new instrument Exploris added by Priska
------ INSERT new instrument rates
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- -- Exploris ----
-- UW
(17, 26, 1, 15.50, NOW(), 1),
-- UW-FFS
(17, 26, 4, 31.00, NOW(), 1),
-- Non Profit
(17, 26, 5, 63.75, NOW(), 1),
-- Commercial
(17, 26, 6, 200.52, NOW(), 1);

---------------------------------------------------------------------------------------------
-- Add new instrument rates
-- TSQ - Vantage
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- TSQ - Vantage --
-- UW
(10, 26, 1, 9.00, NOW(), 1),
-- UW-FFS
(10, 26, 4, 18.00, NOW(), 1),
-- Non Profit
(10, 26, 5, 54.61, NOW(), 1),
-- Commercial
(10, 26, 6, 128.33, NOW(), 1);

-- QE+
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- QE+ --
-- UW
(11, 26, 1, 15.50, NOW(), 1),
-- UW-FFS
(11, 26, 4, 31.00, NOW(), 1),
-- Non Profit
(11, 26, 5, 54.61, NOW(), 1),
-- Commercial
(11, 26, 6, 199.92, NOW(), 1);


-- Fusion
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- Fusion --
-- UW
(12, 26, 1, 15.50, NOW(), 1),
-- UW-FFS
(12, 26, 4, 31.00, NOW(), 1),
-- Non Profit
(12, 26, 5, 64.59, NOW(), 1),
-- Commercial
(12, 26, 6, 201.36, NOW(), 1);


-- Lumos
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- Lumos --
-- UW
(13, 26, 1, 15.50, NOW(), 1),
-- UW-FFS
(13, 26, 4, 31.00, NOW(), 1),
-- Non Profit
(13, 26, 5, 64.59, NOW(), 1),
-- Commercial
(13, 26, 6, 201.36, NOW(), 1);


-- Elite
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- Elite --
-- UW
(14, 26, 1, 12.00, NOW(), 1),
-- UW-FFS
(14, 26, 4, 24.00, NOW(), 1),
-- Non Profit
(14, 26, 5, 50.33, NOW(), 1),
-- Commercial
(14, 26, 6, 197.79, NOW(), 1);


-- TSQ - Altis
INSERT INTO mainDb.instrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- TSQ - Altis --
-- UW
(15, 26, 1, 13.50, NOW(), 1),
-- UW-FFS
(15, 26, 4, 27.00, NOW(), 1),
-- Non Profit
(15, 26, 5, 63.15, NOW(), 1),
-- Commercial
(15, 26, 6, 128.33, NOW(), 1);



---------------------------------------------------------------------------------------------

---------------------------------------------------------------------------------------------


