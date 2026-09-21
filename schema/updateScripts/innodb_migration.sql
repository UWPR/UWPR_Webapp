-- --------------------------------------------------------------------------------
-- INNODB MIGRATION
--
-- Converts mainDb and pr to InnoDB and adds foreign keys, so that deleting a row
-- removes its child rows in one atomic statement.
--
-- Until now nothing enforced referential integrity. There are no foreign keys, no
-- triggers, and most tables are MyISAM, which supports neither. Every gap in the
-- application's own delete code shows up as orphaned rows.
--
-- Run the sections in order. Each one is separately runnable and is followed by a
-- verification query. Read the verification output before starting the next section.
--
-- Each section opens with USE mainDb. Without a default database the first statement of
-- section 1 stops with "No database selected" and nothing is applied, even though every
-- table below is named with its schema.
--
-- BACKUP FIRST. Most tables are MyISAM, so START TRANSACTION and ROLLBACK are
-- accepted and ignored, and every DELETE below applies immediately.
--
--   mysqldump -u <user> -p --databases mainDb pr --skip-triggers \
--     --result-file="$HOME/backups/mainDb_pr-pre-innodb-<date>.sql"
--
-- The connection needs DELETE and ALTER on both mainDb and pr. Several statements
-- join across the two schemas.
--
-- PREREQUISITES, both outside this script.
--
--  1. The InstrumentUsageDAO fix has to be deployed to the server, and then
--     schema/updateScripts/recover_updatedBy.sql run against it. Section 2.3 makes
--     instrumentUsage.updatedBy NOT NULL and section 5.6 keys it to tblResearchers, so every
--     block has to name a real researcher before either runs. That fix makes setUpdatedBy
--     throw rather than store 0, so nothing writes a new one after the recovery.
--  2. The transaction handling has to be deployed as well. Two sites restored autocommit
--     over an open transaction, which commits half of it once the tables are InnoDB.
--
-- --------------------------------------------------------------------------------


-- ================================================================================
-- SECTION 1 - ORPHAN CLEANUP
--
-- ALTER TABLE ... ADD FOREIGN KEY is rejected while any row violates the constraint,
-- so every orphan a later section constrains has to go first. Children are deleted
-- before parents, so no step leaves an orphan for a later step.
-- ================================================================================

USE mainDb;

-- 1.1 Usage blocks whose project is gone.
--
-- Blocks 449 and 463 belong to project 74, deleted at some point after October 2008.
-- Both are FREE rate at $0.00 on the "Block for old usage" time block, with no payment
-- rows, no invoice links and no instrumentLog rows. They are the only surviving trace
-- of project 74. ProjectDAO.getScheduledProjects reads projectID from instrumentUsage
-- with no join to tblProjects, so BillingInformationExcelExporter.getProject throws on
-- them and fails any export covering September or October 2008.

DELETE s FROM mainDb.instrumentUsagePayment s
  JOIN mainDb.instrumentUsage iu ON iu.id = s.instrumentUsageID
  LEFT JOIN mainDb.tblProjects p ON p.projectID = iu.projectID WHERE p.projectID IS NULL;

DELETE v FROM mainDb.invoiceInstrumentUsage v
  JOIN mainDb.instrumentUsage iu ON iu.id = v.instrumentUsageID
  LEFT JOIN mainDb.tblProjects p ON p.projectID = iu.projectID WHERE p.projectID IS NULL;

DELETE iu FROM mainDb.instrumentUsage iu
  LEFT JOIN mainDb.tblProjects p ON p.projectID = iu.projectID WHERE p.projectID IS NULL;

-- 1.2 Payment rows whose usage block is gone.
--
-- They add nothing to any cost, and they stop a payment method being deleted.

DELETE iup FROM mainDb.instrumentUsagePayment iup
  LEFT JOIN mainDb.instrumentUsage iu ON iu.id = iup.instrumentUsageID WHERE iu.id IS NULL;

-- 1.3 Invoice links whose usage block is gone.

DELETE iiu FROM mainDb.invoiceInstrumentUsage iiu
  LEFT JOIN mainDb.instrumentUsage iu ON iu.id = iiu.instrumentUsageID WHERE iu.id IS NULL;

-- 1.4 Invoice links whose invoice is gone.
--
-- A failed export deleted invoice 147 and left this link to block 22078. The block
-- displays as billed, cannot be edited or deleted, and an export covering it fails.
-- Deleting the row removes the only record that the block was ever on an invoice.

DELETE iiu FROM mainDb.invoiceInstrumentUsage iiu
  LEFT JOIN mainDb.invoice i ON i.id = iiu.invoiceID WHERE i.id IS NULL;

