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

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.FormServiceCursor.OperationalStatus;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.MiscTasks.TaskType;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
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

  public void checkTasks(long checkIntervalMilliseconds, String baseWebServerUrl,
      Datastore datastore, User user) throws ODKExternalServiceException, ODKFormNotFoundException,
      ODKDatastoreException, ODKIncompleteSubmissionData {
    checkFormServiceCursors(checkIntervalMilliseconds, baseWebServerUrl, datastore, user);
    checkPersistentResults(baseWebServerUrl, datastore, user);
    checkMiscTasks(baseWebServerUrl, datastore, user);
  }

  private void checkFormServiceCursors(long checkIntervalMilliseconds, String baseWebServerUrl,
      Datastore datastore, User user) throws ODKExternalServiceException, ODKFormNotFoundException,
      ODKDatastoreException, ODKIncompleteSubmissionData {
    Date olderThanDate = new Date(System.currentTimeMillis() - checkIntervalMilliseconds);
    List<FormServiceCursor> fscList = FormServiceCursor.queryFormServiceCursorRelation(
        olderThanDate, datastore, user);
    for (FormServiceCursor fsc : fscList) {
      if ( !fsc.isExternalServicePrepared() ) continue;
      if ( fsc.getOperationalStatus() != OperationalStatus.ACTIVE ) continue;
      
      switch (fsc.getExternalServiceOption()) {
      case UPLOAD_ONLY:
        checkUpload(fsc, baseWebServerUrl, user);
        break;
      case STREAM_ONLY:
        checkStreaming(fsc, baseWebServerUrl, datastore, user);
        break;
      case UPLOAD_N_STREAM:
        if (!fsc.getUploadCompleted())
          checkUpload(fsc, baseWebServerUrl, user);
        if (fsc.getUploadCompleted())
          checkStreaming(fsc, baseWebServerUrl, datastore, user);
        break;
      case NONE:
        break;
      }
    }
  }

  private void checkUpload(FormServiceCursor fsc, String baseWebServerUrl, User user)
      throws ODKExternalServiceException {
    // TODO: remove
    System.out.println("Checking upload for " + fsc.getExternalServiceType());
    if (!fsc.getUploadCompleted()) {
      Date lastUploadDate = fsc.getLastUploadCursorDate();
      Date establishmentDate = fsc.getEstablishmentDateTime();
      if (establishmentDate != null && lastUploadDate == null
          || lastUploadDate.compareTo(establishmentDate) < 0) {
        // there is still work to do
        UploadSubmissions uploadTask = (UploadSubmissions) ContextFactory.get().getBean(
            BeanDefs.UPLOAD_TASK_BEAN);
        uploadTask.createFormUploadTask(fsc, baseWebServerUrl, user);
      }
    }
  }

  private void checkStreaming(FormServiceCursor fsc, String baseWebServerUrl, Datastore datastore,
      User user) throws ODKFormNotFoundException, ODKDatastoreException,
      ODKExternalServiceException, ODKIncompleteSubmissionData {
    // TODO: remove
    System.out.println("Checking streaming for " + fsc.getExternalServiceType());
    // get the last submission sent to the external service
    String lastStreamingKey = fsc.getLastStreamingKey();
    Form form = Form.retrieveForm(fsc.getFormId(), datastore, user);
    // query for last submission submitted for the form
    QueryByDate query = new QueryByDate(form, new Date(System.currentTimeMillis()), true, 1,
        datastore, user);
    List<Submission> submissions = query.getResultSubmissions();
    String lastSubmissionKey = null;
    if (submissions != null && submissions.size() == 1) {
      Submission lastSubmission = submissions.get(0);
      if (lastSubmission.getSubmittedTime().compareTo(fsc.getEstablishmentDateTime()) >= 0)
        lastSubmissionKey = lastSubmission.getKey().getKey();
    }
    if (lastSubmissionKey != null
        && (lastStreamingKey == null || !lastStreamingKey.equals(lastSubmissionKey))) {
      // there is still work to do
      UploadSubmissions uploadTask = (UploadSubmissions) ContextFactory.get().getBean(
          BeanDefs.UPLOAD_TASK_BEAN);
      uploadTask.createFormUploadTask(fsc, baseWebServerUrl, user);
    }
  }

  private void checkPersistentResults(String baseWebServerUrl, Datastore datastore, User user)
      throws ODKDatastoreException, ODKFormNotFoundException {
    // TODO: remove
    System.out.println("Checking persistent results");
    List<PersistentResults> persistentResults = PersistentResults.getStalledRequests(datastore,
        user);
    for (PersistentResults persistentResult : persistentResults) {
      // TODO: remove
      System.out.println("Found stalled request: " + persistentResult.getSubmissionKey());
      long attemptCount = persistentResult.getAttemptCount();
      persistentResult.setAttemptCount(++attemptCount);
      persistentResult.persist(datastore, user);
      Form form = Form.retrieveForm(persistentResult.getFormId(), datastore, user);
      switch (persistentResult.getResultType()) {
      case CSV:
        CsvGenerator csvGenerator = (CsvGenerator) ContextFactory.get().getBean(BeanDefs.CSV_BEAN);
        csvGenerator.createCsvTask(form, persistentResult.getSubmissionKey(), attemptCount,
            baseWebServerUrl, datastore, user);
        break;
      case KML:
        KmlGenerator kmlGenerator = (KmlGenerator) ContextFactory.get().getBean(BeanDefs.KML_BEAN);
        kmlGenerator.createKmlTask(form, persistentResult.getSubmissionKey(), attemptCount,
            baseWebServerUrl, datastore, user);
        break;
      }
    }
  }


  private void checkMiscTasks(String baseWebServerUrl, Datastore datastore, User user)
      throws ODKDatastoreException, ODKFormNotFoundException {
    // TODO: remove
    System.out.println("Checking miscellaneous tasks");
    List<MiscTasks> miscTasks = MiscTasks.getStalledRequests(datastore,
        user);
    for (MiscTasks aTask : miscTasks) {
      // TODO: remove
      System.out.println("Found stalled request: " + aTask.getSubmissionKey());
      long attemptCount = aTask.getAttemptCount();
      aTask.setAttemptCount(++attemptCount);
      aTask.persist(datastore, user);
      Form form = Form.retrieveForm(aTask.getFormId(), datastore, user);
      switch (aTask.getTaskType()) {
      case WORKSHEET_CREATE:
    	WorksheetCreator wsCreator = (WorksheetCreator) ContextFactory.get().getBean(BeanDefs.WORKSHEET_BEAN);
    	wsCreator.createWorksheetTask(form, aTask.getSubmissionKey(), attemptCount,
            baseWebServerUrl, datastore, user);
        break;
      case DELETE_FORM:
        FormDelete formDelete = (FormDelete) ContextFactory.get().getBean(BeanDefs.FORM_DELETE_BEAN);
        formDelete.createFormDeleteTask(form, aTask.getSubmissionKey(), attemptCount,
            baseWebServerUrl, datastore, user);
        break;
      }
    }
  }

}
