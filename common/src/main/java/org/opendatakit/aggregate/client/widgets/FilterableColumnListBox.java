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

package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.UIDisplayType;

public final class FilterableColumnListBox extends AggregateListBox {

  private final ArrayList<Column> headers;

  public FilterableColumnListBox(ArrayList<Column> headers, String tooltipText, String helpBalloonTxt) {
    super(tooltipText, false, helpBalloonTxt);
    this.headers = headers;

    for (Column header : headers) {
      if (header.getUiDisplayType().equals(UIDisplayType.TEXT)) {
        addItem(header.getDisplayHeader(), header.getColumnEncoding());
      }
    }
  }

  private ArrayList<Column> getSelectedColumns() {
    ArrayList<Column> columnfilterheaders = new ArrayList<Column>();
    for (int i = getSelectedIndex(); i < getItemCount(); i++) {
      if (isItemSelected(i)) {
        String colname = getItemText(i);
        String colencode = getValue(i);
        Long colgpsIndex = null;
        for (Column column : headers) {
          if (colname.equals(column.getDisplayHeader())
              && colencode.equals(column.getColumnEncoding())) {
            colgpsIndex = column.getGeopointColumnCode();
            break;
          }
        }
        columnfilterheaders.add(new Column(colname, colencode, colgpsIndex));
      }
    }
    return columnfilterheaders;
  }

  public Column getSelectedColumn() {
    ArrayList<Column> columns = getSelectedColumns();
    if (columns.size() > 0) {
      return columns.get(0);
    } else {
      return null;
    }
  }

}