-- 1.5 Project links whose payment method is gone.
--
-- PaymentMethodDAO.getPaymentMethod logs an ERROR on every load of a project holding
-- one of these.

DELETE ppm FROM mainDb.projectPaymentMethod ppm
  LEFT JOIN mainDb.paymentMethod pm ON pm.id = ppm.paymentMethodID WHERE pm.id IS NULL;

-- 1.6 Rows in mainDb whose project is gone.

DELETE c FROM mainDb.projectResearcher c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM mainDb.tblProjectExperiment c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM mainDb.projectGrant c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM mainDb.projectGroup c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM mainDb.tblProjectProteinInference c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;

-- 1.7 Rows in pr whose project is gone.

DELETE c FROM pr.externalDataLocations c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM pr.projectRawDataSummary c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM pr.projectReportReminder c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM pr.projectReviewer c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;
DELETE c FROM pr.collaborationRejected c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL;

-- 1.8 Uploaded files whose project is gone.
--
-- STEMVAC coding sequence for proteomics.docx and
-- UWPR sample submission form - STEMVAC 12M.xlsx, both uploaded 2024-11-22.
-- Nothing else links to either blob. The blob goes with the link, so both documents
-- are deleted for good.

-- The blobs that at least one dead project points at. Candidates only, because a blob can
-- be linked from more than one project.
CREATE TEMPORARY TABLE orphan_file_ids AS
  SELECT DISTINCT pf.file_id FROM pr.projectFiles pf
  LEFT JOIN mainDb.tblProjects p ON p.projectID = pf.project_id WHERE p.projectID IS NULL;

-- Remove a link row only when its own project is gone. Joining the candidate ids straight
-- back to projectFiles would also remove a live project's link to a shared blob.
DELETE pf FROM pr.projectFiles pf
  LEFT JOIN mainDb.tblProjects p ON p.projectID = pf.project_id
  WHERE p.projectID IS NULL;

-- Then the candidates nothing points at any more. A blob a live project still links to stays.
DELETE f FROM pr.files f
  JOIN orphan_file_ids o ON o.file_id = f.id
  LEFT JOIN pr.projectFiles pf ON pf.file_id = f.id
  WHERE pf.file_id IS NULL;

DROP TEMPORARY TABLE orphan_file_ids;

-- 1.9 updatedBy has to name a real researcher before section 2 makes it NOT NULL.
--
-- Nothing is done here. schema/updateScripts/recover_updatedBy.sql fills every block that
-- held 0 or NULL and is a prerequisite of this script, so the verification below should
-- already read 0. If it does not, stop and run that script before starting section 2.

-- --- Verify section 1. Every count must be 0. ---

SELECT 'instrumentUsage -> tblProjects' AS relationship, COUNT(*) AS orphans
  FROM mainDb.instrumentUsage c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'instrumentUsagePayment -> instrumentUsage', COUNT(*)
  FROM mainDb.instrumentUsagePayment c
  LEFT JOIN mainDb.instrumentUsage p ON p.id = c.instrumentUsageID WHERE p.id IS NULL
UNION ALL SELECT 'instrumentUsagePayment -> paymentMethod', COUNT(*)
  FROM mainDb.instrumentUsagePayment c
  LEFT JOIN mainDb.paymentMethod p ON p.id = c.paymentMethodID WHERE p.id IS NULL
UNION ALL SELECT 'invoiceInstrumentUsage -> instrumentUsage', COUNT(*)
  FROM mainDb.invoiceInstrumentUsage c
  LEFT JOIN mainDb.instrumentUsage p ON p.id = c.instrumentUsageID WHERE p.id IS NULL
UNION ALL SELECT 'invoiceInstrumentUsage -> invoice', COUNT(*)
  FROM mainDb.invoiceInstrumentUsage c
  LEFT JOIN mainDb.invoice p ON p.id = c.invoiceID WHERE p.id IS NULL
UNION ALL SELECT 'instrumentUsage -> instrumentRate', COUNT(*)
  FROM mainDb.instrumentUsage c
  LEFT JOIN mainDb.instrumentRate p ON p.id = c.instrumentRateID WHERE p.id IS NULL
UNION ALL SELECT 'instrumentRate -> timeBlock', COUNT(*)
  FROM mainDb.instrumentRate c
  LEFT JOIN mainDb.timeBlock p ON p.id = c.blockID WHERE p.id IS NULL
UNION ALL SELECT 'instrumentRate -> rateType', COUNT(*)
  FROM mainDb.instrumentRate c
  LEFT JOIN mainDb.rateType p ON p.id = c.rateTypeID WHERE p.id IS NULL
