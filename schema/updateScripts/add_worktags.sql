ALTER TABLE mainDb.paymentMethod ADD COLUMN worktag VARCHAR(10);
-- ALTER TABLE mainDb.paymentMethod ADD COLUMN worktagDescr TEXT; -- Will repurpose the paymentMethodName field for this
ALTER TABLE mainDb.paymentMethod ADD COLUMN resourceWorktag VARCHAR(10);
ALTER TABLE mainDb.paymentMethod ADD COLUMN resourceWorktagDescr TEXT;
ALTER TABLE mainDb.paymentMethod ADD COLUMN assigneeWorktag VARCHAR(10);
ALTER TABLE mainDb.paymentMethod ADD COLUMN assigneeWorktagDescr TEXT;
ALTER TABLE mainDb.paymentMethod ADD COLUMN activityWorktag VARCHAR(10);
ALTER TABLE mainDb.paymentMethod ADD COLUMN activityWorktagDescr TEXT;