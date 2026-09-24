-- The update is a deliberate no-op. It satisfies ON DUPLICATE KEY so a re-run does not fail with
-- ERROR 1062, and leaves a value an administrator has already tuned alone.
INSERT INTO mainDb.config_msdapl_webapp (config_key, config_value) VALUES ('mail.smtp.timeout', '15000')
  ON DUPLICATE KEY UPDATE config_value=config_value;
