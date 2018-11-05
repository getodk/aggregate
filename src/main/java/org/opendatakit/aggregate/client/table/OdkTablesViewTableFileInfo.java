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

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.AggregateSubTabBase;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesManageTableFilesSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.exception.EntityNotFoundExceptionClient;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.TableContentsForFilesClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.widgets.OdkTablesDeleteTableFileButton;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.common.security.common.GrantedAuthorityName;

/**
 * Displays the entries in the DbTableFileInfo table that pertain to a specific
 * table.
 *
 * @author sudar.sam@gmail.com
 */
public class OdkTablesViewTableFileInfo extends FlexTable {

  private static final int DELETE_COLUMN = 0;
  // this is the heading for the delete row button.
  private static final String DELETE_HEADING = "Delete";
  private static final int ODK_CLIENT_VERSION_COLUMN = 1;
  private static final String ODK_CLIENT_VERSION_HEADING = "Client Version";
  private static final int FILENAME_COLUMN = 2;
  private static final String FILENAME_HEADING = "Filename";
  private static final int CONTENT_LENGTH_COLUMN = 3;
  private static final String CONTENT_LENGTH_HEADING = "Size";
  private static final int CONTENT_TYPE_COLUMN = 4;
  private static final String CONTENT_TYPE_HEADING = "Content Type";
  private static final int DOWNLOAD_COLUMN = 5;
  private static final String DOWNLOAD_HEADING = "Download";
  private static final int numColumns = 6;
  // the message to display when there are no rows in the table
  private static String NO_ROWS_MESSAGE = "There are no rows to display.";
  // the table whose info we are currently displaying.
  private TableEntryClient currentTable;
  // that table's info rows
  private ArrayList<FileSummaryClient> fileSummaries;
  // this is just the tab that opened the table
  private AggregateSubTabBase basePanel;

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
          ((OdkTablesManageTableFilesSubTab) AggregateUI.getUI().getSubTab(SubTabs.MANAGE_TABLE_ID_FILES))
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

        fileSummaries = tcc.files;
        setRows();

        // AggregateUI.getUI().getTimer().refreshNow();
      }
    };

    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
      SecureGWT.getServerDataService().getTableFileInfoContents(table.getTableId(), getDataCallback);
    }
  }

  private void setColumnHeadings() {
    // create the table headers.
    setText(0, DELETE_COLUMN, DELETE_HEADING);
    setText(0, ODK_CLIENT_VERSION_COLUMN, ODK_CLIENT_VERSION_HEADING);
    setText(0, FILENAME_COLUMN, FILENAME_HEADING);
    setText(0, CONTENT_LENGTH_COLUMN, CONTENT_LENGTH_HEADING);
    setText(0, CONTENT_TYPE_COLUMN, CONTENT_TYPE_HEADING);
    setText(0, DOWNLOAD_COLUMN, DOWNLOAD_HEADING);
    getRowFormatter().addStyleName(0, "titleBar");
  }

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
        OdkTablesDeleteTableFileButton deleteButton =
            new OdkTablesDeleteTableFileButton(this.basePanel,
                sum.getOdkClientVersion(), currentTable.getTableId(), sum.getFilename());
        if (!AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES)) {
          deleteButton.setEnabled(false);
        }
        setWidget(currentRow, DELETE_COLUMN, deleteButton);
        setText(currentRow, ODK_CLIENT_VERSION_COLUMN, sum.getOdkClientVersion());
        setText(currentRow, FILENAME_COLUMN, sum.getFilename());
        getFlexCellFormatter().setStyleName(currentRow, FILENAME_COLUMN, "dataLeft");
        setText(currentRow, CONTENT_LENGTH_COLUMN, sum.getContentLength().toString());
        getFlexCellFormatter().setStyleName(currentRow, CONTENT_LENGTH_COLUMN, "dataRight");
        setText(currentRow, CONTENT_TYPE_COLUMN, sum.getContentType());
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
  }

}
