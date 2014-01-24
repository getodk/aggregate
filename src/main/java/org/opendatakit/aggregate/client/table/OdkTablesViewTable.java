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

import java.util.ArrayList;

import org.opendatakit.aggregate.client.AggregateSubTabBase;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesViewTableSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.exception.PermissionDeniedExceptionClient;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableContentsClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.widgets.OdkTablesDeleteRowButton;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

/**
 * Displays the contents of a table.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewTable extends FlexTable {

  // the table that we are currently displaying.
  private TableEntryClient currentTable;

  // that table's rows
  private ArrayList<RowClient> rows;

  // that table's column names
  private ArrayList<String> columnNames;

  // this is the heading for the delete row button.
  private static final String DELETE_ROW_HEADING = "Delete";

  private AggregateSubTabBase tableSubTab;

  // this is the number of columns that exist for a table as returned
  // by the server that are NOT user defined.
  private static final int NUMBER_ADMIN_COLUMNS = 2;

  // the message to display when there is no data in the table.
  private static String NO_DATA_MESSAGE = "There is no data in this table.";
  // the message to display when there are no rows in the table
  private static String NO_ROWS_MESSAGE = "There are no rows to display.";

  /**
   * This is the constructor to call when there has not been a table selected.
   * Should this even exist?
   */
  public OdkTablesViewTable(AggregateSubTabBase tableSubTab) {
    // add styling
    addStyleName("dataTable");
    getElement().setId("form_management_table");

    this.tableSubTab = tableSubTab;

    // no current table.
    this.currentTable = null;
  }

  public OdkTablesViewTable(AggregateSubTabBase tableSubTab,
      TableEntryClient table) {
    this(tableSubTab);

    updateDisplay(table);

    this.currentTable = table;
  }

  /**
   * This updates the display to show the contents of the table.
   */
  public void updateDisplay(TableEntryClient table) {
    @SuppressWarnings("unused")
    TableEntryClient oldTable = this.currentTable;

    // for testing timing
    // Window.alert("in odktablesViewTable.updateDisplay()");

    this.currentTable = table;

    if (table == null) {
      this.removeAllRows();
    } else {

      /*** update the data ***/
      updateData(table);
    }

  }

  public void updateData(TableEntryClient table) {
    // set up the callback object
    AsyncCallback<TableContentsClient> getDataCallback =
        new AsyncCallback<TableContentsClient>() {
      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof EntityNotFoundExceptionClient) {
          // if this happens it is PROBABLY, but not necessarily, because
          // we've deleted the table.
          // TODO ensure the correct exception makes it here
          ((OdkTablesViewTableSubTab) AggregateUI.getUI()
              .getSubTab(SubTabs.VIEWTABLE)).setTabToDislpayZero();
        } else if (caught instanceof PermissionDeniedExceptionClient) {
          // do nothing, b/c it's probably legitimate that you don't get an
          // error if there are rows you're not allowed to see.

        } else {
          AggregateUI.getUI().reportError(caught);
        }
      }

      @Override
      public void onSuccess(TableContentsClient tcc) {
        columnNames = tcc.columnNames;
        setColumnHeadings(columnNames);

        rows = tcc.rows;
        setRows(rows);

      }
    };

    SecureGWT.getServerDataService().getTableContents(table.getTableId(),
        getDataCallback);
  }

  /*
   * public void updateRows(TableEntryClient table) { // set up the callback
   * object AsyncCallback<List<RowClient>> getRowsCallback = new
   * AsyncCallback<List<RowClient>>() {
   *
   * @Override public void onFailure(Throwable caught) {
   * AggregateUI.getUI().reportError(caught); }
   *
   * @Override public void onSuccess(List<RowClient> rowList) { rows = rowList;
   * setRows(rows);
   *
   * AggregateUI.getUI().getTimer().refreshNow();
   *
   * } };
   *
   * // otherwise, we need to get the data.
   * SecureGWT.getServerDataService().getRows(table.getTableId(),
   * getRowsCallback); }
   *
   * /** updates the column names.
   *
   * @param table
   *
   * public void updateColumns(TableEntryClient table) {
   *
   * AsyncCallback<List<String>> columnNamesCallback = new
   * AsyncCallback<List<String>>() {
   *
   * @Override public void onFailure(Throwable caught) {
   * AggregateUI.getUI().reportError(caught); }
   *
   * @Override public void onSuccess(List<String> columns) { columnNames =
   * columns; setColumnHeadings(columns);
   *
   * AggregateUI.getUI().getTimer().refreshNow();
   *
   * }
   *
   *
   * };
   *
   *
   * SecureGWT.getServerDataService().getColumnNames(table.getTableId(),
   * columnNamesCallback);
   *
   * }
   */

  /**
   * This is the method that actually updates the column headings. It is its
   * own method so that it can be called cleanly in the updateTableData method.
   * If the code is AFTER the call to SecureGWT, as it was at first, you can
   * get null pointer exceptions, as the async callback may have not returned.
   */
  private void setColumnHeadings(ArrayList<String> columns) {
    this.removeAllRows();

    // If there are no user-defined columns display the message.
    // Otherwise set the headings.
    if (columns.size() == 0) {
      setText(0, 0, NO_DATA_MESSAGE);
    } else {
      // set the delete column
      setText(0, 0, DELETE_ROW_HEADING);
      int i = 1;
      // make the headings
      for (String name : this.columnNames) {
        // We might have to do checking eventually to ensure metadata columns
        // are only displayed when necessary.
        setText(0, i, name);
        i++;
      }

      getRowFormatter().addStyleName(0, "titleBar");
    }

  }

  /*
   * This will set the row values in the listbox.
   */
  private void setRows(ArrayList<RowClient> rows) {
    int start = 1; // b/c the 0 row is the headings.

    int currentRow = start;

    // if there are no columns, then we only want to display the no data
    // message.
    if (columnNames.size() == 0) {
      return;
      // otherwise check if there are no rows.
    } else if (rows.size() == 0) {
      setWidget(currentRow, 0, new HTML(NO_ROWS_MESSAGE));
      // make the display fill all the columns you have. this is the total
      // number of
      // user-defined columns +1 for the delete column.
      this.getFlexCellFormatter().setColSpan(start + currentRow, 0,
          columnNames.size() + NUMBER_ADMIN_COLUMNS);
    } else { // there are rows--display them.

      for (RowClient row : rows) {
        // the rows that come through beginning with "_" are user-defined.
        // we only want to display those (as long as you're not displaying
        // metadata), so select those, remove them, and add them.
        // i is counter, j is columns. fill row by moving to the right, then
        // move on
        // currentRow is the actual current row. this is different than the
        // counter, b/c
        // you can also get deleted rows returned.

        // don't display deleted rows (although the dm doesn't return them at
        // this point)
        if (!row.isDeleted()) {
          // now set the delete button
          setWidget(currentRow, 0, new OdkTablesDeleteRowButton(this,
              currentTable.getTableId(), row.getRowId()));
          int j = 1;
          for (String column : columnNames) {
            setWidget(currentRow, j, new HTML(row.getValues().get(column)));
            j++;

            if (currentRow % 2 == 0) {
              getRowFormatter().addStyleName(currentRow, "evenTableRow");
            }
          }
        }
        currentRow++;
      }
    }
  }

  /**
   * Returns the view this table is currently displaying.
   */
  public TableEntryClient getCurrentTable() {
    return currentTable;
  }

}
