/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.constants.common;

import java.io.Serializable;

/**
 * Status of a MiscTasks action.
 *
 * @author user
 */
public enum FormActionStatus implements Serializable {

  IN_PROGRESS("Active"), // created or task is running
  RETRY_IN_PROGRESS("Retrying"), // task is running
  FAILED("Scheduled"),    // task completed with failure; retry again later.
  ABANDONED("Failed"), // task completed with failure; no more retries should occur.
  SUCCESSFUL("Successful"); // task completed successfully.

  private String displayText;

  private FormActionStatus() {
    // GWT
  }

  private FormActionStatus(String display) {
    displayText = display;
  }

  /**
   * @return true if this request is considered 'active'
   */
  public boolean isActiveRequest() {
    return (this != ABANDONED) && (this != SUCCESSFUL);
  }
}
