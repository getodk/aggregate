/**
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine.sqlserver;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.DataType;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.engine.DatastoreAccessMetrics;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SqlParameterValue;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class DatastoreImpl implements Datastore, InitializingBean {

  private static final boolean logBindDetails = false;
  
  // SQL Server has a 116-character limit in column names.
  // limit to two for future uses
  private static final int MAX_COLUMN_NAME_LEN = 114;
  // and the same limit applies to table names.
  private static final int MAX_TABLE_NAME_LEN = 112; // reserve 4 char for idx
                                                     // name

  static final long MAX_IN_ROW_NVARCHAR = 4000L;
  
  // limit on SqlServer capacity (minus about 100 for where clause filters)
  private static final int MAX_BIND_PARAMS = 2000;

  static final String PATTERN_ISO8601_NO_ZONE = "yyyy-MM-dd'T'HH:mm:ss.SSS";

  // limit to 256MB blob size; don't know the impact of this...
  private static final Long MAX_BLOB_SIZE = 65536 * 4096L;

  private final DatastoreAccessMetrics dam = new DatastoreAccessMetrics();
  private DataSource dataSource = null;
  private DataSourceTransactionManager tm = null;

  private String schemaName = null;

  public DatastoreImpl() throws ODKDatastoreException {
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
    this.tm = new DataSourceTransactionManager(dataSource);
  }

  public void setSchemaName(String schemaName) {
    this.schemaName = schemaName;
  }

  @Override
  public void afterPropertiesSet() throws Exception {
    if (dataSource == null) {
      throw new IllegalStateException("dataSource property must be set!");
    }
    if (schemaName == null) {
      JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
      List<?> databaseNames = jdbcTemplate.queryForList("SELECT current_database()", String.class);
      schemaName = (String) databaseNames.get(0);
    }
  }

  public static final String K_CREATE_TABLE = "CREATE TABLE ";
  public static final String K_DROP_TABLE = "DROP TABLE ";

  public static final String K_OPEN_PAREN = " ( ";
  public static final String K_CLOSE_PAREN = " ) ";
  public static final String K_SELECT = "SELECT ";
  public static final String K_SELECT_DISTINCT = "SELECT DISTINCT ";
  public static final String K_CS = ", ";
  public static final String K_COLON = ";";
  public static final String K_BQ = "\"";
  public static final String K_FROM = " FROM ";
  public static final String K_WHERE = " WHERE ";
  public static final String K_AND = " AND ";
  public static final String K_IS_NULL = " IS NULL";
  public static final String K_EQ_NULL = " = NULL";
  private static final String K_NULL = " NULL ";
  private static final String K_NOT_NULL = " NOT NULL ";
  public static final String K_EQ = " = ";
  public static final String K_BIND_VALUE = "?";
  public static final String K_CREATE_CLUSTERED_INDEX = "CREATE CLUSTERED INDEX ";
  public static final String K_CREATE_NONCLUSTERED_INDEX = "CREATE NONCLUSTERED INDEX ";
  public static final String K_ON = " ON ";
  public static final String K_INSERT_INTO = "INSERT INTO ";
  public static final String K_VALUES = " VALUES ";
  public static final String K_UPDATE = "UPDATE ";
  public static final String K_SET = " SET ";
  public static final String K_DELETE_FROM = "DELETE FROM ";

  public static final Integer DEFAULT_DBL_NUMERIC_SCALE = 10;
  public static final Integer DEFAULT_DBL_NUMERIC_PRECISION = 38;
  public static final Integer DEFAULT_INT_NUMERIC_PRECISION = 9;

  private static final class TableDefinition {

    public DataField.DataType getDataType() {
      return dataType;
    }

    public void setDataType(DataField.DataType dataType) {
      this.dataType = dataType;
    }

    public String getColumnName() {
      return columnName;
    }

    public boolean isNullable() {
      return isNullable;
    }

    public Long getMaxCharLen() {
      return maxCharLen;
    }

    public Integer getNumericScale() {
      return numericScale;
    }

    public Integer getNumericPrecision() {
      return numericPrecision;
    }

    public boolean isDoublePrecision() {
      return isDoublePrecision;
    }
    
    public static final String COLUMN_NAME = "column_name";
    public static final String CHARACTER_MAXIMUM_LENGTH = "character_maximum_length";
    public static final String NUMERIC_PRECISION = "numeric_precision";
    public static final String NUMERIC_SCALE = "numeric_scale";
    public static final String DATA_TYPE = "data_type";
    public static final String IS_NULLABLE = "is_nullable";
    public static final String INFORMATION_SCHEMA_COLUMNS = "sys.columns_join_for_table_def_query";

    public static final String TABLE_DEF_FROM_WHERE_CLAUSE = " from sys.columns c, sys.types t, sys.tables tn, sys.schemas s where "
        + "c.object_id = tn.object_id and tn.schema_id = s.schema_id and "
        + "c.system_type_id = t.system_type_id and c.user_type_id = t.user_type_id and "
        + " s.name = " + K_BIND_VALUE + " and tn.name = " + K_BIND_VALUE;

    public static final String TABLE_DEF_QUERY = "select s.name as \"schema_name\", tn.name as table_name, c.name as column_name, "
        + "c.is_nullable, c.max_length as \"character_maximum_length\", "
        + "c.precision as \"numeric_precision\", c.scale as \"numeric_scale\", t.name as \"data_type\" "
        + TABLE_DEF_FROM_WHERE_CLAUSE;

    public static final String TABLE_EXISTS_QUERY = "select count(1) "
        + TABLE_DEF_FROM_WHERE_CLAUSE;

    private static final String BIT = "bit";

    private static final String VARBINARY = "varbinary";

    private static final String TEXT = "text"; // lower case!
    private static final String CHAR = "char";

    private static final String DATE = "date";
    private static final String TIME = "time";

    private static final String INT = "int";
    private static final String FLOAT = "float";

    private String columnName;
    private boolean isNullable;
    private Long maxCharLen = null;
    private Integer numericScale = null;
    private Integer numericPrecision = null;
    private boolean isDoublePrecision = false;
    private DataField.DataType dataType;

    TableDefinition(ResultSet rs) throws SQLException {
      columnName = rs.getString(COLUMN_NAME);
      int i = rs.getInt(IS_NULLABLE);
      isNullable = (i == 1);
      isDoublePrecision = false;

      String type = rs.getString(DATA_TYPE);
      BigDecimal num = rs.getBigDecimal(CHARACTER_MAXIMUM_LENGTH);

      if (type.equalsIgnoreCase(BIT)) {
        // bit -- boolean
        dataType = DataField.DataType.BOOLEAN;

      } else if (type.equalsIgnoreCase(VARBINARY)) {
        // blob
        dataType = DataField.DataType.BINARY;
        maxCharLen = num.longValueExact();
        // limit to a smaller size than 2GB
        if (maxCharLen < 0L || maxCharLen > MAX_BLOB_SIZE) {
          maxCharLen = MAX_BLOB_SIZE;
        }

      } else if (type.contains(TEXT) || type.contains(CHAR)) {
        // some sort of text field
        maxCharLen = num.longValueExact();
        // limit to a smaller size than 2GB
        if (maxCharLen < 0L || maxCharLen > MAX_BLOB_SIZE) {
          maxCharLen = MAX_BLOB_SIZE;
        }

        if (type.startsWith("n")) {
          // actual number of UTF-8 chars is one half storage size
          maxCharLen = maxCharLen / 2L;
        }

        if (maxCharLen.compareTo(MAX_IN_ROW_NVARCHAR) <= 0) {
          dataType = DataField.DataType.STRING;
        } else {
          dataType = DataField.DataType.LONG_STRING;
        }

      } else if (type.contains(DATE) || type.contains(TIME)) {
        // holds a timestamp
        dataType = DataField.DataType.DATETIME;

      } else if (type.contains(INT)) {
        // some form of integer
        dataType = DataField.DataType.INTEGER;
        numericScale = 0;
        numericPrecision = rs.getBigDecimal(NUMERIC_PRECISION).intValueExact();

      } else {
        // must be numeric...
        if (type.contains(FLOAT)) {

          dataType = DataField.DataType.DECIMAL;
          numericScale = null;
          num = rs.getBigDecimal(NUMERIC_PRECISION);
          numericPrecision = num.intValueExact();
          isDoublePrecision = true;
          
        } else {
          
          num = rs.getBigDecimal(NUMERIC_SCALE);
          if (num == null) {
            throw new IllegalArgumentException("unrecognized data type in schema: " + type);
          } else {
            // discriminate between decimal and integer by looking at value...
            // We assume that nobody is going crazy with the scale here...
            if (BigDecimal.ZERO.equals(num)) {
              dataType = DataField.DataType.INTEGER;
              numericScale = 0;
            } else {
              numericScale = num.intValueExact();
              dataType = DataField.DataType.DECIMAL;
            }
            num = rs.getBigDecimal(NUMERIC_PRECISION);
            numericPrecision = num.intValueExact();
          }
        }
      }
    }
  }

  private static RowMapper<TableDefinition> tableDef = new RowMapper<TableDefinition>() {
    @Override
    public TableDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new TableDefinition(rs);
    }
  };

  static SqlParameterValue getBindValue(DataField f, Object value) {
    switch (f.getDataType()) {
    case BOOLEAN:
      if ( value == null ) {
        return new SqlParameterValue(java.sql.Types.BIT, null);
      } else if ( value instanceof Boolean ) {
        return new SqlParameterValue(java.sql.Types.BIT, (((Boolean) value) ? 1 : 0));
      } else {
        Boolean b = Boolean.valueOf(value.toString());
        return new SqlParameterValue(java.sql.Types.BIT, (b ? 1 : 0));
      }
    case STRING:
    case URI:
      if ( value == null ) {
        return new SqlParameterValue(java.sql.Types.NVARCHAR, null);
      } else {
        return new SqlParameterValue(java.sql.Types.NVARCHAR, value.toString());
      }
    case INTEGER:
      if ( value == null ) {
        return new SqlParameterValue(java.sql.Types.BIGINT, null);
      } else if ( value instanceof Long ) {
        return new SqlParameterValue(java.sql.Types.BIGINT, (Long) value);
      } else {
        Long l = Long.valueOf(value.toString());
        return new SqlParameterValue(java.sql.Types.BIGINT, l);
      }
    case DECIMAL: {
      if ( value == null ) {
        // nulls may not go through as DECIMAL -- assert they are strings
        return new SqlParameterValue(java.sql.Types.NVARCHAR, null);
      } else {
        WrappedBigDecimal wbd;
        if ( value instanceof WrappedBigDecimal ) {
          wbd = (WrappedBigDecimal) value;
        } else {
          wbd = new WrappedBigDecimal(value.toString());
        }
        if ( wbd.isSpecialValue() ) {
          return new SqlParameterValue(java.sql.Types.DOUBLE, wbd.d);
        } else {
          return new SqlParameterValue(java.sql.Types.DECIMAL, wbd.bd);
        }
      }
    }
    case DATETIME: {
      // This doesn't like TIMESTAMP data type
      if ( value == null ) {
        return new SqlParameterValue(java.sql.Types.TIMESTAMP, null);
      } else if ( value instanceof Date ) {
        // This doesn't like TIMESTAMP data type
        Date v = (Date) value;
        String dateTime = null;
        if (v != null) {
          SimpleDateFormat asGMTiso8601 = new SimpleDateFormat(DatastoreImpl.PATTERN_ISO8601_NO_ZONE);
          asGMTiso8601.setTimeZone(TimeZone.getTimeZone("GMT"));
          dateTime = asGMTiso8601.format(v);
        }
        return new SqlParameterValue(java.sql.Types.TIMESTAMP, dateTime);
      } else {
        throw new IllegalArgumentException("expected Date for DATETIME bind parameter");
      }
    }
    case BINARY:
      if ( value == null ) {
        return new SqlParameterValue(java.sql.Types.LONGVARBINARY, null);
      } else if ( value instanceof byte[] ) {
        return new SqlParameterValue(java.sql.Types.LONGVARBINARY, value);
      } else {
        throw new IllegalArgumentException("expected byte[] for BINARY bind parameter");
      }
    case LONG_STRING:
      if ( value == null ) {
        return new SqlParameterValue(java.sql.Types.LONGNVARCHAR, null);
      } else {
        return new SqlParameterValue(java.sql.Types.LONGNVARCHAR, value.toString());
      }

    default:
      throw new IllegalStateException("Unexpected data type");
    }
  }

  public static void buildArgumentList(List<SqlParameterValue> pv, CommonFieldsBase entity,
      DataField f) {
    switch (f.getDataType()) {
    case BOOLEAN:
      pv.add(getBindValue(f, entity.getBooleanField(f)));
      break;
    case STRING:
    case URI:
      pv.add(getBindValue(f, entity.getStringField(f)));
      break;
    case INTEGER:
      pv.add(getBindValue(f, entity.getLongField(f)));
      break;
    case DECIMAL:
      pv.add(getBindValue(f, entity.getNumericField(f)));
      break;
    case DATETIME:
      pv.add(getBindValue(f, entity.getDateField(f)));
      break;
    case BINARY:
      pv.add(getBindValue(f, entity.getBlobField(f)));
      break;
    case LONG_STRING:
      pv.add(getBindValue(f, entity.getStringField(f)));
      break;

    default:
      throw new IllegalStateException("Unexpected data type");
    }
  }

  void recordQueryUsage(CommonFieldsBase relation, int recCount) {
    dam.recordQueryUsage(relation, recCount);
  }

  @Override
  public String getDefaultSchemaName() {
    return schemaName;
  }

  JdbcTemplate getJdbcConnection() {
    return new JdbcTemplate(dataSource);
  }

  @Override
  public int getMaxLenColumnName() {
    return MAX_COLUMN_NAME_LEN;
  }

  @Override
  public int getMaxLenTableName() {
    return MAX_TABLE_NAME_LEN;
  }

  private final boolean updateRelation(JdbcTemplate jc, CommonFieldsBase relation,
      String originalStatement) {

    String qs = TableDefinition.TABLE_DEF_QUERY;
    List<?> columns;
    columns = jc.query(qs, new Object[] { relation.getSchemaName(), relation.getTableName() },
        tableDef);
    dam.recordQueryUsage(TableDefinition.INFORMATION_SCHEMA_COLUMNS, columns.size());

    if (columns.size() > 0) {
      Map<String, TableDefinition> map = new HashMap<String, TableDefinition>();
      for (Object o : columns) {
        TableDefinition t = (TableDefinition) o;
        map.put(t.getColumnName(), t);
      }

      // we may have gotten some results into columns -- go through the fields
      // and
      // assemble the results... we don't care about additional columns in the
      // map...
      for (DataField f : relation.getFieldList()) {
        TableDefinition d = map.get(f.getName());
        if (d == null) {
          StringBuilder b = new StringBuilder();
          if (originalStatement == null) {
            b.append(" Retrieving expected definition (");
            boolean first = true;
            for (DataField field : relation.getFieldList()) {
              if (!first) {
                b.append(K_CS);
              }
              first = false;
              b.append(field.getName());
            }
            b.append(")");
          } else {
            b.append(" Created with: ");
            b.append(originalStatement);
          }
          throw new IllegalStateException(
              "did not find expected column " + f.getName() + " in table "
                  + relation.getSchemaName() + "." + relation.getTableName() + b.toString());
        }
        if (f.getDataType() == DataField.DataType.BOOLEAN
            && d.getDataType() == DataField.DataType.STRING) {
          d.setDataType(DataField.DataType.BOOLEAN);
          // don't care about size...
        }

        if (d.getDataType() == DataField.DataType.STRING && f.getMaxCharLen() != null
            && f.getMaxCharLen().compareTo(d.getMaxCharLen()) > 0) {
          throw new IllegalStateException("column " + f.getName() + " in table "
              + relation.getSchemaName() + "." + relation.getTableName()
              + " stores string-valued keys but is shorter than required by Aggregate "
              + d.getMaxCharLen().toString() + " < " + f.getMaxCharLen().toString());
        }

        if (f.getDataType() == DataField.DataType.URI) {
          if (d.getDataType() != DataField.DataType.STRING) {
            throw new IllegalStateException(
                "column " + f.getName() + " in table " + relation.getSchemaName() + "."
                    + relation.getTableName() + " stores URIs but is not a string field");
          }
          d.setDataType(DataField.DataType.URI);
        }

        if ((d.getDataType() == DataField.DataType.LONG_STRING)
            && (f.getDataType() == DataField.DataType.STRING)) {
          // we have an overly-large string that needed to be
          // stored as a nvarchar(max) string. This is OK
        } else if (d.getDataType() != f.getDataType()) {
          throw new IllegalStateException("column " + f.getName() + " in table "
              + relation.getSchemaName() + "." + relation.getTableName()
              + " is not of the expected type " + f.getDataType().toString());
        }

        // it is OK for the data model to be more strict than the data store.
        if (!d.isNullable() && f.getNullable()) {
          throw new IllegalStateException("column " + f.getName() + " in table "
              + relation.getSchemaName() + "." + relation.getTableName()
              + " is defined as NOT NULL but the data model requires NULL");
        }
        f.setMaxCharLen(d.getMaxCharLen());
        f.setNumericPrecision(d.getNumericPrecision());
        f.setNumericScale(d.getNumericScale());
        f.asDoublePrecision(d.isDoublePrecision());
      }
      return true;
    } else {
      return false;
    }
  }

  /**
   * Relation manipulation APIs
   */
  @Override
  public void assertRelation(CommonFieldsBase relation, User user) throws ODKDatastoreException {
    JdbcTemplate jc = getJdbcConnection();
    TransactionStatus status = null;
    try {
      DefaultTransactionDefinition paramTransactionDefinition = new DefaultTransactionDefinition();

      // do serializable read on the information schema...
      paramTransactionDefinition
          .setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);
      paramTransactionDefinition.setReadOnly(true);
      status = tm.getTransaction(paramTransactionDefinition);

      // see if relation already is defined and update it with dimensions...
      if (updateRelation(jc, relation, null)) {
        // it exists -- we're done!
        tm.commit(status);
        status = null;
        return;
      } else {
        tm.commit(status);
        // Try a new transaction to create the table
        paramTransactionDefinition
            .setIsolationLevel(DefaultTransactionDefinition.ISOLATION_SERIALIZABLE);
        paramTransactionDefinition.setReadOnly(false);
        status = tm.getTransaction(paramTransactionDefinition);

        // total number of columns must be less than MAX_BIND_PARAMS
        int countColumns = 0;
        // need to create the table...
        StringBuilder b = new StringBuilder();
        b.append(K_CREATE_TABLE);
        b.append(K_BQ);
        b.append(relation.getSchemaName());
        b.append(K_BQ);
        b.append(".");
        b.append(K_BQ);
        b.append(relation.getTableName());
        b.append(K_BQ);
        b.append(K_OPEN_PAREN);
        boolean firstTime = true;
        for (DataField f : relation.getFieldList()) {
          if (!firstTime) {
            b.append(K_CS);
          }
          ++countColumns;
          firstTime = false;
          String nullClause;
          if (f.getNullable()) {
            nullClause = K_NULL;
          } else {
            nullClause = K_NOT_NULL;
          }

          b.append(K_BQ);
          b.append(f.getName());
          b.append(K_BQ);
          DataField.DataType type = f.getDataType();
          switch (type) {
          case BINARY:
            b.append(" varbinary(max)").append(nullClause);
            break;
          case LONG_STRING:
            b.append(" nvarchar(max)").append(nullClause);
            break;
          case STRING:
            b.append(" nvarchar(");
            Long len = f.getMaxCharLen();
            if (len == null) {
              len = PersistConsts.DEFAULT_MAX_STRING_LENGTH;
            }
            if (len > MAX_IN_ROW_NVARCHAR) {
              // store value out-of-row
              b.append("max");
            } else {
              b.append(len.toString());
            }
            b.append(K_CLOSE_PAREN).append(nullClause);
            break;
          case BOOLEAN:
            b.append(" bit").append(nullClause);
            break;
          case INTEGER:
            Integer int_digits = f.getNumericPrecision();
            if (int_digits == null) {
              int_digits = DEFAULT_INT_NUMERIC_PRECISION;
            }

            if (int_digits.compareTo(9) > 0) {
              b.append(" bigint").append(nullClause);
            } else {
              b.append(" integer").append(nullClause);
            }
            break;
          case DECIMAL:
            if (f == relation.primaryKey) {
              throw new IllegalStateException("cannot use decimal columns as primary keys");
            }

            if (f.isDoublePrecision()) {
              b.append(" float(53) ").append(nullClause);
            } else {
              Integer dbl_digits = f.getNumericPrecision();
              Integer dbl_fract = f.getNumericScale();
              if (dbl_digits == null) {
                dbl_digits = DEFAULT_DBL_NUMERIC_PRECISION;
              }
              if (dbl_fract == null) {
                dbl_fract = DEFAULT_DBL_NUMERIC_SCALE;
              }
              b.append(" decimal(");
              b.append(dbl_digits.toString());
              b.append(K_CS);
              b.append(dbl_fract.toString());
              b.append(K_CLOSE_PAREN).append(nullClause);
            }
            break;
          case DATETIME:
            b.append(" datetime2(7)").append(nullClause);
            break;
          case URI:
            b.append(" nvarchar(");
            len = f.getMaxCharLen();
            if (len == null) {
              len = PersistConsts.URI_STRING_LEN;
            }
            b.append(len.toString());
            b.append(")").append(nullClause);
            break;
          }

          if (f == relation.primaryKey) {
            b.append(" PRIMARY KEY NONCLUSTERED ");
          }
        }
        b.append(K_CLOSE_PAREN);

        if ( countColumns > MAX_BIND_PARAMS ) {
          throw new IllegalArgumentException("Table size exceeds bind parameter limit");
        }
        
        String createTableStmt = b.toString();
        LogFactory.getLog(DatastoreImpl.class).info("Attempting: " + createTableStmt);

        jc.execute(createTableStmt);
        LogFactory.getLog(DatastoreImpl.class)
            .info("create table success (before updateRelation): " + relation.getTableName());

        boolean alreadyClustered = false;
        String idx;
        // create other indicies
        for (DataField f : relation.getFieldList()) {
          if ((f.getIndexable() != IndexType.NONE) && (f != relation.primaryKey)) {
            idx = relation.getTableName() + "_" + shortPrefix(f.getName());
            alreadyClustered = createIndex(jc, relation, idx, f, alreadyClustered);
          }
        }

        // and update the relation with actual dimensions...
        updateRelation(jc, relation, createTableStmt);
        tm.commit(status);
      }
    } catch (Exception e) {
      if (status != null) {
        tm.rollback(status);
      }
      throw new ODKDatastoreException(e);
    }
  }

  /**
   * Construct a 3-character or more prefix for use in the index name.
   *
   * @param name
   * @return
   */
  private String shortPrefix(String name) {
    StringBuilder b = new StringBuilder();
    String[] splits = name.split("_");
    for (int i = 0; i < splits.length; ++i) {
      if (splits[i].length() > 0) {
        b.append(splits[i].charAt(0));
      }
    }
    if (b.length() < 3) {
      b.append(Integer.toString(name.length() % 10));
    }
    return b.toString().toLowerCase();
  }

  private boolean createIndex(JdbcTemplate jc, CommonFieldsBase tbl, String idxName, DataField field, boolean alreadyClustered) {
    StringBuilder b = new StringBuilder();

    if ( field.getDataType() == DataType.DECIMAL ) {
      // don't allow this. It will conflict with our handling of special values.
      throw new IllegalStateException("Cannot index decimal fields");
    }
    // the options are non-clustered or clustered. 
    // there can only be one clustered index per table. 
    if (field.getIndexable() == IndexType.HASH || alreadyClustered) {
      b.append(K_CREATE_NONCLUSTERED_INDEX);
    } else {
      b.append(K_CREATE_CLUSTERED_INDEX);
      alreadyClustered = true;
    }
    b.append(K_BQ);
    b.append(idxName);
    b.append(K_BQ);
    b.append(K_ON);
    b.append(K_BQ);
    b.append(tbl.getSchemaName());
    b.append(K_BQ);
    b.append(".");
    b.append(K_BQ);
    b.append(tbl.getTableName());
    b.append(K_BQ);
    b.append(" (");
    b.append(K_BQ);
    b.append(field.getName());
    b.append(K_BQ);
    b.append(" )");

    jc.execute(b.toString());
    return alreadyClustered;
  }

  @Override
  public boolean hasRelation(String schema, String tableName, User user) {
    dam.recordQueryUsage(TableDefinition.INFORMATION_SCHEMA_COLUMNS, 1);
    String qs = TableDefinition.TABLE_EXISTS_QUERY;
    Integer columnCount = getJdbcConnection().queryForObject(qs, new Object[] { schema, tableName },
        Integer.class);
    return (columnCount != null && columnCount != 0);
  }

  @Override
  public void dropRelation(CommonFieldsBase relation, User user) throws ODKDatastoreException {
    try {
      StringBuilder b = new StringBuilder();
      b.append(K_DROP_TABLE);
      b.append(K_BQ);
      b.append(relation.getSchemaName());
      b.append(K_BQ);
      b.append(".");
      b.append(K_BQ);
      b.append(relation.getTableName());
      b.append(K_BQ);

      LogFactory.getLog(DatastoreImpl.class)
          .info("Executing " + b.toString() + " by user " + user.getUriUser());
      getJdbcConnection().execute(b.toString());
    } catch (Exception e) {
      LogFactory.getLog(DatastoreImpl.class)
          .warn(relation.getTableName() + " exception: " + e.toString());
      throw new ODKDatastoreException(e);
    }
  }

  /***************************************************************************
   * Entity manipulation APIs
   *
   */

  @SuppressWarnings("unchecked")
  @Override
  public <T extends CommonFieldsBase> T createEntityUsingRelation(T relation, User user) {

    // we are generating our own PK, so we don't need to interact with DB
    // yet...
    T row;
    try {
      row = (T) relation.getEmptyRow(user);
    } catch (Exception e) {
      throw new IllegalArgumentException("failed to create empty row", e);
    }
    return row;
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T extends CommonFieldsBase> T getEntity(T relation, String uri, User user)
      throws ODKEntityNotFoundException {
    Query query = new QueryImpl(relation, "getEntity", this, user);
    query.addFilter(relation.primaryKey, FilterOperation.EQUAL, uri);
    dam.recordGetUsage(relation);
    try {
      List<? extends CommonFieldsBase> results = query.executeQuery();
      if (results == null || results.size() != 1) {
        throw new ODKEntityNotFoundException("Unable to retrieve " + relation.getSchemaName() + "."
            + relation.getTableName() + " key: " + uri);
      }
      return (T) results.get(0);
    } catch (ODKDatastoreException e) {
      throw new ODKEntityNotFoundException("Unable to retrieve " + relation.getSchemaName() + "."
          + relation.getTableName() + " key: " + uri, e);
    }
  }

  @Override
  public Query createQuery(CommonFieldsBase relation, String loggingContextTag, User user) {
    Query query = new QueryImpl(relation, loggingContextTag, this, user);
    return query;
  }

  private static class ReusableStatementSetter implements PreparedStatementSetter {

    String sql = null;
    List<SqlParameterValue> argList = null;

    ReusableStatementSetter() {
    };

    ReusableStatementSetter(String sql, List<SqlParameterValue> args) {
      this.sql = sql;
      this.argList = args;
    }

    public void setArgList(String sql, List<SqlParameterValue> args) {
      this.sql = sql;
      this.argList = args;
    }
    
    private void createLogContent(StringBuilder b, int i, SqlParameterValue arg) {
      b.append("\nbinding[").append(i).append("]: type: ");
      switch ( arg.getSqlType() ) {
      case java.sql.Types.BIT:
        b.append("BIT");
        break;
      case java.sql.Types.BIGINT:
        b.append("BIGINT");
        break;
      case java.sql.Types.DECIMAL:
        b.append("DECIMAL");
        break;
      case java.sql.Types.DOUBLE:
        b.append("DOUBLE");
        break;
      case java.sql.Types.TIMESTAMP:
        b.append("TIMESTAMP");
        break;
      case java.sql.Types.NVARCHAR:
        b.append("NVARCHAR");
        break;
      case java.sql.Types.VARBINARY:
        b.append("VARBINARY");
        break;
      default:
        b.append("**").append(arg.getSqlType()).append("**");
      }
      if ( arg.getValue() == null ) {
        b.append(" is null");
      } else {
        b.append(" = ").append(arg.getValue());
      }
    }

    @Override
    public void setValues(PreparedStatement ps) throws SQLException {
      if ( logBindDetails ) {
        StringBuilder b = new StringBuilder();
        b.append(sql);
        for (int i = 0; i < argList.size(); ++i) {
          SqlParameterValue arg = argList.get(i);
          createLogContent(b, i+1, arg);
        }
        LogFactory.getLog(DatastoreImpl.class).info(b.toString());
      }
      for (int i = 0; i < argList.size(); ++i) {
        SqlParameterValue arg = argList.get(i);
        if ((arg.getSqlType() == java.sql.Types.LONGVARBINARY) ||
            (arg.getSqlType() == java.sql.Types.VARBINARY)) {
          if (arg.getValue() == null) {
            ps.setNull(i + 1, arg.getSqlType());
          } else {
            ps.setBytes(i + 1, (byte[]) arg.getValue());
          }
        } else if (arg.getSqlType() == java.sql.Types.TIMESTAMP) {
          // we actually bind an iso8601 string
          if ( arg.getValue() == null ) {
            // seems to require a type vs. Types.NULL
            ps.setNull(i + 1, java.sql.Types.NVARCHAR);
          } else {
            ps.setObject(i + 1, arg.getValue(), java.sql.Types.NVARCHAR);
          }
        } else if (arg.getSqlType() == java.sql.Types.DECIMAL) {
          // nulls don't go through as DECIMAL -- assert they are strings
          if ( arg.getValue() == null ) {
            // DECIMAL seems to require a type but doesn't like itself
            ps.setNull(i + 1, java.sql.Types.NVARCHAR);
          } else {
            ps.setObject(i + 1, arg.getValue(), arg.getSqlType());
          }
        } else if (arg.getValue() == null) {
          ps.setNull(i + 1, arg.getSqlType());
        } else {
          ps.setObject(i + 1, arg.getValue(), arg.getSqlType());
        }
      }
    }
  }

  @Override
  public void putEntity(CommonFieldsBase entity, User user) throws ODKEntityPersistException {
    dam.recordPutUsage(entity);
    try {
      boolean first;
      StringBuilder b = new StringBuilder();
      if (entity.isFromDatabase()) {
        // we need to do an update
        entity.setDateField(entity.lastUpdateDate, new Date());
        entity.setStringField(entity.lastUpdateUriUser, user.getUriUser());

        b.append(K_UPDATE);
        b.append(K_BQ);
        b.append(entity.getSchemaName());
        b.append(K_BQ);
        b.append(".");
        b.append(K_BQ);
        b.append(entity.getTableName());
        b.append(K_BQ);
        b.append(K_SET);

        ArrayList<SqlParameterValue> pv = new ArrayList<SqlParameterValue>();

        first = true;
        // fields...
        for (DataField f : entity.getFieldList()) {
          // primary key goes in the where clause...
          if (f == entity.primaryKey)
            continue;
          if (!first) {
            b.append(K_CS);
          }
          first = false;
          b.append(K_BQ);
          b.append(f.getName());
          b.append(K_BQ);

          b.append(K_EQ);
          b.append(K_BIND_VALUE);
          buildArgumentList(pv, entity, f);
        }
        b.append(K_WHERE);
        b.append(K_BQ);
        b.append(entity.primaryKey.getName());
        b.append(K_BQ);
        b.append(K_EQ);
        b.append(K_BIND_VALUE);
        buildArgumentList(pv, entity, entity.primaryKey);

        // update...
        String sql = b.toString();
        ReusableStatementSetter setter = new ReusableStatementSetter(sql, pv);
        getJdbcConnection().update(sql, setter);
      } else {
        // not yet in database -- insert
        b.append(K_INSERT_INTO);
        b.append(K_BQ);
        b.append(entity.getSchemaName());
        b.append(K_BQ);
        b.append(".");
        b.append(K_BQ);
        b.append(entity.getTableName());
        b.append(K_BQ);
        first = true;
        b.append(K_OPEN_PAREN);
        // fields...
        for (DataField f : entity.getFieldList()) {
          if (!first) {
            b.append(K_CS);
          }
          first = false;
          b.append(K_BQ);
          b.append(f.getName());
          b.append(K_BQ);
        }
        b.append(K_CLOSE_PAREN);
        b.append(K_VALUES);

        ArrayList<SqlParameterValue> pv = new ArrayList<SqlParameterValue>();

        first = true;
        b.append(K_OPEN_PAREN);
        // fields...
        for (DataField f : entity.getFieldList()) {
          if (!first) {
            b.append(K_CS);
          }
          first = false;
          b.append(K_BIND_VALUE);
          buildArgumentList(pv, entity, f);
        }
        b.append(K_CLOSE_PAREN);

        // insert...
        String sql = b.toString();
        ReusableStatementSetter setter = new ReusableStatementSetter(sql, pv);
        getJdbcConnection().update(sql, setter);
        entity.setFromDatabase(true); // now it is in the database...
      }
    } catch (Exception e) {
      throw new ODKEntityPersistException(e);
    }
  }

  @Override
  public void putEntities(Collection<? extends CommonFieldsBase> entityList, User user)
      throws ODKEntityPersistException {
    for (CommonFieldsBase d : entityList) {
      putEntity(d, user);
    }
  }

  private static final class BatchStatementFieldSetter implements BatchPreparedStatementSetter {

    final String sql;
    final List<List<SqlParameterValue> > batchArgs;
    ReusableStatementSetter setter = new ReusableStatementSetter();

    BatchStatementFieldSetter(String sql, List<List<SqlParameterValue> > batchArgs) {
      this.sql = sql;
      this.batchArgs = batchArgs;
    }

    @Override
    public int getBatchSize() {
      return batchArgs.size();
    }

    @Override
    public void setValues(PreparedStatement ps, int idx) throws SQLException {
      List<SqlParameterValue> argArray = batchArgs.get(idx);
      setter.setArgList(sql, argArray);
      setter.setValues(ps);
    }
  }

  @Override
  public void batchAlterData(List<? extends CommonFieldsBase> changes, User user)
      throws ODKEntityPersistException {
    if (changes.isEmpty()) {
      return;
    }

    // we need to be careful -- SqlServer only allows a small number of 
    // bind parameters on a request. This severely limits the batch size
    // that can be sent.
    CommonFieldsBase firstEntity = changes.get(0);
    int maxPerBatch = (MAX_BIND_PARAMS / firstEntity.getFieldList().size());
    for ( int idxStart = 0; idxStart < changes.size() ; idxStart += maxPerBatch ) {
      int idxAfterEnd = idxStart + maxPerBatch;
      if ( idxAfterEnd > changes.size() ) {
        idxAfterEnd = changes.size();
      }
      partialBatchAlterData(changes, idxStart, idxAfterEnd, user);
    }
    
  }
  
  private void partialBatchAlterData(List<? extends CommonFieldsBase> allChanges, 
      int idxStart, int idxAfterEnd, User user)
      throws ODKEntityPersistException {
    if (allChanges.isEmpty()) {
      return;
    }

    boolean generateSQL = true;
    String sql = null;
    List<List<SqlParameterValue> > batchArgs = new ArrayList<List<SqlParameterValue> >();
    StringBuilder b = new StringBuilder();

    for (int idx = idxStart ; idx < idxAfterEnd ; ++idx ) {
      CommonFieldsBase entity = allChanges.get(idx);
      dam.recordPutUsage(entity);

      boolean first;
      b.setLength(0);

      ArrayList<SqlParameterValue> pv = new ArrayList<SqlParameterValue>();

      if (entity.isFromDatabase()) {
        // we need to do an update
        entity.setDateField(entity.lastUpdateDate, new Date());
        entity.setStringField(entity.lastUpdateUriUser, user.getUriUser());

        if (generateSQL) {
          b.append(K_UPDATE);
          b.append(K_BQ);
          b.append(entity.getSchemaName());
          b.append(K_BQ);
          b.append(".");
          b.append(K_BQ);
          b.append(entity.getTableName());
          b.append(K_BQ);
          b.append(K_SET);
        }

        first = true;
        // fields...
        for (DataField f : entity.getFieldList()) {
          // primary key goes in the where clause...
          if (f == entity.primaryKey)
            continue;

          if (generateSQL) {
            if (!first) {
              b.append(K_CS);
            }
            first = false;
            b.append(K_BQ);
            b.append(f.getName());
            b.append(K_BQ);
            b.append(K_EQ);
            b.append(K_BIND_VALUE);
          }

          buildArgumentList(pv, entity, f);
        }
        if (generateSQL) {
          b.append(K_WHERE);
          b.append(K_BQ);
          b.append(entity.primaryKey.getName());
          b.append(K_BQ);
          b.append(K_EQ);
          b.append(K_BIND_VALUE);
        }
        buildArgumentList(pv, entity, entity.primaryKey);

      } else {
        if (generateSQL) {
          // not yet in database -- insert
          b.append(K_INSERT_INTO);
          b.append(K_BQ);
          b.append(entity.getSchemaName());
          b.append(K_BQ);
          b.append(".");
          b.append(K_BQ);
          b.append(entity.getTableName());
          b.append(K_BQ);
          first = true;
          b.append(K_OPEN_PAREN);
          // fields...
          for (DataField f : entity.getFieldList()) {
            if (!first) {
              b.append(K_CS);
            }
            first = false;
            b.append(K_BQ);
            b.append(f.getName());
            b.append(K_BQ);
          }
          b.append(K_CLOSE_PAREN);
          b.append(K_VALUES);
          b.append(K_OPEN_PAREN);
        }

        first = true;
        // fields...
        for (DataField f : entity.getFieldList()) {
          if (generateSQL) {
            if (!first) {
              b.append(K_CS);
            }
            first = false;
            b.append(K_BIND_VALUE);
          }
          buildArgumentList(pv, entity, f);
        }

        if (generateSQL) {
          b.append(K_CLOSE_PAREN);
        }
      }

      if (generateSQL) {
        b.append(K_COLON);
        sql = b.toString();
      }
      generateSQL = false;
      batchArgs.add(pv);
    }

    try {
      // update...
      BatchStatementFieldSetter setter = new BatchStatementFieldSetter(sql, batchArgs);
      getJdbcConnection().batchUpdate(sql, setter);

      // if this was an insert, set the fromDatabase flag in the entities
      if (!allChanges.get(0).isFromDatabase()) {
        for (int idx = idxStart ; idx < idxAfterEnd ; ++idx ) {
          CommonFieldsBase entity = allChanges.get(idx);
          entity.setFromDatabase(true);
        }
      }
    } catch (Exception e) {
      throw new ODKEntityPersistException(e);
    }
  }

  @Override
  public void deleteEntity(EntityKey key, User user) throws ODKDatastoreException {

    dam.recordDeleteUsage(key);
    try {
      CommonFieldsBase d = key.getRelation();

      StringBuilder b = new StringBuilder();
      b.append(K_DELETE_FROM);
      b.append(K_BQ);
      b.append(d.getSchemaName());
      b.append(K_BQ);
      b.append(".");
      b.append(K_BQ);
      b.append(d.getTableName());
      b.append(K_BQ);
      b.append(K_WHERE);
      b.append(K_BQ);
      b.append(d.primaryKey.getName());
      b.append(K_BQ);
      b.append(K_EQ);
      b.append(K_BIND_VALUE);

      LogFactory.getLog(DatastoreImpl.class).info("Executing " + b.toString() + " with key "
          + key.getKey() + " by user " + user.getUriUser());
      getJdbcConnection().update(b.toString(), new Object[] { key.getKey() });
    } catch (Exception e) {
      throw new ODKDatastoreException("delete failed", e);
    }
  }

  @Override
  public void deleteEntities(Collection<EntityKey> keys, User user) throws ODKDatastoreException {
    ODKDatastoreException e = null;
    for (EntityKey k : keys) {
      try {
        deleteEntity(k, user);
      } catch (ODKDatastoreException ex) {
        ex.printStackTrace();
        if (e == null) {
          e = ex; // save the first exception...
        }
      }
    }
    if (e != null)
      throw e; // throw the first exception...
  }

  @Override
  public TaskLock createTaskLock(User user) {
    return new TaskLockImpl(this, dam, user);
  }
}
