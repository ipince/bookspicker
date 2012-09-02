-- MySQL dump 10.13  Distrib 5.5.27, for osx10.6 (i386)
--
-- Host: localhost    Database: bp_qa
-- ------------------------------------------------------
-- Server version	5.5.27

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
-- Table structure for table `books`
--

DROP TABLE IF EXISTS `books`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `books` (
  `book_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(255) NOT NULL,
  `authorList` mediumblob,
  `isbn` varchar(255) DEFAULT NULL,
  `ean` varchar(255) DEFAULT NULL,
  `listPrice` int(11) NOT NULL,
  `imageUrl` varchar(255) DEFAULT NULL,
  `edition` int(11) DEFAULT NULL,
  `publisher` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`book_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `books`
--

LOCK TABLES `books` WRITE;
/*!40000 ALTER TABLE `books` DISABLE KEYS */;
/*!40000 ALTER TABLE `books` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classbooks`
--

DROP TABLE IF EXISTS `classbooks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `classbooks` (
  `classbook_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `necessity` tinyblob NOT NULL,
  `notes` varchar(255) NOT NULL,
  `source` tinyblob,
  `book_id` bigint(20) NOT NULL,
  `class_id` bigint(20) NOT NULL,
  PRIMARY KEY (`classbook_id`),
  KEY `FKEEDAA43294590380` (`class_id`),
  KEY `FKEEDAA432A30BF680` (`book_id`),
  CONSTRAINT `FKEEDAA432A30BF680` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`),
  CONSTRAINT `FKEEDAA43294590380` FOREIGN KEY (`class_id`) REFERENCES `classes` (`class_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classbooks`
--

LOCK TABLES `classbooks` WRITE;
/*!40000 ALTER TABLE `classbooks` DISABLE KEYS */;
/*!40000 ALTER TABLE `classbooks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `classes`
--

DROP TABLE IF EXISTS `classes`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `classes` (
  `class_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `school` tinyblob NOT NULL,
  `course` varchar(255) NOT NULL,
  `clas` varchar(255) NOT NULL,
  `section` varchar(255) NOT NULL,
  `code` varchar(255) NOT NULL,
  `title` varchar(255) DEFAULT NULL,
  `term` tinyblob NOT NULL,
  `jointSubjects` varchar(255) DEFAULT NULL,
  `lastActivityDate` datetime DEFAULT NULL,
  `warehouseLoadDate` datetime DEFAULT NULL,
  PRIMARY KEY (`class_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `classes`
--

LOCK TABLES `classes` WRITE;
/*!40000 ALTER TABLE `classes` DISABLE KEYS */;
/*!40000 ALTER TABLE `classes` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `coop_offers`
--

DROP TABLE IF EXISTS `coop_offers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `coop_offers` (
  `coop_offer_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `isbn` varchar(255) NOT NULL,
  `price` int(11) NOT NULL,
  `bookCondition` varchar(255) NOT NULL,
  `url` varchar(300) NOT NULL,
  PRIMARY KEY (`coop_offer_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `coop_offers`
--

LOCK TABLES `coop_offers` WRITE;
/*!40000 ALTER TABLE `coop_offers` DISABLE KEYS */;
/*!40000 ALTER TABLE `coop_offers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `friendships`
--

DROP TABLE IF EXISTS `friendships`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `friendships` (
  `friendship_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `prim` varchar(255) NOT NULL,
  `sec` varchar(255) NOT NULL,
  PRIMARY KEY (`friendship_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `friendships`
--

LOCK TABLES `friendships` WRITE;
/*!40000 ALTER TABLE `friendships` DISABLE KEYS */;
/*!40000 ALTER TABLE `friendships` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `local_offers`
--

DROP TABLE IF EXISTS `local_offers`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `local_offers` (
  `offer_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `owner_id` bigint(20) NOT NULL,
  `book_id` bigint(20) NOT NULL,
  `school` tinyblob NOT NULL,
  `classCode` varchar(255) DEFAULT NULL,
  `bookCondition` tinyblob NOT NULL,
  `location` tinyblob,
  `fixedPrice` int(11) DEFAULT NULL,
  `autoPricing` bit(1) NOT NULL,
  `strategy` tinyblob,
  `lowerBoundPrice` int(11) DEFAULT NULL,
  `comments` varchar(255) DEFAULT NULL,
  `creationDate` datetime NOT NULL,
  `active` bit(1) NOT NULL,
  `lastPostingDate` datetime NOT NULL,
  `sold` bit(1) NOT NULL,
  `soldOnce` bit(1) NOT NULL,
  `buyerId` bigint(20) DEFAULT NULL,
  `buyerEmail` varchar(255) DEFAULT NULL,
  `sellingPrice` int(11) DEFAULT NULL,
  `timeSold` datetime DEFAULT NULL,
  `timeOnMarketFixed` bigint(20) NOT NULL,
  `timeOnMarketAggressive` bigint(20) NOT NULL,
  `timeOnMarketConservative` bigint(20) NOT NULL,
  `numTimesShown` int(11) NOT NULL,
  `numTimesNotShown` int(11) NOT NULL,
  `deleted` bit(1) NOT NULL,
  PRIMARY KEY (`offer_id`),
  KEY `FKAC1630EBA30BF680` (`book_id`),
  KEY `FKAC1630EB25B6358` (`owner_id`),
  CONSTRAINT `FKAC1630EB25B6358` FOREIGN KEY (`owner_id`) REFERENCES `users` (`user_id`),
  CONSTRAINT `FKAC1630EBA30BF680` FOREIGN KEY (`book_id`) REFERENCES `books` (`book_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `local_offers`
--

LOCK TABLES `local_offers` WRITE;
/*!40000 ALTER TABLE `local_offers` DISABLE KEYS */;
/*!40000 ALTER TABLE `local_offers` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `market_data`
--

DROP TABLE IF EXISTS `market_data`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `market_data` (
  `market_data_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `buyClickStatId` bigint(20) NOT NULL,
  `store` varchar(255) NOT NULL,
  `price` int(11) NOT NULL,
  `bookCondition` varchar(255) DEFAULT NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY (`market_data_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `market_data`
--

LOCK TABLES `market_data` WRITE;
/*!40000 ALTER TABLE `market_data` DISABLE KEYS */;
/*!40000 ALTER TABLE `market_data` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `transactions`
--

DROP TABLE IF EXISTS `transactions`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `transactions` (
  `transaction_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `buyerId` bigint(20) NOT NULL,
  `isbn` varchar(255) NOT NULL,
  `time` datetime NOT NULL,
  PRIMARY KEY (`transaction_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `transactions`
--

LOCK TABLES `transactions` WRITE;
/*!40000 ALTER TABLE `transactions` DISABLE KEYS */;
/*!40000 ALTER TABLE `transactions` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `usage_stats`
--

DROP TABLE IF EXISTS `usage_stats`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `usage_stats` (
  `stat_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `type` varchar(255) NOT NULL,
  `search` varchar(255) DEFAULT NULL,
  `isbn` varchar(255) DEFAULT NULL,
  `store` varchar(255) DEFAULT NULL,
  `price` int(11) DEFAULT NULL,
  `bookCondition` varchar(255) DEFAULT NULL,
  `localId` varchar(255) DEFAULT NULL,
  `uid` varchar(255) DEFAULT NULL,
  `ip` varchar(255) NOT NULL,
  `date` datetime NOT NULL,
  PRIMARY KEY (`stat_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `usage_stats`
--

LOCK TABLES `usage_stats` WRITE;
/*!40000 ALTER TABLE `usage_stats` DISABLE KEYS */;
/*!40000 ALTER TABLE `usage_stats` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL AUTO_INCREMENT,
  `fib` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  `fbEmail` varchar(255) NOT NULL,
  `mitEmail` varchar(255) DEFAULT NULL,
  `location` tinyblob,
  PRIMARY KEY (`user_id`)
) ENGINE=MyISAM DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
/*!40000 ALTER TABLE `users` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2012-08-26 17:47:41
