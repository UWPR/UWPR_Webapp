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

-- AUTO_INCREMENT is the value to read, not MAX. Widening a column does not touch the
-- rows, so MAX cannot move whatever the ALTER does, while the AUTO_INCREMENT counter
-- can be reset by it. Each must still sit above its MAX, or the next insert collides
-- with a row that is already there. Capture these before section 2 runs, or there is
-- nothing to compare them against.

SELECT t.table_name, t.auto_increment,
       CASE t.table_name
         WHEN 'tblProjects'    THEN (SELECT MAX(projectID)    FROM mainDb.tblProjects)
         WHEN 'tblResearchers' THEN (SELECT MAX(researcherID) FROM mainDb.tblResearchers)
         WHEN 'tblYRCGroups'   THEN (SELECT MAX(groupID)      FROM mainDb.tblYRCGroups)
       END AS max_id
FROM information_schema.tables t
WHERE t.table_schema = 'mainDb'
  AND t.table_name IN ('tblProjects', 'tblResearchers', 'tblYRCGroups')
ORDER BY t.table_name;


-- ================================================================================
