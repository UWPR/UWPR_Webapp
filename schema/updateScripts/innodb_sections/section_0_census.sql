-- Orphan census. Read-only -- this script deletes nothing.
--
-- The same query section_1.sql runs after its deletes, lifted out so it
-- can be run before them. Section 1 has no before-count of its own, and the mysql client
-- prints no row count for a DELETE unless it is given -vvv, so without this the run leaves
-- no record of what was removed.
--
-- Run it before section 1 and keep the output. Every count it reports is a row section 1
-- will delete. Run it again after, where every count must read 0.
--
--   mysql -u <admin> -p mainDb -vvv < section_0_census.sql | tee section_0_census-before.log

USE mainDb;

DROP TEMPORARY TABLE IF EXISTS census;

CREATE TEMPORARY TABLE census AS
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
  LEFT JOIN mainDb.tblResearchers p ON p.researcherID = c.researcherID WHERE p.researcherID IS NULL
;

SELECT * FROM census;

-- relationships_checked says the query ran whole. It is 37 every time, so a smaller number means
-- the script was truncated rather than that the database is cleaner than expected.
--
-- Every non-zero row above is a row section 1 deletes, so the total is what the run will remove.
-- The updatedBy check is left out of it. Section 1.9 deletes nothing, it only verifies that
-- recover_updatedBy.sql has already run.

SELECT COUNT(*) AS relationships_checked,
       SUM(CASE WHEN relationship <> 'instrumentUsage.updatedBy not yet recovered'
                THEN orphans ELSE 0 END) AS rows_section_1_will_delete
  FROM census;

DROP TEMPORARY TABLE census;
