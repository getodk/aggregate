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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.aggregate.task.UploadSubmissionsWorkerImpl;
import org.opendatakit.common.persistence.Datastore;
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
public class UploadSubmissionsImpl implements UploadSubmissions {

	static class UploadSubmissionsRunner implements Runnable {
		final UploadSubmissionsWorkerImpl impl;

		public UploadSubmissionsRunner(FormServiceCursor fsc,
				String baseWebServerUrl, Datastore datastore, User user) {
			impl = new UploadSubmissionsWorkerImpl(fsc, baseWebServerUrl, datastore, user);
		}

		@Override
		public void run() {
			try {
				impl.uploadAllSubmissions();
			} catch (Exception e) {
				e.printStackTrace();
				// TODO: Problem - decide what to do if an exception occurs
			}
		}
	}

  @Override
  public void createFormUploadTask(FormServiceCursor fsc, String baseWebServerUrl, User user)
      throws ODKExternalServiceException {

	  Datastore datastore = (Datastore) ContextFactory.get().getBean(BeanDefs.DATASTORE_BEAN);
	UploadSubmissionsRunner ur = new UploadSubmissionsRunner(fsc,
				baseWebServerUrl, datastore, user);
    System.out.println("THIS IS UPLOAD TASK IN TOMCAT");
    AggregrateThreadExecutor exec = AggregrateThreadExecutor.getAggregateThreadExecutor();
    exec.execute(ur);
  }
}
