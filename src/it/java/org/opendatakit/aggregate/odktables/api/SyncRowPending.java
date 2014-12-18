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

import java.util.List;

/**
 * Class that holds minimal information necessary for synchronizing instance file attachments.
 * This should enable earlier garbage collection of the SyncRow objects.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class SyncRowPending {
  final private String rowId;
  final private String rowETag;
  final private List<String> uriFragments;
  final private boolean getOnly;
  final private boolean shouldDeleteFiles;
  final private boolean updateState;

  public SyncRowPending(SyncRow syncRow, boolean getOnly, boolean shouldDeleteFiles, boolean updateState) {
    this.rowId = syncRow.getRowId();
    this.rowETag = syncRow.getRowETag();
    this.uriFragments = syncRow.getUriFragments();
    this.getOnly = getOnly;
    this.shouldDeleteFiles = shouldDeleteFiles;
    this.updateState = updateState;
  }
  
  public String getRowId() {
    return rowId;
  }
  
  public String getRowETag() {
    return rowETag;
  }
  
  public List<String> getUriFragments() {
    return uriFragments;
  }
  
  public boolean onlyGetFiles() {
    return getOnly;
  }
  
  public boolean shouldDeleteExtraneousLocalFiles() {
    return shouldDeleteFiles;
  }
  
  public boolean updateSyncState() {
    return updateState;
  }
}