package org.opendatakit.aggregate.odktables;

import java.util.Random;
import java.util.UUID;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

public class LockTemplate {
  // At 4 tries and 250 initial backoff, the maximum amount of time a single
  // acquire or release can take is:
  // 250 + 500 + 1000 + 2000 = 3750
  private static final int TRIES = 4;
  private static final int INITIAL_MAX_BACKOFF = 250;

  private String tableId;
  private ODKTablesTaskLockType type;
  private Datastore ds;
  private User user;
  private String lockId;
  private long maxBackoffMs;
  private Random rand;

  public LockTemplate(String tableId, ODKTablesTaskLockType type, CallingContext cc) {
    this.tableId = tableId;
    this.type = type;
    this.ds = cc.getDatastore();
    this.user = cc.getCurrentUser();
    this.lockId = UUID.randomUUID().toString();
    this.maxBackoffMs = INITIAL_MAX_BACKOFF;
    this.rand = new Random();
  }

  /**
   * Tries up to 5 times to acquire the lock. If unsuccessful, throws an
   * ODKTaskLockException.
   * 
   * @throws ODKTaskLockException
   */
  public void acquire() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    boolean acquired = false;
    for (int i = 0; i < TRIES; i++) {
      if (lock.obtainLock(lockId, tableId, type)) {
        acquired = true;
        maxBackoffMs = INITIAL_MAX_BACKOFF;
        break;
      } else {
        try {
          Thread.sleep(getNextBackoff());
        } catch (Exception e) {
          throw new ODKTaskLockException(e);
        }
      }
    }
    if (!acquired) {
      throw new ODKTaskLockException(String.format("Timed out acquiring lock. "
          + "lockId: %s, tableId: %s, type: %s", lockId, tableId, type));
    }
  }

  /**
   * Tries up to 5 times to release lock, then gives up. In this case the lock
   * will eventually timeout and be forced to release.
   * 
   * @throws ODKTaskLockException
   */
  public void release() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    for (int i = 0; i < TRIES; i++) {
      if (lock.releaseLock(lockId, tableId, type)) {
        maxBackoffMs = INITIAL_MAX_BACKOFF;
        break;
      } else {
        try {
          Thread.sleep(getNextBackoff());
        } catch (Exception e) {
          // just move on, this retry mechanism
          // is to make things nice
          break;
        }
      }
    }
  }

  private long getNextBackoff() {
    long backoff = (long) (rand.nextDouble() * maxBackoffMs);
    maxBackoffMs *= 2;
    return backoff;
  }
}
