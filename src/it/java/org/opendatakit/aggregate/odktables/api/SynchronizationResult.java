/*
 * Copyright (C) 2014 University of Washington
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
package org.opendatakit.aggregate.odktables.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An object for measuring the results of a synchronization call. This is
 * especially intended to see how to display the results to the user. For
 * example, imagine you wanted to synchronize three tables. The object should
 * contain three TableResult objects, mapping the dbTableName to the status
 * corresponding to outcome.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class SynchronizationResult {

  /**
   * Status code used at app and table levels
   *
   * WORKING should never be returned (the initial value)
   */
  public enum Status {
    WORKING, SUCCESS, FAILURE, AUTH_EXCEPTION, EXCEPTION, TABLE_DOES_NOT_EXIST_ON_SERVER, TABLE_CONTAINS_CHECKPOINTS, TABLE_CONTAINS_CONFLICTS, TABLE_PENDING_ATTACHMENTS, TABLE_REQUIRES_APP_LEVEL_SYNC;
  }

  private Status appLevelStatus = Status.WORKING;

  private final Map<String, TableResult> mResults = new HashMap<String, TableResult>();

  public SynchronizationResult() {
  }

  public Status getAppLevelStatus() {
    return appLevelStatus;
  }

  public void setAppLevelStatus(Status status) {
    this.appLevelStatus = status;
  }

  /**
   * Get all the {@link TableResult} objects in this result.
   * 
   * @return
   */
  public List<TableResult> getTableResults() {
    List<TableResult> r = new ArrayList<TableResult>();
    r.addAll(this.mResults.values());
    Collections.sort(r, new Comparator<TableResult>() {
      @Override
      public int compare(TableResult lhs, TableResult rhs) {
        return lhs.getTableId().compareTo(rhs.getTableId());
      }
    });

    return r;
  }

  public TableResult getTableResult(String tableId) {
    TableResult r = mResults.get(tableId);
    if (r == null) {
      r = new TableResult(tableId);
      mResults.put(tableId, r);
    }
    return r;
  }

}
