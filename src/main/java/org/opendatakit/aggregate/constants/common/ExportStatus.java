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

public enum ExportStatus implements Serializable {
  GENERATION_IN_PROGRESS, // created or task is running
  RETRY_IN_PROGRESS, // task is running
  FAILED,    // task completed with failure; retry again later.
  ABANDONED, // task completed with failure; no more retries should occur.
  AVAILABLE; // task completed; results are available.

  private ExportStatus() {
    // GWT
  }

  public String toString() {
    switch (this) {
      case GENERATION_IN_PROGRESS:
        return "Generation in progress";
      case RETRY_IN_PROGRESS:
        return "Retry in progress";
      case FAILED:
        return "Failure - will retry later";
      case ABANDONED:
        return "Failure - abandoned all retry attempts";
      case AVAILABLE:
        return "Dataset Available";
      default:
        throw new IllegalStateException("missing enum case");
    }
  }
}