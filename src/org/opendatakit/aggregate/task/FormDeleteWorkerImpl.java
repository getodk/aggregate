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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.exception.ODKExternalServiceDependencyException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKTaskLockException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.MiscTasks.Status;
import org.opendatakit.aggregate.process.DeleteSubmissions;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.CommonFieldsBase;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.Query;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.TopLevelDynamicBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * Common worker implementation for the deletion of a form.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormDeleteWorkerImpl {

	private final Form form;
	private final SubmissionKey miscTasksKey;
	private final long attemptCount;
	private final String baseWebServerUrl;
	private final Datastore datastore;
	private final User user;
	private final String pFormIdLockId;

	public FormDeleteWorkerImpl(Form form, SubmissionKey miscTasksKey,
			long attemptCount, String baseWebServerUrl, Datastore datastore, User user) {
		this.form = form;
		this.miscTasksKey = miscTasksKey;
		this.attemptCount = attemptCount;
		this.baseWebServerUrl = baseWebServerUrl;
		this.datastore = datastore;
		this.user = user;
		pFormIdLockId = UUID.randomUUID().toString();
	}

	public final void deleteForm() throws ODKDatastoreException,
			ODKFormNotFoundException, ODKExternalServiceDependencyException {

		Submission s = Submission.fetchSubmission(miscTasksKey.splitSubmissionKey(), datastore, user);
	    MiscTasks t = new MiscTasks(s);
		boolean deleted = false;
		// gain lock on the formId itself...
		// the locked resource should be the formId, but for testing
		// it is useful to have the external services collide using 
		// formId.  Prefix with MT: to indicate that it is a miscellaneousTask
		// lock.
		String lockedResourceName = t.getMiscTaskLockName();
		TaskLock formIdTaskLock = datastore.createTaskLock();
		try {
			if (formIdTaskLock.obtainLock(pFormIdLockId, lockedResourceName,
					TaskLockType.FORM_DELETION)) {
				formIdTaskLock = null;
				doDeletion(t);
				deleted = true;
			}
		} catch (ODKTaskLockException e1) {
			e1.printStackTrace();
		} finally {
			if ( !deleted ) {
				// TODO: repost...
				return;
			}
		}
		formIdTaskLock = datastore.createTaskLock();
		try {
			for (int i = 0; i < 10; i++) {
				if (formIdTaskLock.releaseLock(pFormIdLockId, lockedResourceName,
						TaskLockType.FORM_DELETION))
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
	
	/**
	 * Abandon any miscellaneous tasks for this form.
	 * 
	 * @param self
	 * @return
	 * @throws ODKDatastoreException
	 */
	private boolean abandonMiscTasks(MiscTasks self) throws ODKDatastoreException {
		// first, delete all the tasks for this form.
		List<MiscTasks> tasks = MiscTasks.getAllTasksForForm(form, datastore, user);
		for (MiscTasks task : tasks ) {
			if ( task.getUri().equals(self.getUri())) continue; // us!
			if ( task.getStatus() == Status.SUCCESSFUL ) continue;
			if ( task.getStatus() == Status.ABANDONED ) continue;
			String lockedResourceName = task.getMiscTaskLockName();
			if ( lockedResourceName.equals(self.getMiscTaskLockName()) ) {
				// we hold this lock already!
				// abaondon the task...
				task.setStatus(Status.ABANDONED);
				task.setAttemptCount(task.getTaskType().getMaxAttemptCount()+1L);
				task.persist(datastore, user);
			} else {
				// otherwise, gain the lock on the task 
				TaskLock taskLock = datastore.createTaskLock();
				String pLockId = UUID.randomUUID().toString();
				boolean deleted = false;
				try {
					if (taskLock.obtainLock(pLockId, lockedResourceName,
							task.getTaskType().getLockType())) {
						taskLock = null;
						// mark the task as abandoned...
						task.setStatus(Status.ABANDONED);
						task.setAttemptCount(task.getTaskType().getMaxAttemptCount()+1L);
						task.persist(datastore, user);
						deleted = true;
					}
				} catch (ODKTaskLockException e1) {
					e1.printStackTrace();
				} finally {
					if ( !deleted ) {
						// TODO: repost...
						return false;
					}
				}
				// release the lock
				taskLock = datastore.createTaskLock();
				try {
					for (int i = 0; i < 10; i++) {
						if (taskLock.releaseLock(pLockId, lockedResourceName,
								task.getTaskType().getLockType()))
							break;
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							// just move on, this retry mechanism is to only
							// make things
							// nice
						}
					}
				} catch (ODKTaskLockException e) {
					e.printStackTrace();
				}
			}
		}
		return true;
	}

	/**
	 * Abandon any persistent result tasks for this form.
	 * 
	 * @param self
	 * @return
	 * @throws ODKDatastoreException
	 */
	private boolean abandonPersistentResultTasks() throws ODKDatastoreException {
		// first, delete all the tasks for this form.
		List<PersistentResults> tasks = PersistentResults.getAllTasksForForm(form, datastore, user);
		for (PersistentResults task : tasks ) {
			if ( task.getStatus() == PersistentResults.Status.AVAILABLE ) continue;
			if ( task.getStatus() == PersistentResults.Status.ABANDONED ) continue;
			// abandon the task...
			task.setStatus(PersistentResults.Status.ABANDONED);
			task.setAttemptCount(PersistentResults.MAX_RETRY_ATTEMPTS+1L);
			task.persist(datastore, user);
		}
		return true;
	}

	/**
	 * Abandon any external service tasks for this form.
	 * 
	 * @return
	 * @throws ODKDatastoreException
	 */
	private boolean abandonExternalServiceTasks() throws ODKDatastoreException {
		List<ExternalService> services = FormServiceCursor
		.getExternalServicesForForm(form, null, datastore, user);
		
		boolean allDeleted = true;
		for (ExternalService service : services) {
			String uriExternalService = service.getFormServiceCursor()
					.getUri();
			TaskLock taskLock = datastore.createTaskLock();
			String pLockId = UUID.randomUUID().toString();
			boolean deleted = false;
			try {
				if (taskLock.obtainLock(pLockId, uriExternalService,
						TaskLockType.UPLOAD_SUBMISSION)) {
					taskLock = null;
					service.abandon();
					deleted = true;
				}
			} catch (ODKTaskLockException e1) {
				e1.printStackTrace();
			} finally {
				if ( !deleted ) {
					allDeleted = false;
				}
			}
			taskLock = datastore.createTaskLock();
			try {
				for (int i = 0; i < 10; i++) {
					if (taskLock.releaseLock(pLockId, uriExternalService,
							TaskLockType.UPLOAD_SUBMISSION))
						break;
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// just move on, this retry mechanism is to only
						// make things
						// nice
					}
				}
			} catch (ODKTaskLockException e) {
				e.printStackTrace();
			}
		}
		return allDeleted;
	}
	
	/**
	 * we have gained a lock on the form.  Now go through and 
	 * try to delete all other MTs and external services related
	 * to this form. 
	 * @throws ODKDatastoreException 
	 * @throws ODKTaskLockException 
	 */
	private void doDeletion(MiscTasks t) throws ODKDatastoreException, ODKTaskLockException {
		Datastore ds = (Datastore) ContextFactory.get().getBean(
				BeanDefs.DATASTORE_BEAN);
		
		if ( !abandonMiscTasks(t) ) return;
		
		abandonPersistentResultTasks();
		
		if ( !abandonExternalServiceTasks() ) return;

		CommonFieldsBase relation = null;
		relation = form.getTopLevelGroupElement().getFormDataModel()
				.getBackingObjectPrototype();

		if (relation != null) {
			for (;;) {
				// retrieve submissions
				Query surveyQuery = ds.createQuery(relation, user);
				surveyQuery.addSort(relation.lastUpdateDate,
						Query.Direction.DESCENDING);
				surveyQuery
						.addSort(relation.primaryKey, Query.Direction.DESCENDING);
	
				List<? extends CommonFieldsBase> submissionEntities = surveyQuery
						.executeQuery(ServletConsts.FORM_DELETE_RECORD_QUERY_LIMIT);
	
				if (submissionEntities.size() == 0) break;

				List<SubmissionKey> keys = new ArrayList<SubmissionKey>();
				String topLevelGroupName = form.getTopLevelGroupElement()
						.getElementName();
				for (CommonFieldsBase en : submissionEntities) {
					TopLevelDynamicBase tl = (TopLevelDynamicBase) en;
					keys.add(new SubmissionKey(form.getFormId(), tl
							.getModelVersion(), tl.getUiVersion(),
							topLevelGroupName, tl.getUri()));
				}
				DeleteSubmissions delete;
				delete = new DeleteSubmissions(keys, ds, user);
				delete.deleteSubmissions();
				
				t.setLastActivityDate(new Date());
				t.persist(ds, user);
				// renew lock
				
				TaskLock taskLock = ds.createTaskLock();
				// TODO: figure out what to do if this returns false
				taskLock.renewLock(pFormIdLockId, t.getMiscTaskLockName(),
									t.getTaskType().getLockType());
				taskLock = null;
			}
		}

		// we are avoiding strong locking, so some services might
		// have been set up during the deletion.  Delete them.
		if ( !abandonExternalServiceTasks() ) return;
		
		abandonPersistentResultTasks();
		
		// same is true with other miscellaneous tasks.  Delete them.
		if ( !abandonMiscTasks(t) ) return;

		// delete the form.
		form.deleteForm(ds, user);

		// and mark us as completed... (don't delete for audit..).
		t.setCompletionDate(new Date());
		t.setStatus(Status.SUCCESSFUL);
		t.persist(ds, user);
	}
}
