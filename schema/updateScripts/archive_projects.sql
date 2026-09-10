# --------------------------------------------------------------------------------
# ARCHIVING PROJECTS
#
# Lets researchers move completed projects out of the main list on the home page.
#  - on tblProjects, not tblBilledProject, so it covers both project types
#  - display only, nothing reads it except the project lists
#  - defaults to 0, so nothing changes on deploy
# --------------------------------------------------------------------------------

ALTER TABLE mainDb.tblProjects
  ADD COLUMN archived TINYINT(1) NOT NULL DEFAULT 0;
