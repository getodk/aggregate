/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.task.tomcat;

import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.WorksheetCreator;
import org.opendatakit.aggregate.task.WorksheetCreatorWorkerImpl;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * This is a singleton bean.  It cannot have any per-request state.
 * It uses a static inner class to encapsulate the per-request state
 * of a running background task.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class WorksheetCreatorImpl implements WorksheetCreator {

	static class WorksheetCreatorRunner implements Runnable {
		final WorksheetCreatorWorkerImpl impl;

		public WorksheetCreatorRunner(String baseWebServerUrl,
				String spreadsheetName, ExternalServiceOption esType,
				Form form, Datastore datastore, User user) {
			impl = new WorksheetCreatorWorkerImpl(baseWebServerUrl,
					spreadsheetName, esType, form, datastore, user);
		}

		@Override
		public void run() {
			try {
				impl.worksheetCreator();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: Problem - decide what to do if an exception occurs
			}
		}
	}

	@Override
	public final void createWorksheetTask(String baseWebServerUrl,
			String spreadsheetName, ExternalServiceOption esType, int delay,
			Form form, Datastore datastore, User user)
			throws ODKExternalServiceException, ODKDatastoreException {

		WorksheetCreatorRunner wr = new WorksheetCreatorRunner( baseWebServerUrl,
				spreadsheetName, esType,
				form, datastore, user );
		System.out.println("THIS IS CREATE WORKSHEET IN TOMCAT");
		AggregrateThreadExecutor exec = AggregrateThreadExecutor
				.getAggregateThreadExecutor();
		exec.execute(wr);
	}
}
