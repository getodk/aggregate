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
  private int lockRetries;
  private String lockId;

  public LockTemplate(String tableId, ODKTablesTaskLockType type, CallingContext cc) {
    this.tableId = tableId;
    this.type = type;
    this.ds = cc.getDatastore();
    this.user = cc.getCurrentUser();
    this.lockRetries = 10;
    this.lockId = UUID.randomUUID().toString();
  }

  public void acquire() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    for (int i = 0; i < lockRetries; i++) {
      if (lock.obtainLock(lockId, tableId, type))
        break;
      try {
        Thread.sleep(1000);
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  public void release() throws ODKTaskLockException {
    TaskLock lock = ds.createTaskLock(user);
    for (int i = 0; i < lockRetries; i++) {
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
