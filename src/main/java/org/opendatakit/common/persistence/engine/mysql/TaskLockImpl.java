/*
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine.mysql;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.DataField;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.ITaskLockType;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.engine.DatastoreAccessMetrics;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ConnectionCallback;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class TaskLockImpl implements TaskLock {

  private static final String PERSISTENCE_LAYER_PROBLEM = "Persistence layer failure";
  
  final DatastoreAccessMetrics dam;
  final DatastoreImpl datastore;
  final User user;

  TaskLockImpl(DatastoreImpl datastore, DatastoreAccessMetrics dam, User user) {
    this.datastore = datastore;
    this.dam = dam;
    this.user = user;
  }

  private static final String K_BQ = "`";

  private TaskLockTable doTransaction(TaskLockTable entity, long l)
      throws ODKEntityNotFoundException, ODKTaskLockException {
    boolean first;

    final List<String> stmts = new ArrayList<String>();

    final String uri = entity.getUri();
    StringBuilder b = new StringBuilder();
    String tableName = K_BQ + datastore.getDefaultSchemaName() + K_BQ + "." + K_BQ
        + TaskLockTable.TABLE_NAME + K_BQ;
    // String tableName= TaskLockTable.TABLE_NAME;

    b.append("SET SESSION TRANSACTION ISOLATION LEVEL SERIALIZABLE");
    stmts.add(b.toString());
    b.setLength(0);
    b.append("START TRANSACTION WITH CONSISTENT SNAPSHOT");
    stmts.add(b.toString());
    b.setLength(0);
    b.append("SET @present := NOW(),");
    b.append(" @uriUser := '");
    b.append(user.getUriUser().replaceAll("'", "''"));
    b.append("',");
    b.append(" @uriLock := '");
    b.append(uri.replaceAll("'", "''"));
    b.append("',");
    b.append(" @formId := '");
    b.append(entity.getFormId().replaceAll("'", "''"));
    b.append("',");
    b.append(" @taskType := '");
    b.append(entity.getTaskType().replaceAll("'", "''"));
    b.append("',");
    b.append(" @lifetimeMicroseconds := ");
    b.append(1000L * l);
    stmts.add(b.toString());
    b.setLength(0);
    b.append("LOCK TABLES ");
    b.append(tableName);
    b.append(" WRITE ");
    stmts.add(b.toString());
    b.setLength(0);

    dam.recordPutUsage(TaskLockTable.TABLE_NAME);
    if (!entity.isFromDatabase()) {
      // insert a new record
      b.append("REPLACE INTO ");
      b.append(tableName);
      b.append(" (");
      first = true;
      for (DataField f : entity.getFieldList()) {
        if (!first) {
          b.append(",");
        }
        first = false;
        b.append(K_BQ);
        b.append(f.getName());
        b.append(K_BQ);
      }
      first = true;
      b.append(") VALUES ( ");
      for (DataField f : entity.getFieldList()) {
        if (!first) {
          b.append(",");
        }
        first = false;
        if (f.equals(entity.creationDate) || f.equals(entity.lastUpdateDate)) {
          b.append("@present");
        } else if (f.equals(entity.creatorUriUser) || f.equals(entity.lastUpdateUriUser)) {
          b.append("@uriUser");
        } else if (f.equals(entity.formId)) {
          b.append("@formId");
        } else if (f.equals(entity.taskType)) {
          b.append("@taskType");
        } else if (f.equals(entity.primaryKey)) {
          b.append("@uriLock");
        } else if (f.equals(entity.expirationDateTime)) {
          b.append(" DATE_ADD(CAST(@present AS DATETIME), INTERVAL @lifetimeMicroseconds MICROSECOND)");
        } else {
          throw new IllegalStateException("unexpected case " + f.getName());
        }
      }
      b.append(")");
      stmts.add(b.toString());
      b.setLength(0);
    } else {
      // update existing record
      b.append("UPDATE ");
      b.append(tableName);
      b.append(" SET ");
      first = true;
      for (DataField f : entity.getFieldList()) {
        if (f == entity.primaryKey)
          continue;
        if (!first) {
          b.append(",");
        }
        first = false;
        b.append(K_BQ);
        b.append(f.getName());
        b.append(K_BQ);
        b.append(" = ");
        if (f.equals(entity.creationDate) || f.equals(entity.lastUpdateDate)) {
          b.append("@present");
        } else if (f.equals(entity.creatorUriUser) || f.equals(entity.lastUpdateUriUser)) {
          b.append("@uriUser");
        } else if (f.equals(entity.formId)) {
          b.append("@formId");
        } else if (f.equals(entity.taskType)) {
          b.append("@taskType");
        } else if (f.equals(entity.primaryKey)) {
          b.append("@uriLock");
        } else if (f.equals(entity.expirationDateTime)) {
          b.append(" DATE_ADD(CAST(@present AS DATETIME), INTERVAL @lifetimeMicroseconds MICROSECOND)");
        } else {
          throw new IllegalStateException("unexpected case " + f.getName());
        }
      }
      b.append(" WHERE ");
      b.append(K_BQ);
      b.append(entity.primaryKey.getName());
      b.append(K_BQ);
      b.append(" = ");
      b.append("@uriLock");
      stmts.add(b.toString());
      b.setLength(0);
    }
    // delete stale locks (don't care who's)
    dam.recordDeleteUsage(TaskLockTable.TABLE_NAME);
    b.append("DELETE FROM ");
    b.append(tableName);
    b.append(" WHERE ");
    b.append(K_BQ);
    b.append(entity.expirationDateTime.getName());
    b.append(K_BQ);
    b.append(" <= CAST(@present AS DATETIME)");
    stmts.add(b.toString());
    b.setLength(0);
    // determine the time of the oldest lock for this resource and task type...
    // do this by querying for the minimum expiration time
    // BUT, first, set the minimum time to the present time in case there are no locks.
    b.append("SET @minExpiration = @present");
    stmts.add(b.toString());
    b.setLength(0);
    dam.recordQueryUsage(TaskLockTable.TABLE_NAME, 1);
    b.append("SELECT @minExpiration := MIN(");
    b.append(K_BQ);
    b.append(entity.expirationDateTime.getName());
    b.append(K_BQ);
    b.append(") FROM ");
    b.append(tableName);
    b.append(" WHERE ");
    b.append(K_BQ);
    b.append(entity.formId.getName());
    b.append(K_BQ);
    b.append(" = @formId AND ");
    b.append(K_BQ);
    b.append(entity.taskType.getName());
    b.append(K_BQ);
    b.append(" = @taskType");
    stmts.add(b.toString());
    b.setLength(0);
    // determine if there are two or more records matching
    // the minimum expiration time.  If there are, we must
    // release the locks and retry, as we cannot determine
    // which one is first.
    b.append("SET @uriCount = 0");
    stmts.add(b.toString());
    b.setLength(0);
    b.append("SELECT @uriCount := COUNT(");
    b.append(K_BQ);
    b.append(entity.primaryKey.getName());
    b.append(K_BQ);
    b.append(") FROM ");
    b.append(tableName);
    b.append(" WHERE ");

    b.append(K_BQ);
    b.append(entity.formId.getName());
    b.append(K_BQ);
    b.append(" = @formId AND ");

    b.append(K_BQ);
    b.append(entity.taskType.getName());
    b.append(K_BQ);
    b.append(" = @taskType AND ");

    b.append(K_BQ);
    b.append(entity.expirationDateTime.getName());
    b.append(K_BQ);
    b.append(" = CAST(@minExpiration AS DATETIME)");
    stmts.add(b.toString());
    b.setLength(0);
    // delete all locks except the oldest one for this resource and task type...
    // or, if we have two or more old locks, release ours
    // whatever lock exists identifies the owner of the resource.
    dam.recordDeleteUsage(TaskLockTable.TABLE_NAME);
    b.append("DELETE FROM ");
    b.append(tableName);
    b.append(" WHERE ");

    b.append(K_BQ);
    b.append(entity.formId.getName());
    b.append(K_BQ);
    b.append(" = @formId AND ");

    b.append(K_BQ);
    b.append(entity.taskType.getName());
    b.append(K_BQ);
    b.append(" = @taskType AND ( ");

    b.append(K_BQ);
    b.append(entity.expirationDateTime.getName());
    b.append(K_BQ);
    b.append(" > CAST(@minExpiration AS DATETIME)");
    b.append(" OR ( @uriCount > 1 AND ");
    b.append(K_BQ);
    b.append(entity.expirationDateTime.getName());
    b.append(K_BQ);
    b.append(" = CAST(@minExpiration AS DATETIME)");
    b.append(" AND ");
    b.append(K_BQ);
    b.append(entity.primaryKey.getName());
    b.append(K_BQ);
    b.append(" = @uriLock ) )");
    stmts.add(b.toString());
    b.setLength(0);
    // and within the transaction, see if the primary key of the winning lock is ours...
    b.append("SELECT ");
    b.append(K_BQ);
    b.append(entity.primaryKey.getName());
    b.append(K_BQ);
    b.append(" FROM ");
    b.append(tableName);
    b.append(" WHERE ");
    b.append(K_BQ);
    b.append(entity.formId.getName());
    b.append(K_BQ);
    b.append(" = @formId AND ");

    b.append(K_BQ);
    b.append(entity.taskType.getName());
    b.append(K_BQ);
    b.append(" = @taskType AND ");
    b.append(K_BQ);
    b.append(entity.expirationDateTime.getName());
    b.append(K_BQ);
    b.append(" = CAST(@minExpiration AS DATETIME)");
    stmts.add(b.toString());
    b.setLength(0);
    b.append("UNLOCK TABLES");
    stmts.add(b.toString());
    b.setLength(0);
    b.append("COMMIT");
    stmts.add(b.toString());
    b.setLength(0);
    
    TaskLockTable relation;
    try {
      relation = TaskLockTable.assertRelation(datastore, user);
    } catch (ODKDatastoreException e) {
      throw new ODKTaskLockException(PERSISTENCE_LAYER_PROBLEM, e);
    }
  
    boolean success = false;
    try {

      JdbcTemplate jdbc = datastore.getJdbcConnection();
      Object o = jdbc.execute(new ConnectionCallback<Object>() {

        @Override
        public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
          boolean success = false;
          boolean oldValue = conn.getAutoCommit();
          try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            String lastResult = null;
            for (String s : stmts) {
              Statement stmt = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.CLOSE_CURSORS_AT_COMMIT);
              if ( s.startsWith("SELECT") ) {
                ResultSet rs = stmt.executeQuery(s);
                if ( rs.first() ) {
                  lastResult = rs.getString(1);
                }
                rs.close();
              } else {
                stmt.executeUpdate(s);
              }
              stmt.close();
            }
            conn.commit();
            success = uri.equals(lastResult);
          } catch (Exception e) {
            e.printStackTrace();
            conn.rollback();
          } finally {
            if ( !success ) {
              Statement stmt = conn.createStatement();
              LogFactory.getLog(TaskLockImpl.class).info("UNLOCK TABLES");
              stmt.execute("UNLOCK TABLES");
              conn.commit();
            }
          }
          conn.setAutoCommit(oldValue);
          return success ? uri : null;
        }

      });
      
      success = o != null && uri.equals((String) o);

    } catch (Exception e) {
      throw new ODKTaskLockException(PERSISTENCE_LAYER_PROBLEM, e);
    }
    if ( success ) {
      return (TaskLockTable) datastore.getEntity(relation, uri, user);
    } else {
      throw new ODKEntityNotFoundException();
    }
  }

  @Override
  public boolean obtainLock(String lockId, String formId, ITaskLockType taskType) {
    boolean result = false;
    try {
      TaskLockTable relation = TaskLockTable.assertRelation(datastore, user);
      TaskLockTable entity = datastore.createEntityUsingRelation(relation, user);
      entity.setStringField(entity.primaryKey, lockId);
      entity.setFormId(formId);
      entity.setTaskType(taskType.getName());
      entity = doTransaction(entity, taskType.getLockExpirationTimeout());
      result = true;
    } catch (ODKEntityNotFoundException e) {
      // didn't gain the lock...
    } catch (ODKTaskLockException e) {
      // unexpected failure...
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      // unexpected failure...
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public boolean renewLock(String lockId, String formId, ITaskLockType taskType) {
    boolean result = false;
    try {
      TaskLockTable relation = TaskLockTable.assertRelation(datastore, user);
      TaskLockTable entity = datastore.getEntity(relation, lockId, user);
      if (!(entity.getFormId().equals(formId) && entity.getTaskType().equals(taskType.getName()))) {
        throw new IllegalArgumentException("formId or taskType don't match datastore values");
      }
      entity = doTransaction(entity, taskType.getLockExpirationTimeout());
      result = true;
    } catch (IllegalArgumentException e) {
      // unexpected failure...
      e.printStackTrace();
    } catch (ODKEntityNotFoundException e) {
      // unexpected failure...
      e.printStackTrace();
    } catch (ODKDatastoreException e) {
      // unexpected failure...
      e.printStackTrace();
    } catch (ODKTaskLockException e) {
      // unexpected failure...
      e.printStackTrace();
    }
    return result;
  }

  @Override
  public boolean releaseLock(String lockId, String formId, ITaskLockType taskType) {
    boolean result = false;
    try {
      TaskLockTable relation = TaskLockTable.assertRelation(datastore, user);
      // we don't have the record that we want to delete; construct
      // the entity key from the relation and the URI for the record.
      datastore.deleteEntity(new EntityKey(relation, lockId), user);
      result = true;
    } catch (ODKDatastoreException e) {
      // if we see a lot of these, we are running too long between renewals
      LogFactory.getLog(TaskLockImpl.class).info("delete of taskLock threw exception!");
      e.printStackTrace();
    }
    return result;
  }

  private static class TaskLockTable extends CommonFieldsBase {
    static final String TABLE_NAME = "_task_lock";

    private static final DataField FORM_ID = new DataField("FORM_ID", DataField.DataType.STRING,
        false, 4096L);
    private static final DataField TASK_TYPE = new DataField("TASK_TYPE",
        DataField.DataType.STRING, false, 80L);
    private static final DataField EXPIRATION_DATETIME = new DataField("EXPIRATION_DATETIME",
        DataField.DataType.DATETIME, true);

    DataField formId;
    DataField taskType;
    DataField expirationDateTime;

    TaskLockTable(String schema) {
      super(schema, TABLE_NAME);
      fieldList.add(formId = new DataField(FORM_ID));
      fieldList.add(taskType = new DataField(TASK_TYPE));
      fieldList.add(expirationDateTime = new DataField(EXPIRATION_DATETIME));
    }

    TaskLockTable(TaskLockTable ref, User user) {
      super(ref, user);
      formId = ref.formId;
      taskType = ref.taskType;
      expirationDateTime = ref.expirationDateTime;
    }

    String getFormId() {
      return getStringField(formId);
    }

    void setFormId(String value) {
      if (!setStringField(formId, value)) {
        throw new IllegalStateException("overflow formId");
      }
    }

    String getTaskType() {
      return getStringField(taskType);
    }

    void setTaskType(String value) {
      if (!setStringField(taskType, value)) {
        throw new IllegalStateException("overflow taskType");
      }
    }

    @SuppressWarnings("unused")
    Date getExpirationDateTime() {
      return getDateField(expirationDateTime);
    }

    @SuppressWarnings("unused")
    void setExpirationDateTime(Date value) {
      setDateField(expirationDateTime, value);
    }

    @Override
    public CommonFieldsBase getEmptyRow(User user) {
      return new TaskLockTable(this, user);
    }

    static TaskLockTable relation = null;

    static synchronized final TaskLockTable assertRelation(Datastore datastore, User user)
        throws ODKDatastoreException {
      if (relation == null) {
        TaskLockTable relationPrototype;
        relationPrototype = new TaskLockTable(datastore.getDefaultSchemaName());
        datastore.assertRelation(relationPrototype, user);
        relation = relationPrototype;
      }
      return relation;
    }
  }
}
