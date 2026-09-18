-- --------------------------------------------------------------------------------
-- RECOVER instrumentUsage.updatedBy FROM instrumentLog, AND FILL THE REST
--
-- updatedBy records the researcher who created or last changed a usage block. Until
-- the InstrumentUsageDAO fix that ships with this script, five paths left it NULL or
-- 0, or left an earlier researcher in it.
--
--  - A new booking stored NULL.
--  - An invoice export that split a block wrote 0 to the shortened block and NULL to
--    the continuation.
--  - A dates edit stored NULL on the replacement block, from 2018 on.
--  - A change to only the payment methods never wrote updatedBy.
--  - An operator-only edit wrote the block's previous updater back.
--
-- instrumentLog still names the researcher for the block splits due to invoicing, the
-- scheduled date edits and the payment changes. Rules 1 to 3 copy that id into updatedBy.
-- The operator-only edit logged the previous updater too, so it cannot be recovered. Rule 4
-- then fills every block still NULL or 0 with the researcher who booked it.
--
-- Rules 1 to 3 look only at the block's latest instrumentLog row, and recover an
-- updater only when that row is the change that lost it. A block changed again
-- afterwards is left to rule 4, because the researcher of the earlier change is no
-- longer the one who last changed it.
--
-- Run it after the fix is deployed, so no new NULL or 0 arrives, and before
-- innodb_migration.sql. Afterwards no block has updatedBy NULL or 0, so section 1.9 of
-- that script finds nothing to convert and the column can be made NOT NULL.
--
-- BACKUP FIRST.
--
--   mysqldump -u <user> -p mainDb instrumentUsage \
--     --result-file="$HOME/backups/instrumentUsage-pre-recover-<date>.sql"
--
-- Every UPDATE assigns lastChanged to itself. lastChanged is ON UPDATE
-- CURRENT_TIMESTAMP, and without that it would be restamped with the time this runs.
--
-- The whole script runs in one session, because the temporary tables below are
-- per-connection.
--
-- --------------------------------------------------------------------------------


-- The temporary tables are created in mainDb.
USE mainDb;

-- The latest instrumentLog row for every block.
CREATE TEMPORARY TABLE recover_latest_log AS
SELECT l.blockId, MAX(l.id) AS logId FROM mainDb.instrumentLog l GROUP BY l.blockId;
ALTER TABLE recover_latest_log ADD PRIMARY KEY (blockId);


-- Before. Keep this output to compare with the counts at the end.
SELECT 'updatedBy = 0' AS what, COUNT(*) AS blocks FROM mainDb.instrumentUsage WHERE updatedBy = 0
UNION ALL SELECT 'updatedBy IS NULL', COUNT(*) FROM mainDb.instrumentUsage WHERE updatedBy IS NULL;


-- ================================================================================
-- 1. Invoice splits
--
-- Both halves of a split record the admin who ran the export. Both log rows name the
-- invoice, and invoice.createdBy is the admin who created it. In the production data
-- every split was logged 0 to 3 seconds after its invoice was created, by the export
-- that created it, so createdBy is the admin who ran that export. The rule requires
-- the log row to be at most 10 seconds after invoice.createDate. A later re-export
-- splits a block only if an admin booked or moved it across the period end after the
-- invoice was created, and the rule leaves such a split alone.
-- ================================================================================

UPDATE mainDb.instrumentUsage u
JOIN recover_latest_log x ON x.blockId = u.id
JOIN mainDb.instrumentLog l ON l.id = x.logId
JOIN mainDb.invoice i
  ON i.id = CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(l.log, 'Invoice: ID: ', -1), ';', 1) AS UNSIGNED)
JOIN mainDb.tblResearchers r ON r.researcherID = i.createdBy
SET u.updatedBy = i.createdBy, u.lastChanged = u.lastChanged
WHERE (u.updatedBy IS NULL OR u.updatedBy = 0)
  AND ((l.action = 'EDITED' AND l.log LIKE 'Update due to invoicing. Invoice: ID: %')
    OR (l.action = 'ADDED' AND l.log LIKE 'Added due to invoicing.%Invoice: ID: %'))
  AND l.created BETWEEN i.createDate AND i.createDate + INTERVAL 10 SECOND;

SELECT ROW_COUNT() AS invoice_splits_recovered;


