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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.FormActionStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.GoogleSpreadsheet;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;

/**
 * Common worker implementation for the creation of google spreadsheets.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class WorksheetCreatorWorkerImpl {

	private final IForm form;
	private final SubmissionKey miscTasksKey;
	private final Long attemptCount;
	private final String spreadsheetName;
	private final ExternalServicePublicationOption esType;
	private final CallingContext cc;
	private final String pFormIdLockId;

	public WorksheetCreatorWorkerImpl(IForm form,
			SubmissionKey miscTasksKey, long attemptCount,
			String spreadsheetName, ExternalServicePublicationOption esType,
			CallingContext cc) {
		this.form = form;
		this.miscTasksKey = miscTasksKey;
		this.attemptCount = attemptCount;
		this.spreadsheetName = spreadsheetName;
		this.esType = esType;
		this.cc = cc;
		pFormIdLockId = UUID.randomUUID().toString();
	}

	private final GoogleSpreadsheet getGoogleSpreadsheetWithName()
						throws ODKDatastoreException {
		List<ExternalService> remoteServers = FormServiceCursor
				.getExternalServicesForForm(form, cc);

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

     Log logger = LogFactory.getLog(WorksheetCreatorWorkerImpl.class);
     logger.info("Beginning Worksheet Creator: " + miscTasksKey.toString() +
                   " form " + form.getFormId());

		MiscTasks t;
		try {
		    t = new MiscTasks(miscTasksKey, cc);
		} catch (Exception e) {
        logger.error("worksheetCreator: " + miscTasksKey.toString() +
            " form " + form.getFormId() + " MiscTasks retrieval exception: " + e.toString());
			return;
		}
		// gain lock on the formId itself...
		// the locked resource should be the formId, but for testing
		// it is useful to have the external services collide using
		// formId.  Prefix with MT: to indicate that it is a miscellaneousTask
		// lock.
	    Datastore ds = cc.getDatastore();
	    User user = cc.getCurrentUser();
		String lockedResourceName = t.getMiscTaskLockName();
		TaskLock formIdTaskLock = ds.createTaskLock(user);

		boolean locked = false;
		try {
			if (formIdTaskLock.obtainLock(pFormIdLockId, lockedResourceName,
					TaskLockType.WORKSHEET_CREATION)) {
				locked = true;
			}
			formIdTaskLock = null;
		} catch (ODKTaskLockException e1) {
			e1.printStackTrace(); // Occasionally expected...
		}

		if(!locked) {
		  return;
		}

		try {
		  if ( t.getRequestDate().before(form.getCreationDate())) {
			  // form is newer, so the task must not refer to this form definition...
			  doMarkAsComplete(t);
		  } else {
			  // worksheet creation request should have been created after the form...
			  doWorksheetCreator(logger);
		  }
		} catch (Exception e2) {
		  // some other unexpected exception...
        logger.error("worksheetCreator: " + miscTasksKey.toString() +
            " form " + form.getFormId() + " Unexpected exception from work body: " + e2.toString());
		  e2.printStackTrace();
		} finally {
			formIdTaskLock = ds.createTaskLock(user);
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

	public void doMarkAsComplete(MiscTasks t) throws ODKEntityPersistException, ODKOverQuotaException {
		// and mark us as completed... (don't delete for audit..).
		t.setCompletionDate(new Date());
		t.setStatus(FormActionStatus.SUCCESSFUL);
		t.persist(cc);
	}

	public final void doWorksheetCreator(Log logger) {
	  try {
		// get spreadsheet
		GoogleSpreadsheet spreadsheet = getGoogleSpreadsheetWithName();

		// verify form has a spreadsheet element
		if (spreadsheet == null) {
			throw new ODKExternalServiceException("unable to find spreadsheet");
		}

		// generate worksheets
		try {
			spreadsheet.generateWorksheets(cc);
         logger.info("doWorksheetCreator: " + miscTasksKey.toString() +
               " form " + form.getFormId() + " Successful worksheet creation!");
		} catch (ODKExternalServiceException e ) {
        logger.error("doWorksheetCreator: " + miscTasksKey.toString() +
            " form " + form.getFormId() + " Exception: " + e.toString());
		  throw e;
		} catch (Exception e) {
	     logger.error("doWorksheetCreator: " + miscTasksKey.toString() +
	                   " form " + form.getFormId() + " Exception: " + e.toString());
			throw new ODKExternalServiceException(e);
		}

		// the above may have taken a while -- re-fetch the data to see if it has changed...
	    MiscTasks r = new MiscTasks(miscTasksKey, cc);
	    if ( attemptCount.equals(r.getAttemptCount()) ) {
	      // still the same attempt...
			// if we need to upload submissions, start a task to do so
	    	UploadSubmissions us = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
			if (!esType.equals(ExternalServicePublicationOption.STREAM_ONLY)) {
				us.createFormUploadTask(spreadsheet.getFormServiceCursor(), true, cc);
			}

			doMarkAsComplete(r);
	    }
	  } catch (Exception e ) {
       logger.error("doWorksheetCreator: " + miscTasksKey.toString() +
           " form " + form.getFormId() + " Initiating failure recovery: " + e.toString());
		  failureRecovery(e);
	  }
	}

	private void failureRecovery(Exception e) {
	// three exceptions possible:
	// ODKFormNotFoundException, ODKDatastoreException, ODKExternalServiceException, Exception
	e.printStackTrace();
	MiscTasks r;
	try {
		r = new MiscTasks(miscTasksKey, cc);
	    if ( attemptCount.equals(r.getAttemptCount()) ) {
	    	r.setStatus(FormActionStatus.FAILED);
	    	r.persist(cc);
	    }
	} catch (Exception ex) {
     Log logger = LogFactory.getLog(WorksheetCreatorWorkerImpl.class);
     logger.error("failureRecovery: " + miscTasksKey.toString() +
         " form " + form.getFormId() + " Exception during failure recovery: " + ex.toString());
		// something is hosed -- don't attempt to continue.
		// TODO: watchdog: find this once lastRetryDate is way late?
	}
}
}
