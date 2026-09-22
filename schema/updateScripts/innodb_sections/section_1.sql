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
--
-- The first two deletes below remove nothing on this data. The third removes the two blocks.

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
