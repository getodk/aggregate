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
import org.opendatakit.aggregate.client.widgets.FilterableColumnListBox;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.TextBox;

public final class FilterPopup extends AbstractPopupBase {

	private static final String VISIBILITY_TOOLTIP = "Whether the filter should show or hide the data.";
	private static final String VISIBILITY_BALLOON = 
		"Select whether your criteria will show or hide the data.";
	private static final String ROW_COL_TOOLTIP = "Filter with columns or rows";
	private static final String ROW_COL_BALLOON = "Select whether you want to filter with columns or rows.";
	private static final String COLUMN_TOOLTIP_RF = "Column to be evaluated upon";
	private static final String COLUMN_BALLOON_RF = "Select the column whose values will be evaluated.";
	private static final String COLUMN_TOOLTIP_CF = "Column to work with";
	private static final String COLUMN_BALLOON_CF = "Select the column(s) to specify in the filter.";
	private static final String FILTER_OP_TOOLTIP = "Filter operation to apply";
	private static final String FILTER_OP_BALLOON = "Select the operation to use in the filter.";

	private static final String APPLY_FILTER_TXT = "<img src=\"images/green_check.png\" /> Apply Filter";
	private static final String APPLY_FILTER_TOOLTIP = "Use the created filter";
	private static final String APPLY_FILTER_HELP_BALLOON = "This will apply the filter specified.  This will"
		+ " need to be saved in order to use it at a later time.";

	private final FilterGroup group;
	private final FlexTable table;
	
	// this will be the standard header
	private final FlexTable topBar;
	// this little widget will keep the "Form: name" together during stretching
	private final FlexTable formDisplay;
	// this will keep the filter and close buttons together during stretching
	private final FlexTable buttons;
	// this will have the filter options
	private final FlexTable optionsBar;
	// this will have the row filter options.
	private final FlexTable rowBar;
	// this will have the column options
	private final FlexTable columnBar;
	// this will be the "create filter" thing
	private final FlexTable creationBar;

	private final EnumListBox<Visibility> visibility;
	private final EnumListBox<RowOrCol> rowCol;
	private final FilterableColumnListBox columnForRowFilter;
	private final ColumnListBox columnsForColumnFilter;
	private final EnumListBox<FilterOperation> filterOp;
	private final TextBox filterValue;

	private final ArrayList<Column> headers;

	public FilterPopup(SubmissionTable submissionData, FilterGroup filterGroup) {
		super(); // do not close popup when user clicks out of it

		this.group = filterGroup;
		this.headers = submissionData.getHeaders();

		// keep or remove
		visibility = new EnumListBox<Visibility>(Visibility.values(), VISIBILITY_TOOLTIP, VISIBILITY_BALLOON);

		// rows or columns
		rowCol = new EnumListBox<RowOrCol>(RowOrCol.values(), ROW_COL_TOOLTIP, ROW_COL_BALLOON);

		// column selection - for row filter
		columnForRowFilter = new FilterableColumnListBox(headers, COLUMN_TOOLTIP_RF, COLUMN_BALLOON_RF);

		// columns selection - for column filter
		columnsForColumnFilter = new ColumnListBox(headers, true, false, COLUMN_TOOLTIP_CF, COLUMN_BALLOON_CF);

		// comparison operator
		filterOp = new EnumListBox<FilterOperation>(FilterOperation.values(), FILTER_OP_TOOLTIP, 
				FILTER_OP_BALLOON);

		// value input
		filterValue = new TextBox();

		AggregateButton applyFilter = new AggregateButton(APPLY_FILTER_TXT, APPLY_FILTER_TOOLTIP,
				APPLY_FILTER_HELP_BALLOON);
		applyFilter.addStyleDependentName("positive");
		applyFilter.addClickHandler(new ApplyFilter());

		table = new FlexTable();
		
		// this will be the header type bar across the top
		topBar = new FlexTable();
		topBar.addStyleName("stretch_header");
		
		formDisplay = new FlexTable();
		formDisplay.setWidget(0, 0, new HTML("<h2>Form:</h2>"));
		formDisplay.setWidget(0, 1, new HTML(filterGroup.getFormId()));
		topBar.setWidget(0, 0, formDisplay);
		topBar.getFlexCellFormatter().setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_LEFT);

