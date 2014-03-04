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
import org.opendatakit.aggregate.client.OdkTablesManageTableFilesSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.TableContentsForFilesClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.popups.OdkTablesMediaFileListPopup;
import org.opendatakit.aggregate.client.widgets.OdkTablesDeleteFileButton;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.odktables.relation.DbTableFileInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the entries in the {@link DbTableFileInfo} table that pertain to a
 * specific table.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewTableFileInfo extends FlexTable {

  // the table whose info we are currently displaying.
  private TableEntryClient currentTable;

  // that table's info rows
  // private List<RowClient> rows;

  private ArrayList<FileSummaryClient> fileSummaries;

  // columnnames.
  private ArrayList<String> columnNames;

  private static final int DELETE_COLUMN = 0;
  // this is the heading for the delete row button.
  private static final String DELETE_HEADING = "Delete";

  private static final int FILENAME_COLUMN = 2;
  private static final String FILENAME_HEADING = "Filename";
  private static final int KEY_COLUMN = 1;
  private static final String KEY_HEADING = "Key";
  private static final int DOWNLOAD_COLUMN = 3;
  private static final String DOWNLOAD_HEADING = "Download";

  private static final int numColumns = 4;

  // this is just the tab that opened the table
  private AggregateSubTabBase basePanel;

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
  public OdkTablesViewTableFileInfo(AggregateSubTabBase tableSubTab) {

    setColumnHeadings();

    // add styling
    addStyleName("dataTable");
    getElement().setId("form_management_table");

    this.basePanel = tableSubTab;

    // no current table.
    this.currentTable = null;
  }

  public OdkTablesViewTableFileInfo(AggregateSubTabBase tableSubTab, TableEntryClient table) {
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
    AsyncCallback<TableContentsForFilesClient> getDataCallback = new AsyncCallback<TableContentsForFilesClient>() {
      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof EntityNotFoundExceptionClient) {
          // if this happens it is PROBABLY, but not necessarily, because
          // we've deleted the table.
          // TODO ensure the correct exception makes it here
          ((OdkTablesManageTableFilesSubTab) AggregateUI.getUI().getSubTab(SubTabs.MANAGEFILES))
              .setTabToDislpayZero();
        } else {
          AggregateUI.getUI().reportError(caught);
        }
      }

      @Override
      public void onSuccess(TableContentsForFilesClient tcc) {
        removeAllRows();
        setColumnHeadings();
        // setColumnHeadings(columnNames);

        fileSummaries = tcc.nonMediaFiles;
        setRows();

        // AggregateUI.getUI().getTimer().refreshNow();
      }
    };

    SecureGWT.getServerDataService().getFileInfoContents(table.getTableId(), getDataCallback);
  }

  private void setColumnHeadings() {
    // create the table headers.
    setText(0, DELETE_COLUMN, DELETE_HEADING);
    setText(0, FILENAME_COLUMN, FILENAME_HEADING);
    setText(0, KEY_COLUMN, KEY_HEADING);
    setText(0, DOWNLOAD_COLUMN, DOWNLOAD_HEADING);
    getRowFormatter().addStyleName(0, "titleBar");
  }

  /*
   * This is the method that actually updates the column headings. It is its own
   * method so that it can be called cleanly in the updateTableData method. If
   * the code is AFTER the call to SecureGWT, as it was at first, you can get
   * null pointer exceptions, as the async callback may have not returned.
   */
  // private void setColumnHeadings(List<String> columns) {
  // this.removeAllRows();
  //
  // //Window.alert(Integer.toString(columns.size()));
  //
  // // If there are no user-defined columns display the message.
  // // Otherwise set the headings.
  // if (columns.size() == NUMBER_ADMIN_COLUMNS) {
  // setText(0, 0, NO_DATA_MESSAGE);
  // } else {
  // // set the delete column
  // setText(0, DELETE_ROW_COLUMN, DELETE_ROW_HEADING);
  // setText(0, MEDIA_FILE_COLUMN, MEDIA_FILE_HEADING);
  // // make the headings
  // int i = 2;
  // for (String name : this.columnNames) {
  // // TODO work on ordering the names properly
  // //
  // // the rows that come through beginning with "_" are user-defined.
  // // we only want to display those (as long as you're not displaying
  // // metadata), so select those, remove them, and add them.
  // if (name.substring(0, 1).equalsIgnoreCase("_")) {
  // setText(0, i, name.substring(1, name.length()));
  // i++;
  // }
  // }
  //
  //
  // getRowFormatter().addStyleName(0, "titleBar");
  // }
  //
  // }

  /*
   * This will set the row values in the listbox.
   */
  private void setRows() {
    int start = 1; // b/c the 0 row is the headings.

    int currentRow = start;
    // Window.alert(Integer.toString(rows.size()));
    // if there are no columns, then we only want to display the no data
    // message.
    // otherwise check if there are no rows.
    if (fileSummaries.size() == 0) {
      setWidget(currentRow, 0, new HTML(NO_ROWS_MESSAGE));
      // make the display fill all the columns you have. this is the total
      // number of
      // user-defined columns +1 for the delete column.
      this.getFlexCellFormatter().setColSpan(1, 0, numColumns);
    } else { // there are rows--display them.

      for (int j = 0; j < fileSummaries.size(); j++) {
        FileSummaryClient sum = fileSummaries.get(j);
        setWidget(currentRow, DELETE_COLUMN, new OdkTablesDeleteFileButton(this.basePanel,
            currentTable.getTableId(), sum.getId()));
        setText(currentRow, KEY_COLUMN, sum.getId());
        setText(currentRow, FILENAME_COLUMN, sum.getFilename());
        Widget downloadCol;
        if (sum.getDownloadUrl() != null) {
          Anchor downloadLink = new Anchor();
          downloadLink.setText("Get");
          downloadLink.setHref(sum.getDownloadUrl());
          downloadCol = downloadLink;
        } else {
          downloadCol = new HTML("");
        }
        setWidget(currentRow, DOWNLOAD_COLUMN, downloadCol);
        if (currentRow % 2 == 0) {
          getRowFormatter().addStyleName(currentRow, "evenTableRow");
        }
        currentRow++;
      }
      while (getRowCount() > currentRow) {
        removeRow(getRowCount() - 1);
      }
    }

    // for (FileSummaryClient summary : fileSummaries) {
    // // the rows that come through beginning with "_" are user-defined.
    // // we only want to display those (as long as you're not displaying
    // // metadata), so select those, remove them, and add them.
    // // i is counter, j is columns. fill row by moving to the right, then
    // // move on
    // // currentRow is the actual current row. this is different than the
    // // counter, b/c
    // // you can also get deleted rows returned.
    //
    // // don't display deleted rows (although the dm doesn't return them at
    // // this point)
    // if (!row.isDeleted()) {
    // // now set the delete button
    // setWidget(currentRow, 0,
    // new OdkTablesDeleteFileButton(this.basePanel,
    // currentTable.getTableId(), row.getRowId()));
    // int j = 1;
    // for (String column : columnNames) {
    // if (column.substring(0, 1).equalsIgnoreCase("_")) {
    // setWidget(currentRow, j, new HTML(row.getValues().get(column)));
    // j++;
    // }
    //
    // if (currentRow % 2 == 0) {
    // getRowFormatter().addStyleName(currentRow, "evenTableRow");
    // }
    // }
    // }
    // currentRow++;
    // }
    // }
  }

  private class MediaFileListClickHandler implements ClickHandler {

    private String tableId;
    private String key;

    public MediaFileListClickHandler(String tableId, String key) {
      this.tableId = tableId;
      this.key = key;
    }

    @Override
    public void onClick(ClickEvent event) {
      OdkTablesMediaFileListPopup mediaListpopup = new OdkTablesMediaFileListPopup(tableId, key);
      mediaListpopup.setPopupPositionAndShow(mediaListpopup.getPositionCallBack());
    }

  }

}
