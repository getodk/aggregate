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

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.OdkTablesTabUI.TablesChangeNotification;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesViewInstanceFileInfo;
import org.opendatakit.aggregate.client.widgets.OdkTablesTableIdServletPopupButton.OdkTablesData;
import org.opendatakit.aggregate.constants.common.UIConsts;

/**
 * This class builds the subtab that allows for viewing and managing the files
 * that are associated with data rows in an ODKTables tables. <br>
 *
 * @author sudar.sam@gmail.com
 */
public class OdkTablesManageInstanceFilesSubTab extends AggregateSubTabBase
    implements OdkTablesData, TablesChangeNotification {

  // array list so that you can access with indices reliably
  private final ArrayList<TableEntryClient> currentTables;
  private OdkTablesTabUI parent;
  // this is the panel with the information and the dropdown box
  // that tells you to select a table
  private FlexTable selectTablePanel;
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
  // the box that shows the data
  private OdkTablesViewInstanceFileInfo tableFileData;

  // the current table that is being displayed
  private TableEntryClient currentTable;

  /**
   * Sets up the View Table subtab.
   */
  public OdkTablesManageInstanceFilesSubTab(OdkTablesTabUI parent) {
    this.parent = parent;

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

    tableFileData = new OdkTablesViewInstanceFileInfo(this);

    selectTablePanel = new FlexTable();
    selectTablePanel.getElement().setId("select_table_panel");
    selectTablePanel.setHTML(0, 0, "<h2 id=\"table_name\"> Select a Table </h2>");
    selectTablePanel.setWidget(0, 1, tableBox);

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
  public void setTabToDisplayZero() {
    selectedValue = 0;
    tableBox.setSelectedIndex(0);
    updateContentsForSelectedTable();
  }

  private boolean updateTableList(ArrayList<TableEntryClient> tables, boolean tableListChanged) {
    boolean realChange = addTablesToListBox(tables, tableListChanged);
    if (tables.isEmpty()) {
      tableBox.clear();
      setTabToDisplayZero();
    } else {
      tableBox.setItemSelected(selectedValue, true);
    }
    return realChange;
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
    parent.update(this);
  }

  public boolean addTablesToListBox(ArrayList<TableEntryClient> tables, boolean tableListChanged) {
    if (currentTables.size() == tables.size() && !tableListChanged &&
        currentTables.containsAll(tables)) {
      // no change...
      return false;
    }

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

    return true;
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
      if (selectTablePanel.getRowCount() > 2) {
        selectTablePanel.removeRow(2);
      }
    } else {
      currentTable = currentTables.get(this.selectedValue - 1);
      tableFileData.updateDisplay(currentTable);

      selectTablePanel.setHTML(2, 0, "<h2 id=\"table_displayed\"> Displaying: </h2>");
      selectTablePanel.setHTML(2, 1, new SafeHtmlBuilder().appendHtmlConstant("<h2 id=\table_name\">").appendEscaped(" " + currentTable.getTableId() + " ").appendHtmlConstant("</h2>").toSafeHtml());
      add(tableFileData);
    }
  }

  @Override
  public String getTableId() {
    if (currentTable == null) {
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

  @Override
  public void updateTableSet(boolean tableListChanged) {
    boolean realChange = updateTableList(parent.getTables(), tableListChanged);
    if (realChange) {
      updateTableData();
    }
  }

}
