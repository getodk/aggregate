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
 * ACTIVE_PAUSE and ACTIVE_RETRY were added because we wanted to be able to not slow down the watch dog schedule for a single exception
 *
 * @author wbrunette@gmail.com
 */
public enum OperationalStatus implements Serializable {
  ESTABLISHED,
  ACTIVE,
  ACTIVE_PAUSE,
  ACTIVE_RETRY,
  PAUSED,
  COMPLETED,
  ABANDONED,
  BAD_CREDENTIALS;

  private OperationalStatus() {
    // GWT
  }
}