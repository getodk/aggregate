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
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.ITaskLockType;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.engine.DatastoreAccessMetrics;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.PreparedQuery.TooManyResultsException;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Transaction;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheService.IdentifiableValue;
import com.google.appengine.api.memcache.MemcacheService.SetPolicy;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class TaskLockImpl implements TaskLock {

  private static final String NO_TIMESTAMP_ERROR = "Some how no Timestamp - Strange things happening!!!!!";
  private static final String NO_LOCK_ID_ERROR = "Some how no Lock ID - Strange things happening!!!!!";
  private static final String NO_TRANSACTION_ACTIVE = "Transaction was no longer active";
  private static final String MULTIPLE_RESULTS_ERROR = "Too close to call: Two locks were created within the settle time of each other";
  private static final String MULTIPLE_MEMCACHE_RESULTS_ERROR = "Memcache: Collision of lock timestamps";
  private static final String HOT_MEMCACHE_ENTRY_ERROR = "Memcache: Unable to record lock due to contention";
  private static final String OTHER_ERROR = "Datastore or other failure";
  private static final String ENTITY_GROUP_KIND = "TASK_LOCK_GROUP";
  private static final String ENTITY_GROUP_KEY = "TASK_LOCKS";
  private static final String KIND = "TASK_LOCK";
  private static final String LOCK_ID_PROPERTY = "LOCK_ID";
  private static final String FORM_ID_PROPERTY = "FORM_ID";
  private static final String TASK_TYPE_PROPERTY = "TASK_TYPE";
  private static final String TIMESTAMP_PROPERTY = "TIMESTAMP";

  private static final Random RNG = new Random();

  private final DatastoreAccessMetrics dam;
  private final MemcacheService syncCache;
  private final DatastoreService ds;
  private final Log log;

  public TaskLockImpl(DatastoreAccessMetrics dam, MemcacheService syncCache) {
    this.dam = dam;
    this.syncCache = syncCache;
    ds = DatastoreServiceFactory.getDatastoreService();
    log = LogFactory.getLog(TaskLockImpl.class);
  }

  /**
   * To minimize the occurrence of ConcurrentModificationException, do not use
   * just one parent entity for all task locks. Instead, use the hashCode() of
   * the concatenation (formId + taskType.getName()) to create one of 256
   * possible parent entities, and use that entity when manipulating this task
   * lock.
   * 
   * @param formId
   * @param taskType
   * @return parent entity to use for strong consistency enforcement
   */
  private Key createTaskGroupKey(String formId, ITaskLockType taskType) {
    // reduce ConcurrentModificationException likelihood by spreading ownership
    // of a given lock (identified by formId + taskType.getName())
    // across 256 different parent entity group kinds.
    String code = formId + taskType.getName();
    String qualifier = Integer.toHexString(code.hashCode() & 0xff);
    if (qualifier.length() == 1) {
      qualifier = "_0" + qualifier;
    } else {
      qualifier = "_" + qualifier;
    }
    Key entityGroupKey = KeyFactory.createKey(ENTITY_GROUP_KIND + qualifier, ENTITY_GROUP_KEY);
    return entityGroupKey;
  }

  /**
   * By using specific parent entities and the High Replication datastore, we
   * avoid the long settle times required by the earlier Master-Slave datastore.
   * But, when things go south, we need to back off and wait for the parent
   * entity to settle.
   * 
   * No idea how long that back-off should be. Try 1100ms plus a bit since
   * appengine transactions can take over a second for database to settle
   */
  private void sleepBriefly() {
    long sleepInterval = 1100L + (0xff & RNG.nextInt());
    try {
      Thread.sleep(sleepInterval);
    } catch (InterruptedException e1) {
      e1.printStackTrace();
    }
  }

  /**
   * Deletes the specified lockId entry and perhaps any other stale entries. If
   * the deletion of the lockId fails, throws the exception associated with that
   * failure (but ignores failures for the deletion of any stale entries).
   * 
   * @param lockId
   * @param formId
   * @param taskType
   * @throws ODKTaskLockException
   */
  private void deleteLock(String lockId, String formId, ITaskLockType taskType)
      throws ODKTaskLockException {
    ODKTaskLockException lockIdTaskLockException = null;
    try {
      Key entityGroupKey = createTaskGroupKey(formId, taskType);
      Query query = new Query(KIND, entityGroupKey);
      query.setAncestor(entityGroupKey);
      query.setFilter(new Query.CompositeFilter(CompositeFilterOperator.AND,
          Arrays.<Filter> asList(
              new Query.FilterPredicate(FORM_ID_PROPERTY, Query.FilterOperator.EQUAL, formId),
              new Query.FilterPredicate(TASK_TYPE_PROPERTY, Query.FilterOperator.EQUAL,
                  taskType.getName()))));
      PreparedQuery pquery = ds.prepare(query);

      Iterable<Entity> entities = pquery.asIterable();
      Key lockIdKey = null;
      List<Key> keysToDelete = new ArrayList<Key>();
      int recCount = 0;
      for (Entity entity : entities) {
        ++recCount;
        boolean shouldDelete = false;
        // see if deadline is more than a day in the past.
        // if so, remove lock from table.
        Long timestamp = getTimestamp(entity);
        if (timestamp + 24L * 3600L * 1000L < System.currentTimeMillis()) {
          shouldDelete = true;
        }
        // see if lock id matches that of the one supplied.
        // if so, remove lock from table.
        String retrievedLockId = getLockId(entity);
        if (lockId.equals(retrievedLockId)) {
          lockIdKey = entity.getKey();
          shouldDelete = true;
        }
        if (shouldDelete) {
          deleteLockIdMemCache(retrievedLockId, formId, taskType);
          keysToDelete.add(entity.getKey());
        }
      }
      dam.recordQueryUsage(KIND, recCount);

      // we have the list of candidate records
      // now gain a transaction lock for each and
      // delete it.
      for (Key key : keysToDelete) {
        // try 3 times to delete the lock from the datastore
        ODKTaskLockException tle = null;
        String loggingString;
        if (key.equals(lockIdKey)) {
          loggingString = "removing " + " lockId: " + lockId + " ";
        } else {
          loggingString = "removing stale lock ";
        }
        for (int i = 0; i < 3; ++i) {
          tle = null;
          try {
            Transaction transaction = ds.beginTransaction();
            try {
              dam.recordDeleteUsage(KIND);
              ds.delete(transaction, key);
            } finally {
              transaction.commit();
            }
          } catch (Exception e) { // catches datastore issues...
            e.printStackTrace();
            tle = new ODKTaskLockException(OTHER_ERROR, e);
          }
          if (tle == null) {
            break;
          }
          // Annoyingly, if there is a lot of contention on the entity or group,
          // the datastore can throw a ConcurrentModificationException or other
          // exception. In this case, we save this triggering exception and
          // attempt to delete the lock after a brief sleep.
          //
          log.warn(loggingString + "intermediate exception " + tle.getCause().toString() + " Retry#"
              + Integer.toString(i) + " : deleteLock : " + formId + " " + taskType.getName());

          sleepBriefly();
        }
        if (tle != null) {
          if (key.equals(lockIdKey)) {
            lockIdTaskLockException = tle;
          } else {
            log.error(loggingString + "FINAL swallowed exception " + tle.getCause().toString()
                + " : deleteLock : " + formId + " " + taskType.getName());
          }
        } else {
          log.info(loggingString + "SUCCESS : deleteLock : " + formId + " " + taskType.getName());
        }
      }
    } catch (Exception e) { // primarily datastore exceptions
      e.printStackTrace();
      throw new ODKTaskLockException(OTHER_ERROR, e);
    } finally {
      // remove lockId from the cache; it should already be removed.
      // But, if exceptions are thrown it might not be.
      deleteLockIdMemCache(lockId, formId, taskType);
    }
    if (lockIdTaskLockException != null) {
      throw lockIdTaskLockException;
    }
  }

  @Override
  public boolean obtainLock(String lockId, String formId, ITaskLockType taskType) {
    boolean result = false;
    boolean delayOtherLockExist = false;
    if (lockId == null || formId == null || taskType == null) {
      throw new IllegalArgumentException();
    }
    Transaction transaction = ds.beginTransaction();
    try {

      try {
        Entity gaeEntity = queryForLock(formId, taskType);
        log.info("Trying to get lock : " + lockId + " " + formId + " " + taskType.getName());
        if (gaeEntity == null) {
          log.info("No pre-existing lock");
          Key entityGroupKey = createTaskGroupKey(formId, taskType);
          gaeEntity = new Entity(KIND, entityGroupKey);
          updateValuesNpersist(transaction, lockId, formId, taskType, gaeEntity);
          result = true;
        } else {
          log.info("FOUND pre-existing lock");
          // see if the lock is ours (generally will not be)
          String retrievedLockId = getLockId(gaeEntity);
          if (lockId.equals(retrievedLockId)) {
            result = true;
          } else {
            result = false;
            delayOtherLockExist = true;
          }
        }
      } catch (ODKTaskLockException e) {
        // else you did not get the lock
        result = false;
        log.info("ODK Task Lock Exception: " + e.getMessage());
        e.printStackTrace();
      } catch (Exception e) {
        // might be a ConcurrentModificationException if another transaction is
        // touching the entity group
        /*
         * You can receive DatastoreTimeoutException or
         * DatastoreFailureException exceptions in cases where transactions have
         * been committed and eventually will be applied successfully. However,
         * if they happen before our commit we roll back to improve consistency
         */
        result = false;
        log.warn("OBTAIN LOCK Not ODK EXCEPTION specific will roll back");
        log.warn("EXECPTION CAUSING ROLLBACK:" + e.toString());
        e.printStackTrace();
      } finally {
        if (result) {
          transaction.commit();
        } else {
          log.info("Rollback obtainLock : " + lockId + " " + formId + " " + taskType.getName());
          transaction.rollback();
          deleteLockIdMemCache(lockId, formId, taskType);
          if (delayOtherLockExist) {
            sleepBriefly();
          }
          return false;
        }
      }

    } catch (Exception e) {
      result = false;
      log.warn("EXCEPTION OUTSIDE try/catch obtaining lock, roll back transaction if still active");
      log.warn("EXECPTION CAUSING ROLLBACK:" + e.toString());
      e.printStackTrace();
    } finally {
      if (transaction.isActive()) {
        log.info("Rollback obtainLock : " + lockId + " " + formId + " " + taskType.getName());
        transaction.rollback();
        deleteLockIdMemCache(lockId, formId, taskType);
      }
      if (!result) {
        return false;
      }
    }

    // and outside the transaction, double-check that we hold the lock

    // rely on strong consistency guarantee on a High Replication datastore.
    // No longer have to worry about Master-Slave datastore.
    // verify no one else made a lock
    result = lockVerification(lockId, formId, taskType);

    if (result) {
      log.info("obtainLock SUCCESS : " + lockId + " " + formId + " " + taskType.getName());
    }

    return result;
  }

  private Long getTimestamp(Entity entity) throws NullPointerException, ODKTaskLockException {
    if (entity == null) {
      throw new NullPointerException();
    }
    Object obj = entity.getProperty(TIMESTAMP_PROPERTY);
    if (obj instanceof Long) {
      Long timestamp = (Long) obj;
      return timestamp;
    } else {
      throw new ODKTaskLockException(NO_TIMESTAMP_ERROR);
    }
  }

  private String getLockId(Entity entity) throws NullPointerException, ODKTaskLockException {
    if (entity == null) {
      throw new NullPointerException();
    }

    Object value = entity.getProperty(LOCK_ID_PROPERTY);
    if (value instanceof String) {
      String retrievedId = (String) value;
      return retrievedId;
    } else {
      throw new ODKTaskLockException(NO_LOCK_ID_ERROR);
    }
  }

  /**
   *
   * @param entity
   * @return true if expired
   * @throws ODKTaskLockException
   */
  private boolean isExpired(Entity entity) throws NullPointerException, ODKTaskLockException {
    if (entity == null) {
      throw new NullPointerException();
    }
    Long timestamp = getTimestamp(entity);
    Long current = System.currentTimeMillis();

    // log the lock
    String lockId = "UNKNOWN LOCK";
    Object obj = entity.getProperty(LOCK_ID_PROPERTY);
    if (obj instanceof String) {
      lockId = (String) obj;
    }
    log.info("LockId: " + lockId + " Time left: " + Long.toString(timestamp - current));
    if (current.compareTo(timestamp) > 0) {
      return true;
    }
    return false;
  }

  /**
   * If there is any form of contention on this resource, the renewal will fail.
   * I.e., the owner of the lock does not have an elevated claim to the lock.
   */
  public boolean renewLock(String lockId, String formId, ITaskLockType taskType) {
    boolean result = false;
    long originalTimestamp = -1;

    if (lockId == null || formId == null || taskType == null) {
      throw new IllegalArgumentException();
    }

    Transaction transaction = ds.beginTransaction();
    try {

      try {
        Entity gaeEntity = queryForLock(formId, taskType);
        if (gaeEntity != null) {
          String retrievedLockId = getLockId(gaeEntity);
          if (retrievedLockId.equals(lockId)) {
            originalTimestamp = getTimestamp(gaeEntity);
            updateValuesNpersist(transaction, lockId, formId, taskType, gaeEntity);
            result = true;
          }
        }
      } catch (ODKTaskLockException e) {
        result = false;
        e.printStackTrace();
      } catch (Exception e) {
        // might be a ConcurrentModificationException if another transaction is
        // touching the entity group
        /*
         * You can receive DatastoreTimeoutException or
         * DatastoreFailureException exceptions in cases where transactions have
         * been committed and eventually will be applied successfully. However,
         * if they happen before our commit we roll back to improve consistency
         */
        result = false;
        log.warn("OBTAIN LOCK Not ODK EXCEPTION specific will roll back");
        log.warn("EXECPTION CAUSING ROLLBACK:" + e.toString());
        e.printStackTrace();
      } finally {
        if (result) {
          transaction.commit();
        } else {
          log.info("Rollback renewLock : " + lockId + " " + formId + " " + taskType.getName());
          transaction.rollback();
          // restore memcache
          if (originalTimestamp > -1) {
            updateLockIdTimestampMemCache(lockId, formId, taskType, originalTimestamp);
          }
          return false;
        }
      }

    } catch (Exception e) {
      result = false;
      log.warn("EXCEPTION OUTSIDE try/catch renewing lock, roll back transaction if still active");
      log.warn("EXECPTION CAUSING ROLLBACK:" + e.toString());
      e.printStackTrace();
    } finally {
      if (transaction.isActive()) {
        log.info("Rollback renewLock : " + lockId + " " + formId + " " + taskType.getName());
        transaction.rollback();
        try {
          // restore memcache
          if (originalTimestamp > -1) {
            updateLockIdTimestampMemCache(lockId, formId, taskType, originalTimestamp);
          }
        } catch (ODKTaskLockException e) {
          // trying to restore memCache back to proper state, letting it report
          // error and moving on
          e.printStackTrace();
        }
      }
      if (!result) {
        return false;
      }
    }

    // and outside the transaction, double-check that we hold the lock

    // rely on strong consistency guarantee on a High Replication datastore.
    // No longer have to worry about Master-Slave datastore.
    // verify no one else made a lock
    result = lockVerification(lockId, formId, taskType);

    if (result) {
      log.info("renewLock SUCCESS : " + lockId + " " + formId + " " + taskType.getName());
    }
    return result;
  }

  /**
   * Throws an exception if the lock cannot be released.
   */
  @Override
  public boolean releaseLock(String lockId, String formId, ITaskLockType taskType)
      throws ODKTaskLockException {

    if (lockId == null || formId == null || taskType == null) {
      throw new IllegalArgumentException();
    }

    deleteLock(lockId, formId, taskType);

    // exception is thrown on failure of delete lock
    log.info("releaseLock SUCCESS : " + lockId + " " + formId + " " + taskType.getName());
    return true;
  }

  /**
   * Verifies that the given lockId owns the mutex. If it doesn't, an
   * ODKTaskLockException is thrown.
   * 
   * @param lockId
   * @param formId
   * @param taskType
   * @throws ODKTaskLockException
   */
  private boolean lockVerification(String lockId, String formId, ITaskLockType taskType) {
    log.info("Starting lock verification");
    Transaction transaction = ds.beginTransaction();
    try {
      Entity verificationEntity = queryForLock(formId, taskType);
      if (verificationEntity == null) {
        log.error("UNABLE TO LOCATE LOCK");
        throw new ODKTaskLockException("UNABLE TO LOCATE LOCK: " + lockId + " For: " + formId
            + " Task: " + taskType.getName());
      }
      String retrievedLockId = getLockId(verificationEntity);
      if (!lockId.equals(retrievedLockId)) {
        log.error("OVERWROTE LOCK");
        throw new ODKTaskLockException(
            "SOMEONE OVERWROTE THE LOCK" + " Actual: " + retrievedLockId + " Expected: " + lockId);
      }
      String retrievedLockIdMemCache = queryForLockIdMemCache(formId, taskType);
      // if it is null, that is OK -- we might have a contention failure or
      // memcache might be down
      if (retrievedLockIdMemCache != null && !lockId.equals(retrievedLockIdMemCache)) {
        log.error("lockVerification -- MemCache lock verification failure. Actual: "
            + retrievedLockIdMemCache + " Expected: " + lockId);

        throw new ODKTaskLockException("MemCache: Undetected Datastore Overwriting of Lock"
            + " Actual: " + retrievedLockIdMemCache + " Expected: " + lockId);
      }

    } catch (ODKTaskLockException e) {
      // need to roll back transaction before trying to delete the lock that was
      // assumed to be created
      transaction.rollback();
      try {
        log.error("******** LOCK VERIFICATION FAILED ********");
        log.error("CAUSE OF FAILURE: " + e.getMessage());
        log.error("EXCEPTION TO STRING: " + e.toString());
        e.printStackTrace();
        log.error("******** LOCK VERIFICATION FAILED ********");
        log.error("Deleting lock just created/updated ....");
        deleteLock(lockId, formId, taskType);
      } catch (ODKTaskLockException ex) {
        log.error("******** LOCK VERIFICATION FAILED ********");
        log.error("!!!!!! UNABLE TO DELETE LOCK BECAUSE OF EXCEPTION: !!!!!!");
        ex.printStackTrace();
      }
      return false;
    } catch (Exception e) {
      // need to roll back transaction before trying to delete the lock that was
      // assumed to be created
      transaction.rollback();
      try {
        log.error("UNEXPECTED EXCEPTION!!!!");
        log.error("###### LOCK VERIFICATION FAILED ######");
        log.error("CAUSE OF FAILURE: " + e.getMessage());
        log.error("EXCEPTION TO STRING: " + e.toString());
        e.printStackTrace();
        log.error("###### LOCK VERIFICATION FAILED ######");
        log.error("Deleting lock just created/updated ....");
        deleteLock(lockId, formId, taskType);
      } catch (ODKTaskLockException ex) {
        log.error("###### LOCK VERIFICATION FAILED ######");
        log.error("!!!!!! UNABLE TO DELETE LOCK BECAUSE OF EXCEPTION: !!!!!!");
        ex.printStackTrace();
      }
      return false;
    }

    // success
    transaction.commit();
    return true;
  }

  /**
   * Update the entity with the given values.
   * 
   * @param transaction
   * @param lockId
   * @param formId
   * @param taskType
   * @param gaeEntity
   * @throws ODKTaskLockException
   */
  private void updateValuesNpersist(Transaction transaction, String lockId, String formId,
      ITaskLockType taskType, Entity gaeEntity) throws ODKTaskLockException {
    Long timestamp = System.currentTimeMillis() + taskType.getLockExpirationTimeout();

    dam.recordPutUsage(KIND);
    try {
      gaeEntity.setProperty(TIMESTAMP_PROPERTY, timestamp);
      gaeEntity.setProperty(LOCK_ID_PROPERTY, lockId);
      gaeEntity.setProperty(FORM_ID_PROPERTY, formId);
      gaeEntity.setProperty(TASK_TYPE_PROPERTY, taskType.getName());
      try {
        updateLockIdTimestampMemCache(lockId, formId, taskType, timestamp);
      } finally {
        ds.put(transaction, gaeEntity);
        log.info("Persisting lock : " + lockId + " " + formId + " " + taskType.getName()
            + " timestamp: " + timestamp);
      }
    } catch (IllegalStateException e) {
      throw new ODKTaskLockException(NO_TRANSACTION_ACTIVE, e);
    } catch (ODKTaskLockException e) {
      throw e;
    } catch (Exception e) { // catches datastore issues...
      e.printStackTrace();
      throw new ODKTaskLockException(OTHER_ERROR, e);
    }
  }

  /**
   * Update the MemCache for (formId, taskType) to record the given timestamp as
   * the expire-time of the lockId. The lockId with the earliest in-the-future
   * expire-time wins as long as there are no other lockIds within
   * SHORTEST_ALLOWABLE_GAIN_LOCK_SEPARATION of it.
   * 
   * @param lockId
   * @param formId
   * @param taskType
   * @param timestamp
   * @throws ODKTaskLockException
   */
  private synchronized void updateLockIdTimestampMemCache(String lockId, String formId,
      ITaskLockType taskType, Long timestamp) throws ODKTaskLockException {
    if (syncCache != null) {
      int i;
      try {
        String formTask = ((formId == null) ? "" : formId) + "@" + taskType.getName();
        for (i = 0; i < 10; i++) {
          IdentifiableValue v = syncCache.contains(formTask) ? syncCache.getIdentifiable(formTask)
              : null;
          if (v == null || v.getValue() == null) {
            TreeMap<Long, String> tm = new TreeMap<Long, String>();
            tm.put(timestamp, lockId);
            if (syncCache.put(formTask, tm, null, SetPolicy.ADD_ONLY_IF_NOT_PRESENT)) {
              break;
            }
          } else {
            @SuppressWarnings("unchecked")
            TreeMap<Long, String> tmOrig = (TreeMap<Long, String>) v.getValue();
            TreeMap<Long, String> tm = new TreeMap<Long, String>(tmOrig);

            // remove any old entries for lockId and any that are very old
            Long currentTimestamp = System.currentTimeMillis();
            Long oldTimestamp;
            do {
              oldTimestamp = null;
              for (Map.Entry<Long, String> entry : tm.entrySet()) {
                if (entry.getKey() + 300000L < currentTimestamp) {
                  // more than 5 minutes old -- remove it
                  oldTimestamp = entry.getKey();
                  break;
                }
                if (entry.getValue().equals(lockId)) {
                  oldTimestamp = entry.getKey();
                  break;
                }
              }
              if (oldTimestamp != null) {
                tm.remove(oldTimestamp);
              }
            } while (oldTimestamp != null);

            // update with new timestamp
            if (tm.put(timestamp, lockId) != null) {
              // some other thread gained the lock first for this timestamp
              throw new ODKTaskLockException(MULTIPLE_MEMCACHE_RESULTS_ERROR);
            }

            // try to update the Memcache with these changes.
            if (syncCache.putIfUntouched(formTask, v, tm)) {
              break;
            }
          }
        }
      } catch (ODKTaskLockException e) {
        throw e;
      } catch (Throwable t) {
        t.printStackTrace();
        throw new ODKTaskLockException(OTHER_ERROR, t);
      }

      if (i == 10) {
        // crazy contention
        throw new ODKTaskLockException(HOT_MEMCACHE_ENTRY_ERROR);
      }
    }
  }

  /**
   * Remove the given lockId from the (formId, taskType) entry.
   * 
   * NOTE: We make 10 attempts. If all of these fail, the lockId will be left
   * active. This can cause lock-outs for the duration of the locking period.
   * 
   * @param lockId
   * @param formId
   * @param taskType
   */
  private synchronized void deleteLockIdMemCache(String lockId, String formId,
      ITaskLockType taskType) {
    if (syncCache != null) {
      int i = 0;
      try {
        String formTask = ((formId == null) ? "" : formId) + "@" + taskType.getName();
        for (i = 0; i < 10; i++) {

          IdentifiableValue v = syncCache.contains(formTask) ? syncCache.getIdentifiable(formTask)
              : null;
          if (v == null || v.getValue() == null) {
            break;
          } else {
            @SuppressWarnings("unchecked")
            TreeMap<Long, String> tmOrig = (TreeMap<Long, String>) v.getValue();
            TreeMap<Long, String> tm = new TreeMap<Long, String>(tmOrig);

            // remove any old entries for lockId and any that are very old
            Long currentTimestamp = System.currentTimeMillis();
            Long oldTimestamp;
            do {
              oldTimestamp = null;
              for (Map.Entry<Long, String> entry : tm.entrySet()) {
                if (entry.getKey() + 300000L < currentTimestamp) {
                  // more than 5 minutes old -- remove it
                  oldTimestamp = entry.getKey();
                  break;
                }
                if (entry.getValue().equals(lockId)) {
                  oldTimestamp = entry.getKey();
                  break;
                }
              }
              if (oldTimestamp != null) {
                tm.remove(oldTimestamp);
              }
            } while (oldTimestamp != null);

            if (syncCache.putIfUntouched(formTask, v, tm)) {
              break;
            }
          }
        }
      } catch (Throwable t) {
        t.printStackTrace();
        // ignore
      }
      // don't care if we had contention and didn't do anything.
      // This will eventually self-correct.
      if (i == 10) {
        log.warn("deleteLockIdMemCache -- stall has been introduced  lock : " + lockId + " "
            + formId + " " + taskType.getName());
      }
    }
  }

  /**
   * Returns the lockId for the lock with the earliest in-the-future expiration
   * timestamp. Whatever lock holds that is considered the winner of the mutex.
   * 
   * NOTE: Returning null does not mean there is no active lock. It can mean
   * that the Memcache is unavailable or has been cleared.
   * 
   * @param formId
   * @param taskType
   * @return
   * @throws ODKTaskLockException
   */
  private synchronized String queryForLockIdMemCache(String formId, ITaskLockType taskType)
      throws ODKTaskLockException {
    if (syncCache != null) {
      try {
        String formTask = ((formId == null) ? "" : formId) + "@" + taskType.getName();
        IdentifiableValue v = syncCache.contains(formTask) ? syncCache.getIdentifiable(formTask)
            : null;
        if (v == null || v.getValue() == null) {
          return null;
        } else {
          @SuppressWarnings("unchecked")
          TreeMap<Long, String> tm = (TreeMap<Long, String>) v.getValue();
          Long currentTimestamp = System.currentTimeMillis();
          Long youngestActiveTimestamp = 0L;

          for (Long timestamp : tm.keySet()) {
            if (timestamp >= currentTimestamp) {
              if (youngestActiveTimestamp == 0L) {
                youngestActiveTimestamp = timestamp;
              } else {
                // same logic as datastore
                if (Math.abs(youngestActiveTimestamp - timestamp) < taskType.getMinSettleTime()) {
                  throw new ODKTaskLockException(MULTIPLE_RESULTS_ERROR);
                }
                if (youngestActiveTimestamp > timestamp) {
                  youngestActiveTimestamp = timestamp;
                }
              }
            }
          }
          if (youngestActiveTimestamp != 0L) {
            return tm.get(youngestActiveTimestamp);
          }
          return null;
        }
      } catch (ODKTaskLockException e) {
        throw e;
      } catch (Throwable t) {
        t.printStackTrace();
        log.warn("queryForLockIdMemCache -- ignored exception " + t.toString() + " : " + formId
            + " " + taskType.getName());
        // ignore
      }
    }
    return null;
  }

  /**
   * Returns the entity for the lock with the earliest in-the-future expiration
   * timestamp. Whatever lock holds that is considered the winner of the mutex.
   * 
   * Returning null means there is no active lock.
   * 
   * @param formId
   * @param taskType
   * @return
   * @throws ODKTaskLockException
   */
  private Entity queryForLock(String formId, ITaskLockType taskType) throws ODKTaskLockException {
    int readCount = 0;
    try {
      Key entityGroupKey = createTaskGroupKey(formId, taskType);
      Query query = new Query(KIND, entityGroupKey);
      query.setAncestor(entityGroupKey);
      query.setFilter(new Query.CompositeFilter(CompositeFilterOperator.AND,
          Arrays.<Filter> asList(
              new Query.FilterPredicate(FORM_ID_PROPERTY, Query.FilterOperator.EQUAL, formId),
              new Query.FilterPredicate(TASK_TYPE_PROPERTY, Query.FilterOperator.EQUAL,
                  taskType.getName()))));
      PreparedQuery pquery = ds.prepare(query);
      Iterable<Entity> entities = pquery.asIterable();
      // There may be expired locks in the database.
      // Skip over those and find the active lock.
      Entity active = null;
      for (Entity e : entities) {
        ++readCount;
        if (!isExpired(e)) {
          if (active != null) {
            Long timestamp1 = getTimestamp(active);
            Long timestamp2 = getTimestamp(e);

            // can't tell who won if we are within the settle interval.
            if (Math.abs(timestamp1 - timestamp2) < taskType.getMinSettleTime()) {
              throw new ODKTaskLockException(MULTIPLE_RESULTS_ERROR);
            }

            // otherwise, whichever holder held the lock first wins
            int cmp = timestamp1.compareTo(timestamp2);
            if (cmp > 0) {
              active = e;
            }
          } else {
            active = e;
          }
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
    } finally {
      dam.recordQueryUsage(KIND, readCount);
    }

  }

}
