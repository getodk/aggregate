package org.opendatakit.aggregate.client.table;

import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesViewTableSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.widgets.OdkTablesDeleteRowButton;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class OdkTablesViewTable extends FlexTable {
		
	// the table that we are currently displaying.
	private TableEntryClient currentTable;
	
	// that table's rows
	private List<RowClient> rows;
	
	// that table's column names
	private List<String> columnNames;
	
	// this is the heading for the delete row button.
	private static final String DELETE_ROW_HEADING = "Delete";
	
	private OdkTablesViewTableSubTab tableSubTab;
	
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
	public OdkTablesViewTable(OdkTablesViewTableSubTab tableSubTab) {
		// add styling
		addStyleName("dataTable");
		getElement().setId("form_management_table");
		
		this.tableSubTab = tableSubTab;
		
		// no current table.
		this.currentTable = null;
	}
	
	public OdkTablesViewTable(OdkTablesViewTableSubTab tableSubTab, TableEntryClient table) {
		this(tableSubTab);
		
		updateDisplay(table);
		
		this.currentTable = table;
	}
	
	
	/**
	 * This updates the display to show the contents of the table.
	 */
	public void updateDisplay(TableEntryClient table) {
		TableEntryClient oldTable = this.currentTable;
		
		this.currentTable = table;
		
		if (table == null) {
			this.removeAllRows();
		} else {
			
			/*** update the column headings ***/
			updateColumns(table);
			
			// TODO could i have something here to compare etags, perhaps
			// preventing a potentially expensive refresh if it is not
			// necessary?
			
			//Window.alert("past return 2");
			
			/*** update the rows ***/
			
			updateRows(table);
		}


		
		
	
	}
	
	public void updateRows(TableEntryClient table) {
		// set up the callback object
		AsyncCallback<List<RowClient>> getRowsCallback = new AsyncCallback<List<RowClient>>() {
			@Override
			public void onFailure(Throwable caught) {
				AggregateUI.getUI().reportError(caught);
			}
			
			@Override 
			public void onSuccess(List<RowClient> rowList) {
				rows = rowList;
				setRows(rows);
				
				AggregateUI.getUI().getTimer().refreshNow();

			}
		};
		
		// otherwise, we need to get the data.
		SecureGWT.getServerDataService().getRows(table.getTableId(), getRowsCallback);		
	}
	
	/**
	 * updates the column names.
	 * @param table
	 */
	public void updateColumns(TableEntryClient table) {
		
		AsyncCallback<List<String>> columnNamesCallback = new AsyncCallback<List<String>>() {
			@Override 
			public void onFailure(Throwable caught) {
				AggregateUI.getUI().reportError(caught);
			}
			
			@Override
			public void onSuccess(List<String> columns) {
				columnNames = columns;
				setColumnHeadings(columns);
				
				AggregateUI.getUI().getTimer().refreshNow();

			}
			
			
		};
		

		SecureGWT.getServerDataService().getColumnNames(table.getTableId(), columnNamesCallback);
	
	}
	
	/*
	 * This is the method that actually updates the column headings. It is 
	 * its own method so that it can be called cleanly in the updateTableData
	 * method. If the code is AFTER the call to SecureGWT, as it was at first, you
	 * can get null pointer exceptions, as the async callback may have not returned.
	 */
	private void setColumnHeadings(List<String> columns) {
		this.removeAllRows();
		
		// If there are no user-defined columns display the message. 
		// Otherwise set the headings.
		if (columns.size() == NUMBER_ADMIN_COLUMNS) {
			setText(0, 0, NO_DATA_MESSAGE);
		} else { 
			// make the headings
			int i = 0;
			for (String name : this.columnNames) {
				// the rows that come through beginning with "_" are user-defined.
				// we only want to display those (as long as you're not displaying 
				// metadata), so select those, remove them, and add them.
				if (name.substring(0, 1).equalsIgnoreCase("_")) {
					setText(0, i, name.substring(1, name.length()));
					i++;
				}
			}
			// set the delete column 
			setText(0, i, DELETE_ROW_HEADING);
			
			getRowFormatter().addStyleName(0, "titleBar");
		}
			
	}
	
	/*
	 * This will set the row values in the listbox.
	 */
	private void setRows(List<RowClient> rows) {
		int start = 1; // b/c the 0 row is the headings.
		
		int currentRow = start;
		
		// if there are no columns, then we only want to display the no data message.
		if (columnNames.size() == NUMBER_ADMIN_COLUMNS) {
			return;
		//otherwise check if there are no rows.
		} else if (rows.size() == 0 ) {
			setWidget(currentRow, 0, new HTML(NO_ROWS_MESSAGE));
			// make the display fill all the columns you have. this is the total number of 
			// user-defined columns +1 for the delete column.
			this.getFlexCellFormatter().setColSpan(start+currentRow, 0, columnNames.size() - 
					NUMBER_ADMIN_COLUMNS + 1);
		} else { // there are rows--display them.
			
			for (RowClient row : rows) {
				// the rows that come through beginning with "_" are user-defined.
				// we only want to display those (as long as you're not displaying 
				// metadata), so select those, remove them, and add them.
				// i is counter, j is columns. fill row by moving to the right, then move on
				// currentRow is the actual current row. this is different than the counter, b/c
				// you can also get deleted rows returned.
				
				// don't display deleted rows (although the dm doesn't return them at this point)
				if (!row.isDeleted()) {
					int j = 0;
					for (String column : columnNames) {
						if (column.substring(0, 1).equalsIgnoreCase("_")) {
							setWidget(currentRow, j, new HTML(row.getValues().get(column)));
							j++;
						}
						// now set the delete button
						setWidget(currentRow, j, 
								new OdkTablesDeleteRowButton(currentTable.getTableId(),
										row.getRowId()));
						if (currentRow % 2 == 0) {
							getRowFormatter().addStyleName(currentRow, "evenTableRow");
						}
					}
				}
				currentRow++;
			}
		}
	}
	
	
}
