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

import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * Common worker implementation for restarting stalled tasks.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class WatchdogWorkerImpl {

  public void checkTasks(long checkIntervalMilliseconds, CallingContext cc) throws ODKExternalServiceException, ODKFormNotFoundException,
      ODKDatastoreException, ODKIncompleteSubmissionData {
	UploadSubmissions uploadSubmissions = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
	CsvGenerator csvGenerator = (CsvGenerator) cc.getBean(BeanDefs.CSV_BEAN);
	KmlGenerator kmlGenerator = (KmlGenerator) cc.getBean(BeanDefs.KML_BEAN);
	WorksheetCreator worksheetCreator = (WorksheetCreator) cc.getBean(BeanDefs.WORKSHEET_BEAN);
	FormDelete formDelete = (FormDelete) cc.getBean(BeanDefs.FORM_DELETE_BEAN);
	checkFormServiceCursors(checkIntervalMilliseconds, uploadSubmissions, cc);
    checkPersistentResults(csvGenerator, kmlGenerator, cc);
    checkMiscTasks(worksheetCreator, formDelete, cc);
  }

  private void checkFormServiceCursors(long checkIntervalMilliseconds, 
		  UploadSubmissions uploadSubmissions,
		  CallingContext cc) throws ODKExternalServiceException, ODKFormNotFoundException,
      ODKDatastoreException, ODKIncompleteSubmissionData {
    Date olderThanDate = new Date(System.currentTimeMillis() - checkIntervalMilliseconds);
    List<FormServiceCursor> fscList = FormServiceCursor.queryFormServiceCursorRelation(
        olderThanDate, cc);
    for (FormServiceCursor fsc : fscList) {
      if ( !fsc.isExternalServicePrepared() ) continue;
      if ( fsc.getOperationalStatus() != OperationalStatus.ACTIVE ) continue;
      
      switch (fsc.getExternalServiceOption()) {
      case UPLOAD_ONLY:
        checkUpload(fsc, uploadSubmissions, cc);
        break;
      case STREAM_ONLY:
        checkStreaming(fsc, uploadSubmissions, cc);
        break;
      case UPLOAD_N_STREAM:
        if (!fsc.getUploadCompleted())
          checkUpload(fsc, uploadSubmissions, cc);
        if (fsc.getUploadCompleted())
          checkStreaming(fsc, uploadSubmissions, cc);
        break;
      default:
        break;
      }
    }
  }

  private void checkUpload(FormServiceCursor fsc, 
		  UploadSubmissions uploadSubmissions,
		  CallingContext cc)
      throws ODKExternalServiceException {
    // TODO: remove
    System.out.println("Checking upload for " + fsc.getExternalServiceType());
    if (!fsc.getUploadCompleted()) {
      Date lastUploadDate = fsc.getLastUploadCursorDate();
      Date establishmentDate = fsc.getEstablishmentDateTime();
      if (establishmentDate != null && lastUploadDate == null
          || lastUploadDate.compareTo(establishmentDate) < 0) {
        // there is still work to do
    	  uploadSubmissions.createFormUploadTask(fsc, cc);
      }
    }
  }

  private void checkStreaming(FormServiceCursor fsc,
		  UploadSubmissions uploadSubmissions,
		  CallingContext cc) throws ODKFormNotFoundException, ODKDatastoreException,
      ODKExternalServiceException, ODKIncompleteSubmissionData {
    // TODO: remove
    System.out.println("Checking streaming for " + fsc.getExternalServiceType());
    // get the last submission sent to the external service
    String lastStreamingKey = fsc.getLastStreamingKey();
    Form form = Form.retrieveForm(fsc.getFormId(), cc);
    // query for last submission submitted for the form
    QueryByDate query = new QueryByDate(form, new Date(System.currentTimeMillis()), true, 1,
        cc);
    List<Submission> submissions = query.getResultSubmissions(cc);
    String lastSubmissionKey = null;
    if (submissions != null && submissions.size() == 1) {
      Submission lastSubmission = submissions.get(0);
      // NOTE: using LastUpdateDate instead of SubmissionDate because the submission date
      // marks the initiation of the upload of the submission to the server, whereas the 
      // lastUpdateDate is likely to mark the updating of the record to isComplete = 'true'
      // status.  We only want to stream completed uploads...
      if (lastSubmission.getLastUpdateDate().compareTo(fsc.getEstablishmentDateTime()) >= 0)
        lastSubmissionKey = lastSubmission.getKey().getKey();
    }
    if (lastSubmissionKey != null
        && (lastStreamingKey == null || !lastStreamingKey.equals(lastSubmissionKey))) {
      // there is still work to do
    	uploadSubmissions.createFormUploadTask(fsc, cc);
    }
  }

  private void checkPersistentResults( 
		  CsvGenerator csvGenerator, KmlGenerator kmlGenerator,
		  CallingContext cc)
      throws ODKDatastoreException, ODKFormNotFoundException {
	try {
	    // TODO: remove
	    System.out.println("Checking persistent results");
	    List<PersistentResults> persistentResults = PersistentResults.getStalledRequests(cc);
	    for (PersistentResults persistentResult : persistentResults) {
	      // TODO: remove
	      System.out.println("Found stalled request: " + persistentResult.getSubmissionKey());
	      long attemptCount = persistentResult.getAttemptCount();
	      persistentResult.setAttemptCount(++attemptCount);
	      persistentResult.persist(cc);
	      Form form = Form.retrieveForm(persistentResult.getFormId(), cc);
	      switch (persistentResult.getResultType()) {
	      case CSV:
	        csvGenerator.createCsvTask(form, persistentResult.getSubmissionKey(), attemptCount,
	            cc);
	        break;
	      case KML:
	        kmlGenerator.createKmlTask(form, persistentResult.getSubmissionKey(), attemptCount,
	            cc);
	        break;
	      }
	    }
	} finally {
	    System.out.println("Done checking persistent results");
	}
  }


  private void checkMiscTasks(WorksheetCreator wsCreator, FormDelete formDelete,
		  CallingContext cc)
      throws ODKDatastoreException, ODKFormNotFoundException {
	try {
	    // TODO: remove
	    System.out.println("Checking miscellaneous tasks");
	    List<MiscTasks> miscTasks = MiscTasks.getStalledRequests(cc);
	    for (MiscTasks aTask : miscTasks) {
	      // TODO: remove
	      System.out.println("Found stalled request: " + aTask.getSubmissionKey());
	      long attemptCount = aTask.getAttemptCount();
	      aTask.setAttemptCount(++attemptCount);
	      aTask.persist(cc);
	      Form form = Form.retrieveForm(aTask.getFormId(), cc);
	      switch (aTask.getTaskType()) {
	      case WORKSHEET_CREATE:
	    	wsCreator.createWorksheetTask(form, aTask.getSubmissionKey(), attemptCount,
	            cc);
	        break;
	      case DELETE_FORM:
	        formDelete.createFormDeleteTask(form, aTask.getSubmissionKey(), attemptCount,
	            cc);
	        break;
	      }
	    }
	} finally {
	    System.out.println("Done checking miscellaneous tasks");
	}
  }

}
