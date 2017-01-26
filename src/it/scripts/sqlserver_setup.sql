USE master;
IF EXISTS(select * from sys.databases where name='odk_unit') 
  DROP database odk_unit;
go
CREATE DATABASE odk_unit;
go
USE odk_unit;
go
CREATE SCHEMA odk_schema;
go

