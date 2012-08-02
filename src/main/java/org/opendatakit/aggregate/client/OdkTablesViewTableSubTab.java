package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesViewTable;
import org.opendatakit.aggregate.client.widgets.AggregateListBox;
import org.opendatakit.aggregate.client.widgets.OdkTablesDisplayDeletedRowsCheckBox;
import org.opendatakit.aggregate.client.widgets.TableEntryClientListBox;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

/**
 * This class builds the subtab that allows for viewing the ODKTables tables 
 * that are currently present in the datastore.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesViewTableSubTab extends AggregateSubTabBase {

	// this is the panel with the information and the dropdown box
	// that tells you to select a table
	private FlexTable selectTablePanel;
	/**
	 * This will be the box that lets you choose which of the tables you
	 * are going to view.
	 * @return
	 */
	private ListBox tableBox;
	
	private HorizontalPanel topPanel;
	
	// array list so that you can access with indices reliably
	private final ArrayList<TableEntryClient> currentTables;
	// the box that shows the data
	private OdkTablesViewTable tableData;
	
	// whether or not to display the deleted rows
	//private Boolean displayDeleted;
	//private OdkTablesDisplayDeletedRowsCheckBox deletedRowsCheckBox;
	
	// the current table that is being displayed
	private TableEntryClient currentTable;
	
	/**
	 * Sets up the View Table subtab.
	 */
	public OdkTablesViewTableSubTab() {
		
		setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
		
		//displayDeleted = false;
		currentTable = null;
		
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
		
		tableData = new OdkTablesViewTable(this);
		
		selectTablePanel = new FlexTable();
		selectTablePanel.getElement().setId("select_table_panel");
		selectTablePanel.setHTML(0, 0, "<h2 id=\"table_name\"> Select a Table </h2>");
		selectTablePanel.setWidget(0, 1, tableBox);
		
		//deletedRowsCheckBox = new OdkTablesDisplayDeletedRowsCheckBox(this, displayDeleted);
		//selectTablePanel.setWidget(0, 2, deletedRowsCheckBox);
		
		topPanel = new HorizontalPanel();
		topPanel.add(selectTablePanel);
		topPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
		add(topPanel);
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
	
	/* temporarily existed to display deleted rows
	public Boolean getDisplayDeleted() {
		return displayDeleted;
	}
	
	public void setDisplayDeleted(Boolean display) {
		this.displayDeleted = display;
	}*/
	
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
		if (selectedIndex == 0) {
			updateTableData();
		} else {
			currentTable = currentTables.get(selectedIndex - 1);
			tableData.updateDisplay(currentTable);
			
			selectTablePanel.setHTML(1, 0, "<h2 id=\"table_displayed\"> Displaying: </h2>");
			selectTablePanel.setHTML(1, 1, "<h2 id=\table_name\"> " + currentTable.getTableName()
					+ " </h2>");
			add(tableData);
		}
	}
	
	
	public void updateTableData() {
		tableData.updateDisplay(null);
	}
	

}
