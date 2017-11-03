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

import org.opendatakit.aggregate.odktables.api.SynchronizationResult.Status;

/**
 * The mapping of a table to the status of its synchronization.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class TableResult {

  private final String mTableId;
  private String mDisplayName;
  private Status mStatus = Status.WORKING;
  private String mMessage = Status.WORKING.name();
  /** Flag if schema was pulled from the server. */
  private boolean mPulledServerSchema;
  /** Flag if properties were pulled from the server. */
  private boolean mPulledServerProps;
  /** Flag if data was pulled from the server. */
  private boolean mPulledServerData;
  /** Flag if properties were pushed to the server. */
  private boolean mPushedLocalProps;
  /** Flag if data was pushed to the server. */
  private boolean mPushedLocalData;
  /** Flag if properties had to be pushed to the server. */
  private boolean mHadLocalPropChanges;
  /** Flag if local data had to be pushed to the server. */
  private boolean mHadLocalDataChanges;
  /** Flag if schema had to be pulled from the server. */
  private boolean mHadServerSchemaChanges;
  /** Flag if properties had to be pulled from the server. */
  private boolean mHadServerPropChanges;
  /** Flat if data had to be pulled from the server. */
  private boolean mHadServerDataChanges;

  private int mServerNumUpserts = 0;
  private int mServerNumDeletes = 0;
  private int mLocalNumInserts = 0;
  private int mLocalNumUpdates = 0;
  private int mLocalNumDeletes = 0;
  private int mLocalNumConflicts = 0;
  private int mLocalNumAttachmentRetries = 0;

  public void incServerUpserts() {
    ++mServerNumUpserts;
  }

  public void incServerDeletes() {
    ++mServerNumDeletes;
  }

  public void incLocalInserts() {
    ++mLocalNumInserts;
  }

  public void incLocalUpdates() {
    ++mLocalNumUpdates;
  }

  public void incLocalDeletes() {
    ++mLocalNumDeletes;
  }

  public void incLocalConflicts() {
    ++mLocalNumConflicts;
  }

  public void incLocalAttachmentRetries() {
    ++mLocalNumAttachmentRetries;
  }

  /**
   * Create a table result with a status of {@link Status#FAILURE}. This should
   * then only be updated in the case of success or exceptions. The boolean
   * flags are initialized to false;
   * 
   * @param dbTableName
   * @param status
   */
  public TableResult(String tableId) {
    this.mTableId = tableId;
    this.mDisplayName = tableId;
    this.mPulledServerData = false;
    this.mPulledServerProps = false;
    this.mPulledServerSchema = false;
    this.mPushedLocalData = false;
    this.mPushedLocalData = false;
    this.mHadLocalDataChanges = false;
    this.mHadLocalPropChanges = false;
    this.mHadServerDataChanges = false;
    this.mHadServerPropChanges = false;
    this.mHadServerSchemaChanges = false;
  }

  public String getTableId() {
    return this.mTableId;
  }

  public void setTableDisplayName(String displayName) {
    this.mDisplayName = displayName;
  }

  public String getTableDisplayName() {
    return this.mDisplayName;
  }

  public Status getStatus() {
    return this.mStatus;
  }

  public boolean pulledServerData() {
    return this.mPulledServerData;
  }

  public boolean pulledServerProperties() {
    return this.mPulledServerProps;
  }

  public boolean pulledServerSchema() {
    return this.mPulledServerSchema;
  }

  public boolean pushedLocalProperties() {
    return this.mPushedLocalProps;
  }

  public boolean pushedLocalData() {
    return this.mPushedLocalData;
  }

  public boolean hadLocalDataChanges() {
    return this.mHadLocalDataChanges;
  }

  public boolean hadLocalPropertiesChanges() {
    return this.mHadLocalPropChanges;
  }

  public boolean serverHadDataChanges() {
    return this.mHadServerDataChanges;
  }

  public boolean serverHadPropertiesChanges() {
    return this.mHadServerPropChanges;
  }

  public boolean serverHadSchemaChanges() {
    return this.mHadServerSchemaChanges;
  }

  public void setPulledServerData(boolean pulledData) {
    this.mPulledServerData = pulledData;
  }

  public void setPulledServerProperties(boolean pulledProperties) {
    this.mPulledServerProps = pulledProperties;
  }

  public void setPulledServerSchema(boolean pulledSchema) {
    this.mPulledServerSchema = pulledSchema;
  }

  public void setPushedLocalProperties(boolean pushedProperties) {
    this.mPushedLocalProps = pushedProperties;
  }

  public void setPushedLocalData(boolean pushedData) {
    this.mPushedLocalData = pushedData;
  }

  public void setHadLocalPropertiesChanges(boolean hadChanges) {
    this.mHadLocalPropChanges = hadChanges;
  }

  public void setHadLocalDataChanges(boolean hadChanges) {
    this.mHadLocalDataChanges = hadChanges;
  }

  public void setServerHadSchemaChanges(boolean serverHadChanges) {
    this.mHadServerSchemaChanges = serverHadChanges;
  }

  public void setServerHadPropertiesChanges(boolean serverHadChanges) {
    this.mHadServerPropChanges = serverHadChanges;
  }

  public void setServerHadDataChanges(boolean serverHadChanges) {
    this.mHadServerDataChanges = serverHadChanges;
  }

  /**
   * Set a message that might be passed back to the user. Likely a place to pass
   * the error message back to the user in case of exceptions.
   * 
   * @param message
   */
  public void setMessage(String message) {
    this.mMessage = message;
  }

  public String getMessage() {
    return this.mMessage;
  }

  /**
   * Update the status of this result.
   * 
   * @param newStatus
   * @throws UnsupportedOperationException
   *           if the satus has been set to {@link Status#EXCEPTION} and the
   *           newStatus is something other than {@link Status#EXCEPTION}.
   */
  public void setStatus(Status newStatus) {
    if (this.mStatus == Status.EXCEPTION && newStatus != Status.EXCEPTION) {
      throw new UnsupportedOperationException("Tried to set TableResult " + "status"
          + " to something other than exception when it had alread been set" + " to exception.");
    }
    this.mStatus = newStatus;
  }

}
