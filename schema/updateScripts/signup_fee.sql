ALTER TABLE mainDb.instrumentUsage Engine=InnoDB;
ALTER TABLE mainDb.instrumentUsagePayment Engine=InnoDB;
ALTER TABLE mainDb.projectPaymentMethod Engine=InnoDb;

CREATE TABLE mainDb.instrumentSignup (
  id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  projectId INT(10) unsigned NOT NULL,
  instrumentID INT(10) unsigned NOT NULL,
  startDate datetime DEFAULT NULL,
  endDate datetime DEFAULT NULL,
  created DATETIME DEFAULT CURRENT_TIMESTAMP,
  createdBy INT(10) unsigned NOT NULL,
  PRIMARY KEY (id),
  KEY projectID (projectID),
  KEY instrumentID (instrumentID),
  KEY startDate (startDate),
  KEY endDate (endDate),
  KEY created (created)
)ENGINE = InnoDB;

CREATE TABLE mainDb.instrumentSignupBlock (
  id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  instrumentSignupId INT(10) unsigned NOT NULL,
  instrumentRateId INT UNSIGNED NOT NULL,
  startDate datetime DEFAULT NULL,
  endDate datetime DEFAULT NULL,
  PRIMARY KEY (id),
  KEY instrumentSignupId (instrumentSignupId),
  KEY instrumentRateId (instrumentRateId),
  KEY startDate (startDate),
  KEY endDate (endDate)
)ENGINE = InnoDB;

CREATE TABLE mainDb.instrumentSignupPayment (
  instrumentSignupId INT(10) unsigned NOT NULL,
  paymentMethodId INT(10) unsigned NOT NULL,
  percentPayment DECIMAL(5,2) NOT NULL,
  PRIMARY KEY (instrumentSignupId, paymentMethodId)
)ENGINE = InnoDB;

CREATE TABLE mainDb.invoiceSignupBlock
(
  id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  invoiceId INT(10) UNSIGNED NOT NULL,
  instrumentSignupBlockId INT(10) UNSIGNED NOT NULL,
  PRIMARY KEY (id),
  KEY invoiceId (invoiceId),
  KEY instrumentSignupId (instrumentSignupBlockId)
)ENGINE = InnoDB;

CREATE TABLE mainDb.instrumentSignupLog
(
  id INT(10) UNSIGNED NOT NULL AUTO_INCREMENT,
  projectId INT(10) unsigned NOT NULL,
  instrumentID INT(10) unsigned NOT NULL,
  startDate datetime DEFAULT NULL,
  endDate datetime DEFAULT NULL,
  created datetime DEFAULT CURRENT_TIMESTAMP,
  createdBy INT(10) unsigned NOT NULL,
  PRIMARY KEY (id),
  KEY projectId (projectId),
  KEY instrumentID (instrumentID),
  KEY startDate (startDate),
  KEY endDate (endDate),
  KEY created (created)
)ENGINE = InnoDB;


DELIMITER |
CREATE TRIGGER instrumentSignup_bdelete BEFORE DELETE ON instrumentSignup
FOR EACH ROW
  BEGIN
    DELETE FROM instrumentSignupBlock WHERE instrumentSignupId = OLD.id;
    DELETE FROM instrumentSignupPayment WHERE instrumentSignupId = OLD.id;
  END;
|
DELIMITER ;

DELIMITER |
CREATE TRIGGER instrumentSignupBlock_bdelete BEFORE DELETE ON instrumentSignupBlock
FOR EACH ROW
  BEGIN
    DELETE FROM invoiceSignupBlock WHERE instrumentSignupBlockId = OLD.id;
  END;
|
DELIMITER ;
