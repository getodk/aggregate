package org.opendatakit.aggregate.odktables;

import java.util.UUID;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

public class LockTemplate {
  private static final int TRIES = 5;
  private static final long SLEEP = 1000;

  private String tableId;
  private ODKTablesTaskLockType type;
  private Datastore ds;
  private User user;
  private String lockId;

  public LockTemplate(String tableId, ODKTablesTaskLockType type, CallingContext cc) {
    this.tableId = tableId;
    this.type = type;
    this.ds = cc.getDatastore();
    this.user = cc.getCurrentUser();
    this.lockId = UUID.randomUUID().toString();
  }

  /**
   * Tries up to 5 times to acquire the lock. If unsuccessful, throws an
   * ODKTaskLockException.
   * 
   * @throws ODKTaskLockException
   */
  public void acquire() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    for (int i = 0; i < TRIES; i++) {
      if (lock.obtainLock(lockId, tableId, type)) {
        break;
      } else {
        try {
          Thread.sleep(SLEEP);
        } catch (Exception e) {
          throw new ODKTaskLockException(e);
        }
      }
    }
    throw new ODKTaskLockException(String.format("Timed out acquiring lock. "
        + "lockId: %s, tableId: %s, type: %s", lockId, tableId, type));
  }

  /**
   * Tries up to 5 times to release lock, then gives up. In this case the lock
   * will eventually timeout and be forced to release.
   * 
   * @throws ODKTaskLockException
   */
  public void release() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    for (int i = 0; i < 5; i++) {
      if (lock.releaseLock(lockId, tableId, type)) {
        break;
      } else {
        try {
          Thread.sleep(SLEEP);
        } catch (Exception e) {
          // just move on, this retry mechanism
          // is to make things nice
          break;
        }
      }
    }
  }

}
