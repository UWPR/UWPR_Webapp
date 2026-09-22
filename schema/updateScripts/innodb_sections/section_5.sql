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