UNION ALL SELECT 'projectPaymentMethod -> paymentMethod', COUNT(*)
  FROM mainDb.projectPaymentMethod c
  LEFT JOIN mainDb.paymentMethod p ON p.id = c.paymentMethodID WHERE p.id IS NULL
UNION ALL SELECT 'projectPaymentMethod -> tblProjects', COUNT(*)
  FROM mainDb.projectPaymentMethod c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'projectResearcher -> tblProjects', COUNT(*)
  FROM mainDb.projectResearcher c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'tblProjectExperiment -> tblProjects', COUNT(*)
  FROM mainDb.tblProjectExperiment c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'projectGrant -> tblProjects', COUNT(*)
  FROM mainDb.projectGrant c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'projectGroup -> tblProjects', COUNT(*)
  FROM mainDb.projectGroup c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'tblProjectProteinInference -> tblProjects', COUNT(*)
  FROM mainDb.tblProjectProteinInference c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'tblCollaboration -> tblProjects', COUNT(*)
  FROM mainDb.tblCollaboration c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.tblBilledProject -> tblProjects', COUNT(*)
  FROM pr.tblBilledProject c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.externalDataLocations -> tblProjects', COUNT(*)
  FROM pr.externalDataLocations c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.projectRawDataSummary -> tblProjects', COUNT(*)
  FROM pr.projectRawDataSummary c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.projectReportReminder -> tblProjects', COUNT(*)
  FROM pr.projectReportReminder c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.projectReviewer -> tblProjects', COUNT(*)
  FROM pr.projectReviewer c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.collaborationRejected -> tblProjects', COUNT(*)
  FROM pr.collaborationRejected c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.projectID WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.projectFiles -> tblProjects', COUNT(*)
  FROM pr.projectFiles c
  LEFT JOIN mainDb.tblProjects p ON p.projectID = c.project_id WHERE p.projectID IS NULL
UNION ALL SELECT 'pr.projectFiles -> pr.files', COUNT(*)
  FROM pr.projectFiles c
  LEFT JOIN pr.files p ON p.id = c.file_id WHERE p.id IS NULL
UNION ALL SELECT 'instrumentUsage.updatedBy not yet recovered', COUNT(*)
  FROM mainDb.instrumentUsage WHERE updatedBy = 0 OR updatedBy IS NULL
UNION ALL SELECT 'instrumentUsage.updatedBy -> tblResearchers', COUNT(*)
  FROM mainDb.instrumentUsage c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.updatedBy
  WHERE c.updatedBy IS NOT NULL AND p.researcherID IS NULL
UNION ALL SELECT 'instrumentUsage.enteredBy -> tblResearchers', COUNT(*)
  FROM mainDb.instrumentUsage c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.enteredBy WHERE p.researcherID IS NULL
UNION ALL SELECT 'instrumentUsage.instrumentOperatorId -> tblResearchers', COUNT(*)
  FROM mainDb.instrumentUsage c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.instrumentOperatorId
  WHERE c.instrumentOperatorId IS NOT NULL AND p.researcherID IS NULL
UNION ALL SELECT 'tblProjects.projectPI -> tblResearchers', COUNT(*)
  FROM mainDb.tblProjects c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.projectPI
  WHERE c.projectPI IS NOT NULL AND p.researcherID IS NULL
UNION ALL SELECT 'tblUsers -> tblResearchers', COUNT(*)
  FROM mainDb.tblUsers c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.researcherID WHERE p.researcherID IS NULL
UNION ALL SELECT 'projectResearcher -> tblResearchers', COUNT(*)
  FROM mainDb.projectResearcher c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.researcherID WHERE p.researcherID IS NULL
UNION ALL SELECT 'tblProjectProteinInference -> tblResearchers', COUNT(*)
  FROM mainDb.tblProjectProteinInference c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.researcherID WHERE p.researcherID IS NULL
UNION ALL SELECT 'tblYRCGroupMembers -> tblResearchers', COUNT(*)
  FROM mainDb.tblYRCGroupMembers c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.researcherID WHERE p.researcherID IS NULL
UNION ALL SELECT 'tblYRCGroupMembers -> tblYRCGroups', COUNT(*)
  FROM mainDb.tblYRCGroupMembers c
  LEFT JOIN mainDb.tblYRCGroups p ON p.groupID = c.groupID WHERE p.groupID IS NULL
UNION ALL SELECT 'projectGroup -> tblYRCGroups', COUNT(*)
  FROM mainDb.projectGroup c
  LEFT JOIN mainDb.tblYRCGroups p ON p.groupID = c.groupID WHERE p.groupID IS NULL
