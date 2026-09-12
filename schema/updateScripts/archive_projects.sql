# --------------------------------------------------------------------------------
# ARCHIVING PROJECTS
#
# Lets researchers move completed projects out of the main list on the home page.
#  - on tblProjects, not tblBilledProject, so it covers both project types
#  - gates writes -- an archived project takes no new payment methods and its
#    instrument time cannot be changed
#  - filtered out of the project lists and the scheduler selectors
#  - billing and invoicing ignore it
#  - defaults to 0, so nothing changes on deploy
# --------------------------------------------------------------------------------

ALTER TABLE mainDb.tblProjects
  ADD COLUMN archived TINYINT(1) NOT NULL DEFAULT 0;
