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

package org.opendatakit.aggregate.client.popups;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.table.SubmissionTable;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ColumnListBox;
import org.opendatakit.aggregate.client.widgets.EnumListBox;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public final class FilterPopup extends AbstractPopupBase {

  private static final String VISIBILITY_TOOLTIP = "Whether to show the column";
  
  private static final String ROW_COL_TOOLTIP = "Filter out columns or rows";

  private static final String COLUMN_TOOLTIP = "Column to be filtered out";
  
  private static final String FILTER_OP_TOOLTIP = "Filter operation to apply";
  
  private static final String APPLY_FILTER_TXT = "<img src=\"images/green_check.png\" /> Apply Filter";
  private static final String APPLY_FILTER_TOOLTIP = "Use the created filter";
  private static final String APPLY_FILTER_HELP_BALLOON = "This will apply the filter specified.  This will"
      + " need to be saved in order to use it at a later time.";

  private final FilterGroup group;
  private final FlexTable table;

  private final EnumListBox<Visibility> visibility;
  private final EnumListBox<RowOrCol> rowCol;
  private final ColumnListBox columnForRowFilter;
  private final ColumnListBox columnsForColumnFilter;
  private final EnumListBox<FilterOperation> filterOp;
  private final TextBox filterValue;

  private final ArrayList<Column> headers;

  private final Label whereCols = new Label("where column");

  public FilterPopup(SubmissionTable submissionData, FilterGroup filterGroup) {
    super(); // do not close popup when user clicks out of it
    
    this.group = filterGroup;
    this.headers = submissionData.getHeaders();

    // keep or remove
    visibility = new EnumListBox<Visibility>(Visibility.values(), VISIBILITY_TOOLTIP);

    // rows or columns
    rowCol = new EnumListBox<RowOrCol>(RowOrCol.values(), ROW_COL_TOOLTIP);
    rowCol.addChangeHandler(new ChangeHandler() {
      public void onChange(ChangeEvent event) {
        updateUIoptions();
      }
    });

    // column selection - for row filter
    columnForRowFilter = new ColumnListBox(headers, COLUMN_TOOLTIP, false);

    // columns selection - for column filter
    columnsForColumnFilter = new ColumnListBox(headers, COLUMN_TOOLTIP, true);

    // comparison operator
    filterOp = new EnumListBox<FilterOperation>(FilterOperation.values(), FILTER_OP_TOOLTIP);

    // value input
    filterValue = new TextBox();

    AggregateButton applyFilter = new AggregateButton(APPLY_FILTER_TXT, APPLY_FILTER_TOOLTIP,
        APPLY_FILTER_HELP_BALLOON);
    applyFilter.addStyleDependentName("positive");
    applyFilter.addClickHandler(new ApplyFilter());

    table = new FlexTable();
    table.setWidget(0, 0, visibility);
    table.setWidget(0, 1, rowCol);
    table.setWidget(0, 2, whereCols);
    table.setWidget(0, 3, columnForRowFilter);
    table.setWidget(0, 4, filterOp);
    table.setWidget(0, 5, filterValue);
    table.setWidget(0, 6, new ClosePopupButton(this));
    table.setWidget(1, 0, applyFilter);

    updateUIoptions();

    setWidget(table);
  }

  public void updateUIoptions() {
    if (rowCol.getSelectedValue().equals(RowOrCol.ROW)) {
      table.setWidget(0, 2, whereCols);
      columnForRowFilter.setVisible(true);
      columnsForColumnFilter.setVisible(false);
      whereCols.setVisible(true);
      filterOp.setVisible(true);
      filterValue.setVisible(true);
    } else {
      table.setWidget(0, 2, columnsForColumnFilter);
      columnForRowFilter.setVisible(false);
      whereCols.setVisible(false);
      columnsForColumnFilter.setVisible(true);
      filterOp.setVisible(false);
      filterValue.setVisible(false);
    }
  }

  private class ApplyFilter implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {

      if (group == null) {
        return;
      }
      
      Visibility kr = visibility.getSelectedValue();
      RowOrCol rowcol = rowCol.getSelectedValue();
      long numFilters = (long) group.getFilters().size();
      
      Filter newFilter;
      if (rowcol.equals(RowOrCol.ROW)) {
        Column column = null;
        ArrayList<Column> columns = columnForRowFilter.getSelectedColumns();
        if (columns.size() > 0) {
          column = columns.get(0);
        }          
        newFilter = new RowFilter(kr, column, filterOp.getSelectedValue(), filterValue.getValue(), numFilters);
      } else {
        ArrayList<Column> columnfilterheaders = columnsForColumnFilter.getSelectedColumns();
        newFilter = new ColumnFilter(kr, columnfilterheaders, numFilters);
      }

      group.addFilter(newFilter);

      hide();
    }
  }
}
