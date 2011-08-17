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

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.common.persistence.ITaskLockType;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class TaskLockImpl implements TaskLock {

  private static final String NO_TRANSACTION_ACTIVE = "Transaction was no longer active";
  private static final String MULTIPLE_RESULTS_ERROR = "SOMETHING HORRIBLE!! - Some how a second lock was created";
  private static final String OTHER_ERROR = "Datastore or other failure";
  private static final String KIND = "TASK_LOCK";
  private static final String LOCK_ID_PROPERTY = "LOCK_ID";
  private static final String FORM_ID_PROPERTY = "FORM_ID";
  private static final String TASK_TYPE_PROPERTY = "TASK_TYPE";
  private static final String TIMESTAMP_PROPERTY = "TIMESTAMP";

  private DatastoreService ds;

  public TaskLockImpl() {
    ds = DatastoreServiceFactory.getDatastoreService();
  }

  private void deleteLock(String lockId, String formId, ITaskLockType taskType) {
      boolean deleteResult = false;
      try {
	      // The lock state in the db is bad, so delete bad locks
	      Transaction deleteTransaction = ds.beginTransaction();
	      try {
	        Query query = new Query(KIND);
	        query.addFilter(FORM_ID_PROPERTY, Query.FilterOperator.EQUAL, formId);
	        query.addFilter(TASK_TYPE_PROPERTY, Query.FilterOperator.EQUAL, taskType.getName());
	        PreparedQuery pquery = ds.prepare(query);
	        Iterable<Entity> entities = pquery.asIterable();
	        List<Key> keysToDelete = new ArrayList<Key>();
	//        long oldestTimestamp = System.currentTimeMillis();
	        for (Entity entity : entities) {
	          Object value = entity.getProperty(LOCK_ID_PROPERTY);
	          if (value instanceof String) {
	            String retrievedLockId = (String) value;
	            if (lockId.equals(retrievedLockId)) {
	              keysToDelete.add(entity.getKey());
	            }
	//            oldestTimestamp = Math.min(oldestTimestamp, Long.parseLong((String) entity.getProperty(TIMESTAMP_PROPERTY)));
	          }
	        }
	        ds.delete(deleteTransaction, keysToDelete);
	        deleteResult = true;
	      } catch (Exception e1) {
	        deleteResult = false;
	        e1.printStackTrace();
	      }
	      finally {
	        if (deleteResult) {
	          deleteTransaction.commit();
	        } else {
	          deleteTransaction.rollback();
	        }
	      }
      } catch ( Exception e) { // primarily datastore exceptions
    	  e.printStackTrace();
      }
  }
  
  @Override
  public boolean obtainLock(String lockId, String formId, ITaskLockType taskType) {
    boolean result = false;
    
    try {
	    Transaction transaction = ds.beginTransaction();
	
	    try {
	      Entity gaeEntity = queryForLock(formId, taskType);
	      System.out.println("Trying to get lock : " + lockId);
	      if (gaeEntity == null) {
	        gaeEntity = new Entity(KIND);
	        updateValuesNpersist(transaction, lockId, formId, taskType, gaeEntity);
	        result = true;
	      } else if (checkForExpiration(gaeEntity)) {
	        // note don't delete as there is no guarantee that the lock is in the
	        // same entity group (only one entity group per transaction)
	        updateValuesNpersist(transaction, lockId, formId, taskType, gaeEntity);
	        result = true;
	      }
	      // else you did not get the lock
	    } catch (ODKTaskLockException e) {
	      result = false;
	      e.printStackTrace();
	    } finally {
	      if (result) {
	        transaction.commit();
	      } else {
	        transaction.rollback();
	        return result;
	      }
	    }
    } catch ( DatastoreFailureException e ) {
    	e.printStackTrace();
    	return false;
    }
    
    // and outside the transaction, double-check that we hold the lock
    try {
      // sleep a little to let GAE datastore stabilize
      try {
		Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
      // verify no one else made a lock
      lockVerification(lockId, formId, taskType);
    } catch (ODKTaskLockException e) {
      result = false;
      deleteLock( lockId, formId, taskType );
    }

    return result;
  }

  private boolean checkForExpiration(Entity entity) {
    if (entity == null) {
      return false;
    }
    Object obj = entity.getProperty(TIMESTAMP_PROPERTY);
    if (obj instanceof Long) {
      Long timestamp = (Long) obj;
      Long current = System.currentTimeMillis();
      System.out.println("Time left on lock: " + Long.toString(timestamp - current));
      if (current.compareTo(timestamp) > 0) {
        return true;
      }
    }
    return false;
  }

  public boolean renewLock(String lockId, String formId, ITaskLockType taskType) {
    boolean result = false;
    try {
	    Transaction transaction = ds.beginTransaction();
	    try {
	      Entity gaeEntity = queryForLock(formId, taskType);
	      if (gaeEntity != null) {
	        if (gaeEntity.getProperty(LOCK_ID_PROPERTY).equals(lockId)) {
	          updateValuesNpersist(transaction, lockId, formId, taskType, gaeEntity);
	          result = true;
	        }
	      }
	      // else you did not get find and update your lock
	    } catch (ODKTaskLockException e) {
	      result = false;
	      e.printStackTrace();
	    } finally {
	      if (result) {
	        transaction.commit();
	      } else {
	        transaction.rollback();
	        return result;
	      }
	    }
    } catch (DatastoreFailureException e) {
    	e.printStackTrace();
    	return false;
    }
    
    // and outside the transaction, double-check that we hold the lock
    try {
      // sleep a little to let GAE datastore stabilize
      try {
		Thread.sleep(PersistConsts.MIN_SETTLE_MILLISECONDS);
	  } catch (InterruptedException e) {
		e.printStackTrace();
	  }
      // verify no one else made a lock
      lockVerification(lockId, formId, taskType);
    } catch (ODKTaskLockException e) {
      result = false;
      deleteLock( lockId, formId, taskType );
    }
    return result;
  }



  public boolean releaseLock(String lockId, String formId, ITaskLockType taskType) throws ODKTaskLockException {
    boolean result = false;
    Transaction transaction = ds.beginTransaction();
    try {
      Entity gaeEntity = queryForLock(formId, taskType);
      if (gaeEntity != null && gaeEntity.getProperty(LOCK_ID_PROPERTY).equals(lockId)) {
        ds.delete(transaction, gaeEntity.getKey());
        result = true;
      }
    } catch (ODKTaskLockException e) {
    	throw e;
    } catch (Exception e) { // catches datastore issues...
    	e.printStackTrace();
        throw new ODKTaskLockException(OTHER_ERROR, e);
    } finally {
      if (result) {
    	try {
    	  transaction.commit();
    	} catch ( DatastoreFailureException e ) {
    	  throw new ODKTaskLockException(OTHER_ERROR, e);
    	}
      } else {
    	try {
    	  transaction.rollback();
    	} catch ( DatastoreFailureException e ) {
    	  throw new ODKTaskLockException(OTHER_ERROR, e);
    	}
      }
    }
    return result;
  }

  private void lockVerification(String lockId, String formId, ITaskLockType taskType)
      throws ODKTaskLockException {
    Entity verificationEntity = queryForLock(formId, taskType);
    if ( verificationEntity == null ) {
    	throw new ODKTaskLockException("UNABLE TO LOCATE LOCK: " + lockId + " For: " + formId + " Task: " + taskType.getName());
    }
    Object value = verificationEntity.getProperty(LOCK_ID_PROPERTY);
    if (value instanceof String) {
      String retrievedLockId = (String) value;
      if (!lockId.equals(retrievedLockId)) {
        throw new ODKTaskLockException("SOMEONE OVERWROTE THE LOCK" + " Actual: " + retrievedLockId + " Expected: " + lockId);
      }
    }
  }

  private void updateValuesNpersist(Transaction transaction, String lockId, String formId,
		  ITaskLockType taskType, Entity gaeEntity) throws ODKTaskLockException {
    System.out.println("Persisting lock: " + lockId);
    try {
      Long timestamp = System.currentTimeMillis() + taskType.getLockExpirationTimeout();
      gaeEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
      gaeEntity.setProperty(LOCK_ID_PROPERTY, lockId);
      gaeEntity.setProperty(FORM_ID_PROPERTY, formId);
      gaeEntity.setProperty(TASK_TYPE_PROPERTY, taskType.getName());
      ds.put(transaction, gaeEntity);
    } catch (IllegalStateException e) {
      throw new ODKTaskLockException(NO_TRANSACTION_ACTIVE, e);
    } catch (Exception e) { // catches datastore issues...
    	e.printStackTrace();
        throw new ODKTaskLockException(OTHER_ERROR, e);
    }
  }

  private Entity queryForLock(String formId, ITaskLockType taskType) throws ODKTaskLockException {
    try {
      Query query = new Query(KIND);
      query.addFilter(FORM_ID_PROPERTY, Query.FilterOperator.EQUAL, formId);
      query.addFilter(TASK_TYPE_PROPERTY, Query.FilterOperator.EQUAL, taskType.getName());
      PreparedQuery pquery = ds.prepare(query);
      Iterable<Entity> entities = pquery.asIterable();
      // There may be expired locks in the database.  
      // Skip over those and find the active lock. 
      Entity active = null;
      for ( Entity e : entities ) {
    	  if ( !checkForExpiration(e) ) {
    		  if ( active != null ) {
    			  throw new ODKTaskLockException(MULTIPLE_RESULTS_ERROR);
    		  }
    		  active = e;
    	  }
      }
      return active;
    } catch (ODKTaskLockException e) {
      throw e;
    } catch (TooManyResultsException e) {
      throw new ODKTaskLockException(MULTIPLE_RESULTS_ERROR, e);
    } catch (IllegalStateException e) {
      throw new ODKTaskLockException(NO_TRANSACTION_ACTIVE, e);
    } catch (Exception e) { // may catch datastore issues?
      e.printStackTrace();
      throw new ODKTaskLockException(OTHER_ERROR, e);
    }
  }

}
