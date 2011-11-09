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

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.datanucleus.sco.simple.Date;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.utils.WebUtils;

/**
 * Tracks the access patterns for the datastore layer.  Useful for identifying inefficient
 * datastore access patterns and minimizing excessive read/write actions.
 * 
 * Note that if you create and delete many tables and don't reuse table names, the
 * tableMap and count arrays will grow without bounds.  
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class DatastoreAccessMetrics {
  
  private static final Log logger = LogFactory.getLog(DatastoreAccessMetrics.class);
  
  // map of fully qualified table name to index in counter array.
  private final Map<String, Short> tableMap = new TreeMap<String, Short>();
  private short nextCountIdx = 0;
  
  /**
   * Manage a ring buffer of the last MAX_RING_BUFFER_SIZE actions.
   * Maintain tally of which tables those actions were against.
   *
   */
  private static class RingBufferCountArray {
    private static final int MAX_RING_BUFFER_SIZE = 1000;
    private final List<Short> ringBuffer = new LinkedList<Short>();
    
    private final List<Integer> countArray = new ArrayList<Integer>();
    
    RingBufferCountArray() {};
    
    public synchronized void recordUsage( Short countArrayIdx ) {
      // make sure our count array is sized big enough...
      while ( countArrayIdx >= countArray.size()) {
        countArray.add(0);
      }
      
      if ( ringBuffer.size() >= MAX_RING_BUFFER_SIZE ) {
        Short idxStale = ringBuffer.get(0);
        countArray.set(idxStale, countArray.get(idxStale) - 1);
        ringBuffer.remove(0);
      }
      ringBuffer.add(countArrayIdx);
      countArray.set(countArrayIdx, countArray.get(countArrayIdx) + 1);
    }
    
    public synchronized Integer getUsage(Short countArrayIdx) {
      // make sure our count array is sized big enough...
      while ( countArrayIdx >= countArray.size()) {
        countArray.add(0);
      }

      return countArray.get(countArrayIdx);
    }
  }
  
  private final RingBufferCountArray countQueryArray = new RingBufferCountArray();
  private final RingBufferCountArray countGetArray = new RingBufferCountArray();
  private final RingBufferCountArray countPutArray = new RingBufferCountArray();
  private final RingBufferCountArray countDeleteArray = new RingBufferCountArray();
  
  public DatastoreAccessMetrics() {
  }
  
  private synchronized void recordUsage( String fullyQualifiedName, RingBufferCountArray rbc ) {
    Short countArrayIdx = tableMap.get(fullyQualifiedName);
    if ( countArrayIdx == null ) {
      countArrayIdx = nextCountIdx++;
      tableMap.put(fullyQualifiedName, countArrayIdx);
    }
    
    rbc.recordUsage(countArrayIdx);
  }
  
  private void recordUsage( CommonFieldsBase relation, RingBufferCountArray rbc ) {
    String fullyQualifiedName = relation.getSchemaName() + "." + relation.getTableName();
    recordUsage( fullyQualifiedName, rbc);
  }
  
  private static final long ACCESS_METRIC_DUMP_INTERVAL = 10*1000L;
  private long lastLogging = 0L;
  private long synchronizedLastLogging = 0L;
  
  private synchronized void synchronizedLogUsage() {
    if ( synchronizedLastLogging + ACCESS_METRIC_DUMP_INTERVAL < lastLogging ) {
      synchronizedLastLogging = lastLogging;
      
      String gmtDate = WebUtils.iso8601Date(new java.util.Date());
      logger.info("---------- " + gmtDate + " ------------");
      for ( Map.Entry<String, Short> entry : tableMap.entrySet() ) {
        Short idx = entry.getValue();
        logger.info(entry.getKey() + 
            "," + countQueryArray.getUsage(idx) + 
            "," + countGetArray.getUsage(idx) + 
            "," + countPutArray.getUsage(idx) + 
            "," + countDeleteArray.getUsage(idx));
      }
      logger.info("-----------------------------------------");
    }
  }
  
  private void logUsage() {
    if ( lastLogging + ACCESS_METRIC_DUMP_INTERVAL < System.currentTimeMillis() ) {
      lastLogging = System.currentTimeMillis();
      synchronizedLogUsage();
    }
  }
  
  public void recordQueryUsage( String specialTableName ) {
    recordUsage( specialTableName, countQueryArray );
    logUsage();
  }
  
  public void recordQueryUsage(  CommonFieldsBase relation ) {
    recordUsage( relation, countQueryArray );
    logUsage();
  }

  public void recordGetUsage( String specialTableName ) {
    recordUsage( specialTableName, countGetArray );
    logUsage();
  }
    
  public void recordGetUsage(  CommonFieldsBase relation ) {
    recordUsage( relation, countGetArray );
    logUsage();
  }

  public void recordPutUsage( String specialTableName ) {
    recordUsage( specialTableName, countPutArray );
    logUsage();
  }
  
  public void recordPutUsage(  CommonFieldsBase relation ) {
    recordUsage( relation, countPutArray );
    logUsage();
  }

  public void recordDeleteUsage( String specialTableName ) {
    recordUsage( specialTableName, countDeleteArray );
    logUsage();
  }
  
  public void recordDeleteUsage(  EntityKey key ) {
    recordUsage( key.getRelation(), countDeleteArray );
    logUsage();
  }
}
