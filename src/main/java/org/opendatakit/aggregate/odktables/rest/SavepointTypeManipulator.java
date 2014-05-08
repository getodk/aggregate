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
package org.opendatakit.aggregate.odktables.rest;

/**
 * A manipulator for the 'savepoint_type' column.
 * This is a manipulator largely because this field is
 * transmitted between the client and the server and
 * enums are difficult to extend.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class SavepointTypeManipulator {
  // values are limited to 10 characters or less!
  private static final String INCOMPLETE = "INCOMPLETE";
  private static final String COMPLETE = "COMPLETE";

  public static boolean isCheckpoint(String savepointType) {
    return savepointType == null;
  }

  public static boolean isIncomplete(String savepointType) {
    return INCOMPLETE.equals(savepointType);
  }

  public static boolean isComplete(String savepointType) {
    return COMPLETE.equals(savepointType);
  }

  public static boolean isSavepointType(String savepointType) {
    return isCheckpoint(savepointType) ||
        isIncomplete(savepointType) ||
        isComplete(savepointType);
  }

  public static String incomplete() {
    return INCOMPLETE;
  }

  public static String complete() {
    return COMPLETE;
  }

  public static String checkpoint() {
    return null;
  }
}
