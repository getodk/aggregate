/*
 * Copyright (C) 2013 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client;


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