UNION ALL SELECT 'pr.projectReviewer -> tblResearchers', COUNT(*)
  FROM pr.projectReviewer c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.researcherID WHERE p.researcherID IS NULL
UNION ALL SELECT 'pr.collaborationRejected -> tblResearchers', COUNT(*)
  FROM pr.collaborationRejected c
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.researcherID WHERE p.researcherID IS NULL;


-- ================================================================================
-- SECTION 2 - COLUMN TYPE HARMONISATION
--
-- A foreign key requires the referencing and referenced columns to have exactly
-- matching types, including width and signedness.
--
-- Three parent keys are mediumint(8) unsigned while most of their children are
-- int(10) unsigned. Widening the parents is the safe direction. Every one of them is
-- far below the mediumint unsigned ceiling, so no value can change.
--
-- Three child columns are signed int(11). A signed column blocks a foreign key even
-- once the widths agree. None holds a negative value.
--
-- No value changes here, so this section can be run and left in place on its own.
-- ================================================================================

USE mainDb;

-- 2.3 makes updatedBy NOT NULL, and that only refuses a surviving NULL while the session is
-- strict. Measured both ways on MariaDB 10.6 -- with sql_mode empty the same ALTER succeeds
-- and writes 0, which is the value recover_updatedBy.sql exists to remove, with nothing said
-- to the operator. 10.6 is strict by default, but sql_mode is a session setting a client can
-- change, so this does not rely on the default.
SET SESSION sql_mode = CONCAT(@@sql_mode, ',STRICT_ALL_TABLES');

-- 2.1 Parent keys.

ALTER TABLE mainDb.tblProjects    MODIFY COLUMN projectID    INT(10) UNSIGNED NOT NULL AUTO_INCREMENT;
ALTER TABLE mainDb.tblResearchers MODIFY COLUMN researcherID INT(10) UNSIGNED NOT NULL AUTO_INCREMENT;
ALTER TABLE mainDb.tblYRCGroups   MODIFY COLUMN groupID      INT(10) UNSIGNED NOT NULL AUTO_INCREMENT;

-- 2.2 Children of tblProjects still on mediumint.

ALTER TABLE mainDb.projectGrant     MODIFY COLUMN projectID INT(10) UNSIGNED NOT NULL;
ALTER TABLE mainDb.tblCollaboration MODIFY COLUMN projectID INT(10) UNSIGNED NOT NULL DEFAULT 0;

-- 2.3 Children of tblResearchers. tblUsers is signed today.
--
-- updatedBy becomes NOT NULL. Every block names a real researcher once
-- recover_updatedBy.sql has run, and InstrumentUsageDAO.setUpdatedBy throws rather than
-- writing 0 or NULL, so nothing puts one back. The ALTER fails on a row that is still NULL,
-- which is what stops the migration when that script has not run.
--
-- instrumentOperatorId stays nullable. 4736 blocks from 2016 and earlier have no operator.

ALTER TABLE mainDb.tblProjects        MODIFY COLUMN projectPI            INT(10) UNSIGNED NULL;
ALTER TABLE mainDb.tblUsers           MODIFY COLUMN researcherID         INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE mainDb.tblYRCGroupMembers MODIFY COLUMN researcherID         INT(10) UNSIGNED NOT NULL DEFAULT 0;
ALTER TABLE mainDb.instrumentUsage    MODIFY COLUMN instrumentOperatorId INT(10) UNSIGNED NULL;
ALTER TABLE mainDb.instrumentUsage    MODIFY COLUMN updatedBy            INT(10) UNSIGNED NOT NULL;

-- 2.4 Children of tblYRCGroups. tblYRCGroupMembers is signed today.

ALTER TABLE mainDb.tblYRCGroupMembers MODIFY COLUMN groupID INT(10) UNSIGNED NOT NULL DEFAULT 0;

-- --- Verify section 2. Every column must read int(10) unsigned, and the three
-- --- AUTO_INCREMENT values must be unchanged. ---

SELECT table_schema, table_name, column_name, column_type, extra
FROM information_schema.columns
WHERE (table_schema = 'mainDb' AND
       ((table_name = 'tblProjects'        AND column_name IN ('projectID', 'projectPI', 'parentProjectID')) OR
        (table_name = 'tblResearchers'     AND column_name = 'researcherID') OR
        (table_name = 'tblYRCGroups'       AND column_name = 'groupID') OR
        (table_name = 'projectGrant'       AND column_name = 'projectID') OR
        (table_name = 'tblCollaboration'   AND column_name = 'projectID') OR
        (table_name = 'tblUsers'           AND column_name = 'researcherID') OR
        (table_name = 'tblYRCGroupMembers' AND column_name IN ('groupID', 'researcherID')) OR
        (table_name = 'instrumentUsage'    AND column_name IN ('instrumentOperatorId', 'updatedBy'))))
