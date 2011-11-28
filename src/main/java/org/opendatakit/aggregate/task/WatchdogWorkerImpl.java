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
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.query.submission.QueryByDateRange;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.util.BackendActionsTable;
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

  public void checkTasks(long checkIntervalMilliseconds, CallingContext cc)
      throws ODKExternalServiceException, ODKFormNotFoundException, ODKDatastoreException,
      ODKIncompleteSubmissionData {
    UploadSubmissions uploadSubmissions = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
    CsvGenerator csvGenerator = (CsvGenerator) cc.getBean(BeanDefs.CSV_BEAN);
    KmlGenerator kmlGenerator = (KmlGenerator) cc.getBean(BeanDefs.KML_BEAN);
    WorksheetCreator worksheetCreator = (WorksheetCreator) cc.getBean(BeanDefs.WORKSHEET_BEAN);
    FormDelete formDelete = (FormDelete) cc.getBean(BeanDefs.FORM_DELETE_BEAN);
    PurgeOlderSubmissions purgeSubmissions = (PurgeOlderSubmissions) cc
        .getBean(BeanDefs.PURGE_OLDER_SUBMISSIONS_BEAN);
    boolean activeTasks = false;
    // NOTE: do not short-circuit these check actions...
    activeTasks = activeTasks | checkFormServiceCursors(checkIntervalMilliseconds, uploadSubmissions, cc);
    activeTasks = activeTasks | checkPersistentResults(csvGenerator, kmlGenerator, cc);
    activeTasks = activeTasks | checkMiscTasks(worksheetCreator, formDelete, purgeSubmissions, cc);
    if ( activeTasks ) {
      BackendActionsTable.scheduleFutureWatchdog(Watchdog.WATCHDOG_BUSY_RETRY_INTERVAL_MILLISECONDS, cc);
    }
  }

  private boolean checkFormServiceCursors(long checkIntervalMilliseconds,
      UploadSubmissions uploadSubmissions, CallingContext cc) throws ODKExternalServiceException,
      ODKFormNotFoundException, ODKDatastoreException, ODKIncompleteSubmissionData {
    Date olderThanDate = new Date(System.currentTimeMillis() - checkIntervalMilliseconds);
    List<FormServiceCursor> fscList = FormServiceCursor.queryFormServiceCursorRelation(
        olderThanDate, cc);
    boolean activeTasks = false;
    for (FormServiceCursor fsc : fscList) {
      if (!fsc.isExternalServicePrepared())
        continue;
      if (fsc.getOperationalStatus() != OperationalStatus.ACTIVE)
        continue;

      switch (fsc.getExternalServicePublicationOption()) {
      case UPLOAD_ONLY:
        activeTasks = activeTasks | checkUpload(fsc, uploadSubmissions, cc);
        break;
      case STREAM_ONLY:
        activeTasks = activeTasks | checkStreaming(fsc, uploadSubmissions, cc);
        break;
      case UPLOAD_N_STREAM:
        if (!fsc.getUploadCompleted())
          activeTasks = activeTasks | checkUpload(fsc, uploadSubmissions, cc);
        if (fsc.getUploadCompleted())
          activeTasks = activeTasks | checkStreaming(fsc, uploadSubmissions, cc);
        break;
      default:
        break;
      }
    }
    return activeTasks;
  }

  private boolean checkUpload(FormServiceCursor fsc, UploadSubmissions uploadSubmissions,
      CallingContext cc) throws ODKExternalServiceException {
    // TODO: remove
    System.out.println("Checking upload for " + fsc.getExternalServiceType());
    boolean activeTask = false;
    if (!fsc.getUploadCompleted()) {
      Date lastUploadDate = fsc.getLastUploadCursorDate();
      Date establishmentDate = fsc.getEstablishmentDateTime();
      if (establishmentDate != null && lastUploadDate == null
          || lastUploadDate.compareTo(establishmentDate) < 0) {
        // there is still work to do
        activeTask = true;
        uploadSubmissions.createFormUploadTask(fsc, cc);
      }
    }
    return activeTask;
  }

  private boolean checkStreaming(FormServiceCursor fsc, UploadSubmissions uploadSubmissions,
      CallingContext cc) throws ODKFormNotFoundException, ODKDatastoreException,
      ODKExternalServiceException, ODKIncompleteSubmissionData {
    // TODO: remove
    System.out.println("Checking streaming for " + fsc.getExternalServiceType());
    boolean activeTask = false;
    // get the last submission sent to the external service
    String lastStreamingKey = fsc.getLastStreamingKey();
    IForm form = FormFactory.retrieveFormByFormId(fsc.getFormId(), cc);
    if (!form.hasValidFormDefinition()) {
      System.out.println("Form definition was ill-formed while checking for streaming for "
          + fsc.getExternalServiceType());
      return false;
    }
    // query for last submission submitted for the form
    QueryByDateRange query = new QueryByDateRange(form, cc);
    List<Submission> submissions = query.getResultSubmissions(cc);
    String lastSubmissionKey = null;
    if (submissions != null && submissions.size() >= 1) {
      Submission lastSubmission = submissions.get(0);
      // NOTE: using markedAsCompleteDate because the submission date
      // marks the original initiation of the upload of the submission
      // to the server and is preserved as briefcase entries are copied
      // across servers. We only want to stream completed uploads...
      if (lastSubmission.getMarkedAsCompleteDate().compareTo(fsc.getEstablishmentDateTime()) >= 0) {
        lastSubmissionKey = lastSubmission.getKey().getKey();
        if (lastStreamingKey == null || !lastStreamingKey.equals(lastSubmissionKey)) {
          // there is work to do
          activeTask = true;
          uploadSubmissions.createFormUploadTask(fsc, cc);
        }
      }
    }
    return activeTask;
  }

  private boolean checkPersistentResults(CsvGenerator csvGenerator, KmlGenerator kmlGenerator,
      CallingContext cc) throws ODKDatastoreException, ODKFormNotFoundException {
    try {
      // TODO: remove
      System.out.println("Checking persistent results");
      boolean activeTasks = false;
      List<PersistentResults> persistentResults = PersistentResults.getStalledRequests(cc);
      for (PersistentResults persistentResult : persistentResults) {
        // TODO: remove
        System.out.println("Found stalled request: " + persistentResult.getSubmissionKey());
        long attemptCount = persistentResult.getAttemptCount();
        persistentResult.setAttemptCount(++attemptCount);
        persistentResult.persist(cc);
        IForm form = FormFactory.retrieveFormByFormId(persistentResult.getFormId(), cc);
        if (!form.hasValidFormDefinition()) {
          System.out.println("Form of stalled task is ill-formed");
          continue; // skip this and move on...
        }
        activeTasks = true;
        switch (persistentResult.getResultType()) {
        case CSV:
          csvGenerator.createCsvTask(form, persistentResult.getSubmissionKey(), attemptCount, cc);
          break;
        case KML:
          kmlGenerator.createKmlTask(form, persistentResult.getSubmissionKey(), attemptCount, cc);
          break;
        }
      }
      return activeTasks;
    } finally {
      System.out.println("Done checking persistent results");
    }
  }

  private boolean checkMiscTasks(WorksheetCreator wsCreator, FormDelete formDelete,
      PurgeOlderSubmissions purgeSubmissions, CallingContext cc) throws ODKDatastoreException,
      ODKFormNotFoundException {
    try {
      // TODO: remove
      System.out.println("Checking miscellaneous tasks");
      boolean activeTasks = false;
      List<MiscTasks> miscTasks = MiscTasks.getStalledRequests(cc);
      for (MiscTasks aTask : miscTasks) {
        // TODO: remove
        System.out.println("Found stalled request: " + aTask.getSubmissionKey());
        long attemptCount = aTask.getAttemptCount();
        aTask.setAttemptCount(++attemptCount);
        aTask.persist(cc);
        IForm form = FormFactory.retrieveFormByFormId(aTask.getFormId(), cc);
        if (!form.hasValidFormDefinition()) {
          System.out.println("Form definition is ill-formed while checking stalled request: "
              + aTask.getSubmissionKey());
        }
        switch (aTask.getTaskType()) {
        case WORKSHEET_CREATE:
          if (form.hasValidFormDefinition()) {
            activeTasks = true;
            wsCreator.createWorksheetTask(form, aTask.getSubmissionKey(), attemptCount, cc);
          }
          break;
        case DELETE_FORM:
          activeTasks = true;
          formDelete.createFormDeleteTask(form, aTask.getSubmissionKey(), attemptCount, cc);
          break;
        case PURGE_OLDER_SUBMISSIONS:
          if (form.hasValidFormDefinition()) {
            activeTasks = true;
            purgeSubmissions.createPurgeOlderSubmissionsTask(form, aTask.getSubmissionKey(),
                attemptCount, cc);
          }
          break;
        }
      }
      return activeTasks;
    } finally {
      System.out.println("Done checking miscellaneous tasks");
    }
  }

}
