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

package org.opendatakit.aggregate.client;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesViewTableFileInfo;
import org.opendatakit.aggregate.client.widgets.OdkTablesTableIdServletPopupButton;
import org.opendatakit.aggregate.client.widgets.OdkTablesTableIdServletPopupButton.OdkTablesData;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * This class builds the subtab that allows for viewing and managing the files
 * that are associated with ODKTables tables. <br>
 * The idea here is that you will have uploaded files to the table, like an html
 * file with information about how to display a list view for the data in your
 * table. And then you can come to this page to see which files are actually
 * associated with the table, as well as set the keys which will say which file
 * does what.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesManageTableFilesSubTab extends AggregateSubTabBase implements OdkTablesData {

  // this is the panel with the information and the dropdown box
  // that tells you to select a table
  private FlexTable selectTablePanel;

  // the string constants for adding a file
  private static final String ADD_FILE_TXT = "Add a table file";
  private static final String ADD_FILE_TOOLTIP_TXT = "Upload a file";
  private static final String ADD_FILE_BALLOON_TXT = "Upload a file to be associated with a specific table";
  private static final String ADD_FILE_BUTTON_TXT = "<img src=\"images/yellow_plus.png\" />"
      + ADD_FILE_TXT;

  // this is a button for adding a file to be associated with a table.
  private OdkTablesTableIdServletPopupButton addFileButton;
  /**
   * This will be the box that lets you choose which of the tables you are going
   * to view.
   *
   * @return
   */
  private ListBox tableBox;
  /**
   * This is the int in the list box that is selected.
   */
  private int selectedValue;

  private HorizontalPanel topPanel;

  // array list so that you can access with indices reliably
  private final ArrayList<TableEntryClient> currentTables;
  // the box that shows the data
  private OdkTablesViewTableFileInfo tableFileData;

  // the current table that is being displayed
  private TableEntryClient currentTable;

  /**
   * Sets up the View Table subtab.
   */
  public OdkTablesManageTableFilesSubTab() {

    addFileButton = new OdkTablesTableIdServletPopupButton(ADD_FILE_BUTTON_TXT, ADD_FILE_TXT,
        UIConsts.TABLE_FILE_UPLOAD_SERVLET_ADDR, ADD_FILE_TOOLTIP_TXT, ADD_FILE_BALLOON_TXT, this, this);

    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    // displayDeleted = false;
    currentTable = null;

    // first construct a copy so you can build the list box before you
    // update it. This seems like bad style.
    currentTables = new ArrayList<TableEntryClient>();

    // set up the box so you can only select one and you provide both the
    // table name and ID.
    tableBox = new ListBox();
    // new TableEntryClientListBox(currentTables, false, false,
    // "Select a table to view.");
    tableBox.addChangeHandler(new ChangeHandler() {

      public void onChange(ChangeEvent event) {
        int selectedIndex = tableBox.getSelectedIndex();
        // Call this to clear the contents while you are waiting on
        // the response from the server.
        tableFileData.updateDisplay(null);
        currentTable = null;
        selectedValue = selectedIndex;
        updateContentsForSelectedTable();
      }
    });

    tableFileData = new OdkTablesViewTableFileInfo(this);

    selectTablePanel = new FlexTable();
    selectTablePanel.getElement().setId("select_table_panel");
    selectTablePanel.setHTML(0, 0, "<h2 id=\"table_name\"> Select a Table </h2>");
    selectTablePanel.setWidget(0, 1, tableBox);
    selectTablePanel.setWidget(1, 0, addFileButton);

    // deletedRowsCheckBox = new OdkTablesDisplayDeletedRowsCheckBox(this,
    // displayDeleted);
    // selectTablePanel.setWidget(0, 2, deletedRowsCheckBox);

    topPanel = new HorizontalPanel();
    topPanel.add(selectTablePanel);
    topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
    add(topPanel);
    add(tableFileData);

  }

  /**
   * Call this to remove any currently displayed data, set the selected table in
   * the list box to zero, and generally reset this page.
   */
  public void setTabToDislpayZero() {
    selectedValue = 0;
    tableBox.setSelectedIndex(0);
    updateContentsForSelectedTable();
  }

  private void updateTableList() {
    SecureGWT.getServerTableService().getTables(new AsyncCallback<ArrayList<TableEntryClient>>() {

      @Override
      public void onFailure(Throwable caught) {
        if ( caught instanceof AccessDeniedException ) {
          // swallow it...
          AggregateUI.getUI().clearError();
          ArrayList<TableEntryClient> tables = new ArrayList<TableEntryClient>();
          addTablesToListBox(tables);
          tableBox.clear();
          setTabToDislpayZero();
        } else {
          AggregateUI.getUI().reportError(caught);
        }
      }

      @Override
      public void onSuccess(ArrayList<TableEntryClient> tables) {
        AggregateUI.getUI().clearError();

        addTablesToListBox(tables);
        tableBox.setItemSelected(selectedValue, true);

        // This makes the server go crazy with requests.
        // AggregateUI.getUI().getTimer().refreshNow();

      }
    });
  }

  @Override
  public boolean canLeave() {
    // sure you can leave.
    return true;
  }

  /*
   * temporarily existed to display deleted rows public Boolean
   * getDisplayDeleted() { return displayDeleted; }
   *
   * public void setDisplayDeleted(Boolean display) { this.displayDeleted =
   * display; }
   */

  /**
   * This should just update the table list.
   */
  // does so by calling other methods.
  @Override
  public void update() {

    if ( AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {
      updateTableList();
      // this causing trouble
      updateTableData();
    }
  }

  public void addTablesToListBox(ArrayList<TableEntryClient> tables) {
    // clear the old tables
    currentTables.clear();
    // and add the new
    currentTables.addAll(tables);

    // now update the list box
    tableBox.clear();
    tableBox.addItem(""); // blank holder to start with no selection
    for (int i = 0; i < currentTables.size(); i++) {
      tableBox.addItem(currentTables.get(i).getTableId());
    }
  }

  public void updateContentsForSelectedTable() {
    // - 1 because you have an extra entry that is the "" holder so
    // that the listbox starts empty.
    if (this.selectedValue == 0) {
      // if they select 0, clear the table
      tableFileData.updateDisplay(null);
      // we also want to have no curren table.
      currentTable = null;
      // clear the "displaying" thing
      if ( selectTablePanel.getRowCount() > 2 ) {
        selectTablePanel.removeRow(2);
      }
    } else {
      currentTable = currentTables.get(this.selectedValue - 1);
      tableFileData.updateDisplay(currentTable);

      selectTablePanel.setHTML(2, 0, "<h2 id=\"table_displayed\"> Displaying: </h2>");
      selectTablePanel.setHTML(2, 1, "<h2 id=\table_name\"> " + currentTable.getTableId()
          + " </h2>");
      add(tableFileData);
    }
  }

  @Override
  public String getTableId() {
    if ( currentTable == null ) {
      return null;
    } else {
      return currentTable.getTableId();
    }
  }

  public void updateTableData() {
    tableFileData.updateDisplay(currentTable);
  }

  /**
   * Set the table to be displayed. You have to set the it in the selectedValue
   * and update it. O(n), could be improved.
   *
   * @param tableId
   */
  public void setCurrentTable(String tableId) {
    boolean foundTable = false;
    // We want to traverse the list of tables and find the index.
    for (int i = 0; i < currentTables.size(); i++) {
      if (currentTables.get(i).getTableId().equals(tableId)) {
        // +1 because the zeroth spot in the list box is the blank placeholder
        selectedValue = i + 1;
        currentTable = currentTables.get(i);
        updateContentsForSelectedTable();
        foundTable = true;
        break;
      }
    }
    if (!foundTable) {
      selectedValue = 0;
      tableFileData.removeAllRows();
    }
  }

}
