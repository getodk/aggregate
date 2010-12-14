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
package org.opendatakit.aggregate.task;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKTaskLockException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.MiscTasks.Status;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * Common worker implementation for the creation of google spreadsheets.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class WorksheetCreatorWorkerImpl {

	private final Form form;
	private final SubmissionKey miscTasksKey;
	private final Long attemptCount;
	private final String spreadsheetName;
	private final ExternalServiceOption esType;
	private final String baseWebServerUrl;
	private final Datastore datastore;
	private final User user;
	private final String pFormIdLockId;
	
	public WorksheetCreatorWorkerImpl(Form form, 
			SubmissionKey miscTasksKey, long attemptCount, 
			String spreadsheetName, ExternalServiceOption esType,
			String baseWebServerUrl,Datastore datastore, User user) {
		this.form = form;
		this.miscTasksKey = miscTasksKey;
		this.attemptCount = attemptCount;
		this.spreadsheetName = spreadsheetName;
		this.esType = esType;
		this.baseWebServerUrl = baseWebServerUrl;
		this.datastore = datastore;
		this.user = user;
		pFormIdLockId = UUID.randomUUID().toString();
	}

	private final GoogleSpreadsheet getGoogleSpreadsheetWithName() 
						throws ODKDatastoreException {
		List<ExternalService> remoteServers = FormServiceCursor
				.getExternalServicesForForm(form, baseWebServerUrl, datastore,
						user);

		if (remoteServers == null) {
			return null;
		}

		// find spreadsheet with name
		for (ExternalService rs : remoteServers) {
			if (rs instanceof GoogleSpreadsheet) {
				GoogleSpreadsheet sheet = (GoogleSpreadsheet) rs;

				if (sheet.getSpreadsheetName().equals(spreadsheetName)) {
					return sheet;
				}
			}
		}

		return null;
	}

	public final void worksheetCreator() {
		Submission s;
		try {
			s = Submission.fetchSubmission(miscTasksKey.splitSubmissionKey(), datastore, user);
		} catch (Exception e) {
			return;
		}
	    MiscTasks t = new MiscTasks(s);
		// gain lock on the formId itself...
		// the locked resource should be the formId, but for testing
		// it is useful to have the external services collide using 
		// formId.  Prefix with MT: to indicate that it is a miscellaneousTask
		// lock.
		String lockedResourceName = t.getMiscTaskLockName();
		TaskLock formIdTaskLock = datastore.createTaskLock();
		try {
			if (formIdTaskLock.obtainLock(pFormIdLockId, lockedResourceName,
					TaskLockType.WORKSHEET_CREATION)) {
				formIdTaskLock = null;
				doWorksheetCreator();
			}
		} catch (ODKTaskLockException e1) {
			e1.printStackTrace(); // Occasionally expected...
		} catch (Exception e2) {
			// some other unexpected exception...
			e2.printStackTrace();
		} finally {
			formIdTaskLock = datastore.createTaskLock();
			try {
				for (int i = 0; i < 10; i++) {
					if (formIdTaskLock.releaseLock(pFormIdLockId, lockedResourceName,
							TaskLockType.WORKSHEET_CREATION))
						break;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// just move on, this retry mechanism 
						// is to make things nice
					}
				}
			} catch (ODKTaskLockException e) {
				e.printStackTrace();
			}
		}
	}
	
	public final void doWorksheetCreator() {
	  try {
		// get spreadsheet
		GoogleSpreadsheet spreadsheet = getGoogleSpreadsheetWithName();

		// verify form has a spreadsheet element
		if (spreadsheet == null) {
			throw new ODKExternalServiceException("unable to find spreadsheet");
		}

		// generate worksheets
		try {
			spreadsheet.generateWorksheets();
		} catch (Exception e) {
			throw new ODKExternalServiceException(e);
		}

		// the above may have taken a while -- re-fetch the data to see if it has changed...
	    Submission s = Submission.fetchSubmission(miscTasksKey.splitSubmissionKey(), datastore, user);
	    MiscTasks r = new MiscTasks(s);
	    if ( attemptCount.equals(r.getAttemptCount()) ) {
			// if we need to upload submissions, start a task to do so
			if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
				UploadSubmissions uploadTask = (UploadSubmissions) ContextFactory
						.get().getBean(BeanDefs.UPLOAD_TASK_BEAN);
				uploadTask.createFormUploadTask(spreadsheet.getFormServiceCursor(),
						baseWebServerUrl, user);
			}
	    	r.setStatus(Status.SUCCESSFUL);
			r.setCompletionDate(new Date());
			r.objectEntity.persist(datastore, user);
	    }
	  } catch (Exception e ) {
		  failureRecovery(e);
	  }
	}

	private void failureRecovery(Exception e) {
	// three exceptions possible: 
	// ODKFormNotFoundException, ODKDatastoreException, ODKExternalServiceException, Exception
	e.printStackTrace();
    Submission s;
	try {
		s = Submission.fetchSubmission(miscTasksKey.splitSubmissionKey(), datastore, user);
		MiscTasks r = new MiscTasks(s);
	    if ( attemptCount.equals(r.getAttemptCount()) ) {
	    	r.setStatus(Status.FAILED);
	    	r.objectEntity.persist(datastore, user);
	    }
	} catch (Exception ex) {
		// something is hosed -- don't attempt to continue.
		// TODO: watchdog: find this once lastRetryDate is way late?
	}
}
}
