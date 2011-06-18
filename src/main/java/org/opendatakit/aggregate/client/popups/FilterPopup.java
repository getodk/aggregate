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
import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.table.SubmissionTable;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextBox;

public class FilterPopup extends PopupPanel{
	
	private final FilterGroup group;
	private final SubmissionTable data;
	
	public FilterPopup(SubmissionTable submissionData, FilterGroup filterGroup) {
		super(false); //do not close popup when user clicks out of it
		this.group = filterGroup;
		this.data = submissionData;
		
		
		final FlexTable create = new FlexTable();
		//keep or remove
		final ListBox keepRemove = new ListBox();
		keepRemove.addItem(Visibility.KEEP.toString());
		keepRemove.addItem(Visibility.REMOVE.toString());
		
		//rows or columns
		final ListBox rowCol = new ListBox();
		rowCol.addItem(RowOrCol.ROW.toString());
		rowCol.addItem(RowOrCol.COLUMN.toString());
		
		//where columns
		final Label whereCols = new Label("where column");
		
		//column selection - for row filter
		final ListBox col = new ListBox();
		for(int i = 0; i < data.getCellCount(0); i++) {
			col.addItem(data.getText(0, i));
		}
		
		//columns selection - for column filter
		final ListBox cols = new ListBox(true);
		for(int i = 0; i < data.getCellCount(0); i++) {
			cols.addItem(data.getText(0, i));
		}
		
		//comparison operator
		final ListBox comp = new ListBox();
		comp.addItem(FilterOperation.LESS_THAN.toString());
		comp.addItem(FilterOperation.LESS_THAN_OR_EQUAL.toString());
		comp.addItem(FilterOperation.EQUAL.toString());
		comp.addItem(FilterOperation.GREATER_THAN_OR_EQUAL.toString());
		comp.addItem(FilterOperation.GREATER_THAN.toString());
		//value input
		final TextBox var = new TextBox();
		
		//on exit
		final Button submit = new Button("<img src=\"images/green_check.png\" /> Save Filter");
		submit.addStyleDependentName("positive");
		submit.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				Visibility kr;
				FilterOperation op;
				
				String korr = keepRemove.getValue(keepRemove.getSelectedIndex());
				if(korr.compareTo(Visibility.KEEP.toString()) == 0) {
					kr = Visibility.KEEP;
				} else {
					kr = Visibility.REMOVE;
				}
				
				String rowcol = rowCol.getValue(rowCol.getSelectedIndex());
				if(rowcol.compareTo(RowOrCol.ROW.toString()) == 0) {
					String colname = col.getValue(col.getSelectedIndex());
					String colencode = "";
               Long colgpsIndex = null;
					for(Column column: data.getHeaders()) {
						if(colname.compareTo(column.getDisplayHeader()) == 0) {
							colencode = column.getColumnEncoding();
							colgpsIndex = column.getGeopointColumnCode();
							break;
						}
					}
					
					String compare = comp.getValue(comp.getSelectedIndex());
					if(compare.compareTo(FilterOperation.LESS_THAN.toString()) == 0) {
						op = FilterOperation.LESS_THAN;
					} else if (compare.compareTo(FilterOperation.LESS_THAN_OR_EQUAL.toString()) == 0) {
						op = FilterOperation.LESS_THAN_OR_EQUAL;
					} else if (compare.compareTo(FilterOperation.EQUAL.toString()) == 0) {
						op = FilterOperation.EQUAL;
					} else if (compare.compareTo(FilterOperation.GREATER_THAN_OR_EQUAL.toString()) == 0) {
						op = FilterOperation.GREATER_THAN_OR_EQUAL;
					} else {
						op = FilterOperation.GREATER_THAN;
					}
					
					String variable = var.getValue();
					
					Filter newFilter = new RowFilter(kr, new Column(colname, colencode, colgpsIndex), op, 
							variable, (long) group.getFilters().size());
					group.addFilter(newFilter);
				} else {
					List<ColumnFilterHeader> columnfilterheaders = new ArrayList<ColumnFilterHeader>();
					for (int i = cols.getSelectedIndex(); i < cols.getItemCount(); i++) {
						String colname = "";
						String colencode = "";
						Long colgpsIndex = null;
						if(cols.isItemSelected(i)) {
							colname = cols.getValue(i);
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
					Filter newFilter = new ColumnFilter(kr, columnfilterheaders,(long) group.getFilters().size());
					group.addFilter(newFilter);
				}
				AggregateUI.getUI().getTimer().refreshNow();
				hide();
			}
		
		});
		
		create.setWidget(0, 0, keepRemove);
		create.setWidget(0, 1, rowCol);
		create.setWidget(1, 0, submit);
		rowCol.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				if(rowCol.getValue(rowCol.getSelectedIndex()).compareTo(RowOrCol.ROW.toString()) == 0) {
					create.setWidget(0, 2, whereCols);
					col.setVisible(true);
					cols.setVisible(false);
					whereCols.setVisible(true);
					comp.setVisible(true);
					var.setVisible(true);
				} else {
					create.setWidget(0, 2, cols);
					col.setVisible(false);
					whereCols.setVisible(false);
					cols.setVisible(true);
					comp.setVisible(false);
					var.setVisible(false);
				}
			}
			
		});
		create.setWidget(0, 2, whereCols);
		create.setWidget(0, 3, col);
		create.setWidget(0, 4, comp);
		create.setWidget(0, 5, var);
		create.setWidget(0, 6, new ClosePopupButton(this));
		
		col.setVisible(true);
		cols.setVisible(false);
		whereCols.setVisible(true);
		comp.setVisible(true);
		var.setVisible(true);
		setWidget(create);
	}
	
}
