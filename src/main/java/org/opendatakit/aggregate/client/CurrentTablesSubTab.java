package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.odktables.TableResourceClient;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.entity.TableEntry;

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
public class CurrentTablesSubTab extends AggregateSubTabBase {

	private OdkTablesTableList tableList;
	
	public CurrentTablesSubTab() {
		// vertical
		setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
		
		tableList = new OdkTablesTableList();
		add(tableList);
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
		
		// Set up the callback object
		AsyncCallback<List<TableResourceClient>> callback = 
				new AsyncCallback<List<TableResourceClient>>() {
			@Override
			public void onFailure(Throwable caught) {
				AggregateUI.getUI().reportError(caught);
			}
			
			@Override
			public void onSuccess(List<TableResourceClient> tableList) {
				AggregateUI.getUI().clearError();
				tableList.updateTableList(tableList);
			}
		};
		// Make the call to the service
		SecureGWT.getServerTableService().getTables(info, callback);
	}
}