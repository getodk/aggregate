package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.client.odktables.TableResourceClient;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.entity.TableEntry;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

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
		try {
			SecureGWT.getServerTableService().getTables(new AsyncCallback<List<TableEntryClient>>() {

				@Override
				public void onFailure(Throwable caught) {
					AggregateUI.getUI().reportError(caught);
				}
				
				@Override
				public void onSuccess(List<TableEntryClient> tables) {
					AggregateUI.getUI().clearError();
					// weird thing here with calling itself as a param...
					tableList.updateTableList(tables);
				}
			});	
		// is this the right way to handle these exceptions? unsure. isn't that what
		// the onFailure should be doing?
		} catch (PermissionDeniedException e) {
			e.printStackTrace();
		} catch (DatastoreFailureException e) {
			e.printStackTrace();
		} catch (RequestFailureException e) {
			e.printStackTrace();
		} catch (AccessDeniedException e) {
			e.printStackTrace();
		}
	}
}