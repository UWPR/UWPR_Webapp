INSERT INTO maindb.config_msdapl_webapp (config_key, config_value) VALUES ('from.email.address', 'uwpr-webapp@uw.edu');
INSERT INTO maindb.config_msdapl_webapp (config_key, config_value) VALUES ('from.email.password', '<password>');
INSERT INTO maindb.config_msdapl_webapp (config_key, config_value) VALUES ('mail.smtp.port', '587');
UPDATE maindb.config_msdapl_webapp SET config_value='smtp.uw.edu' WHERE config_key ='mail.smtp.host';

