ALTER TABLE invoiceInstrumentUsage ADD CONSTRAINT UQ_invoiceId_instrumentUsageId UNIQUE(invoiceID, instrumentUsageID);

