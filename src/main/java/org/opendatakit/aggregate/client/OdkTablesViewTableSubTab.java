package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesViewTable;
import org.opendatakit.aggregate.client.widgets.AggregateListBox;
import org.opendatakit.aggregate.client.widgets.TableEntryClientListBox;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

/**
 * This class builds the subtab that allows for viewing the ODKTables tables 
 * that are currently present in the datastore.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewTableSubTab extends AggregateSubTabBase {

	/**
	 * This will be the box that lets you choose which of the tables you
	 * are going to view.
	 * @return
	 */
	private ListBox tableBox;
	
	// array list so that you can access with indices reliably
	private final ArrayList<TableEntryClient> currentTables;
	// the box that shows the data
	private OdkTablesViewTable tableData;
	
	/**
	 * Sets up the View Table subtab.
	 */
	public OdkTablesViewTableSubTab() {
		// first construct a copy so you can build the list box before you
		// update it. This seems like bad style.
		currentTables = new ArrayList<TableEntryClient>();
		
		// set up the box so you can only select one and you provide both the
		// table name and ID.
		tableBox = new ListBox();
				//new TableEntryClientListBox(currentTables, false, false,
				//"Select a table to view.");
		tableBox.addChangeHandler(new ChangeHandler() {
			
			public void onChange(ChangeEvent event) {
				int selectedIndex = tableBox.getSelectedIndex();
				if (selectedIndex > 0) {
					updateTableData(selectedIndex);
				}
			}
		});
		
		// now populate the list.
		updateTableList();
		
		tableData = new OdkTablesViewTable();
		
		add(tableBox);
		add(tableData);
		
		
	}
	
	private void updateTableList() {
		SecureGWT.getServerTableService().getTables(new AsyncCallback<List<TableEntryClient>>() {
			
			@Override
			public void onFailure(Throwable caught) {
				AggregateUI.getUI().reportError(caught);
			}
			
			@Override
			public void onSuccess(List<TableEntryClient> tables) {
				AggregateUI.getUI().clearError();
				
				update(tables);
				
			}
		});
	}
	
	@Override
	public boolean canLeave() {
		// sure you can leave.
		return true;
	}
	
	/** 
	 * This should just update the table list. 
	 */
	// does so by calling other methods.
	@Override
	public void update() {
		updateTableList();
	}
	
	public void update(List<TableEntryClient> tables) {
		// clear the old tables
		currentTables.clear();
		// and add the new
		currentTables.addAll(tables);
		
		// now update the list box
		tableBox.clear();
		tableBox.addItem(""); // blank holder to start with no selection
		for(int i = 0; i < currentTables.size(); i++) {
			tableBox.addItem(currentTables.get(i).getTableName());
		}
	}
	
	public void updateTableData(int selectedIndex) {
		// - 1 because you have an extra entry that is the "" holder so
		// that the listbox starts empty. 
		tableData.updateDisplay(currentTables.get(selectedIndex - 1));
		add(tableData);
	}
	

}
