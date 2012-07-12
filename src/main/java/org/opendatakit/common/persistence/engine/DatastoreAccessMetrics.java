/**
 * Copyright (C) 2011 University of Washington
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.common.persistence.engine;

import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.utils.WebUtils;

/**
 * Tracks the access patterns for the datastore layer. Useful for identifying
 * inefficient datastore access patterns and minimizing excessive read/write
 * actions.
 * 
 * Note that if you create and delete many tables and don't reuse table names,
 * the tableMap and count arrays will grow without bounds.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public final class DatastoreAccessMetrics {

  private static final Log logger = LogFactory.getLog(DatastoreAccessMetrics.class);

  // map of fully qualified table name to index in counter array.
  private final Map<String, Short> tableMap = new TreeMap<String, Short>();
  private short nextCountIdx = 0;
  private int readCount = 0;

  /**
   * Maintain a tally of which tables those actions were against.
   * 
   * NOTE: this is NOT thread-safe. All accesses should occur only from within
   * synchronized methods.
   */
  private final static class RingBufferCountArray {
    private static final int ARRAY_INCREMENT = 20;
    private int[] countArray = new int[36];

    RingBufferCountArray() {
      clear();
    };

    private void resizeArray(short arrayIdx) {
      if (arrayIdx >= countArray.length) {
        int newLen = ((arrayIdx / ARRAY_INCREMENT) + 1) * ARRAY_INCREMENT;
        int[] newArray = new int[newLen];
        for (int i = 0; i < newLen; ++i) {
          newArray[i] = 0;
        }
        for (int i = 0; i < countArray.length; ++i) {
          newArray[i] = countArray[i];
        }
        countArray = newArray;
        logger.info("Resizing metrics array to " + newLen);
      }
    }

    private void recordUsage(short countArrayIdx, int incCount) {
      // make sure our count array is sized big enough...
      resizeArray(countArrayIdx);

      countArray[countArrayIdx] = countArray[countArrayIdx] + incCount;
    }

    private Integer getUsage(short countArrayIdx) {
      // make sure our count array is sized big enough...
      resizeArray(countArrayIdx);

      return countArray[countArrayIdx];
    }

    private void clear() {
      for (int i = 0; i < countArray.length; ++i) {
        countArray[i] = 0;
      }
    }
  }

  // 20-second dump
  private static final long ACCESS_METRIC_DUMP_INTERVAL = 20 * 1000L;

  private long lastLogging = 0L;
  private final RingBufferCountArray countQueryArray = new RingBufferCountArray();
  private final RingBufferCountArray countQueryResultArray = new RingBufferCountArray();
  private final RingBufferCountArray countGetArray = new RingBufferCountArray();
  private final RingBufferCountArray countPutArray = new RingBufferCountArray();
  private final RingBufferCountArray countDeleteArray = new RingBufferCountArray();

  public DatastoreAccessMetrics() {
  }

  public synchronized void logUsage() {
    long now = System.currentTimeMillis();
    lastLogging = now;
    String gmtDate = WebUtils.iso8601Date(new java.util.Date(now));
    logger.info("---------- " + gmtDate + " @ " + readCount + " ------------");
    for (Map.Entry<String, Short> entry : tableMap.entrySet()) {
      Short idx = entry.getValue();
      logger.info(entry.getKey() + "," + countQueryArray.getUsage(idx) + ","
          + countQueryResultArray.getUsage(idx) + "," + countGetArray.getUsage(idx) + ","
          + countPutArray.getUsage(idx) + "," + countDeleteArray.getUsage(idx));
    }
    logger.info("-----------------------------------------");
    countQueryArray.clear();
    countQueryResultArray.clear();
    countGetArray.clear();
    countPutArray.clear();
    countDeleteArray.clear();
  }

  /**
   * NOTE: This method is NOT thread-safe. Call only from within a synchronized
   * method!
   */
  private void logUsage(int incCount) {
    long now = System.currentTimeMillis();
    int oldReadCount = readCount;
    readCount = readCount + incCount;
    if ((readCount / 500) != (oldReadCount / 500)
        || (lastLogging + ACCESS_METRIC_DUMP_INTERVAL < now)) {
    }
  }

  /**
   * Synchronized - this is the ONLY method that should manipulate the
   * RingBufferCountArray or log usage statistics.
   * 
   * @param fullyQualifiedName
   * @param rbc
   */
  private synchronized void synchronizedRecordUsage(String fullyQualifiedName,
      RingBufferCountArray rbc, int incCount) {
    Short countArrayIdx = tableMap.get(fullyQualifiedName);
    if (countArrayIdx == null) {
      countArrayIdx = nextCountIdx++;
      tableMap.put(fullyQualifiedName, countArrayIdx);
    }

    rbc.recordUsage(countArrayIdx, incCount);
    logUsage(incCount);
  }

  private void recordUsage(String fullyQualifiedName, RingBufferCountArray rbc, int incCount) {
    try {
      synchronizedRecordUsage(fullyQualifiedName, rbc, incCount);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void recordUsage(String fullyQualifiedName, RingBufferCountArray rbc) {
    recordUsage(fullyQualifiedName, rbc, 1);
  }

  private void recordUsage(CommonFieldsBase relation, RingBufferCountArray rbc, int incCount) {
    String fullyQualifiedName = relation.getSchemaName() + "." + relation.getTableName();
    recordUsage(fullyQualifiedName, rbc, incCount);
  }

  private void recordUsage(CommonFieldsBase relation, RingBufferCountArray rbc) {
    recordUsage(relation, rbc, 1);
  }

  public void recordQueryUsage(String specialTableName, int resultCount) {
    recordUsage(specialTableName, countQueryArray);
    recordUsage(specialTableName, countQueryResultArray, resultCount);
  }

  public void recordQueryUsage(CommonFieldsBase relation, int resultCount) {
    recordUsage(relation, countQueryArray);
    recordUsage(relation, countQueryResultArray, resultCount);
  }

  public void recordGetUsage(String specialTableName) {
    recordUsage(specialTableName, countGetArray);
  }

  public void recordGetUsage(CommonFieldsBase relation) {
    recordUsage(relation, countGetArray);
  }

  public void recordPutUsage(String specialTableName) {
    recordUsage(specialTableName, countPutArray);
  }

  public void recordPutUsage(CommonFieldsBase relation) {
    recordUsage(relation, countPutArray);
  }

  public void recordDeleteUsage(String specialTableName) {
    recordUsage(specialTableName, countDeleteArray);
  }

  public void recordDeleteUsage(EntityKey key) {
    recordUsage(key.getRelation(), countDeleteArray);
  }
}
