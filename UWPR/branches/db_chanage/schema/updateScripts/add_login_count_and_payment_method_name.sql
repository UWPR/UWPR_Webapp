ALTER TABLE mainDb.paymentMethod ADD COLUMN paymentMethodName VARCHAR(500);
ALTER TABLE mainDb.tblUsers ADD COLUMN loginCount INT UNSIGNED NOT NULL DEFAULT 0;