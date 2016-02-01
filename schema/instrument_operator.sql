ALTER TABLE instrumentUsage add column instrumentOperatorId int AFTER instrumentID;
INSERT INTO tblYRCGroups (groupName, groupDesc) VALUES ('Instrument Operators', 'Verified instrument operators');