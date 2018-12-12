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

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTMLTable;
import com.google.gwt.user.client.ui.Image;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.widgets.DeleteSubmissionButton;
import org.opendatakit.aggregate.client.widgets.MarkSubmissionCompleteButton;
import org.opendatakit.aggregate.client.widgets.RepeatViewButton;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.web.constants.BasicConsts;

public class SubmissionAdminTable extends FlexTable {

  private static final String BLANK_VALUE = " ";


  public SubmissionAdminTable(SubmissionUISummary summary) {
    ArrayList<Column> tableHeaders = summary.getHeaders();
    ArrayList<SubmissionUI> tableSubmissions = summary.getSubmissions();

    addStyleName("dataTable");
    getElement().setId("submission_table");

    // setup header
    int headerIndex = 0;
    setHTML(0, headerIndex++, BLANK_VALUE); // delete button
    setHTML(0, headerIndex++, BLANK_VALUE); // mark submission as complete
    for (Column column : tableHeaders) {
      setText(0, headerIndex++, column.getDisplayHeader().replace(":", "\n"));
    }
    setHTML(0, headerIndex, BLANK_VALUE);
    setColumnFormatter(new HTMLTable.ColumnFormatter());
    getColumnFormatter().addStyleName(headerIndex, "blank-submission-column");

    getRowFormatter().addStyleName(0, "titleBar");

    // create rows
    int rowPosition = 1;
    for (SubmissionUI row : tableSubmissions) {
      int valueIndex = 0; // index matches to column headers
      int columnPosition = 0; // position matches to position in table

      // add delete button
      DeleteSubmissionButton deleteButton = new DeleteSubmissionButton(row.getSubmissionKeyAsString());
      setWidget(rowPosition, columnPosition, deleteButton);
      columnPosition++;

      // add mark complete button
      MarkSubmissionCompleteButton markCompleteButton = new MarkSubmissionCompleteButton(row.getSubmissionKeyAsString());
      setWidget(rowPosition, columnPosition, markCompleteButton);
      columnPosition++;

      // generate row
      for (final String value : row.getValues()) {
        switch (tableHeaders.get(valueIndex++).getUiDisplayType()) {
          case BINARY:
            if (value == null) {
              setText(rowPosition, columnPosition, BasicConsts.EMPTY_STRING);
            } else {
              Image image = new Image(value + UIConsts.PREVIEW_SET);
              image.addClickHandler(new BinaryPopupClickHandler(value, false));
              setWidget(rowPosition, columnPosition, image);
            }
            break;
          case REPEAT:
            if (value == null) {
              setText(rowPosition, columnPosition, BasicConsts.EMPTY_STRING);
            } else {
              RepeatViewButton repeat = new RepeatViewButton(value);
              setWidget(rowPosition, columnPosition, repeat);
            }
            break;
          default:
            setText(rowPosition, columnPosition, value);
        }
        columnPosition++;
      }
      setHTML(rowPosition, columnPosition, BLANK_VALUE);
      if (rowPosition % 2 == 0) {
        getRowFormatter().setStyleName(rowPosition, "evenTableRow");
      }
      rowPosition++;
    }
  }

}
