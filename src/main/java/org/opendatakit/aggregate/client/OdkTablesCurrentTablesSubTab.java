package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
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

	private OdkTablesTableList tableList;
	
	public OdkTablesCurrentTablesSubTab() {
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