ORDER BY table_name, column_name;

SELECT MAX(projectID) AS max_project FROM mainDb.tblProjects;
SELECT MAX(researcherID) AS max_researcher FROM mainDb.tblResearchers;
SELECT MAX(groupID) AS max_group FROM mainDb.tblYRCGroups;


-- ================================================================================
-- SECTION 3 - ENGINE CONVERSION
--
-- InnoDB is what makes a foreign key possible. It also makes the multi-step writes
-- in InvoiceDAO.delete and ProjectPaymentMethodDAO.deletePaymentMethod atomic rather
-- than partial.
--
-- Every table rebuilds. All are small except pr.files, which holds the uploaded file
-- blobs. Each ALTER locks its table, so run this in a maintenance window.
--
-- msData is left on MyISAM. Only msInstrument is read by this webapp, and the keys
-- from instrumentUsage and instrumentRate to msInstrument stay deferred.
-- ================================================================================

USE mainDb;

-- 3.1 mainDb, the 26 MyISAM tables.
--
-- The tblYates* tables, tblExperiments, grants and ncbi_taxonomy are YRC-era tables
-- that no source file reads. They convert with the rest so the schema is uniform.

ALTER TABLE mainDb.config_msdapl_webapp       ENGINE=InnoDB;
ALTER TABLE mainDb.grants                     ENGINE=InnoDB;
ALTER TABLE mainDb.instrumentRate             ENGINE=InnoDB;
ALTER TABLE mainDb.invoice                    ENGINE=InnoDB;
ALTER TABLE mainDb.ncbi_taxonomy              ENGINE=InnoDB;
ALTER TABLE mainDb.paymentMethod              ENGINE=InnoDB;
ALTER TABLE mainDb.projectGrant               ENGINE=InnoDB;
ALTER TABLE mainDb.projectGroup               ENGINE=InnoDB;
ALTER TABLE mainDb.projectResearcher          ENGINE=InnoDB;
ALTER TABLE mainDb.rateType                   ENGINE=InnoDB;
ALTER TABLE mainDb.tblCollaboration           ENGINE=InnoDB;
ALTER TABLE mainDb.tblExperiments             ENGINE=InnoDB;
ALTER TABLE mainDb.tblProjectExperiment       ENGINE=InnoDB;
ALTER TABLE mainDb.tblProjectProteinInference ENGINE=InnoDB;
ALTER TABLE mainDb.tblProjects                ENGINE=InnoDB;
ALTER TABLE mainDb.tblResearchers             ENGINE=InnoDB;
ALTER TABLE mainDb.tblUsers                   ENGINE=InnoDB;
ALTER TABLE mainDb.tblYatesCycleMS2Data       ENGINE=InnoDB;
ALTER TABLE mainDb.tblYatesCycles             ENGINE=InnoDB;
ALTER TABLE mainDb.tblYatesCyclesQTData       ENGINE=InnoDB;
ALTER TABLE mainDb.tblYatesResultPeptide      ENGINE=InnoDB;
ALTER TABLE mainDb.tblYatesRun                ENGINE=InnoDB;
ALTER TABLE mainDb.tblYatesRunResult          ENGINE=InnoDB;
ALTER TABLE mainDb.tblYRCGroupMembers         ENGINE=InnoDB;
ALTER TABLE mainDb.tblYRCGroups               ENGINE=InnoDB;
ALTER TABLE mainDb.timeBlock                  ENGINE=InnoDB;

-- 3.2 pr. pr.files is much the largest, since it holds the uploaded file blobs.

ALTER TABLE pr.collaborationRejected       ENGINE=InnoDB;
ALTER TABLE pr.collaborationRejectionCause ENGINE=InnoDB;
ALTER TABLE pr.externalDataLocations       ENGINE=InnoDB;
ALTER TABLE pr.files                       ENGINE=InnoDB;
ALTER TABLE pr.projectFiles                ENGINE=InnoDB;
ALTER TABLE pr.projectRawDataSummary       ENGINE=InnoDB;
ALTER TABLE pr.projectReportReminder       ENGINE=InnoDB;
ALTER TABLE pr.projectReviewer             ENGINE=InnoDB;
ALTER TABLE pr.tblBilledProject            ENGINE=InnoDB;

-- --- Verify section 3. The result must be empty. A row here names a schema that
-- --- still has MyISAM tables. ---

SELECT table_schema, COUNT(*) AS myisam_tables_left
FROM information_schema.tables
WHERE table_schema IN ('mainDb', 'pr') AND engine = 'MyISAM'
GROUP BY table_schema;


