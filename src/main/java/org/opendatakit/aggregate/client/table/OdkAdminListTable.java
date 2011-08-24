package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.widgets.TablesAdminDeleteButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class OdkAdminListTable extends FlexTable {

  private static int DELETE_COLUMN = 0;
  private static String DELETE_HEADING = "Delete";
  private static int USER_COLUMN = 1;
  private static String USER_HEADING = "User";
  private static int ID_COLUMN = 2;
  private static String ID_HEADING = "Phone ID";

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
    setText(0, ID_COLUMN, ID_HEADING);
    getRowFormatter().addStyleName(0, "titleBar");

    for (int i = 0; i < adminList.length; i++) {
      OdkTablesAdmin admin = adminList[i];
      int j = i + 1;
      setWidget(j, DELETE_COLUMN, new TablesAdminDeleteButton(admin.getAggregateUid()));
      setWidget(j, USER_COLUMN, new HTML(admin.getName()));
      setWidget(j, ID_COLUMN, new HTML(admin.getExternalUid()));

      if (j % 2 == 0)
        getRowFormatter().addStyleName(j, "evenTableRow");
    }
  }
}