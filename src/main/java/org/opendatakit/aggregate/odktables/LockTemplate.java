/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.odktables;

import java.util.Random;
import java.util.UUID;

import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Make datastore locks a little easier. NOT threadsafe.
 *
 * @author the.dylan.price@gmail.com
 *
 */
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
    maxBackoffMs = INITIAL_MAX_BACKOFF;
    for (int i = 0; i < TRIES; i++) {
      if (lock.obtainLock(lockId, tableId, type)) {
        acquired = true;
        break;
      } else {
        try {
          Thread.sleep(getNextBackoff());
        } catch (RuntimeException e) {
          throw new ODKTaskLockException(e);
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
    maxBackoffMs = INITIAL_MAX_BACKOFF;
    for (int i = 0; i < TRIES; i++) {
      if (lock.releaseLock(lockId, tableId, type)) {
        break;
      } else {
        try {
          Thread.sleep(getNextBackoff());
        } catch (RuntimeException e) {
          throw new ODKTaskLockException(e);
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
