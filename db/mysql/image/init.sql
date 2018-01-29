CREATE DATABASE `odk_db`;
CREATE USER 'odk_unit'@'%' IDENTIFIED BY 'test';
GRANT ALL ON `odk_db`.* TO 'odk_unit'@'%' IDENTIFIED BY 'test';
FLUSH PRIVILEGES;