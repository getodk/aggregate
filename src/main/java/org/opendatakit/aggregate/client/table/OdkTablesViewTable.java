package org.opendatakit.aggregate.client.table;

import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.odktables.RowClient;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;

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

	/**
	 * This is the constructor to call when there has not been a table selected.
	 * Should this even exist?
	 */
	public OdkTablesViewTable() {
		// add styling
		addStyleName("dataTable");
		getElement().setId("form_management_table");	
		
		// no current table.
		this.currentTable = null;
	}
	
	public OdkTablesViewTable(TableEntryClient table) {
		this();
		
		updateDisplay(table);
		
		this.currentTable = table;
	}
	
	
	/**
	 * This updates the display to show the contents of the table.
	 */
	public void updateDisplay(TableEntryClient table) {
		TableEntryClient oldTable = this.currentTable;
		
		this.currentTable = table;
		
		//Window.alert("in updateDisplay");
		GWT.log("in update display");
		
		/*** update the column headings ***/
		updateColumns(table);
		
		// TODO could i have something here to compare etags, perhaps
		// preventing a potentially expensive refresh if it is not
		// necessary?
		
		//Window.alert("past return 2");
		
		/*** update the rows ***/
		
		updateRows(table);


		
		
	
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
				//Window.alert("success rowful");
				//Window.alert(Integer.toString(rowList.size()));
				rows = rowList;
				if (rows.size() > 0) {
					//Window.alert("problem?");
					//Window.alert(rows.get(0).getValues().keySet().toString());
					setRows(rows);
				} else {
					//Window.alert("no rows returns");
				}
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
		
		getRowFormatter().addStyleName(0, "titleBar");			
	}
	
	/*
	 * This will set the row values in the listbox.
	 */
	private void setRows(List<RowClient> rows) {
		int start = 1; // b/c the 0 row is the headings.
		
		// the rows that come through beginning with "_" are user-defined.
		// we only want to display those (as long as you're not displaying 
		// metadata), so select those, remove them, and add them.
		// i is rows, j is columns. fill row by moving to the right, then move on
		for (int i = 0; i < rows.size(); i++) {
			int j = 0; // j is the column counter
			for (String column : columnNames) {
				if (column.substring(0, 1).equalsIgnoreCase("_")) {
					setWidget(start+i, j, new HTML(rows.get(i).getValues().get(column)));
					j++;
				}
				if (j % 2 == 0) {
					getRowFormatter().addStyleName(j, "evenTableRow");
				}
			}
		}
	}
}
