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
import java.util.logging.Logger;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.ExternalServiceOption;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.query.submission.QueryByDateRange;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;

/**
 * Common worker implementation for the publishing of data to an external service.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UploadSubmissionsWorkerImpl {

	private static final int MAX_QUERY_LIMIT = ServletConsts.FETCH_LIMIT;
	private static final int DELAY_BETWEEN_RELEASE_RETRIES = 1000;
	private static final int MAX_NUMBER_OF_RELEASE_RETRIES = 10;
	private static final int SUBMISSIONS_PER_LOCK_RENEWAL = 25;

	private final String lockId;
	private final CallingContext cc;
	private final FormServiceCursor pFsc;
	private final ExternalServiceOption pEsOption;
	private ExternalService pExtService;
	private Form form;

	public UploadSubmissionsWorkerImpl(FormServiceCursor fsc, CallingContext cc) {
		pFsc = fsc;
		this.cc = cc;
		pEsOption = fsc.getExternalServiceOption();
		lockId = UUID.randomUUID().toString();
	}

	public String getUploadSubmissionsTaskLockName() {
		return pFsc.getUri();
	}
	
	public void uploadAllSubmissions()
			throws ODKEntityNotFoundException, ODKExternalServiceException,
			ODKFormNotFoundException, ODKTaskLockException {

		pExtService = pFsc.getExternalService(cc);
		form = Form.retrieveForm(pFsc.getFormId(), cc);

		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		TaskLock taskLock = ds.createTaskLock(user);
		if (!taskLock
				.obtainLock(lockId, getUploadSubmissionsTaskLockName(), TaskLockType.UPLOAD_SUBMISSION)) {
			return;
			// TODO: what should happen if you can't obtain a lock
			// TODO: come back and think about this
			// createFormUploadTask(fsc, user);
		}
		taskLock = null;

		if ( !pFsc.isExternalServicePrepared() ) return;
		if ( pFsc.getOperationalStatus() != OperationalStatus.ACTIVE ) return;
		
		try {
			switch (pEsOption) {
			case UPLOAD_ONLY:
				if (pFsc.getUploadCompleted()) {
					// leave the record so we know action has occurred.
					Logger.getLogger(UploadSubmissionsWorkerImpl.class.getName())
						.warning("Upload completed for UPLOAD_ONLY but formServiceCursor operational status slow to be revised");
					// update this value here, but it should have already been set...
					pFsc.setOperationalStatus(OperationalStatus.COMPLETED);
					ds.putEntity(pFsc, user);
				} else {
					uploadSubmissions();
				}
				break;
			case STREAM_ONLY:
				streamSubmissions();
				break;
			case UPLOAD_N_STREAM:
				if (!pFsc.getUploadCompleted()) {
					uploadSubmissions();
				} else {
					streamSubmissions();
				}
				break;
			default:
				break;
			}
		} catch (Exception e) {
			// TODO: do something smarter with exceptions
			throw new ODKExternalServiceException(e);
		} finally {
			taskLock = ds.createTaskLock(user);
			for (int i = 0; i < MAX_NUMBER_OF_RELEASE_RETRIES; i++) {
				if (taskLock.releaseLock(lockId, getUploadSubmissionsTaskLockName(),
						TaskLockType.UPLOAD_SUBMISSION))
					break;
				try {
					Thread.sleep(DELAY_BETWEEN_RELEASE_RETRIES);
				} catch (InterruptedException e) {
					// just move on, this retry mechanism is to only make things
					// nice
				}
			}
		}
	}

	private void uploadSubmissions() throws Exception {

		Date startDate = pFsc.getLastUploadCursorDate();
		if (startDate == null) {
			startDate = BasicConsts.EPOCH;
		}

		Date endDate = pFsc.getEstablishmentDateTime();
		// submissions are queried by the lastUpdateDate, since the submissionDate
		// marks the initiation of the upload, but it may not have completed and 
		// been marked as uploaded until later.  This is particularly significant for
		// debrief-uploaded data, which preserves the submissionDate, but would have 
		// a much-later creationDate and lastUpdatedDate.
		List<Submission> submissions = querySubmissionsDateRange(startDate,
				endDate);
		String lastUploadKey = pFsc.getLastUploadKey();
		if (lastUploadKey != null && !submissions.isEmpty()) {
			submissions = getRemainingSubmissions(lastUploadKey, submissions);
			// check if all submissions were removed as already uploaded, if
			// true then try to get a new batch.
			if (submissions.isEmpty()) {
				startDate = submissions.get(submissions.size() - 1).getLastUpdateDate();
				submissions = querySubmissionsDateRange(startDate, endDate);
			}
		}
		if (submissions.isEmpty()) {
			// there are no submissions so uploading is complete
			pExtService.setUploadCompleted(cc);
		} else {
			sendSubmissions(submissions, false);
		}

		// create another task to either start streaming
		// OR to delete if upload ONLY
		UploadSubmissions uploadSubmissionsBean = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
		uploadSubmissionsBean.createFormUploadTask(pFsc, cc);
	}

	private void streamSubmissions() throws ODKFormNotFoundException,
			ODKIncompleteSubmissionData, ODKDatastoreException,
			ODKExternalServiceException {

		Date startDate = pFsc.getLastStreamingCursorDate();
		if (startDate == null) {
			startDate = pFsc.getEstablishmentDateTime();
		}

		List<Submission> submissions = querySubmissionsStartDate(startDate);
		String lastStreamedKey = pFsc.getLastStreamingKey();
		if (lastStreamedKey != null && !submissions.isEmpty()) {
			submissions = getRemainingSubmissions(lastStreamedKey, submissions);
			// check if all submissions were removed as already uploaded, if
			// true then try to get a new batch.  NOTE: uses LastUpdateDate.
			// See longer comment above.
			if (submissions.isEmpty()) {
				startDate = submissions.get(submissions.size() - 1).getLastUpdateDate();
				submissions = querySubmissionsStartDate(startDate);
			}
		}
		if (!submissions.isEmpty()) {
			sendSubmissions(submissions, true);
		}
	}

	private List<Submission> getRemainingSubmissions(String lastUploadKey,
			List<Submission> submissions) {
	  
		// find the last submission sent, so we don't resend records
		int indexOfLastSubmission = -1;
		for (int i = 0; i < submissions.size(); i++) {
			if (submissions.get(i).getKey().getKey().equals(lastUploadKey))
				indexOfLastSubmission = i;
		}
		if (indexOfLastSubmission > -1) {
			// we found the last submission that was sent, so now we can send
			// all the submission after that one
			return submissions.subList(indexOfLastSubmission + 1, submissions
					.size());
		} else {
			return submissions;
		}
	}

	private void sendSubmissions(List<Submission> submissionsToSend,
			boolean streaming) throws ODKExternalServiceException {
		Date lastDateSent = null;
		String lastKeySent = null;
		Datastore ds = cc.getDatastore();
		User user = cc.getCurrentUser();
		try {
			int counter = 0;
			for (Submission submission : submissionsToSend) {
				pExtService.sendSubmission(submission, cc);
				// See QueryByDate
				// -- we are querying by the lastUpdateDate, not the
				// creationDate.
				lastDateSent = submission.getLastUpdateDate();
				lastKeySent = submission.getKey().getKey();
				if (streaming) {
					pFsc.setLastStreamingCursorDate(lastDateSent);
					pFsc.setLastStreamingKey(lastKeySent);
				} else {
					pFsc.setLastUploadCursorDate(lastDateSent);
					pFsc.setLastUploadKey(lastKeySent);
				}
				ds.putEntity(pFsc, user);

				// after a certain amount of submissions sent renew lock
				if (++counter >= SUBMISSIONS_PER_LOCK_RENEWAL) {
					TaskLock taskLock = ds.createTaskLock(user);
					// TODO: figure out what to do if this returns false
					taskLock.renewLock(lockId, getUploadSubmissionsTaskLockName(),
							TaskLockType.UPLOAD_SUBMISSION);
					taskLock = null;
					counter = 0;
				}
			}
		} catch (Exception e) {

			throw new ODKExternalServiceException(e);
		}

	}

	private List<Submission> querySubmissionsDateRange(Date startDate,
			Date endDate) throws ODKFormNotFoundException,
			ODKIncompleteSubmissionData, ODKDatastoreException {
		// query for next set of submissions
		QueryByDateRange query = new QueryByDateRange(form, MAX_QUERY_LIMIT,
				startDate, endDate, cc);
		List<Submission> submissions = query.getResultSubmissions(cc);

		// here so we don't have to do null checks on the rest of the code in
		// this
		// class
		if (submissions == null) {
			submissions = new ArrayList<Submission>();
		}
		return submissions;
	}

	private List<Submission> querySubmissionsStartDate(Date startDate)
			throws ODKFormNotFoundException, ODKIncompleteSubmissionData,
			ODKDatastoreException {
		// query for next set of submissions
		QueryByDate query = new QueryByDate(form, startDate, false,
				MAX_QUERY_LIMIT, cc);
		List<Submission> submissions = query.getResultSubmissions(cc);

		// here so we don't have to do null checks on the rest of the code in
		// this
		// class
		if (submissions == null) {
			submissions = new ArrayList<Submission>();
		}
		return submissions;
	}
}
