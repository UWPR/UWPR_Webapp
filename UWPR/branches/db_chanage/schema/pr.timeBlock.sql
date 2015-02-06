-- MySQL dump 10.13  Distrib 5.1.26-rc, for apple-darwin9.0.0b5 (i686)
--
-- Host: localhost    Database: pr
-- ------------------------------------------------------
-- Server version	5.1.26-rc-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `timeBlock`
--

DROP TABLE IF EXISTS `timeBlock`;
SET @saved_cs_client     = @@character_set_client;
SET character_set_client = utf8;
CREATE TABLE `timeBlock` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `numHours` int(10) unsigned NOT NULL,
  `startTime` time DEFAULT NULL,
  `endTime` time DEFAULT NULL,
  `name` varchar(50) DEFAULT NULL,
  `createDate` datetime NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=MyISAM AUTO_INCREMENT=26 DEFAULT CHARSET=latin1;
SET character_set_client = @saved_cs_client;

--
-- Dumping data for table `timeBlock`
--

LOCK TABLES `timeBlock` WRITE;
/*!40000 ALTER TABLE `timeBlock` DISABLE KEYS */;
INSERT INTO `timeBlock` VALUES (1,1,NULL,NULL,'1hr','2011-08-09 11:00:22'),(2,2,NULL,NULL,'2hr','2011-08-09 11:00:22'),(3,3,NULL,NULL,'3hr','2011-08-09 11:00:22'),(4,4,NULL,NULL,'4hr','2011-08-09 11:00:22'),(5,5,NULL,NULL,'5hr','2011-08-09 11:00:22'),(6,6,NULL,NULL,'6hr','2011-08-09 11:00:22'),(7,7,NULL,NULL,'7hr','2011-08-09 11:00:22'),(8,8,NULL,NULL,'8hr','2011-08-09 11:00:22'),(9,9,NULL,NULL,'9hr','2011-08-09 11:00:22'),(10,10,NULL,NULL,'10hr','2011-08-09 11:00:22'),(11,11,NULL,NULL,'11hr','2011-08-09 11:00:22'),(12,12,NULL,NULL,'12hr','2011-08-09 11:00:22'),(13,13,NULL,NULL,'13hr','2011-08-09 11:00:22'),(14,14,NULL,NULL,'14hr','2011-08-09 11:00:22'),(15,15,NULL,NULL,'15hr','2011-08-09 11:00:22'),(16,16,NULL,NULL,'16hr','2011-08-09 11:00:22'),(17,17,NULL,NULL,'17hr','2011-08-09 11:00:22'),(18,18,NULL,NULL,'18hr','2011-08-09 11:00:22'),(19,19,NULL,NULL,'19hr','2011-08-09 11:00:22'),(20,20,NULL,NULL,'20hr','2011-08-09 11:00:22'),(21,21,NULL,NULL,'21hr','2011-08-09 11:00:22'),(22,22,NULL,NULL,'22hr','2011-08-09 11:00:22'),(23,23,NULL,NULL,'23hr','2011-08-09 11:00:22'),(24,24,NULL,NULL,'24hr','2011-08-09 11:00:22'),(25,0,NULL,NULL,'Block for old usage','2011-08-09 11:15:25');
/*!40000 ALTER TABLE `timeBlock` ENABLE KEYS */;
UNLOCK TABLES;
/*!50003 SET @saved_cs_client      = @@character_set_client */ ;
/*!50003 SET @saved_cs_results     = @@character_set_results */ ;
/*!50003 SET @saved_col_connection = @@collation_connection */ ;
/*!50003 SET character_set_client  = latin1 */ ;
/*!50003 SET character_set_results = latin1 */ ;
/*!50003 SET collation_connection  = latin1_swedish_ci */ ;
/*!50003 SET @saved_sql_mode       = @@sql_mode */ ;
/*!50003 SET sql_mode              = '' */ ;
DELIMITER ;;
/*!50003 CREATE*/ /*!50017 DEFINER=`root`@`localhost`*/ /*!50003 TRIGGER timeBlock_bdelete BEFORE DELETE ON timeBlock
 FOR EACH ROW
 BEGIN
   DELETE FROM instrumentRate WHERE blockID = OLD.id;
 END */;;
DELIMITER ;
/*!50003 SET sql_mode              = @saved_sql_mode */ ;
/*!50003 SET character_set_client  = @saved_cs_client */ ;
/*!50003 SET character_set_results = @saved_cs_results */ ;
/*!50003 SET collation_connection  = @saved_col_connection */ ;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2011-08-09 21:04:04
