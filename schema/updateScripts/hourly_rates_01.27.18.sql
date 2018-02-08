ALTER TABLE mainDb.instrumentUsage ADD COLUMN setupBlock BIT(1) DEFAULT 0 AFTER instrumentRateID ;

ALTER TABLE mainDb.instrumentUsagePayment ADD INDEX (paymentMethodID);

-- ------ CREATE the hourly time block
INSERT INTO mainDb.timeBlock (numHours, name, createDate) VALUES (1,'hourly', NOW())

-- ------ ADD a setup fee in the rateType table ----
ALTER TABLE mainDb.rateType ADD COLUMN setupFee DECIMAL(5,2) DEFAULT 0.0;
UPDATE mainDb.rateType SET setupFee = 25.0 WHERE id IN (1,4); -- UW, UW FFS
UPDATE mainDb.rateType SET setupFee = 69.45 WHERE id IN (5,6); -- NON PROFIT FFS, COMMERCIAL FFS

-- ------ MARK old rates obsolete --------
UPDATE mainDb.instrumentRate SET isCurrent = 0 WHERE isCurrent = 1 AND instrumentID in (4, 7, 10, 11, 12, 13) AND rateTypeID in (1,4,5,6);
-- SELECT * FROM maindb.instrumentrate WHERE isCurrent = 1 AND instrumentID in (4, 7, 10, 11, 12, 13) AND rateTypeID in (1,4,5,6);

-- ------ INSERT new instrument rates
INSERT INTO mainDb.InstrumentRate (instrumentID, blockID, rateTypeID, fee, createDate, isCurrent) VALUES
-- -- TSQ ----
-- UW
(7, 26, 1, 3.75, NOW(), 1),
-- UW-FFS
(7, 26, 4, 7.50, NOW(), 1),
-- Non Profit
(7, 26, 5, 42.92, NOW(), 1),
-- Commercial
(7, 26, 6, 97.92, NOW(), 1),

-- -- TSQV ----
(10, 26, 1, 8.00, NOW(), 1),
-- UW-FFS
(10, 26, 4, 16.00, NOW(), 1),
-- Non Profit
(10, 26, 5, 50.62, NOW(), 1),
-- Commercial
(10, 26, 6, 105.62, NOW(), 1),

-- -- OT1 ----
-- UW
(4, 26, 1, 8.00, NOW(), 1),
-- UW-FFS
(4, 26, 4, 16.00, NOW(), 1),
-- Non Profit
(4, 26, 5, 53.05, NOW(), 1),
-- Commercial
(4, 26, 6, 108.05, NOW(), 1),

-- -- QE+ ----
-- UW
(11, 26, 1, 13.50, NOW(), 1),
-- UW-FFS
(11, 26, 4, 27.00, NOW(), 1),
-- Non Profit
(11, 26, 5, 56.51, NOW(), 1),
-- Commercial
(11, 26, 6, 181.51, NOW(), 1),

-- -- Fusion ----
-- UW
(12, 26, 1, 13.50, NOW(), 1),
-- UW-FFS
(12, 26, 4, 27.00, NOW(), 1),
-- Non Profit
(12, 26, 5, 64.42, NOW(), 1),
-- Commercial
(12, 26, 6, 189.42, NOW(), 1),

-- -- Lumos ----
-- UW
(13, 26, 1, 13.50, NOW(), 1),
-- UW-FFS
(13, 26, 4, 27.00, NOW(), 1),
-- Non Profit
(13, 26, 5, 64.85, NOW(), 1),
-- Commercial
(13, 26, 6, 190.86, NOW(), 1)
;

-- -------------------------------------------------------------------------------- --
-- Change rateIDs for previously scheduled blocks
-- NOTE: These are all UW projects: rateyTypeID = 1
-- -------------------------------------------------------------------------------- --
--SELECT iu.projectID, p.affiliation,
--iu.instrumentID, iu.instrumentRateID, ir.id
--ir.rateTypeID, ir.blockID, ir.fee, iu.setupBlock,
--iu.startDate, iu.endDate, iu.deleted
--FROM mainDb.instrumentUsage iu
--INNER JOIN mainDb.instrumentRate ir ON (iu.instrumentID = ir.instrumentID AND ir.blockID=26 AND rateTypeID=1)
--INNER JOIN mainDb.tblProjects p ON (p.projectID = iu.projectID)
--WHERE iu.startDate >= '2018-02-01'
UPDATE mainDb.instrumentUsage iu
SET iu.instrumentRateID = (SELECT id from mainDb.instrumentRate ir WHERE ir.instrumentID = iu.instrumentID AND ir.blockID=26 AND rateTypeID=1)
WHERE iu.endDate > '2018-02-01'
AND iu.projectID != 314; -- Project 314 is UW-FFS

UPDATE mainDb.instrumentUsage iu
SET iu.instrumentRateID = (SELECT id from mainDb.instrumentRate ir WHERE ir.instrumentID = iu.instrumentID AND ir.blockID=26 AND rateTypeID=4)
WHERE iu.endDate > '2018-02-01'
AND iu.projectID = 314; -- Project 314 is UW-FFS

