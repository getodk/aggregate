IF EXISTS(select name from sys.schemas where name='odk_unit_test') 
  BEGIN
    DECLARE @A NVARCHAR(MAX)
    DECLARE @DROPSQL NVARCHAR(MAX)

    DECLARE tbl_list CURSOR FOR
    select name from sys.tables where schema_id in (select schema_id from sys.schemas where name = 'odk_unit_test')

    OPEN tbl_list
    FETCH NEXT FROM tbl_list INTO @A

    WHILE (@@FETCH_STATUS = 0)
      BEGIN
	    SET @DROPSQL = 'DROP TABLE [odk_unit_test].' + QUOTENAME(@A)
		PRINT @DROPSQL
		EXEC sp_executesql @DROPSQL; 
		FETCH NEXT FROM tbl_list INTO @A
      END

    CLOSE tbl_list
    DEALLOCATE tbl_list
	
	DROP SCHEMA [odk_unit_test];
  END
go
CREATE SCHEMA [odk_unit_test] AUTHORIZATION [odk_prod];

