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

-- instrumentRate carries only PRIMARY (id) and instrumentID, so both keys in 5.2 need one.

ALTER TABLE mainDb.instrumentRate       ADD INDEX blockID (blockID);
ALTER TABLE mainDb.instrumentRate       ADD INDEX rateTypeID (rateTypeID);

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

-- --- Verify section 4. Must read 18, one per ADD INDEX above. A short count means a
-- --- statement failed, and section 5 would then let InnoDB create that index under a name
-- --- of its own choosing, which is what this section exists to prevent. ---
-- ---
-- --- It counts the indexes that exist, not the ones this section added, so it means what
-- --- it says only from a start of 0. Read it before running the section as well as after.

SELECT COUNT(DISTINCT table_schema, table_name, index_name) AS indexes_added
FROM information_schema.statistics
WHERE (table_schema = 'mainDb' AND
       ((table_name = 'instrumentUsage'            AND index_name IN ('instrumentRateID', 'enteredBy', 'updatedBy', 'instrumentOperatorId')) OR
        (table_name = 'instrumentRate'             AND index_name IN ('blockID', 'rateTypeID')) OR
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