-- ================================================================================
-- SECTION 4 - INDEXES FOR THE FOREIGN KEYS
--
-- InnoDB creates an index for a foreign key when no suitable one exists, under a name
-- it chooses. These are named here instead, so the schema does not depend on that.
--
-- projectGroup and tblProjectExperiment have no index of any kind today.
-- ================================================================================

USE mainDb;

-- 4.1 For the project, usage and invoicing keys in 5.1 to 5.5.

ALTER TABLE mainDb.instrumentUsage      ADD INDEX instrumentRateID (instrumentRateID);
ALTER TABLE mainDb.projectPaymentMethod ADD INDEX paymentMethodID (paymentMethodID);
ALTER TABLE mainDb.projectGrant         ADD INDEX projectID (projectID);
ALTER TABLE mainDb.projectGroup         ADD INDEX projectID (projectID);
ALTER TABLE mainDb.tblProjectExperiment ADD INDEX projectID (projectID);
ALTER TABLE pr.externalDataLocations    ADD INDEX projectID (projectID);
ALTER TABLE pr.projectFiles             ADD INDEX project_id (project_id);

-- 4.2 For the researcher and group keys in 5.6.
--
-- projectResearcher.researcherID, tblUsers.researcherID and
-- tblYRCGroupMembers.groupID are already covered.

ALTER TABLE mainDb.instrumentUsage            ADD INDEX enteredBy (enteredBy);
ALTER TABLE mainDb.instrumentUsage            ADD INDEX updatedBy (updatedBy);
ALTER TABLE mainDb.instrumentUsage            ADD INDEX instrumentOperatorId (instrumentOperatorId);
ALTER TABLE mainDb.tblProjects                ADD INDEX projectPI (projectPI);
ALTER TABLE mainDb.tblProjectProteinInference ADD INDEX researcherID (researcherID);
ALTER TABLE mainDb.tblYRCGroupMembers         ADD INDEX researcherID (researcherID);
ALTER TABLE mainDb.projectGroup               ADD INDEX groupID (groupID);
ALTER TABLE pr.projectReviewer                ADD INDEX researcherID (researcherID);
ALTER TABLE pr.collaborationRejected          ADD INDEX researcherID (researcherID);

-- --- Verify section 4. Must read 16, one per ADD INDEX above. A short count means a
-- --- statement failed, and section 5 would then let InnoDB create that index under a name
-- --- of its own choosing, which is what this section exists to prevent. ---

SELECT COUNT(DISTINCT table_schema, table_name, index_name) AS indexes_added
FROM information_schema.statistics
WHERE (table_schema = 'mainDb' AND
       ((table_name = 'instrumentUsage'            AND index_name IN ('instrumentRateID', 'enteredBy', 'updatedBy', 'instrumentOperatorId')) OR
        (table_name = 'projectPaymentMethod'       AND index_name = 'paymentMethodID') OR
        (table_name = 'projectGrant'               AND index_name = 'projectID') OR
        (table_name = 'projectGroup'               AND index_name IN ('projectID', 'groupID')) OR
        (table_name = 'tblProjectExperiment'       AND index_name = 'projectID') OR
        (table_name = 'tblProjects'                AND index_name = 'projectPI') OR
        (table_name = 'tblProjectProteinInference' AND index_name = 'researcherID') OR
        (table_name = 'tblYRCGroupMembers'         AND index_name = 'researcherID')))
   OR (table_schema = 'pr' AND
       ((table_name = 'externalDataLocations'      AND index_name = 'projectID') OR
        (table_name = 'projectFiles'               AND index_name = 'project_id') OR
        (table_name = 'projectReviewer'            AND index_name = 'researcherID') OR
        (table_name = 'collaborationRejected'      AND index_name = 'researcherID')));


-- ================================================================================
-- SECTION 5 - FOREIGN KEYS
--
-- CASCADE where a child row should follow its parent. RESTRICT where the delete
-- should be refused instead.
-- ================================================================================

USE mainDb;

-- 5.1 The usage and invoicing tables.
--
-- instrumentRate is RESTRICT, never CASCADE. Cascading a rate delete is the
-- instrumentRate_bdelete trigger behaviour that would wipe usage history. The
-- trigger definitions in schema/cost_center_tables.sql were never applied to any
-- live database and must not be.

ALTER TABLE mainDb.instrumentUsagePayment
  ADD CONSTRAINT fk_instrumentUsagePayment_instrumentUsage
  FOREIGN KEY (instrumentUsageID) REFERENCES mainDb.instrumentUsage (id) ON DELETE CASCADE;

