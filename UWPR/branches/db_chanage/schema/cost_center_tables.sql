# --------------------------------------------------------------------------------
# COST CENTER TABLES
# --------------------------------------------------------------------------------
create table timeBlock 
(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, 
	numHours INT UNSIGNED NOT NULL,
	startTime time, 
	endTime time, 
	name varchar(50), 
	createDate DATETIME NOT NULL
);

create table rateType
(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	name varchar(50) NOT NULL,
	description TEXT
);

create table instrumentRate
(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY, 
	instrumentID INT UNSIGNED NOT NULL,
	blockID INT UNSIGNED NOT NULL,
	rateTypeID INT UNSIGNED NOT NULL,
	fee DECIMAL(7,2) NOT NULL,
	createDate DATETIME NOT NULL, 
	lastUpdate TIMESTAMP NOT NULL,
	isCurrent TINYINT(1)
);
ALTER TABLE instrumentRate ADD INDEX(instrumentID); 


# ---- TRIGGERS ------------------------------------------------------------------
DELIMITER |
CREATE TRIGGER timeBlock_bdelete BEFORE DELETE ON timeBlock
 FOR EACH ROW
 BEGIN
   DELETE FROM instrumentRate WHERE blockID = OLD.id;
 END;
|
DELIMITER ;

DELIMITER |
CREATE TRIGGER rateType_bdelete BEFORE DELETE ON rateType
 FOR EACH ROW
 BEGIN
   DELETE FROM instrumentRate WHERE rateTypeID = OLD.id;
 END;
|
DELIMITER ;

DELIMITER |
CREATE TRIGGER instruments_bdelete BEFORE DELETE ON instruments
 FOR EACH ROW
 BEGIN
   DELETE FROM instrumentRate WHERE instrumentID = OLD.id;
 END;
|
DELIMITER ;

DELIMITER |
CREATE TRIGGER instrumentRate_bdelete BEFORE DELETE ON instrumentRate
 FOR EACH ROW
 BEGIN
   DELETE FROM instrumentUsage WHERE instrumentRateID = OLD.id;
 END;
|
DELIMITER ;

# --------------------------------------------------------------------------------
# INVOICE TABLES
# --------------------------------------------------------------------------------
create table invoice
(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	createDate TIMESTAMP NOT NULL,
	billStartDate DATETIME NOT NULL,
	billEndDate DATETIME NOT NULL,
	createdBy INT UNSIGNED NOT NULL
);

create table invoiceInstrumentUsage
(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	invoiceID INT UNSIGNED NOT NULL,
	instrumentUsageID INT UNSIGNED NOT NULL
);
ALTER TABLE invoiceInstrumentUsage ADD INDEX (instrumentUsageID);
ALTER TABLE invoiceInstrumentUsage ADD INDEX (invoiceID);


# ---- TRIGGERS ------------------------------------------------------------------
DELIMITER |
CREATE TRIGGER invoice_bdelete BEFORE DELETE ON invoice
 FOR EACH ROW
 BEGIN
   DELETE FROM invoiceInstrumentUsage WHERE invoiceID = OLD.id;
 END;
|
DELIMITER ;


# --------------------------------------------------------------------------------
# BILLED PROJECT TABLES
# --------------------------------------------------------------------------------
create table tblBilledProject 
(
	projectID INT UNSIGNED NOT NULL,
	collGroups set('Core','Goodlett','MacCoss','von_Haller','Heinecke','Informatics','Bruce','Villen','Hoofnagle','Wolf-Yadlin', 'Ong'),
	affiliation VARCHAR(15) NOT NULL,
	blocked TINYINT(1) NOT NULL DEFAULT 0
);
alter table tblBilledProject add primary key(projectID);


create table paymentMethod
(
	id INT UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY,
	UWBudgetNumber VARCHAR(45),
	PONumber VARCHAR(45),
	contactNameFirst VARCHAR(30) NOT NULL,
	contactLastName VARCHAR(30) NOT NULL,
	contactEmail VARCHAR(50) NOT NULL,
	contactPhone VARCHAR(20) NOT NULL,
	organization VARCHAR(60) NOT NULL,
	addressLine1 VARCHAR(255) NOT NULL,
	addressLine2 VARCHAR(255),
	city VARCHAR(50) NOT NULL,
	state CHAR(2) NOT NULL,
	zip VARCHAR(11) NOT NULL,
	country VARCHAR(40) NOT NULL,
	dateCreated DATETIME NOT NULL,
	lastUpdated timestamp NOT NULL,
	createdBy INT UNSIGNED NOT NULL,
	isCurrent TINYINT(1) NOT NULL,
	federalFunding TINYINT(1) NOT NULL DEFAULT 0
);
-- Added 10/04/13
ALTER TABLE paymentMethod ADD COLUMN poAmount DECIMAL(11,2);

create table projectPaymentMethod
(
	projectID INT UNSIGNED NOT NULL,
	paymentMethodID INT UNSIGNED NOT NULL
);
ALTER TABLE projectPaymentMethod ADD PRIMARY KEY (projectID, paymentMethodID);

 
create table instrumentUsagePayment 
(
	instrumentUsageID INT UNSIGNED NOT NULL,
	paymentMethodID INT UNSIGNED NOT NULL,
	percentPayment DECIMAL(5,2) NOT NULL
);
ALTER TABLE instrumentUsagePayment ADD PRIMARY KEY (instrumentUsageID, paymentMethodID);

# ---- TRIGGERS ------------------------------------------------------------------

DELIMITER |
CREATE TRIGGER paymentMethod_bdelete BEFORE DELETE ON paymentMethod
 FOR EACH ROW
 BEGIN
   DELETE FROM projectPaymentMethod WHERE paymentMethodID = OLD.id;
   DELETE FROM instrumentUsagePayment WHERE paymentMethodID = OLD.id;
 END;
|
DELIMITER ;

DELIMITER |
CREATE TRIGGER instrumentUsage_bdelete BEFORE DELETE ON instrumentUsage
 FOR EACH ROW
 BEGIN
   DELETE FROM instrumentUsagePayment WHERE instrumentUsageID = OLD.id;
   DELETE FROM invoiceInstrumentUsage WHERE instrumentUsageID = OLD.id;
 END;
|
DELIMITER ;

