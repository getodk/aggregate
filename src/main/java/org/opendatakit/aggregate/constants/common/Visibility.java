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


public enum Visibility implements Serializable {
  DISPLAY("Display"), HIDE("Hide");

  private String displayText;

  private Visibility() {
    // GWT
  }

  private Visibility(String display) {
    displayText = display;
  }

  public static Visibility historicalConverter(String str) {
    // TODO: remove after a long time after the upgrade
    // this is to allow an upgrade when we change visibility constant names
    if (str.equals("KEEP")) {
      return Visibility.DISPLAY;
    } else if (str.equals("REMOVE")) {
      return Visibility.HIDE;
    } else {
      throw new IllegalArgumentException("Unable to Convert Historical Visibility Values");
    }
  }

  public String getDisplayText() {
    return displayText;
  }

  @Override
  public String toString() {
    return displayText;
  }
}
