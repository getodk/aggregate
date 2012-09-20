package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
import org.opendatakit.aggregate.client.widgets.OdkTablesAddTableButton;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

/**
 * This is the subtab that will house the display of the current ODK Tables tables in
 * the datastore.
 * <br>
 * Based on OdkTablesAdminSubTab.
 * 
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesCurrentTablesSubTab extends AggregateSubTabBase {
	
	//private static final String ADD_FILE_TXT = "Add a table file";
	//private static final String ADD_FILE_TOOLTIP_TXT = "Upload a file";
	//private static final String ADD_FILE_BALLOON_TXT = "Upload a file to be associated with a specific table";
	//private static final String ADD_FILE_BUTTON_TXT = "<img src=\"images/yellow_plus.png\" />" 
	//		+ ADD_FILE_TXT;

	private OdkTablesTableList tableList;
	
	private OdkTablesAddTableButton addButton;
	
	// this is a button for adding a file to be associated with a table.
	//private ServletPopupButton addFileButton;
	
	public OdkTablesCurrentTablesSubTab() {
		// vertical
		setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
		
		tableList = new OdkTablesTableList();
		
		addButton = new OdkTablesAddTableButton();
		
		//addFileButton = new ServletPopupButton(ADD_FILE_BUTTON_TXT, ADD_FILE_TXT,
		//		UIConsts.TABLE_FILE_UPLOAD_SERVLET_ADDR, this, ADD_FILE_TOOLTIP_TXT,
		//		ADD_FILE_BALLOON_TXT);
		
		add(tableList);
		add(addButton);
		//add(addFileButton);
	}
	
	@Override
	public boolean canLeave() {
		return true;
	}

	/**
	 * Update the displayed table page to reflect the contents of the 
	 * datastore.
	 */
	@Override
	public void update() {
		SecureGWT.getServerTableService().getTables(new AsyncCallback<List<TableEntryClient>>() {

			@Override
			public void onFailure(Throwable caught) {
				AggregateUI.getUI().reportError(caught);
			}
			
			@Override
			public void onSuccess(List<TableEntryClient> tables) {
				AggregateUI.getUI().clearError();
				tableList.updateTableList(tables);
				tableList.setVisible(true);
				
				// for some reason this line was making a crazy number of
				// refreshes when you were just sitting on the page.
				//AggregateUI.getUI().getTimer().refreshNow();
			}
		});	
	}
}