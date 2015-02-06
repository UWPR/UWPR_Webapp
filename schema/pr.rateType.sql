LOCK TABLES `rateType` WRITE;
/*!40000 ALTER TABLE `rateType` DISABLE KEYS */;
INSERT INTO `rateType` VALUES 
(1,'UW','UW internal'),
(2,'NON_PROFIT','Non-UW; non-profit'),
(3,'COMMERCIAL','Non-UW; Commercial'),
(4,'UW_FFS','UW internal Fee for Service'),
(5,'NON_PROFIT_FFS','Non-UW; non-profit Fee for Service'),
(6,'COMMERCIAL_FFS','Non-UW; Commercial Fee for Service'),
(7,'FREE','Old UWPR Supported Project');
/*!40000 ALTER TABLE `rateType` ENABLE KEYS */;
UNLOCK TABLES;
