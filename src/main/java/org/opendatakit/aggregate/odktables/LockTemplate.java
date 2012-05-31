package org.opendatakit.aggregate.odktables;

import java.util.UUID;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

public class LockTemplate {

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
   * Spins until lock is acquired, or an error occurs.
   * 
   * @throws ODKTaskLockException
   */
  public void acquire() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    while (!lock.obtainLock(lockId, tableId, type)) {
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  /**
   * Tries up to 10 times to release lock, then gives up. In this case the lock
   * will eventually timeout and be forced to release.
   * 
   * @throws ODKTaskLockException
   */
  public void release() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    for (int i = 0; i < 10; i++) {
      if (lock.releaseLock(lockId, tableId, type))
        break;
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // just move on, this retry mechanism
        // is to make things nice
      }
    }
  }

}