-- ================================================================================
-- 2. Dates edits
--
-- A dates edit deletes the old block and inserts a replacement in one transaction.
-- The replacement's "Added by edit action" row logs the original booker, but the
-- "Deleted by edit action" row for the old block logs the editor.
--
-- The rule takes the editor from a delete row for the same project and instrument
-- within one second. If delete rows from two different researchers match, the rule
-- cannot tell which edit made the block and skips it. None did in the production data.
--
-- The rule was checked on the 591 blocks created by a dates edit before 2018-04-24,
-- when a dates edit still stored its editor in updatedBy. For every one it gives the
-- stored updatedBy.
-- ================================================================================

CREATE TEMPORARY TABLE recover_edit_editor AS
SELECT x.blockId, MIN(d.userId) AS editor, COUNT(DISTINCT d.userId) AS editors
FROM recover_latest_log x
JOIN mainDb.instrumentLog l ON l.id = x.logId
JOIN mainDb.instrumentLog d
  ON d.projectId = l.projectId AND d.instrumentID = l.instrumentID
 AND d.created BETWEEN l.created - INTERVAL 1 SECOND AND l.created + INTERVAL 1 SECOND
 AND d.action IN ('DELETED', 'PURGED') AND d.log LIKE 'Deleted by edit action%'
WHERE l.action = 'ADDED' AND l.log = 'Added by edit action'
GROUP BY x.blockId;

UPDATE mainDb.instrumentUsage u
JOIN recover_edit_editor e ON e.blockId = u.id
JOIN mainDb.tblResearchers r ON r.researcherID = e.editor
SET u.updatedBy = e.editor, u.lastChanged = u.lastChanged
WHERE u.updatedBy IS NULL AND e.editors = 1;

SELECT ROW_COUNT() AS dates_edits_recovered;


-- ================================================================================
-- 3. Payment method changes
--
-- Edit Project & Payment Method (EditBlockDetailsAction) logs each payment change
-- with the editor's id, but wrote updatedBy only when the project changed as well. A
-- payment-only change left updatedBy NULL, or naming whoever changed the block before.
-- The rule sets updatedBy to the researcher on the block's latest log row when that
-- row is a "Changed payment method" row.
--
-- A payment-only change does not write the instrumentUsage row, so lastChanged stays
-- older than its log row. A block whose lastChanged is newer was changed afterwards
-- by something that logs nothing, for example updateSetupBlocks, which records the
-- researcher who moved the setup flag. The rule leaves that block alone.
--
-- Saving the form with the payments unchanged also wrote these log rows, so the
-- recovered researcher may be someone who saved the form without changing anything.
-- ================================================================================

UPDATE mainDb.instrumentUsage u
JOIN recover_latest_log x ON x.blockId = u.id
JOIN mainDb.instrumentLog l ON l.id = x.logId
JOIN mainDb.tblResearchers r ON r.researcherID = l.userId
SET u.updatedBy = l.userId, u.lastChanged = u.lastChanged
WHERE l.action = 'EDITED' AND l.log LIKE 'Changed payment method%'
  AND (u.updatedBy IS NULL OR u.updatedBy <> l.userId)
  AND u.lastChanged <= l.created + INTERVAL 1 SECOND;

SELECT ROW_COUNT() AS payment_changes_recovered;


-- ================================================================================
-- 4. Every other block
--
-- A block with no recorded change takes the researcher who booked it. That is exact
-- for a block never changed after booking. For a block changed before instrumentLog
-- began in May 2017, or changed by a path that logged no researcher, the researcher
-- who changed it is unknown and the booker stands in.
-- ================================================================================

UPDATE mainDb.instrumentUsage u
JOIN mainDb.tblResearchers r ON r.researcherID = u.enteredBy
SET u.updatedBy = u.enteredBy, u.lastChanged = u.lastChanged
WHERE u.updatedBy IS NULL OR u.updatedBy = 0;

SELECT ROW_COUNT() AS filled_from_enteredBy;


-- After. updatedBy = 0 and updatedBy IS NULL should both count 0.
SELECT 'updatedBy = 0' AS what, COUNT(*) AS blocks FROM mainDb.instrumentUsage WHERE updatedBy = 0
UNION ALL SELECT 'updatedBy IS NULL', COUNT(*) FROM mainDb.instrumentUsage WHERE updatedBy IS NULL
UNION ALL SELECT 'updatedBy with no researcher', COUNT(*) FROM mainDb.instrumentUsage u
  LEFT JOIN mainDb.tblResearchers r ON r.researcherID = u.updatedBy
  WHERE u.updatedBy IS NOT NULL AND u.updatedBy <> 0 AND r.researcherID IS NULL;

DROP TEMPORARY TABLE recover_edit_editor;
DROP TEMPORARY TABLE recover_latest_log;
