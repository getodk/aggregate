package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.exception.RequestFailureException;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.common.persistence.client.exception.DatastoreFailureException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;

import com.google.gwt.user.client.rpc.AsyncCallback;

/**
 * This houses methods that are commonly used by ODKTables, making it
 * easy to update them.
 * @author sudars
 *
 */
public class OdkTablesUtils {

	/* not sure how to use utils to do this...
	public static List<TableEntryClient> getTables() {
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
					return tables;
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
	}*/
}
