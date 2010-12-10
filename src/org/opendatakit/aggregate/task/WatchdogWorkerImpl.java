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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.User;

/**
 * Common worker implementation for restarting stalled tasks.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class WatchdogWorkerImpl {

	private final long checkIntervalMilliseconds;
	private final String baseWebServerUrl;
	private final Datastore ds;
	private final User user;

	public WatchdogWorkerImpl(long checkIntervalMilliseconds,
			String baseWebServerUrl, Datastore ds, User user) {
		this.checkIntervalMilliseconds = checkIntervalMilliseconds;
		this.baseWebServerUrl = baseWebServerUrl;
		this.ds = ds;
		this.user = user;
	}

	public void checkTasks() throws ODKExternalServiceException,
			ODKFormNotFoundException, ODKDatastoreException {
		checkFormServiceCursors(checkIntervalMilliseconds, baseWebServerUrl,
				ds, user);
		checkPersistentResults(baseWebServerUrl, ds, user);
	}

	private void checkFormServiceCursors(long checkIntervalMilliseconds,
			String baseWebServerUrl, Datastore ds, User user)
			throws ODKExternalServiceException, ODKFormNotFoundException,
			ODKDatastoreException {
		Date olderThanDate = new Date(System.currentTimeMillis()
				- checkIntervalMilliseconds);
		List<FormServiceCursor> fscList = FormServiceCursor
				.queryFormServiceCursorRelation(olderThanDate, ds, user);
		for (FormServiceCursor fsc : fscList) {
			switch (fsc.getExternalServiceOption()) {
			case UPLOAD_ONLY:
				checkUpload(fsc, baseWebServerUrl, user);
				break;
			case STREAM_ONLY:
				checkStreaming(fsc, baseWebServerUrl, ds, user);
				break;
			case UPLOAD_N_STREAM:
				if (!fsc.getUploadCompleted())
					checkUpload(fsc, baseWebServerUrl, user);
				checkStreaming(fsc, baseWebServerUrl, ds, user);
				break;
			case NONE:
				break;
			}
		}
	}

	private void checkUpload(FormServiceCursor fsc, String baseWebServerUrl,
			User user) throws ODKExternalServiceException {
		if (!fsc.getUploadCompleted()) {
			Date lastUploadDate = fsc.getLastUploadCursorDate();
			Date establishmentDate = fsc.getEstablishmentDateTime();
			if (lastUploadDate != null && establishmentDate != null
					&& lastUploadDate.compareTo(establishmentDate) < 0) {
				// there is still work to do
				UploadSubmissions uploadTask = (UploadSubmissions) ContextFactory
						.get().getBean(BeanDefs.UPLOAD_TASK_BEAN);
				uploadTask.createFormUploadTask(fsc, baseWebServerUrl, user);
			}
		}
	}

	private void checkStreaming(FormServiceCursor fsc, String baseWebServerUrl,
			Datastore ds, User user) throws ODKFormNotFoundException,
			ODKDatastoreException, ODKExternalServiceException {
		// get the last submission sent to the external service
		String lastStreamingKey = fsc.getLastStreamingKey();
		List<SubmissionKey> submissionKeyList = new ArrayList<SubmissionKey>();
		submissionKeyList.add(new SubmissionKey(lastStreamingKey));
		Form form = Form.retrieveForm(lastStreamingKey, ds, user);
		// query for last submission submitted for the form
		QueryByDate query = new QueryByDate(form, new Date(System
				.currentTimeMillis()), true, 1, ds, user);
		List<Submission> submissions = query.getResultSubmissions();
		if (submissions != null && submissions.size() == 1) {
			Submission lastSubmission = submissions.get(0);
			String lastSubmissionKey = lastSubmission.getKey().getKey();
			if (!lastStreamingKey.equals(lastSubmissionKey)) {
				// there is still work to do
				UploadSubmissions uploadTask = (UploadSubmissions) ContextFactory
						.get().getBean(BeanDefs.UPLOAD_TASK_BEAN);
				uploadTask.createFormUploadTask(fsc, baseWebServerUrl, user);
			}
		}
	}

	private void checkPersistentResults(String baseWebServerUrl, Datastore ds,
			User user) throws ODKDatastoreException, ODKFormNotFoundException {
		// TODO
		List<PersistentResults> persistentResults = PersistentResults
				.getStalledRequests(ds, user);
		for (PersistentResults persistentResult : persistentResults) {
			long attemptCount = persistentResult.getAttemptCount();
			persistentResult.setAttemptCount(attemptCount + 1);
			persistentResult.persist(ds, user);
			Form form = Form.retrieveForm(persistentResult.getSubmissionKey()
					.toString(), ds, user);
			switch (persistentResult.getResultType()) {
			case CSV:
				CsvGenerator csvGenerator = (CsvGenerator) ContextFactory.get()
						.getBean(BeanDefs.CSV_BEAN);
				csvGenerator.recreateCsvTask(form, persistentResult
						.getSubmissionKey(), attemptCount, baseWebServerUrl,
						ds, user);
				break;
			case KML:
				KmlGenerator kmlGenerator = (KmlGenerator) ContextFactory.get()
						.getBean(BeanDefs.KML_BEAN);
				kmlGenerator.recreateKmlTask(form, persistentResult
						.getSubmissionKey(), attemptCount, baseWebServerUrl,
						ds, user);
				break;
			}
		}
	}

}