ALTER TABLE mainDb.instrumentUsagePayment
  ADD CONSTRAINT fk_instrumentUsagePayment_paymentMethod
  FOREIGN KEY (paymentMethodID) REFERENCES mainDb.paymentMethod (id) ON DELETE RESTRICT;

ALTER TABLE mainDb.invoiceInstrumentUsage
  ADD CONSTRAINT fk_invoiceInstrumentUsage_instrumentUsage
  FOREIGN KEY (instrumentUsageID) REFERENCES mainDb.instrumentUsage (id) ON DELETE CASCADE;

ALTER TABLE mainDb.invoiceInstrumentUsage
  ADD CONSTRAINT fk_invoiceInstrumentUsage_invoice
  FOREIGN KEY (invoiceID) REFERENCES mainDb.invoice (id) ON DELETE CASCADE;

ALTER TABLE mainDb.projectPaymentMethod
  ADD CONSTRAINT fk_projectPaymentMethod_paymentMethod
  FOREIGN KEY (paymentMethodID) REFERENCES mainDb.paymentMethod (id) ON DELETE CASCADE;

ALTER TABLE mainDb.instrumentUsage
  ADD CONSTRAINT fk_instrumentUsage_instrumentRate
  FOREIGN KEY (instrumentRateID) REFERENCES mainDb.instrumentRate (id) ON DELETE RESTRICT;

-- 5.2 The rate tables.

ALTER TABLE mainDb.instrumentRate
  ADD CONSTRAINT fk_instrumentRate_timeBlock
  FOREIGN KEY (blockID) REFERENCES mainDb.timeBlock (id) ON DELETE RESTRICT;

ALTER TABLE mainDb.instrumentRate
  ADD CONSTRAINT fk_instrumentRate_rateType
  FOREIGN KEY (rateTypeID) REFERENCES mainDb.rateType (id) ON DELETE RESTRICT;

-- 5.3 Children of tblProjects in mainDb.
--
-- instrumentUsage is RESTRICT. DeleteProjectAction already refuses a project that has
-- any instrumentUsage row, including a cancelled block with deleted = 1.

ALTER TABLE mainDb.instrumentUsage
  ADD CONSTRAINT fk_instrumentUsage_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE RESTRICT;

ALTER TABLE mainDb.projectPaymentMethod
  ADD CONSTRAINT fk_projectPaymentMethod_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE mainDb.projectResearcher
  ADD CONSTRAINT fk_projectResearcher_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE mainDb.tblProjectExperiment
  ADD CONSTRAINT fk_tblProjectExperiment_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE mainDb.projectGrant
  ADD CONSTRAINT fk_projectGrant_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE mainDb.projectGroup
  ADD CONSTRAINT fk_projectGroup_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE mainDb.tblProjectProteinInference
  ADD CONSTRAINT fk_tblProjectProteinInference_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE mainDb.tblCollaboration
  ADD CONSTRAINT fk_tblCollaboration_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

-- 5.4 Children of tblProjects in pr.
--
-- MariaDB supports a foreign key across schemas on the same server instance. From here
-- on mainDb and pr can no longer be dumped or restored independently.

ALTER TABLE pr.tblBilledProject
  ADD CONSTRAINT fk_tblBilledProject_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE pr.externalDataLocations
  ADD CONSTRAINT fk_externalDataLocations_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE pr.collaborationRejected
  ADD CONSTRAINT fk_collaborationRejected_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE pr.projectRawDataSummary
  ADD CONSTRAINT fk_projectRawDataSummary_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE pr.projectReportReminder
  ADD CONSTRAINT fk_projectReportReminder_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE pr.projectReviewer
  ADD CONSTRAINT fk_projectReviewer_tblProjects
  FOREIGN KEY (projectID) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

ALTER TABLE pr.projectFiles
  ADD CONSTRAINT fk_projectFiles_tblProjects
  FOREIGN KEY (project_id) REFERENCES mainDb.tblProjects (projectID) ON DELETE CASCADE;

-- 5.5 The uploaded file link.
--
-- Project.deleteSharedRows does not touch pr.projectFiles or pr.files today, so a
-- project delete leaves both behind. fk_projectFiles_tblProjects above removes the
-- link row. No key reaches the blob in pr.files, because a blob can be shared, so
-- deleting it is still the application's job.
--
-- That changes what a stranded blob looks like. Today the link row survives a project
-- delete, so the blob can still be traced back to the project it belonged to. Once the key
-- cascades the link away, the blob is left with nothing pointing at it. DataFileDeleter
-- holds the only DELETE FROM files and the per-file page is its only caller, so every blob
-- a deleted project had is stranded and unreachable.

