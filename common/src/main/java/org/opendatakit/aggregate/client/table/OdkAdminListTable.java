/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.widgets.TablesAdminDeleteButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class OdkAdminListTable extends FlexTable {

  private static int DELETE_COLUMN = 0;
  private static String DELETE_HEADING = "Delete";
  private static int USER_COLUMN = 1;
  private static String USER_HEADING = "User Name";
  private static int ODK_TABLES_USER_ID_COLUMN = 2;
  private static String ODK_TABLES_USER_HEADING = "User Id";

  public OdkAdminListTable() {
    // add styling
    addStyleName("dataTable");
    getElement().setId("form_management_table");
  }

  /**
   * Update the list of admins
   *
   * @param adminList
   */
  public void updateAdmin(OdkTablesAdmin[] adminList) {
    if (adminList == null) {
      return;
    }

    removeAllRows();
    // create table headers
    setText(0, DELETE_COLUMN, DELETE_HEADING);
    setText(0, USER_COLUMN, USER_HEADING);
    setText(0, ODK_TABLES_USER_ID_COLUMN, ODK_TABLES_USER_HEADING);
    getRowFormatter().addStyleName(0, "titleBar");

    for (int i = 0; i < adminList.length; i++) {
      OdkTablesAdmin admin = adminList[i];
      int j = i + 1;
      setWidget(j, DELETE_COLUMN, new TablesAdminDeleteButton(admin.getUriUser()));
      setWidget(j, USER_COLUMN, new HTML(admin.getName()));
      setWidget(j, ODK_TABLES_USER_ID_COLUMN, new HTML(admin.getOdkTablesUserId()));

      if (j % 2 == 0)
        getRowFormatter().addStyleName(j, "evenTableRow");
    }
  }
}