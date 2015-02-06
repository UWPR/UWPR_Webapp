CREATE TABLE projectResearcher
(
  projectID INT UNSIGNED NOT NULL,
  researcherID INT UNSIGNED NOT NULL,
  PRIMARY KEY (projectID, researcherID)
);

DELIMITER |
CREATE TRIGGER project_bdelete BEFORE DELETE ON tblProjects
FOR EACH ROW
BEGIN
  DELETE FROM projectResearcher WHERE projectID=OLD.projectID;
END;
|
DELIMITER ;

INSERT INTO projectResearcher (SELECT projectID, projectResearcherB from tblProjects WHERE projectResearcherB IS NOT NULL);
INSERT INTO projectResearcher (SELECT projectID, projectResearcherC from tblProjects WHERE projectResearcherC IS NOT NULL);
INSERT INTO projectResearcher (SELECT projectID, projectResearcherD from tblProjects WHERE projectResearcherD IS NOT NULL);
INSERT INTO projectResearcher (SELECT projectID, projectResearcherE from tblProjects WHERE projectResearcherE IS NOT NULL);