ALTER TABLE pr.projectFiles
  ADD CONSTRAINT fk_projectFiles_files
  FOREIGN KEY (file_id) REFERENCES pr.files (id) ON DELETE CASCADE;

-- 5.6 Children of tblResearchers and tblYRCGroups.
--
-- recover_updatedBy.sql has to have run, and the InstrumentUsageDAO fix has to be live on
-- the server. See PREREQUISITES at the top.
--
-- RESTRICT on the columns that record who did something, so a researcher who entered,
-- edited or ran a block cannot be deleted out from under that record. CASCADE on the
-- rows that only describe what a researcher belongs to.
--
-- Nothing deletes a researcher today. Neither Researcher.delete() nor User.delete() has a
-- caller, and User.delete() removes only the tblUsers row. These keys decide what happens
-- when something does.

ALTER TABLE mainDb.instrumentUsage
  ADD CONSTRAINT fk_instrumentUsage_enteredBy
  FOREIGN KEY (enteredBy) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE RESTRICT;

ALTER TABLE mainDb.instrumentUsage
  ADD CONSTRAINT fk_instrumentUsage_updatedBy
  FOREIGN KEY (updatedBy) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE RESTRICT;

ALTER TABLE mainDb.instrumentUsage
  ADD CONSTRAINT fk_instrumentUsage_instrumentOperator
  FOREIGN KEY (instrumentOperatorId) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE RESTRICT;

ALTER TABLE mainDb.tblProjects
  ADD CONSTRAINT fk_tblProjects_projectPI
  FOREIGN KEY (projectPI) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE RESTRICT;

ALTER TABLE mainDb.tblUsers
  ADD CONSTRAINT fk_tblUsers_tblResearchers
  FOREIGN KEY (researcherID) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE CASCADE;

ALTER TABLE mainDb.projectResearcher
  ADD CONSTRAINT fk_projectResearcher_tblResearchers
  FOREIGN KEY (researcherID) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE CASCADE;

ALTER TABLE mainDb.tblProjectProteinInference
  ADD CONSTRAINT fk_tblProjectProteinInference_tblResearchers
  FOREIGN KEY (researcherID) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE CASCADE;

ALTER TABLE mainDb.tblYRCGroupMembers
  ADD CONSTRAINT fk_tblYRCGroupMembers_tblResearchers
  FOREIGN KEY (researcherID) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE CASCADE;

ALTER TABLE mainDb.tblYRCGroupMembers
  ADD CONSTRAINT fk_tblYRCGroupMembers_tblYRCGroups
  FOREIGN KEY (groupID) REFERENCES mainDb.tblYRCGroups (groupID) ON DELETE CASCADE;

ALTER TABLE mainDb.projectGroup
  ADD CONSTRAINT fk_projectGroup_tblYRCGroups
  FOREIGN KEY (groupID) REFERENCES mainDb.tblYRCGroups (groupID) ON DELETE CASCADE;

ALTER TABLE pr.projectReviewer
  ADD CONSTRAINT fk_projectReviewer_tblResearchers
  FOREIGN KEY (researcherID) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE CASCADE;

ALTER TABLE pr.collaborationRejected
  ADD CONSTRAINT fk_collaborationRejected_tblResearchers
  FOREIGN KEY (researcherID) REFERENCES mainDb.tblResearchers (researcherID) ON DELETE CASCADE;

-- --- Verify section 5. One row per ADD CONSTRAINT above, and no others. ---

SELECT constraint_schema, table_name, constraint_name, referenced_table_name
FROM information_schema.referential_constraints
WHERE constraint_schema IN ('mainDb', 'pr')
ORDER BY constraint_schema, table_name, constraint_name;


-- ================================================================================
-- NOT DONE HERE
--
-- Four keys into msData, which stays on MyISAM. instrumentUsage.instrumentID and
-- instrumentRate.instrumentID to msInstrument, tblProjectExperiment.experimentID to
-- msExperiment, and tblProjectProteinInference.piRunID to msProteinInferRun.
--
-- Any key at all on instrumentLog, the largest table in mainDb. blockId must not have one,
-- since most of its rows name a block that has since been deleted and recording that is what
-- the table is for. projectId and userId could have one. Neither does, so a project with log
-- rows and no usage rows can still be deleted and strand them -- DeleteProjectAction:90
-- checks instrumentUsage only. Projects 456, 515 and 555 are in that state today.
--
-- Removing the now-redundant child deletes from InstrumentUsageDAO,
-- ProjectPaymentMethodDAO, InvoiceDAO and Project.deleteRowsForProject. Those wait
-- until these keys are running on tephra.
-- ================================================================================
