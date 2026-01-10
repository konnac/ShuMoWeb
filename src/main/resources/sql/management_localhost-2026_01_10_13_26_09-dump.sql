-- MySQL dump 10.13  Distrib 8.0.44, for Win64 (x86_64)
--
-- Host: 127.0.0.1    Database: management
-- ------------------------------------------------------
-- Server version	8.0.44

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `departments`
--

DROP TABLE IF EXISTS `departments`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `departments` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `name` varchar(100) NOT NULL COMMENT '部门名称',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_department_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='部门表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `departments`
--

LOCK TABLES `departments` WRITE;
/*!40000 ALTER TABLE `departments` DISABLE KEYS */;
INSERT INTO `departments` VALUES (1,'开发部');
/*!40000 ALTER TABLE `departments` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `documents`
--

DROP TABLE IF EXISTS `documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `documents` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `project_id` int NOT NULL COMMENT '项目ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `file_url` varchar(500) NOT NULL COMMENT '文件URL',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(100) DEFAULT NULL COMMENT '文件类型(MIME类型)',
  `category` varchar(50) NOT NULL DEFAULT 'OTHER' COMMENT '文档分类',
  `description` text COMMENT '文档描述',
  `uploader_id` int NOT NULL COMMENT '上传者ID',
  `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`),
  KEY `idx_uploader_id` (`uploader_id`),
  KEY `idx_category` (`category`),
  KEY `idx_upload_time` (`upload_time`),
  CONSTRAINT `documents_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `documents_ibfk_2` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目文档表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `documents`
--

LOCK TABLES `documents` WRITE;
/*!40000 ALTER TABLE `documents` DISABLE KEYS */;
INSERT INTO `documents` VALUES (1,1,'Java开发手册(黄山版).pdf','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/1/TECHNICAL/63a5c4d2692342ca8ab8a16392208b66.pdf',1496687,'application/pdf','TECHNICAL','',1,'2026-01-08 13:39:10','2026-01-09 17:43:58'),(2,1,'2_个性化广告_完成证明_徐静_20250106.txt','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/1/PROVEMENT/257e4c1c970742c5861522ab6c4bf9eb.txt',9,'text/plain','PROVEMENT',NULL,12,'2026-01-08 19:14:01','2026-01-08 19:14:01');
/*!40000 ALTER TABLE `documents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `notifications`
--

DROP TABLE IF EXISTS `notifications`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `notifications` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int NOT NULL COMMENT '接收用户ID',
  `title` varchar(255) NOT NULL COMMENT '通知标题',
  `content` text COMMENT '通知内容',
  `type` varchar(50) NOT NULL COMMENT '通知类型',
  `related_type` varchar(50) DEFAULT NULL COMMENT '关联类型（项目、任务等）',
  `related_id` int DEFAULT NULL COMMENT '关联ID',
  `active` tinyint(1) DEFAULT NULL,
  `is_read` tinyint(1) DEFAULT '0' COMMENT '是否已读',
  `created_at` datetime DEFAULT NULL COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_created_at` (`created_at`),
  KEY `idx_is_read` (`is_read`),
  CONSTRAINT `notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=88 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='系统通知表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `notifications`
--

LOCK TABLES `notifications` WRITE;
/*!40000 ALTER TABLE `notifications` DISABLE KEYS */;
INSERT INTO `notifications` VALUES (1,9,'任务已分配','任务【1】已分配给【杨帆】','TASK_ASSIGNED','task',1,1,0,'2026-01-07 23:14:12'),(2,10,'任务已分配','任务【1】已分配给【黄娟】','TASK_ASSIGNED','task',1,1,0,'2026-01-07 23:14:23'),(3,11,'任务已分配','任务【1】已分配给【林峰】','TASK_ASSIGNED','task',1,1,0,'2026-01-07 23:14:32'),(4,12,'任务已分配','任务【1】已分配给【徐静】','TASK_ASSIGNED','task',1,0,1,'2026-01-07 23:14:38'),(5,12,'任务已分配','任务【摇一摇广告功能的开发】已分配给【徐静】','TASK_ASSIGNED','task',1,1,1,'2026-01-07 23:16:34'),(6,9,'任务已分配','任务【摇一摇广告功能的开发】已分配给【杨帆】','TASK_ASSIGNED','task',1,1,0,'2026-01-08 12:34:31'),(7,1,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(8,2,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(9,3,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(10,4,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(11,5,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(12,6,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(13,7,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(14,8,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(15,9,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(16,10,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(17,11,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(18,12,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,1,'2026-01-08 12:58:30'),(19,13,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(20,14,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(21,15,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(22,16,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(23,17,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(24,18,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(25,19,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(26,20,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(27,21,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(28,22,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(29,23,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(30,24,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(31,25,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(32,26,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(33,27,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(34,28,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(35,29,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(36,30,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(37,31,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(38,32,'2026年春节放假安排通知','根据国家法定节假日安排，结合公司实际情况，现将2026年春节放假安排通知如下：\n\n一、放假时间\n2026年2月15日（星期日）至2月21日（星期六）放假调休，共7天。\n2月22日（星期日）正常上班。\n\n二、注意事项\n1. 请各部门在放假前做好工作安排，确保节日期间重要工作的正常运转。\n2. 放假期间请关闭办公室电源、门窗，做好防火防盗工作。\n3. 注意出行安全，遵守交通规则。\n4. 保持手机畅通，以便紧急联系。\n\n三、值班安排\n节日期间安排值班人员，具体值班表请关注后续通知。\n\n预祝大家新春快乐，阖家幸福！\n','ANNOUNCEMENT','COMMON',-1,1,0,'2026-01-08 12:58:30'),(39,10,'任务已分配','任务【摇一摇广告功能的开发】已分配给【黄娟】','TASK_ASSIGNED','task',1,1,0,'2026-01-08 14:43:00'),(40,4,'关于报表的通知','各项目成员要记得填写日报/周报','ANNOUNCEMENT','project',1,1,0,'2026-01-08 15:04:19'),(41,9,'关于报表的通知','各项目成员要记得填写日报/周报','ANNOUNCEMENT','project',1,1,0,'2026-01-08 15:04:19'),(42,10,'关于报表的通知','各项目成员要记得填写日报/周报','ANNOUNCEMENT','project',1,1,0,'2026-01-08 15:04:19'),(43,11,'关于报表的通知','各项目成员要记得填写日报/周报','ANNOUNCEMENT','project',1,1,0,'2026-01-08 15:04:19'),(44,12,'关于报表的通知','各项目成员要记得填写日报/周报','ANNOUNCEMENT','project',1,1,1,'2026-01-08 15:04:19'),(45,12,'任务已分配','任务【个性化的广告推荐】已分配给【徐静】','TASK_ASSIGNED','task',2,1,1,'2026-01-08 15:14:10'),(46,12,'任务已分配','任务【个性化广告功能开发】已分配给【徐静】','TASK_ASSIGNED','task',2,1,1,'2026-01-08 15:18:16'),(47,10,'任务已分配','任务【个性化广告功能开发】已分配给【黄娟】','TASK_ASSIGNED','task',2,1,0,'2026-01-08 15:18:36'),(48,9,'需要开发文档','接口需要开发文档','ANNOUNCEMENT','task',1,1,0,'2026-01-08 15:32:36'),(49,10,'需要开发文档','接口需要开发文档','ANNOUNCEMENT','task',1,1,0,'2026-01-08 15:32:36'),(50,11,'需要开发文档','接口需要开发文档','ANNOUNCEMENT','task',1,1,0,'2026-01-08 15:32:36'),(51,12,'需要开发文档','接口需要开发文档','ANNOUNCEMENT','task',1,1,1,'2026-01-08 15:32:36'),(52,17,'任务已分配','任务【个性化广告功能开发】已分配给【马超】','TASK_ASSIGNED','task',2,1,0,'2026-01-08 18:44:35'),(53,18,'任务已分配','任务【个性化广告功能开发】已分配给【梁静】','TASK_ASSIGNED','task',2,1,0,'2026-01-08 18:44:39'),(54,17,'任务已分配','任务【高精度定位的适配和使用】已分配给【马超】','TASK_ASSIGNED','task',3,1,0,'2026-01-08 18:45:28'),(55,18,'任务已分配','任务【高精度定位的适配和使用】已分配给【梁静】','TASK_ASSIGNED','task',3,1,0,'2026-01-08 18:45:38'),(56,1,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,0,0,'2026-01-09 20:45:34'),(57,2,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(58,3,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(59,4,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(60,5,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(61,6,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(62,7,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(63,8,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(64,9,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(65,10,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(66,11,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(67,12,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(68,13,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(69,14,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(70,15,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(71,16,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(72,17,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(73,18,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(74,19,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(75,20,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(76,21,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(77,22,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(78,23,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(79,24,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(80,25,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(81,26,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(82,27,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(83,28,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(84,29,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:34'),(85,30,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:35'),(86,31,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:35'),(87,32,'测试通知','测试通知','SYSTEM_ANNOUNCEMENT','SYSTEM',0,1,0,'2026-01-09 20:45:35');
/*!40000 ALTER TABLE `notifications` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `performance`
--

DROP TABLE IF EXISTS `performance`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `performance` (
  `id` int NOT NULL AUTO_INCREMENT,
  `user_id` int DEFAULT NULL,
  `period_start` date DEFAULT NULL,
  `period_end` date DEFAULT NULL,
  `tasks_completed` int DEFAULT NULL,
  `avg_delay_rate` decimal(5,2) DEFAULT NULL,
  `total_hours` int DEFAULT NULL,
  `manager_score` int DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `performance`
--

LOCK TABLES `performance` WRITE;
/*!40000 ALTER TABLE `performance` DISABLE KEYS */;
/*!40000 ALTER TABLE `performance` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_documents`
--

DROP TABLE IF EXISTS `project_documents`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_documents` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '文档ID',
  `project_id` int NOT NULL COMMENT '项目ID',
  `document_type` enum('REQUIREMENT','MEETING_MINUTES','DESIGN','TEST_PLAN','OTHER') DEFAULT 'OTHER' COMMENT '文档类型',
  `title` varchar(200) NOT NULL COMMENT '文档标题',
  `file_name` varchar(255) NOT NULL COMMENT '存储的文件名（含扩展名）',
  `file_path` varchar(500) NOT NULL COMMENT '文件在服务器上的相对路径',
  `file_size` bigint DEFAULT NULL COMMENT '文件大小（字节）',
  `file_format` varchar(20) DEFAULT NULL COMMENT '文件格式（pdf, doc, xls等）',
  `version` varchar(20) DEFAULT '1.0' COMMENT '文档版本',
  `uploader_id` int NOT NULL COMMENT '上传人ID',
  `description` text COMMENT '文档描述',
  `download_count` int DEFAULT '0' COMMENT '下载次数',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_document_project` (`project_id`),
  KEY `idx_document_type` (`document_type`),
  KEY `idx_document_uploader` (`uploader_id`),
  CONSTRAINT `project_documents_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `project_documents_ibfk_2` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目文档表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_documents`
--

LOCK TABLES `project_documents` WRITE;
/*!40000 ALTER TABLE `project_documents` DISABLE KEYS */;
/*!40000 ALTER TABLE `project_documents` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `project_members`
--

DROP TABLE IF EXISTS `project_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `project_members` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int NOT NULL,
  `user_id` int NOT NULL,
  `project_role` varchar(50) NOT NULL,
  `join_date` date DEFAULT NULL,
  `join_by` int DEFAULT NULL,
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_user` (`project_id`,`user_id`),
  KEY `user_id` (`user_id`),
  KEY `join_by` (`join_by`),
  CONSTRAINT `project_members_ibfk_1` FOREIGN KEY (`project_id`) REFERENCES `projects` (`id`) ON DELETE CASCADE,
  CONSTRAINT `project_members_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `project_members_ibfk_3` FOREIGN KEY (`join_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=9 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `project_members`
--

LOCK TABLES `project_members` WRITE;
/*!40000 ALTER TABLE `project_members` DISABLE KEYS */;
INSERT INTO `project_members` VALUES (1,1,4,'PROJECT_MANAGER','2026-01-07',1,'ACTIVE','2026-01-07 22:41:18','2026-01-08 18:40:14'),(2,1,9,'DEVELOPER','2026-01-07',1,'ACTIVE','2026-01-07 23:14:12','2026-01-08 18:40:13'),(3,1,10,'DEVELOPER','2026-01-07',1,'ACTIVE','2026-01-07 23:14:22','2026-01-08 18:40:13'),(4,1,11,'DEVELOPER','2026-01-07',1,'ACTIVE','2026-01-07 23:14:31','2026-01-08 18:40:12'),(5,1,12,'DEVELOPER','2026-01-07',1,'ACTIVE','2026-01-07 23:14:38','2026-01-08 18:40:12'),(6,2,5,'PROJECT_MANAGER','2026-01-08',1,'ACTIVE','2026-01-08 14:24:24','2026-01-08 14:43:24'),(7,2,17,'DEVELOPER','2026-01-08',1,'ACTIVE','2026-01-08 18:44:34','2026-01-08 18:44:34'),(8,2,18,'DEVELOPER','2026-01-08',1,'ACTIVE','2026-01-08 18:44:39','2026-01-08 18:44:39');
/*!40000 ALTER TABLE `project_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `projects`
--

DROP TABLE IF EXISTS `projects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `projects` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `description` text,
  `manager_id` int DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `priority` enum('HIGH','MEDIUM','LOW') DEFAULT NULL,
  `status` enum('IN_PROGRESS','DELAYED','COMPLETED','TERMINATED') DEFAULT NULL,
  `created_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `projects`
--

LOCK TABLES `projects` WRITE;
/*!40000 ALTER TABLE `projects` DISABLE KEYS */;
INSERT INTO `projects` VALUES (1,'智能家居APP开发','进行新一代APP的开发',4,'2026-01-07','2026-02-01','MEDIUM','IN_PROGRESS','2026-01-07 22:41:19','2026-01-08 18:40:07'),(2,'校园跑APP开发项目','为大学学生开发一款运动APP',5,'2026-01-08','2026-02-28','LOW','IN_PROGRESS','2026-01-08 13:56:53','2026-01-08 14:24:24');
/*!40000 ALTER TABLE `projects` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `reports`
--

DROP TABLE IF EXISTS `reports`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `reports` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '报表ID',
  `user_id` int NOT NULL COMMENT '提交人ID',
  `user_name` varchar(50) NOT NULL COMMENT '提交人姓名',
  `report_type` varchar(20) NOT NULL COMMENT '报表类型：WEEKLY-周报，MONTHLY-月报',
  `work_content` text COMMENT '本周/本月工作内容',
  `completed_tasks` text COMMENT '已完成任务列表（JSON格式存储任务ID和名称）',
  `next_plan` text COMMENT '下周/下月计划',
  `problems` text COMMENT '遇到的问题',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_report_type` (`report_type`),
  KEY `idx_submit_time` (`submit_time`),
  CONSTRAINT `reports_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='周报/月报表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `reports`
--

LOCK TABLES `reports` WRITE;
/*!40000 ALTER TABLE `reports` DISABLE KEYS */;
INSERT INTO `reports` VALUES (1,1,'张伟','WEEKLY','进行了系统的各项测试','','继续进行系统的各项测试','bug有点多啊','2026-01-08 13:00:06','2026-01-08 13:00:06'),(2,12,'徐静','WEEKLY','正在努力完成个性化广告的开发','','','需要和摇一摇广告联动','2026-01-08 15:39:35','2026-01-08 15:39:35');
/*!40000 ALTER TABLE `reports` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `task_members`
--

DROP TABLE IF EXISTS `task_members`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `task_members` (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键，自增ID',
  `task_id` int NOT NULL COMMENT '关联的任务ID，指向 tasks 表',
  `user_id` int NOT NULL COMMENT '关联的用户ID，指向 users 表',
  `task_role` varchar(50) NOT NULL COMMENT 'ASSIGNEE(负责人),  COLLABORATOR(协作者)',
  `join_date` date DEFAULT NULL COMMENT '加入任务的日期',
  `join_by` int DEFAULT NULL COMMENT '谁将此成员添加到任务中，指向 users 表',
  `status` enum('ACTIVE','INACTIVE') DEFAULT 'ACTIVE' COMMENT '成员状态：ACTIVE(活跃中), INACTIVE(已退出/非活跃)',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间，自动更新',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_task_user` (`task_id`,`user_id`),
  KEY `idx_task_id` (`task_id`) COMMENT '基于任务ID的查询索引',
  KEY `idx_user_id` (`user_id`) COMMENT '基于用户ID的查询索引',
  KEY `idx_join_by` (`join_by`) COMMENT '基于添加者的查询索引',
  KEY `idx_status` (`status`) COMMENT '基于状态的查询索引',
  KEY `idx_user_task` (`user_id`,`task_id`) COMMENT '用户-任务复合索引，优化联合查询',
  CONSTRAINT `task_members_ibfk_1` FOREIGN KEY (`task_id`) REFERENCES `tasks` (`id`) ON DELETE CASCADE,
  CONSTRAINT `task_members_ibfk_2` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `task_members_ibfk_3` FOREIGN KEY (`join_by`) REFERENCES `users` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='任务成员关系表 - 记录任务的参与人员及其角色';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `task_members`
--

LOCK TABLES `task_members` WRITE;
/*!40000 ALTER TABLE `task_members` DISABLE KEYS */;
INSERT INTO `task_members` VALUES (1,1,12,'COLLABORATOR','2026-01-07',1,'ACTIVE','2026-01-07 23:16:33','2026-01-08 19:10:21'),(2,1,9,'COLLABORATOR','2026-01-08',1,'ACTIVE','2026-01-08 12:34:30','2026-01-08 19:10:17'),(3,1,11,'ASSIGNEE','2026-01-08',1,'ACTIVE','2026-01-08 12:48:10','2026-01-08 19:10:20'),(4,1,10,'COLLABORATOR','2026-01-08',4,'ACTIVE','2026-01-08 14:43:00','2026-01-08 19:10:19'),(6,2,12,'ASSIGNEE','2026-01-08',4,'ACTIVE','2026-01-08 15:18:15','2026-01-08 19:10:10'),(7,2,10,'COLLABORATOR','2026-01-08',4,'ACTIVE','2026-01-08 15:18:36','2026-01-08 19:10:09'),(8,3,17,'ASSIGNEE','2026-01-08',5,'ACTIVE','2026-01-08 18:45:28','2026-01-08 18:45:28'),(9,3,18,'COLLABORATOR','2026-01-08',5,'ACTIVE','2026-01-08 18:45:37','2026-01-08 18:45:37');
/*!40000 ALTER TABLE `task_members` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `tasks`
--

DROP TABLE IF EXISTS `tasks`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `tasks` (
  `id` int NOT NULL AUTO_INCREMENT,
  `project_id` int DEFAULT NULL,
  `title` varchar(200) DEFAULT NULL,
  `description` text,
  `assignee_id` int DEFAULT NULL,
  `estimated_hours` int DEFAULT NULL,
  `actual_hours` int DEFAULT '0',
  `deadline` date DEFAULT NULL,
  `status` enum('NOT_STARTED','IN_PROGRESS','DELAY','COMPLETED','CANCELLED') DEFAULT NULL COMMENT '任务状态',
  `created_time` datetime DEFAULT NULL,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_project_id` (`project_id`) COMMENT '基于项目ID的查询索引',
  KEY `idx_assignee_id` (`assignee_id`) COMMENT '基于负责人ID的查询索引',
  KEY `idx_status` (`status`) COMMENT '基于任务状态的查询索引',
  KEY `idx_deadline` (`deadline`) COMMENT '基于截止日期的查询索引'
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `tasks`
--

LOCK TABLES `tasks` WRITE;
/*!40000 ALTER TABLE `tasks` DISABLE KEYS */;
INSERT INTO `tasks` VALUES (1,1,'摇一摇广告功能的开发','一动就进广告',11,20,0,'2026-01-18','IN_PROGRESS','2026-01-07 23:16:34','2026-01-08 19:10:04'),(2,1,'个性化广告功能开发','读取用户数据推送个性化的广告',12,22,0,'2026-01-31','COMPLETED','2026-01-08 15:18:15','2026-01-08 19:11:04'),(3,2,'高精度定位的适配和使用','防止学生偷鸡',17,25,0,'2026-01-31','NOT_STARTED','2026-01-08 18:45:28','2026-01-08 18:45:28');
/*!40000 ALTER TABLE `tasks` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `users`
--

DROP TABLE IF EXISTS `users`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL,
  `password` varchar(100) NOT NULL,
  `role` enum('ADMIN','PROJECT_MANAGER','EMPLOYEE') NOT NULL,
  `real_name` varchar(50) DEFAULT NULL,
  `email` varchar(100) DEFAULT NULL,
  `department` varchar(50) DEFAULT NULL COMMENT '部门ID（关联departments表）',
  `department_id` int DEFAULT NULL,
  `phone` varchar(20) DEFAULT NULL COMMENT '电话',
  `avatar` varchar(255) DEFAULT NULL COMMENT '用户头像URL',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`),
  KEY `idx_phone` (`phone`) COMMENT '基于电话的查询索引',
  KEY `idx_department` (`department`) COMMENT '基于部门的查询索引',
  KEY `fk_users_department_id` (`department_id`),
  CONSTRAINT `fk_users_department_id` FOREIGN KEY (`department_id`) REFERENCES `departments` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `users`
--

LOCK TABLES `users` WRITE;
/*!40000 ALTER TABLE `users` DISABLE KEYS */;
INSERT INTO `users` VALUES (1,'admin001','123456','ADMIN','张伟','admin001@company.com','开发部',1,'13800138001','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/9c17ed2f020f426bbebde0690cb54bac.jpg','2025-12-29 17:15:36','2026-01-09 20:16:20'),(2,'admin002','123456','ADMIN','李娜','admin002@company.com','开发部',1,'13800138002','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/210d864ad79b4e7a8479a6fb8c45e7f2.jpg','2025-12-29 17:15:36','2026-01-07 14:00:38'),(3,'admin003','123456','ADMIN','王强','admin003@company.com','开发部',1,'13800138003','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/8d680de3635a4509b740c1cc6954e638.jpg','2025-12-29 17:15:36','2026-01-07 14:00:43'),(4,'pm001','123456','PROJECT_MANAGER','赵明','pm001@company.com','开发部',1,'13800138004','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/f6d97be35c784755b12a80157a077def.jpg','2025-12-29 17:15:36','2026-01-08 15:40:43'),(5,'pm002','123456','PROJECT_MANAGER','孙丽','pm002@company.com','开发部',1,'13800138005','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/96bf9921612d4069a351af939260318e.jpg','2025-12-29 17:15:36','2026-01-07 14:00:55'),(6,'pm003','123456','PROJECT_MANAGER','周涛','pm003@company.com','开发部',1,'13800138006','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/a0d4f2f5d5224708a3f989b340973c86.jpg','2025-12-29 17:15:36','2026-01-07 14:01:01'),(7,'pm004','123456','PROJECT_MANAGER','吴芳','pm004@company.com','开发部',1,'13800138007','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/134ebf257c9a439689298e0d7ce309af.jpg','2025-12-29 17:15:36','2026-01-07 14:01:07'),(8,'pm005','123456','PROJECT_MANAGER','郑浩','pm005@company.com','开发部',1,'13800138008','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/6d621e0747ed47cabd5ef76bc0355ced.jpg','2025-12-29 17:15:36','2026-01-07 14:01:14'),(9,'emp001','123456','EMPLOYEE','杨帆','emp001@company.com','开发部',1,'13800138009','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/ee9318aeb1144b48bd437cc39be46e8c.jpg','2025-12-29 17:15:36','2026-01-07 14:01:18'),(10,'emp002','123456','EMPLOYEE','黄娟','emp002@company.com','开发部',1,'13800138010','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/9e5d3abcba644e04810a6348c5c4004a.jpg','2025-12-29 17:15:36','2026-01-07 14:01:25'),(11,'emp003','123456','EMPLOYEE','林峰','emp003@company.com','开发部',1,'13800138011','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/99ec26b67c134bd8bd62d95fe384273f.jpg','2025-12-29 17:15:36','2026-01-07 14:01:38'),(12,'emp004','123456','EMPLOYEE','徐静','emp004@company.com','开发部',1,'13800138012','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/d180a6e767d44528b66270b4250c9562.jpg','2025-12-29 17:15:36','2026-01-07 14:01:44'),(13,'emp005','123456','EMPLOYEE','朱辉','emp005@company.com','开发部',1,'13800138013','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/85f00e25226743e68bc3331e03db2eb9.jpg','2025-12-29 17:15:36','2026-01-07 14:01:48'),(14,'emp006','123456','EMPLOYEE','何婷','emp006@company.com','开发部',1,'13800138014','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/2f53a70749404a9ab519e688505517cd.jpg','2025-12-29 17:15:36','2026-01-07 14:01:54'),(15,'emp007','123456','EMPLOYEE','高翔','emp007@company.com','开发部',1,'13800138015','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/cfdef28404574ad68f3219a73ceb4303.jpg','2025-12-29 17:15:36','2026-01-07 14:01:59'),(16,'emp008','123456','EMPLOYEE','罗娜','emp008@company.com','开发部',1,'13800138016','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/9dcc4887aeef422f9bc61fd9d28cc2bf.jpg','2025-12-29 17:15:36','2026-01-07 14:02:04'),(17,'emp009','123456','EMPLOYEE','马超','emp009@company.com','开发部',1,'13800138017','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/a8dc508f7ee24654a558b5c348f45df1.jpg','2025-12-29 17:15:36','2026-01-07 14:02:12'),(18,'emp010','123456','EMPLOYEE','梁静','emp010@company.com','开发部',1,'13800138018','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/55fbd7fe43494be8af852de341ed4241.jpg','2025-12-29 17:15:36','2026-01-07 14:02:25'),(19,'emp011','123456','EMPLOYEE','宋杰','emp011@company.com','开发部',1,'13800138019','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/8920bc64e5c54ecca655d9fbae2e4f8a.jpg','2025-12-29 17:15:36','2026-01-07 14:02:29'),(20,'emp012','123456','EMPLOYEE','谢芳','emp012@company.com','开发部',1,'13800138020','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/e2f984a0743d423ab00b6927620485b6.jpg','2025-12-29 17:15:36','2026-01-07 14:02:36'),(21,'emp013','123456','EMPLOYEE','唐宇','emp013@company.com','开发部',1,'13800138021','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/f66f35167f694a4d9765cec36288f0a9.jpg','2025-12-29 17:15:36','2026-01-07 14:02:45'),(22,'emp014','123456','EMPLOYEE','董洁','emp014@company.com','开发部',1,'13800138022','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/f6a182a177504a3986d7c35a1f3bface.jpg','2025-12-29 17:15:36','2026-01-07 14:02:51'),(23,'emp015','123456','EMPLOYEE','程浩','emp015@company.com','开发部',1,'13800138023','https://randomuser.me/api/portraits/men/23.jpg','2025-12-29 17:15:36','2025-12-29 17:15:36'),(24,'emp016','123456','EMPLOYEE','袁媛','emp016@company.com','开发部',1,'13800138024','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/dff076ddc1664eefb46eec1c0c963cff.jpg','2025-12-29 17:15:36','2026-01-07 14:03:03'),(25,'emp017','123456','EMPLOYEE','邓超','emp017@company.com','开发部',1,'13800138025','https://randomuser.me/api/portraits/men/25.jpg','2025-12-29 17:15:36','2025-12-29 17:15:36'),(26,'emp018','123456','EMPLOYEE','许晴','emp018@company.com','开发部',1,'13800138026','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/324293ada1224bbda91f0bed46808145.jpg','2025-12-29 17:15:36','2026-01-07 14:03:16'),(27,'emp019','123456','EMPLOYEE','韩磊','emp019@company.com','开发部',1,'13800138027','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/7d4abde6715843b481945ae83f35c02b.jpg','2025-12-29 17:15:36','2026-01-07 14:03:24'),(28,'emp020','123456','EMPLOYEE','沈冰','emp020@company.com','开发部',1,'13800138028','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/8dd3d9a6a6904a0b8262b2f0aa4d3cab.jpg','2025-12-29 17:15:36','2026-01-07 14:03:31'),(29,'emp021','123456','EMPLOYEE','曾伟','emp021@company.com','开发部',1,'13800138029','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/bfd4314194324149af15baee5dd95676.jpg','2025-12-29 17:15:36','2026-01-07 14:03:37'),(30,'emp022','123456','EMPLOYEE','彭丽','emp022@company.com','开发部',1,'13800138030','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/62f07757f56a43958943783a67ee711f.jpg','2025-12-29 17:15:36','2026-01-07 14:03:43'),(31,'emp023','123456','EMPLOYEE','梁源','emp023@company.com',NULL,NULL,'13800138031','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/24edca27daeb4b49b12485915938bfd1.jpg','2026-01-07 13:40:15','2026-01-07 14:04:07'),(32,'emp024','123456','EMPLOYEE','潘俊','emp024@company.com',NULL,NULL,'13800138032','https://web-konnac-management.oss-cn-hangzhou.aliyuncs.com/avatar/9bd6f48cdd0e46c2980f156e7ff20ce4.jpg','2026-01-07 14:05:11','2026-01-07 14:05:11');
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

-- Dump completed on 2026-01-10 13:26:09
