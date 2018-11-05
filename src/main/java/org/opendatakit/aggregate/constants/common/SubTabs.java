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

public enum SubTabs implements Serializable {
  /*
   * NB: If you add a subtab here, be sure to also add it to
   * org.opendatakit.aggregate.client.RefreshTimer 's run method, or the
   * subtab's update method will never get called.
   */
  EXPORT("Exported Submissions", "export"),
  FILTER("Filter Submissions", "filter"),
  FORMS("Forms List", "forms"),
  PERMISSIONS("Permissions", "permission"),
  PREFERENCES("Preferences", "preferences"),
  TABLES("ODK Tables Admin", "tablesadmin"),
  PUBLISH("Published Data", "publish"),
  SUBMISSION_ADMIN("Submission Admin", "subadmin"),
  // These fall under the ODKTables Tab
  CURRENTTABLES("Current Tables", "viewCurrentTables"),
  VIEWTABLE("View Table", "viewTable"),
  MANAGE_INSTANCE_FILES("Manage Instance Files", "manageInstanceFiles"),
  MANAGE_TABLE_ID_FILES("Manage Table Files", "manageTableFiles"),
  MANAGE_APP_LEVEL_FILES("Manage App Level Files", "manageAppLevelFiles");

  private String tabLabel;
  private String hashString;

  private SubTabs() {
    // GWT
  }

  private SubTabs(String label, String hash) {
    tabLabel = label;
    hashString = hash;
  }

  public String getTabLabel() {
    return tabLabel;
  }

  public String getHashString() {
    return hashString;
  }
};
