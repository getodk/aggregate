package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This is the subtab that will house the display of the current ODK Tables
 * tables in the datastore. <br>
 * Based on OdkTablesAdminSubTab.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesCurrentTablesSubTab extends AggregateSubTabBase {

  private static final String IMPORT_TABLE_TXT = "Import table from CSV";
  private static final String IMPORT_TABLE_TOOLTIP_TEXT =
      "Create a new  table by importing a CSV";
  private static final String IMPORT_TABLE_BALLOON_TXT =
      "Create a new table by importing from a CSV";
  private static final String IMPORT_TABLE_BUTTON_TXT =
      "<img src =\"images/yellow_plus.png\" />Import Table From CSV";

  private OdkTablesTableList tableList;

  //private OdkTablesAddTableButton addButton;

  private ServletPopupButton importTableButton;

  // this is a button for adding a file to be associated with a table.
  // private ServletPopupButton addFileButton;

  public OdkTablesCurrentTablesSubTab() {
    // vertical
    // setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    tableList = new OdkTablesTableList();

    //addButton = new OdkTablesAddTableButton();

    importTableButton = new ServletPopupButton(IMPORT_TABLE_BUTTON_TXT,
        IMPORT_TABLE_TXT, UIConsts.UPLOAD_TABLE_FROM_CSV_SERVLET_ADDR, this,
        IMPORT_TABLE_TOOLTIP_TEXT, IMPORT_TABLE_BALLOON_TXT);

    add(importTableButton);
    add(tableList);

  }

  @Override
  public boolean canLeave() {
    return true;
  }

  /**
   * Update the displayed table page to reflect the contents of the datastore.
   */
  @Override
  public void update() {
    SecureGWT.getServerTableService().getTables(
        new AsyncCallback<List<TableEntryClient>>() {

      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(List<TableEntryClient> tables) {
        AggregateUI.getUI().clearError();
        tableList.updateTableList(tables);
        tableList.setVisible(true);

        // for some reason this line was making a crazy number of
        // refreshes when you were just sitting on the page.
        // AggregateUI.getUI().getTimer().refreshNow();
      }
    });
  }
}