IF EXISTS(select name from sys.schemas where name='odk_schema') 
  BEGIN
    DECLARE @A NVARCHAR(MAX)
    DECLARE @DROPSQL NVARCHAR(MAX)

    DECLARE tbl_list CURSOR FOR
    select name from sys.tables where schema_id in (select schema_id from sys.schemas where name = 'odk_schema')

    OPEN tbl_list
    FETCH NEXT FROM tbl_list INTO @A

    WHILE (@@FETCH_STATUS = 0)
      BEGIN
	    SET @DROPSQL = 'DROP TABLE [odk_schema].' + QUOTENAME(@A)
		PRINT @DROPSQL
		EXEC sp_executesql @DROPSQL; 
		FETCH NEXT FROM tbl_list INTO @A
      END

    CLOSE tbl_list
    DEALLOCATE tbl_list
	
	DROP SCHEMA [odk_schema];
  END
go
CREATE SCHEMA [odk_schema] AUTHORIZATION [odk_unit_login];

