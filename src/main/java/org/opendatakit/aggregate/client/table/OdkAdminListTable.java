package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.widgets.TablesAdminDelete;

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

    // create table headers
    setText(0, DELETE_COLUMN, DELETE_HEADING);
    setText(0, USER_COLUMN, USER_HEADING);
    setText(0, ID_COLUMN, ID_HEADING);

    // add styling
    getRowFormatter().addStyleName(0, "titleBar");
    addStyleName("dataTable");
    getElement().setId("form_management_table");
  }

  /**
   * Update the list of admins
   * 
   * @param adminList
   */
  public void updateAdmin(OdkTablesAdmin[] adminList) {
    if(adminList == null) {
      return;
    }
    
    int i = 0;
    
    for (int j = 0; j < adminList.length; j++) {
      OdkTablesAdmin admin = adminList[j];
      setWidget(i, DELETE_COLUMN, new TablesAdminDelete(admin.getAggregateUid()));
      setWidget(i, USER_COLUMN, new HTML(admin.getName()));
      setWidget(i, ID_COLUMN, new HTML(admin.getExternalUid()));

      if (i % 2 == 0)
        getRowFormatter().addStyleName(i, "evenTableRow");
    }

    // remove any trailing rows...
    ++i; // to get number or rows in actual table...
    while (getRowCount() > i) {
      removeRow(getRowCount() - 1);
    }
  }

}