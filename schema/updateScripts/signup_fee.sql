ALTER TABLE mainDb.instrumentUsage Engine=InnoDB;
ALTER TABLE mainDb.instrumentUsagePayment Engine=InnoDB;
ALTER TABLE mainDb.projectPaymentMethod Engine=InnoDb;
ALTER TABLE mainDb.invoice Engine=InnoDB;
ALTER TABLE mainDb.invoiceInstrumentUsage Engine=InnoDb;

ALTER TABLE instrumentUsage ADD COLUMN deleted BOOLEAN DEFAULT FALSE;

ALTER TABLE maindb.instrumentusage MODIFY COLUMN lastChanged timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP

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


