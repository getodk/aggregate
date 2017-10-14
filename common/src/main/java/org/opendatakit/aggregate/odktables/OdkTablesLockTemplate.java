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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Make datastore locks a little easier. NOT threadsafe.
 *
 * @author the.dylan.price@gmail.com
 * @author wbrunette@gmail.com
 *
 */
public class OdkTablesLockTemplate {
  public enum DelayStrategy {
    SHORT(200), MEDIUM(1200), LONG(2500);

    private final long baseBackOffTime;

    private DelayStrategy(long baseBackOffTime) {
      this.baseBackOffTime = baseBackOffTime;
    }

    public long getBaseBackOffTime() {
      return baseBackOffTime;
    }

  }

  // At 4 tries and 250 baseBackOffTime, the maximum amount of time a single
  // acquire or release can take is: 250 + 500 + 1000 + 2000 = 3750
  private static final int TRIES = 5;

  private String tableId;
  private ODKTablesTaskLockType type;
  private Datastore ds;
  private User user;
  private String lockId;
  private DelayStrategy delay;
  private long maxBackoffMs;
  private Random rand;
  private Log log;

  /**
   * File-manager-specific lock for accessing app-level, table-level and
   * instance-level files and manifests. For app-level and table-level files,
   * the rowId should be an empty string or null. For instance-level files, the
   * rowId is mapped into one of 256 buckets to allow for multiple simultaneous
   * instance-level file accesses.
   * 
   * @param tableId
   * @param rowId
   * @param type
   * @param delay
   *          TODO
   * @param cc
   */
  public OdkTablesLockTemplate(String tableId, String rowId, ODKTablesTaskLockType type,
      DelayStrategy delay, CallingContext cc) {
    String tmpTableId;
    if (rowId == null || rowId.length() == 0) {
      if (tableId.length() == 0) {
        tmpTableId = "app-level|";
      } else {
        tmpTableId = "table-level|" + tableId;
      }
    } else {
      tmpTableId = "row-level|" + tableId + "|" + Integer.toHexString(rowId.hashCode() & 0xff);
    }

    constructorHelper(tmpTableId, type, cc, delay);
  }

  public OdkTablesLockTemplate(String tableId, ODKTablesTaskLockType type, DelayStrategy delay,
      CallingContext cc) {
    constructorHelper(tableId, type, cc, delay);
  }

  private void constructorHelper(String tableId, ODKTablesTaskLockType type, CallingContext cc,
      DelayStrategy delay) {
    this.tableId = tableId;
    this.type = type;
    this.ds = cc.getDatastore();
    this.user = cc.getCurrentUser();
    this.lockId = UUID.randomUUID().toString();
    this.delay = delay;
    this.maxBackoffMs = delay.getBaseBackOffTime();
    this.rand = new Random();
    this.log = LogFactory.getLog(FileManifestManager.class);
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
    maxBackoffMs = delay.getBaseBackOffTime();
    for (int i = 0; i < TRIES; i++) {
      if (lock.obtainLock(lockId, tableId, type)) {
        acquired = true;
        break;
      } else {
        try {
          long backOffTime = getNextBackoff();
          log.info("acquire lock sleeping for: " + backOffTime);
          Thread.sleep(backOffTime);
        } catch (RuntimeException e) {
          throw new ODKTaskLockException(e);
        } catch (Exception e) {
          throw new ODKTaskLockException(e);
        }
      }
    }
    if (!acquired) {
      throw new ODKTaskLockException(
          String.format("Timed out acquiring lock. " + "lockId: %s, tableId: %s, type: %s", lockId,
              tableId, type));
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
    maxBackoffMs = delay.getBaseBackOffTime();
    for (int i = 0; i < TRIES; i++) {
      if (lock.releaseLock(lockId, tableId, type)) {
        break;
      } else {
        try {
          long backOffTime = getNextBackoff();
          log.info("release lock sleeping for: " + backOffTime);
          Thread.sleep(backOffTime);
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
    long backoff = (long) (rand.nextDouble() * delay.getBaseBackOffTime());
    backoff += maxBackoffMs;
    maxBackoffMs *= 2;
    return backoff;
  }

}
