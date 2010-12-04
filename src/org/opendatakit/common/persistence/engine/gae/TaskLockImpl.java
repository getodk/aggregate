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
package org.opendatakit.common.persistence.engine.gae;

import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.exception.ODKTaskLockException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.persistence.TaskLock;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class TaskLockImpl implements TaskLock {

  private static final String NO_TRANSACTION_ACTIVE = "Transaction was no longer active";
  private static final String MULTIPLE_RESULTS_ERROR = "SOMETHING HORRIBLE!! - Some how a second lock was created";
  private static final String KIND = "TASK_LOCK";
  private static final String LOCK_ID_PROPERTY = "LOCK_ID";
  private static final String FORM_ID_PROPERTY = "FORM_ID";
  private static final String TASK_TYPE_PROPERTY = "TASK_TYPE";
  private static final String TIMESTAMP_PROPERTY = "TIMESTAMP";
    
  
  private DatastoreService ds;

  public TaskLockImpl() {
    ds = DatastoreServiceFactory.getDatastoreService();
  }

  @Override
  public boolean obtainLock(String lockId, Form form, TaskLockType taskType) throws ODKTaskLockException {
    boolean result = false;
    Transaction transaction = ds.beginTransaction();

    try {
      Entity gaeEntity = queryForLock(form, taskType);
      if (gaeEntity == null) {
        gaeEntity = new Entity(KIND);
        updateValuesNpersist(transaction, lockId, form, taskType, gaeEntity);
        result = true;
      } else if(checkForExpiration(gaeEntity)) {
        // TODO: remove old lock and create a new lock
      }
    } finally {
      if(result) {
        transaction.commit();
      } else {
        transaction.rollback();
      }
    }

    return result;
  }

  private boolean checkForExpiration(Entity entity) {
    if(entity == null) {
      return false;
    }
    Object obj = entity.getProperty(TIMESTAMP_PROPERTY);
    if(obj instanceof Long) {
      Long timestamp = (Long) obj;
      Long current = System.currentTimeMillis();
      if(current > timestamp) {
        return true;
      }
    }
    return false;
  }
  
  @Override
  public boolean renewLock(String lockId, Form form, TaskLockType taskType) throws ODKTaskLockException {
    
    boolean result = false;
    Transaction transaction = ds.beginTransaction();
    try {
      Entity gaeEntity = queryForLock(form, taskType);
      if (gaeEntity.getProperty(LOCK_ID_PROPERTY).equals(lockId)) {
        updateValuesNpersist(transaction, lockId, form, taskType, gaeEntity);
        result = true;
      }
    } finally {
      if(result) {
        transaction.commit();
      } else {
        transaction.rollback();
      }
    }
    return result;
  }

  @Override
  public boolean releaseLock(String lockId, Form form, TaskLockType taskType) throws ODKTaskLockException{    
    boolean result = false;
    Transaction transaction = ds.beginTransaction();
    try {
      Entity gaeEntity = queryForLock(form, taskType);
      if (gaeEntity.getProperty(LOCK_ID_PROPERTY).equals(lockId)) {
        ds.delete(transaction, gaeEntity.getKey());
        result = true;
      }
    } finally {
      if(result) {
        transaction.commit();
      } else {
        transaction.rollback();
      }
    }
    return result;
  }

  private void updateValuesNpersist(Transaction transaction, String lockId, Form form,
      TaskLockType taskType, Entity gaeEntity) throws ODKTaskLockException{
    try {
      Long timestamp = System.currentTimeMillis() + taskType.getLockExpirationTimeout();
      gaeEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
      gaeEntity.setProperty(LOCK_ID_PROPERTY, lockId);
      gaeEntity.setProperty(FORM_ID_PROPERTY, form.getFormId());
      gaeEntity.setProperty(TASK_TYPE_PROPERTY, taskType.toString());
      ds.put(transaction, gaeEntity);
    } catch (IllegalStateException e) {
      throw new ODKTaskLockException(NO_TRANSACTION_ACTIVE, e);
    } 
  }

  private Entity queryForLock(Form form, TaskLockType taskType) throws ODKTaskLockException {
    try {
      Query query = new Query(KIND);
      query.addFilter(FORM_ID_PROPERTY, Query.FilterOperator.EQUAL, form.getFormId());
      query.addFilter(TASK_TYPE_PROPERTY, Query.FilterOperator.EQUAL, taskType.toString());
      PreparedQuery pquery = ds.prepare(query);
      return pquery.asSingleEntity();
    } catch (TooManyResultsException e) {
      throw new ODKTaskLockException(MULTIPLE_RESULTS_ERROR, e);
    } catch (IllegalStateException e) {
      throw new ODKTaskLockException(NO_TRANSACTION_ACTIVE, e);
    }
  }
}
