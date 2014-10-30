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
package org.opendatakit.common.persistence.engine.mysql;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.DataField.IndexType;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.engine.DatastoreAccessMetrics;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class DatastoreImpl implements Datastore, InitializingBean {

  private static final int MAX_COLUMN_NAME_LEN = 64;
  private static final int MAX_TABLE_NAME_LEN = 64;

  private final DatastoreAccessMetrics dam = new DatastoreAccessMetrics();
  private DataSource dataSource = null;

  private String schemaName = null;

  public DatastoreImpl() throws ODKDatastoreException {
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
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
      List<?> databaseNames = jdbcTemplate.queryForList("SELECT DATABASE()", String.class);
      schemaName = (String) databaseNames.get(0);
    }
  }

  public static final String K_CREATE_TABLE = "CREATE TABLE ";
  public static final String K_DROP_TABLE = "DROP TABLE ";
  public static final String K_SHOW_CREATE_TABLE = "SHOW CREATE TABLE ";

  public static final String K_OPEN_PAREN = " ( ";
  public static final String K_CLOSE_PAREN = " ) ";
  public static final String K_SELECT = "SELECT ";
  public static final String K_SELECT_DISTINCT = "SELECT DISTINCT ";
  public static final String K_CS = ", ";
  public static final String K_BQ = "`";
  public static final String K_COMMA = ", ";
  public static final String K_FROM = " FROM ";
  public static final String K_WHERE = " WHERE ";
  public static final String K_AND = " AND ";
  public static final String K_EQ = " = ";
  public static final String K_BIND_VALUE = "?";
  public static final String K_USING_HASH = " USING HASH ";
  public static final String K_INSERT_INTO = "INSERT INTO ";
  public static final String K_VALUES = " VALUES ";
  public static final String K_UPDATE = "UPDATE ";
  public static final String K_SET = " SET ";
  public static final String K_DELETE_FROM = "DELETE FROM ";

  public static final Integer DEFAULT_DBL_NUMERIC_SCALE = 10;
  public static final Integer DEFAULT_DBL_NUMERIC_PRECISION = 38;
  public static final Integer DEFAULT_INT_NUMERIC_PRECISION = 9;

  private static RowMapper<ShowDefinition> showDef = new RowMapper<ShowDefinition>() {
    @Override
    public ShowDefinition mapRow(ResultSet rs, int rowNum) throws SQLException {
      return new ShowDefinition(rs);
    }
  };

  private static final class ShowDefinition {

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

    final private String columnName;
    final private boolean isNullable;
    final private Long maxCharLen;
    final private Integer numericScale;
    final private Integer numericPrecision;
    private DataField.DataType dataType;

    private static final String K_SHOW = "SHOW COLUMNS FROM ";
    private static final int IDX_COLUMN_NAME = 1;
    private static final int IDX_COLUMN_TYPE = 2;
    private static final int IDX_IS_NULLABLE = 3;
    private static final String K_VARCHAR = "varchar";
    private static final String K_BINARY = "binary";
    private static final String K_DECIMAL = "decimal";
    private static final String K_INT = "int";
    private static final String K_CHAR = "char";
    private static final String K_DATE = "date";
    private static final String K_TIME = "time";
    private static final String K_BLOB = "blob";
    private static final String K_TEXT = "text";
    private static final String K_TINY = "tiny";
    private static final String K_MEDIUM = "medium";
    private static final String K_LONG = "long";
    private static final Long MAX_ROW_SIZE = 65000L;

    private static final Map<String, ShowDefinition> query(String schemaName, String tableName,
        JdbcTemplate db, DatastoreAccessMetrics dam) {
      StringBuilder b = new StringBuilder();
      b.append(K_SHOW);
      b.append(K_BQ);
      b.append(schemaName);
      b.append(K_BQ);
      b.append(".");
      b.append(K_BQ);
      b.append(tableName);
      b.append(K_BQ);

      Map<String, ShowDefinition> defs = new HashMap<String, ShowDefinition>();
      try {
        List<?> columns;
        columns = db.query(b.toString(), showDef);
        dam.recordQueryUsage("SHOW COLUMNS", columns.size());

        for (Object o : columns) {
          ShowDefinition sd = (ShowDefinition) o;
          defs.put(sd.getColumnName(), sd);
        }
      } catch (BadSqlGrammarException e) {
        // we expect this if the table doesn't exist...
      }
      return defs;
    }

    ShowDefinition(ResultSet rs) throws SQLException {
      this.columnName = rs.getString(IDX_COLUMN_NAME);
      this.isNullable = rs.getBoolean(IDX_IS_NULLABLE);

      String dataType = null;
      Integer firstTerm = null;
      Integer secondTerm = null;
      {
        String columnType = rs.getString(IDX_COLUMN_TYPE);
        dataType = columnType;

        int idx = columnType.indexOf("(");
        if (idx != -1) {
          dataType = columnType.substring(0, idx);
          String parenTerm = columnType.substring(idx + 1, columnType.length() - 1);
          idx = parenTerm.indexOf(",");
          if (idx != -1) {
            String part = parenTerm.substring(0, idx);
            if (part.length() != 0) {
              firstTerm = Integer.valueOf(part);
            }
            part = parenTerm.substring(idx + 1);
            if (part.length() != 0) {
              secondTerm = Integer.valueOf(part);
            }
          } else if (parenTerm.length() != 0) {
            firstTerm = Integer.valueOf(parenTerm);
          }
        }
      }

      if (dataType.contains(K_VARCHAR) || dataType.contains(K_CHAR)) {
        this.maxCharLen = Long.valueOf(firstTerm);
        this.dataType = DataField.DataType.STRING;
        this.numericPrecision = null;
        this.numericScale = null;
      } else if (dataType.contains(K_DECIMAL)) {
        if (secondTerm.equals(0)) {
          this.dataType = DataField.DataType.INTEGER;
          this.maxCharLen = null;
          this.numericPrecision = firstTerm;
          this.numericScale = null;
        } else {
          this.dataType = DataField.DataType.DECIMAL;
          this.maxCharLen = null;
          this.numericPrecision = firstTerm;
          this.numericScale = secondTerm;
        }
      } else if (dataType.contains(K_INT)) {
        this.dataType = DataField.DataType.INTEGER;
        this.maxCharLen = null;
        this.numericPrecision = firstTerm;
        this.numericScale = null;
      } else {
        this.numericPrecision = null;
        this.numericScale = null;

        if (dataType.contains(K_DATE) || dataType.contains(K_TIME)) {
          this.maxCharLen = null;
          this.dataType = DataField.DataType.DATETIME;
        } else if (dataType.contains(K_BINARY)) {
          this.maxCharLen = Long.valueOf(firstTerm);
          this.dataType = DataField.DataType.BINARY;
        } else if (dataType.contains(K_BLOB)) {
          this.dataType = DataField.DataType.BINARY;
          if (dataType.contains(K_TINY)) {
            this.maxCharLen = 255L;
          } else if (dataType.contains(K_MEDIUM)) {
            this.maxCharLen = 16777215L;
          } else if (dataType.contains(K_LONG)) {
            this.maxCharLen = 4294967295L;
          } else {
            this.maxCharLen = 65535L;
          }
        } else if (dataType.contains(K_TEXT)) {
          this.dataType = DataField.DataType.LONG_STRING;
          if (dataType.contains(K_TINY)) {
            this.maxCharLen = 255L;
          } else if (dataType.contains(K_MEDIUM)) {
            this.maxCharLen = 16777215L;
          } else if (dataType.contains(K_LONG)) {
            this.maxCharLen = 4294967295L;
          } else {
            this.maxCharLen = 65535L;
          }
        } else {
          throw new IllegalStateException("unexpected dataType: " + dataType);
        }
      }

      if (this.dataType == DataField.DataType.STRING && this.maxCharLen.compareTo(MAX_ROW_SIZE) > 0) {
        this.dataType = DataField.DataType.LONG_STRING;
      }
    }
  }

  public static void buildArgumentList(Object[] ol, int[] il, int idx, CommonFieldsBase entity,
      DataField f) {
    switch (f.getDataType()) {
    case BOOLEAN:
      ol[idx] = entity.getBooleanField(f);
      il[idx] = java.sql.Types.BOOLEAN;
      break;
    case STRING:
    case URI:
      ol[idx] = entity.getStringField(f);
      il[idx] = java.sql.Types.VARCHAR;
      break;
    case INTEGER:
      ol[idx] = entity.getLongField(f);
      il[idx] = java.sql.Types.BIGINT;
      break;
    case DECIMAL:
      ol[idx] = entity.getNumericField(f);
      il[idx] = java.sql.Types.DECIMAL;
      break;
    case DATETIME:
      ol[idx] = entity.getDateField(f);
      il[idx] = java.sql.Types.TIMESTAMP;
      break;
    case BINARY:
      ol[idx] = entity.getBlobField(f);
      il[idx] = java.sql.Types.LONGVARBINARY;
      break;
    case LONG_STRING:
      ol[idx] = entity.getStringField(f);
      il[idx] = java.sql.Types.LONGVARCHAR;
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

  private final boolean updateRelation(CommonFieldsBase relation, String originalStatement) {

    Map<String, ShowDefinition> defns = ShowDefinition.query(relation.getSchemaName(),
        relation.getTableName(), getJdbcConnection(), dam);

    if (defns.size() > 0) {

      // we may have gotten results into columns -- go through the
      // fields and
      // assemble the results... we don't care about additional columns in
      // the map...
      for (DataField f : relation.getFieldList()) {
        ShowDefinition d = defns.get(f.getName());
        if (d == null) {
          StringBuilder b = new StringBuilder();
          if ( originalStatement == null ) {
            b.append(" Retrieving expected definition (");
              boolean first = true;
              for (DataField field : relation.getFieldList()) {
                if ( !first ) {
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
          throw new IllegalStateException("did not find expected column " + f.getName()
              + " in table " + relation.getSchemaName() + "." + relation.getTableName() +
              b.toString());
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
            throw new IllegalStateException("column " + f.getName() + " in table "
                + relation.getSchemaName() + "." + relation.getTableName()
                + " stores URIs but is not a string field");
          }
          d.setDataType(DataField.DataType.URI);
        }

        if (d.getDataType() != f.getDataType()) {
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
    try {
      LogFactory.getLog(DatastoreImpl.class).info("before updateRelation: " + relation.getTableName());
      // see if relation already is defined and update it with dimensions...
      if (updateRelation(relation, null)) {
        // it exists -- we're done!
        return;
      } else {
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
          firstTime = false;
          b.append(K_BQ);
          b.append(f.getName());
          b.append(K_BQ);
          DataField.DataType type = f.getDataType();
          switch (type) {
          case BINARY:
            b.append(" LONGBLOB");
            break;
          case LONG_STRING:
            b.append(" LONGTEXT CHARACTER SET utf8");
            break;
          case STRING:
            b.append(" VARCHAR(");
            Long len = f.getMaxCharLen();
            if (len == null) {
              len = PersistConsts.DEFAULT_MAX_STRING_LENGTH;
            }
            b.append(len.toString());
            b.append(K_CLOSE_PAREN);
            b.append(" CHARACTER SET utf8");
            break;
          case BOOLEAN:
            b.append(" CHAR(1) CHARACTER SET utf8");
            break;
          case INTEGER:
            Integer int_digits = f.getNumericPrecision();
            if (int_digits == null) {
              int_digits = DEFAULT_INT_NUMERIC_PRECISION;
            }

            if (int_digits.compareTo(10) > 0) {
              b.append(" BIGINT(");
              b.append(int_digits.toString());
              b.append(K_CLOSE_PAREN);
            } else {
              b.append(" INTEGER(");
              b.append(int_digits.toString());
              b.append(K_CLOSE_PAREN);
            }
            break;
          case DECIMAL:
            Integer dbl_digits = f.getNumericPrecision();
            Integer dbl_fract = f.getNumericScale();
            if (dbl_digits == null) {
              dbl_digits = DEFAULT_DBL_NUMERIC_PRECISION;
            }
            if (dbl_fract == null) {
              dbl_fract = DEFAULT_DBL_NUMERIC_SCALE;
            }
            b.append(" DECIMAL(");
            b.append(dbl_digits.toString());
            b.append(K_CS);
            b.append(dbl_fract.toString());
            b.append(K_CLOSE_PAREN);
            break;
          case DATETIME:
            b.append(" DATETIME");
            break;
          case URI:
            b.append(" VARCHAR(");
            len = f.getMaxCharLen();
            if (len == null) {
              len = PersistConsts.URI_STRING_LEN;
            }
            b.append(len.toString());
            b.append(") CHARACTER SET utf8");
            break;
          }

          if (f.getNullable()) {
            b.append(" NULL ");
          } else {
            b.append(" NOT NULL ");
          }
        }
        /*
         * Setting the primary key as the _URI and making it use
         * HASH.
         */
        
        // For MySQL, it is important to NOT declare a PRIMARY KEY
        // when that key is a UUID. It should only be declared as 
        // a non-unique index...
        //
        // http://kccoder.com/mysql/uuid-vs-int-insert-performance/
        //
        b.append(", INDEX(");
        b.append(K_BQ);
        b.append(relation.primaryKey.getName());
        b.append(K_BQ);
        b.append(K_CLOSE_PAREN);
        b.append(K_USING_HASH);
        // create other indicies
        for (DataField f : relation.getFieldList()) {
          if ((f.getIndexable() != IndexType.NONE) && (f != relation.primaryKey)) {
            b.append(", INDEX(");
            b.append(K_BQ);
            b.append(f.getName());
            b.append(K_BQ);
            b.append(K_CLOSE_PAREN);
            if (f.getIndexable() == IndexType.HASH) {
              b.append(K_USING_HASH);
            }
          }
        }
        b.append(K_CLOSE_PAREN);

        String createTableStmt = b.toString();
        LogFactory.getLog(DatastoreImpl.class).info("Attempting: " + createTableStmt);
        getJdbcConnection().execute(createTableStmt);
        LogFactory.getLog(DatastoreImpl.class).info("create table success (before updateRelation): " + relation.getTableName());

        // and update the relation with actual dimensions...
        updateRelation(relation, createTableStmt);
      }
    } catch (Exception e) {
      LogFactory.getLog(DatastoreImpl.class).warn("Failure: " + relation.getTableName() + " exception: " + e.toString());
      throw new ODKDatastoreException(e);
    }
  }

  @Override
  public boolean hasRelation(String schema, String tableName, User user) {
    // Query for the create table string.
    try {
      StringBuilder b = new StringBuilder();
      b.setLength(0);
      b.append(K_SHOW_CREATE_TABLE);
      b.append(K_BQ);
      b.append(schema);
      b.append(K_BQ);
      b.append(".");
      b.append(K_BQ);
      b.append(tableName);
      b.append(K_BQ);
      // this will throw an exception if the table doesn't exist...
      List<Map<String, Object>> l = getJdbcConnection().queryForList(b.toString());
      dam.recordQueryUsage("SHOW CREATE TABLE", l.size());
      // and if it does exist, we don't care about the return value...
    } catch (BadSqlGrammarException e) {
      dam.recordQueryUsage("SHOW CREATE TABLE", 0);
      // we expect this if the table does not exist...
      LogFactory.getLog(DatastoreImpl.class).info(tableName + " does not exist!");
      return false;
    }
    LogFactory.getLog(DatastoreImpl.class).info(tableName + " exists!");
    return true;
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

      LogFactory.getLog(DatastoreImpl.class).info(
          "Executing " + b.toString() + " by user " + user.getUriUser());
      getJdbcConnection().execute(b.toString());
    } catch (Exception e) {
      LogFactory.getLog(DatastoreImpl.class).warn(relation.getTableName() + " exception: " + e.toString());
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

        int idx = 0;
        Object[] ol = new Object[entity.getFieldList().size()];
        int[] il = new int[entity.getFieldList().size()];

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

          buildArgumentList(ol, il, idx, entity, f);
          ++idx;
        }
        b.append(K_WHERE);
        b.append(K_BQ);
        b.append(entity.primaryKey.getName());
        b.append(K_BQ);
        b.append(K_EQ);
        b.append(K_BIND_VALUE);
        buildArgumentList(ol, il, idx, entity, entity.primaryKey);

        // update...
        getJdbcConnection().update(b.toString(), ol, il);
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

        int idx = 0;
        Object[] ol = new Object[entity.getFieldList().size()];
        int[] il = new int[entity.getFieldList().size()];

        first = true;
        b.append(K_OPEN_PAREN);
        // fields...
        for (DataField f : entity.getFieldList()) {
          if (!first) {
            b.append(K_CS);
          }
          first = false;
          b.append(K_BIND_VALUE);

          buildArgumentList(ol, il, idx, entity, f);
          ++idx;
        }
        b.append(K_CLOSE_PAREN);

        // insert...
        getJdbcConnection().update(b.toString(), ol, il);
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

      LogFactory.getLog(DatastoreImpl.class).info(
          "Executing " + b.toString() + " with key " + key.getKey() + " by user "
              + user.getUriUser());
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
