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
