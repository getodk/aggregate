package org.opendatakit.aggregate.client.table;

import java.util.List;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

import org.opendatakit.aggregate.client.odktables.TableResourceClient;
import org.opendatakit.aggregate.client.widgets.TablesAdminDeleteButton;
import org.opendatakit.aggregate.odktables.entity.TableEntry;


/**
 * This is the class that displays the list of the database tables that
 * belong to ODK Tables to the user. It may eventually be the point of interaction
 * to enter the tables and add coarse control. 
 * <br>
 * Based on OdkAdminListTable.
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesTableList extends FlexTable {

	// This will be the column with the table name
	private static int TABLE_NAME_COLUMN = 0;
	private static String TABLE_NAME_HEADING = "Table Name";
	
	public OdkTablesTableList() {
		// add styling
		addStyleName("dataTable");
		getElement().setId("form_management_table");
	}
	
	/**
	 * Updates the list of tables.
	 * @param tableList the list of tables to be displayed.
	 */
	public void updateTableList(List<TableResourceClient> tableList) {
		if (tableList == null) {
			return;
		}
		
		// else we proceed to update.
		// first clear the table
		removeAllRows();
		// now create the table headers
		setText(0, TABLE_NAME_COLUMN, TABLE_NAME_HEADING);
		getRowFormatter().addStyleName(0, "titleBar");
		
		for (int i = 0; i < tableList.size(); i++) {
			TableResourceClient table = tableList.get(i);
			// this will maintain the row you're adding to, always +1 
			// because of the title row
			int j = i + 1;
			setWidget(j, TABLE_NAME_COLUMN, new HTML(table.getTableName()));
			
			if (j % 2 == 0) {
				getRowFormatter().addStyleName(j, "evenTableRow");
			}
		}
	}
}