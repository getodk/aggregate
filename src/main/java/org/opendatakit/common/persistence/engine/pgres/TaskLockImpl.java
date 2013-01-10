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
package org.opendatakit.common.persistence.engine.pgres;

import java.sql.Connection;
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

  private static final String K_BQ = "\"";

  private TaskLockTable doTransaction(TaskLockTable entity, long l)
      throws ODKEntityNotFoundException, ODKTaskLockException {
    boolean first;

    final List<String> stmts = new ArrayList<String>();
    
    String uri = entity.getUri();
    
    StringBuilder b = new StringBuilder();
    String tableName = K_BQ + datastore.getDefaultSchemaName() + K_BQ + "." + K_BQ
        + TaskLockTable.TABLE_NAME + K_BQ;

    b.append("'").append(user.getUriUser().replaceAll("'", "''")).append("'");
    String uriUserInline = b.toString();
    b.setLength(0);
    b.append("'").append(uri.replaceAll("'", "''")).append("'");
    String uriLockInline = b.toString();
    b.setLength(0);
    b.append("'").append(entity.getFormId().replaceAll("'", "''")).append("'");
    String formIdInline = b.toString();
    b.setLength(0);
    b.append("'").append(entity.getTaskType().replaceAll("'", "''")).append("'");
    String taskTypeInline = b.toString();
    b.setLength(0);
    b.append("interval '").append(l).append(" milliseconds'");
    String lifetimeIntervalMilliseconds = b.toString();
    b.setLength(0);

    b.append("LOCK TABLE ").append(tableName).append(" IN ACCESS EXCLUSIVE MODE");
    stmts.add(b.toString());
    b.setLength(0);

    dam.recordPutUsage(TaskLockTable.TABLE_NAME);
    if (!entity.isFromDatabase()) {
      // insert a new record (prospective lock)
      b.append("INSERT INTO ");
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
          b.append("NOW()");
        } else if (f.equals(entity.creatorUriUser) || f.equals(entity.lastUpdateUriUser)) {
          b.append(uriUserInline);
        } else if (f.equals(entity.formId)) {
          b.append(formIdInline);
        } else if (f.equals(entity.taskType)) {
          b.append(taskTypeInline);
        } else if (f.equals(entity.primaryKey)) {
          b.append(uriLockInline);
        } else if (f.equals(entity.expirationDateTime)) {
          b.append(" NOW() + ");
          b.append(lifetimeIntervalMilliseconds);
        } else {
          throw new IllegalStateException("unexpected case " + f.getName());
        }
      }
      b.append(")");
      stmts.add(b.toString());
      b.setLength(0);
    } else {
      // update existing record (prospective lock)
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
          b.append("NOW()");
        } else if (f.equals(entity.creatorUriUser) || f.equals(entity.lastUpdateUriUser)) {
          b.append(uriUserInline);
        } else if (f.equals(entity.formId)) {
          b.append(formIdInline);
        } else if (f.equals(entity.taskType)) {
          b.append(taskTypeInline);
        } else if (f.equals(entity.primaryKey)) {
          b.append(uriLockInline);
        } else if (f.equals(entity.expirationDateTime)) {
          b.append(" NOW() + ");
          b.append(lifetimeIntervalMilliseconds);
        } else {
          throw new IllegalStateException("unexpected case " + f.getName());
        }
      }
      b.append(" WHERE ");
      b.append(K_BQ);
      b.append(entity.primaryKey.getName());
      b.append(K_BQ);
      b.append(" = ");
      b.append(uriLockInline);
      stmts.add(b.toString());
      b.setLength(0);
    }
    // delete stale locks (don't care who's)
    dam.recordDeleteUsage(TaskLockTable.TABLE_NAME);
    b.append("DELETE FROM ").append(tableName).append(" WHERE ");
    b.append(K_BQ).append(entity.expirationDateTime.getName()).append(K_BQ).append(" <= NOW()");
    stmts.add(b.toString());
    b.setLength(0);
    // delete prospective locks which are not the oldest for that resource and
    // task type
    dam.recordDeleteUsage(TaskLockTable.TABLE_NAME);
    b.append("DELETE FROM ").append(tableName).append(" WHERE ");
    b.append(K_BQ).append(entity.formId.getName()).append(K_BQ).append(" = ")
        .append(formIdInline).append(" AND ");
    b.append(K_BQ).append(entity.taskType.getName()).append(K_BQ).append(" = ")
        .append(taskTypeInline).append(" AND ");
    b.append(K_BQ).append(entity.expirationDateTime.getName()).append(K_BQ);
    b.append(" > (SELECT MIN(t3.").append(K_BQ).append(entity.expirationDateTime.getName())
        .append(K_BQ);
    b.append(") FROM ").append(tableName).append(" AS t3 WHERE t3.");
    b.append(K_BQ).append(entity.formId.getName()).append(K_BQ).append(" = ").append(formIdInline)
        .append(" AND t3.");
    b.append(K_BQ).append(entity.taskType.getName()).append(K_BQ).append(" = ")
        .append(taskTypeInline).append(")");
    stmts.add(b.toString());
    b.setLength(0);
    // delete our entry if it collides with another entry with exactly 
    // this time.
    b.append("DELETE FROM ").append(tableName).append(" WHERE ");
    b.append(K_BQ).append(entity.formId.getName()).append(K_BQ).append(" = ")
        .append(formIdInline).append(" AND ");
    b.append(K_BQ).append(entity.taskType.getName()).append(K_BQ).append(" = ")
        .append(taskTypeInline).append(" AND ");
    b.append(K_BQ).append(entity.primaryKey.getName()).append(K_BQ).append(" = ")
        .append(uriLockInline).append(" AND ");
    b.append("1 < (SELECT COUNT(t3.").append(K_BQ).append(entity.expirationDateTime.getName())
        .append(K_BQ);
    b.append(") FROM ").append(tableName).append(" AS t3 WHERE t3.");
    b.append(K_BQ).append(entity.formId.getName()).append(K_BQ).append(" = ").append(formIdInline)
        .append(" AND t3.");
    b.append(K_BQ).append(entity.taskType.getName()).append(K_BQ).append(" = ")
        .append(taskTypeInline).append(")");
    stmts.add(b.toString());
    b.setLength(0);
    // assert: only the lock that holds the resource for that task type appears
    // in the task lock table
    TaskLockTable relation;
    try {

      JdbcTemplate jdbc = datastore.getJdbcConnection();
      jdbc.execute(new ConnectionCallback<Object>() {

        @Override
        public Object doInConnection(Connection conn) throws SQLException, DataAccessException {
          boolean oldAutoCommitValue = conn.getAutoCommit();
          int oldTransactionValue = conn.getTransactionIsolation();
          try {
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);
            Statement stmt = conn.createStatement();
            for (String s : stmts) {
              // for debugging: LogFactory.getLog(TaskLockImpl.class).info(s);
              stmt.execute(s);
            }
            conn.commit();
          } catch (Exception e) {
            e.printStackTrace();
            conn.rollback();
          }
          conn.setTransactionIsolation(oldTransactionValue);
          conn.setAutoCommit(oldAutoCommitValue);
          return null;
        }

      });

      relation = TaskLockTable.assertRelation(datastore, user);
    } catch (Exception e) {
      throw new ODKTaskLockException(PERSISTENCE_LAYER_PROBLEM, e);
    }
    return (TaskLockTable) datastore.getEntity(relation, entity.getUri(), user);
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
