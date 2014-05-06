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
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.odktables.FileSummaryClient;
import org.opendatakit.aggregate.client.odktables.TableContentsForFilesClient;
import org.opendatakit.aggregate.client.widgets.OdkTablesDeleteFileButton;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Widget;

/**
 * Displays the entries in the DbTableFileInfo table that pertain to
 * the application and not to any specific table.
 *
 * Based upon OdkTablesViewTableFileInfo
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewAppLevelFileInfo extends FlexTable {

  // the table whose info we are currently displaying.
//  private TableEntryClient currentTable;

  // that table's info rows
  private ArrayList<FileSummaryClient> fileSummaries;

  private static final int DELETE_COLUMN = 0;
  // this is the heading for the delete row button.
  private static final String DELETE_HEADING = "Delete";

  private static final int ODK_CLIENT_VERSION_COLUMN = 1;
  private static final String ODK_CLIENT_VERSION_HEADING = "Client Version";
  private static final int FILENAME_COLUMN = 2;
  private static final String FILENAME_HEADING = "Filename";
  private static final int DOWNLOAD_COLUMN = 3;
  private static final String DOWNLOAD_HEADING = "Download";

  private static final int numColumns = 4;

  // this is just the tab that opened the table
  private AggregateSubTabBase basePanel;

  // the message to display when there are no rows in the table
  private static String NO_ROWS_MESSAGE = "There are no rows to display.";

  public OdkTablesViewAppLevelFileInfo(AggregateSubTabBase tableSubTab) {

    setColumnHeadings();

    // add styling
    addStyleName("dataTable");
    getElement().setId("form_management_table");

    this.basePanel = tableSubTab;
  }

  public void updateData() {
    // set up the callback object
    AsyncCallback<TableContentsForFilesClient> getDataCallback = new AsyncCallback<TableContentsForFilesClient>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(TableContentsForFilesClient tcc) {
        removeAllRows();
        setColumnHeadings();
        fileSummaries = tcc.files;
        setRows();
      }
    };

    SecureGWT.getServerDataService().getAppLevelFileInfoContents(getDataCallback);
  }

  private void setColumnHeadings() {
    // create the table headers.
    setText(0, DELETE_COLUMN, DELETE_HEADING);
    setText(0, ODK_CLIENT_VERSION_COLUMN, ODK_CLIENT_VERSION_HEADING);
    setText(0, FILENAME_COLUMN, FILENAME_HEADING);
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
        setWidget(currentRow, DELETE_COLUMN, new OdkTablesDeleteFileButton(this.basePanel,
            "", sum.getId()));
        setText(currentRow, ODK_CLIENT_VERSION_COLUMN, sum.getOdkClientVersion());
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
  }

}
