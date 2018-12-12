/*
 * Copyright (C) 2011 University of Washington
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

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.widgets.DeleteExportButton;
import org.opendatakit.aggregate.constants.common.ExportStatus;

/**
 * List all the requests for downloadable documents and their status.
 */
public class ExportTable extends FlexTable {

  private final static int FILE_TYPE = 0;
  private final static int STATUS = 1;
  private final static int TIME_COMPLETED = 2;
  private final static int DOWNLOAD_FILE = 3;
  private final static int DELETE = 4;

  private final static int HEADER_ROW = 1;
  private final static int STARTING_ROW = HEADER_ROW + 1;

  public ExportTable() {
    super();
    this.setHTML(0, 1, "<h2 id=\"form_name\">Exported Files</h2>");
    this.setText(HEADER_ROW, FILE_TYPE, "File Type");
    this.setText(HEADER_ROW, STATUS, "Status");
    this.setText(HEADER_ROW, TIME_COMPLETED, "Time Completed");
    this.setText(HEADER_ROW, DOWNLOAD_FILE, "Download File");
    this.setText(HEADER_ROW, DELETE, "Delete");
    this.addStyleName("exportTable");
    this.getRowFormatter().addStyleName(1, "titleBar");
  }

  public void updateExportPanel(ArrayList<ExportSummary> eS) {
    if (eS == null)
      return;
    while (this.getRowCount() > HEADER_ROW + 1) // need to add one because of the zero index
      this.removeRow(STARTING_ROW);
    for (int i = 0; i < eS.size(); i++) {
      ExportSummary e = eS.get(i);
      if (e.getFileType() != null) {
        this.setText(i + STARTING_ROW, FILE_TYPE, e.getFileType().getDisplayText());
      }
      if (e.getTimeCompleted() != null) {
        this.setText(i + STARTING_ROW, TIME_COMPLETED, e.getTimeCompleted().toString());
      }

      if (e.getStatus() != null) {
        this.setText(i + STARTING_ROW, STATUS, e.getStatus().toString());
        if (e.getResultFile() != null && e.getStatus() == ExportStatus.AVAILABLE) {
          this.setWidget(i + STARTING_ROW, DOWNLOAD_FILE, new HTML(new SafeHtmlBuilder().appendHtmlConstant(e.getResultFile()).toSafeHtml()));
        }
      }
      this.setWidget(i + STARTING_ROW, DELETE, new DeleteExportButton(e));
    }
  }


}
