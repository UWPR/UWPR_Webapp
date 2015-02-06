alter table instrumentUsage modify column dateEntered DATETIME NOT NULL;

alter table instrumentUsage add column instrumentRateID int(10) unsigned NOT NULL after instrumentID;
update instrumentUsage as iu set iu.instrumentRateID=(select id from instrumentRate as ir where ir.instrumentID = iu.instrumentID and ir.rateTypeID=7 AND ir.blockID=25);

# TODO -- make this change manually
alter table tblProjects modify column projectType enum('B','C');

# CREATE a payment method for UWPR supported projects
INSERT INTO paymentMethod (UWBudgetNumber, contactNameFirst, contactLastName, contactEmail, contactPhone, organization, addressLine1, city, state, zip, country, dateCreated) 
VALUES ("07-5229", "UWPR", "UWPR", "priska@u.washington.edu", "206-616-0659", "University of Washington", "815 Mercer St", "Seattle", "WA", "98109-4714", "us", CURRENT_DATE);

# Updated 01/16/2012
INSERT INTO paymentMethod (UWBudgetNumber, contactNameFirst, contactLastName, contactEmail, contactPhone, organization, addressLine1, city, state, zip, country, dateCreated) 
VALUES ("14-5220", "UWPR", "UWPR", "priska@u.washington.edu", "206-616-0659", "University of Washington", "815 Mercer St", "Seattle", "WA", "98109-4714", "us", CURRENT_DATE);

alter table instrumentUsage add column updatedBy INT unsigned after enteredBy;

# 01.18.12
# These changes were made to associate all maintenance project (ID 42) blocks with the new UW budget number.
select count(*) from instrumentUsage as iu, instrumentUsagePayment as iup where iu.id = iup.instrumentUsageID and iu.projectID=42 and iup.paymentMethodID=1;
update instrumentUsage as iu, instrumentUsagePayment as iup set iup.paymentMethodID=7 where iu.id = iup.instrumentUsageID and iu.projectID=42 and iup.paymentMethodID=1;