		buttons = new FlexTable();
		buttons.setWidget(0, 0, applyFilter);
		buttons.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		buttons.setWidget(0, 1, new ClosePopupButton(this));
		buttons.getFlexCellFormatter().setHorizontalAlignment(0, 3, HasHorizontalAlignment.ALIGN_RIGHT);
		topBar.setWidget(0, 2, buttons);
		topBar.getFlexCellFormatter().setHorizontalAlignment(0, 2, HasHorizontalAlignment.ALIGN_RIGHT);
		
		// the bar with the options. this will house everything that is NOT in the 
		// top header bar.
		optionsBar = new FlexTable();
		optionsBar.addStyleName("flexTableBorderTopStretchWidth");
		
		creationBar = new FlexTable();
		creationBar.setWidget(0, 0, new HTML("<h2>Create filter to "));
		creationBar.setWidget(0, 1, visibility);
		creationBar.setWidget(0, 2, rowCol);
		creationBar.setWidget(0, 3, new HTML("<h2>...</h2>"));
		
		rowBar = new FlexTable();
		rowBar.setWidget(0, 0, new HTML("<h3>...where </h3>"));
		rowBar.setWidget(0, 1, columnForRowFilter);
		rowBar.setWidget(0, 2, filterOp);
		rowBar.setWidget(0, 3, filterValue);
		
		columnBar = new FlexTable();
		columnBar.setWidget(0, 0, new HTML("<h3>...titled</h3>"));
		columnBar.setWidget(0, 1, columnsForColumnFilter);
		
		optionsBar.setWidget(0, 0, creationBar);
		optionsBar.setWidget(1, 0, rowBar);
		optionsBar.setWidget(2, 0, columnBar);
		
		table.setWidget(0, 0, topBar);
		table.setWidget(1, 0, optionsBar);

		// enable actions
		rowCol.addChangeHandler(new ChangeHandler() {
			public void onChange(ChangeEvent event) {
				updateUIoptions();
			}
		});

		updateUIoptions();

		setWidget(table);
	}

	public void updateUIoptions() {
     String rowColChoiceString = rowCol.getSelectedValue();
     RowOrCol rowColChoice = (rowColChoiceString == null) ? null : RowOrCol.valueOf(rowColChoiceString);
     
		if (rowColChoice == RowOrCol.ROW) {
			// they want to filter based on rows, so enable/disable appropriately
			optionsBar.getRowFormatter().setStyleName(1, "enabledTableRow");
			optionsBar.getRowFormatter().setStyleName(2, "disabledTableRow");
			columnForRowFilter.setEnabled(true);
			columnsForColumnFilter.setEnabled(false);
			filterOp.setEnabled(true);
			filterValue.setEnabled(true);
			filterValue.setReadOnly(false);
		} else {
			// they want to filter based on columns, so enable/disable appropriately
			optionsBar.getRowFormatter().setStyleName(1, "disabledTableRow");
			optionsBar.getRowFormatter().setStyleName(2, "enabledTableRow");
			columnForRowFilter.setEnabled(false);
			columnsForColumnFilter.setEnabled(true);
			filterOp.setEnabled(false);
			filterValue.setEnabled(false);
			filterValue.setReadOnly(true);
		}
	}

	private class ApplyFilter implements ClickHandler {
		@Override
		public void onClick(ClickEvent event) {

			if (group == null) {
				return;
			}

         String krString = visibility.getSelectedValue();
         Visibility kr = (krString == null) ? null : Visibility.valueOf(krString);

			String rowColChoiceString = rowCol.getSelectedValue();
		   RowOrCol rowColChoice = (rowColChoiceString == null) ? null : RowOrCol.valueOf(rowColChoiceString);

		   long numFilters = (long) group.getFilters().size();

			if (rowColChoice != null) {
	         Filter newFilter = null;
    			if (rowColChoice == RowOrCol.ROW) {
    				Column column = columnForRowFilter.getSelectedColumn();
    
    	         String filterOpString = filterOp.getSelectedValue();
    	         FilterOperation filterOp = (filterOpString == null) ? null : FilterOperation.valueOf(filterOpString);
    
    	         if ( filterOp != null && column != null) {
    	           newFilter = new RowFilter(kr, column, filterOp, filterValue.getValue(), numFilters);
    	         }
    			} else {
    				ArrayList<Column> columnfilterheaders = columnsForColumnFilter.getSelectedColumns();
    				if ( columnfilterheaders != null && !columnfilterheaders.isEmpty() ) {
    				  newFilter = new ColumnFilter(kr, columnfilterheaders, numFilters);
    				}
    			}
    
    			if ( newFilter != null ) {
    			  group.addFilter(newFilter);
    			}
			}

			hide();
		}
	}
}
