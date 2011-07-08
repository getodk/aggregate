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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.table.SubmissionTable;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.SaveFilterButton;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class FilterPopup extends PopupPanel{
	
	private final FilterGroup group;
	private final SubmissionTable data;
	private final FlexTable table;
	
	private final ListBox keepRemove;
	private final ListBox rowCol;
	
	private final ListBox columnForRowFilter;
	private final ListBox columnsForColumnFilter;
	private final ListBox filterOp;
	private final TextBox filterValue;
	
	private final Map<String, RowOrCol> rowColMapping;
	private final Map<String, Visibility> visibilityMapping;
	
	public FilterPopup(SubmissionTable submissionData, FilterGroup filterGroup) {
		super(false); //do not close popup when user clicks out of it
		this.group = filterGroup;
		this.data = submissionData;
		
		table = new FlexTable();
		
		//keep or remove
		keepRemove = new ListBox();
		visibilityMapping = new HashMap<String, Visibility>();
		for(Visibility vis : Visibility.values()) {
		  String displayText = vis.getDisplayText();
		  keepRemove.addItem(displayText);
		  visibilityMapping.put(displayText, vis);
		}
		
		//rows or columns
		rowCol = new ListBox();
		rowColMapping = new HashMap<String, RowOrCol>();
		for(RowOrCol type : RowOrCol.values()) {
		  String displayText = type.getDisplayText();
		  rowCol.addItem(displayText);
		  rowColMapping.put(displayText, type);
		}
		
		//where columns
		final Label whereCols = new Label("where column");
		
		//column selection - for row filter
		columnForRowFilter = new ListBox();
		for(int i = 0; i < data.getCellCount(0); i++) {
			columnForRowFilter.addItem(data.getText(0, i));
		}
		
		//columns selection - for column filter
		columnsForColumnFilter = new ListBox(true);
		for(int i = 0; i < data.getCellCount(0); i++) {
			columnsForColumnFilter.addItem(data.getText(0, i));
		}
		
		//comparison operator
		filterOp = new ListBox();
		for(FilterOperation op : FilterOperation.values()) {
		  filterOp.addItem(op.toString());
		}

		//value input
		filterValue = new TextBox();
				
		table.setWidget(0, 0, keepRemove);
		table.setWidget(0, 1, rowCol);
		rowCol.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				if(rowCol.getValue(rowCol.getSelectedIndex()).equals(RowOrCol.ROW.getDisplayText())) {
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
			
		});
		table.setWidget(0, 2, whereCols);
		table.setWidget(0, 3, columnForRowFilter);
		table.setWidget(0, 4, filterOp);
		table.setWidget(0, 5, filterValue);
		table.setWidget(0, 6, new ClosePopupButton(this));
	   table.setWidget(1, 0, new SaveFilterButton(this));
		
		columnForRowFilter.setVisible(true);
		columnsForColumnFilter.setVisible(false);
		whereCols.setVisible(true);
		filterOp.setVisible(true);
		filterValue.setVisible(true);
		setWidget(table);
	}

  public FilterGroup getGroup() {
    return group;
  }

  public Visibility getKeepRemove() {    
    String korr = keepRemove.getValue(keepRemove.getSelectedIndex());
    return visibilityMapping.get(korr);
  }

  public RowOrCol getRowCol() {
    String rowcol = rowCol.getValue(rowCol.getSelectedIndex());
    return rowColMapping.get(rowcol);
  }

  public Column getColumnForRowFilter() {
    String colname = columnForRowFilter.getValue(columnForRowFilter.getSelectedIndex());
    String colencode = "";
    Long colgpsIndex = null;
    for(Column column: data.getHeaders()) {
       if(colname.compareTo(column.getDisplayHeader()) == 0) {
          colencode = column.getColumnEncoding();
          colgpsIndex = column.getGeopointColumnCode();
          break;
       }
    }
    return new Column(colname, colencode, colgpsIndex);
  }

  public List<ColumnFilterHeader> getColumnsForColumnFilter() { 
    List<ColumnFilterHeader> columnfilterheaders = new ArrayList<ColumnFilterHeader>();
    for (int i = columnsForColumnFilter.getSelectedIndex(); i < columnsForColumnFilter.getItemCount(); i++) {
       String colname = "";
       String colencode = "";
       Long colgpsIndex = null;
       if(columnsForColumnFilter.isItemSelected(i)) {
          colname = columnsForColumnFilter.getValue(i);
          for(Column column: data.getHeaders()) {
             if(colname.compareTo(column.getDisplayHeader()) == 0) {
                colencode = column.getColumnEncoding();
                colgpsIndex = column.getGeopointColumnCode();
                break;
             }
          }
          columnfilterheaders.add(new ColumnFilterHeader(colname, colencode, colgpsIndex));
       }
    }
    return columnfilterheaders;
  }

  public FilterOperation getFilterOp() {
    String compare = filterOp.getValue(filterOp.getSelectedIndex());
    return FilterOperation.valueOf(compare);
  }

  public String getFilterValue() {
    return filterValue.getValue();
  }
	
}
