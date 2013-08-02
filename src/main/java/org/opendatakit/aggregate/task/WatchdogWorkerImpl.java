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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.constants.externalservice.JsonServerConsts;
import org.opendatakit.aggregate.constants.externalservice.OhmageJsonServerConsts;
import org.opendatakit.aggregate.constants.externalservice.REDCapServerConsts;
import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.datamodel.TopLevelDynamicBase;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.form.MiscTasks;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup;
import org.opendatakit.aggregate.query.submission.QueryByUIFilterGroup.CompletionFlag;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.util.BackendActionsTable;
import org.opendatakit.common.persistence.PersistConsts;
import org.opendatakit.common.persistence.QueryResumePoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;

/**
 * Common worker implementation for restarting stalled tasks.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class WatchdogWorkerImpl {

  private Log logger = LogFactory.getLog(WatchdogWorkerImpl.class);

  private static class SubmissionMetadata {
    public final String uri;
    public final Date markedAsCompleteDate;

    SubmissionMetadata(String uri, Date markedAsCompleteDate) {
      this.uri = uri;
      this.markedAsCompleteDate = markedAsCompleteDate;
    }
  }

  // accessed only by getLastSubmissionMetadata
  private Map<String, SubmissionMetadata> formSubmissionsMap = new HashMap<String, SubmissionMetadata>();

  /**
   * Determine and return the metadata for the last submission against this
   * form. That metadata consists of the marked-as-complete date and uri of the
   * submission. Used to determine whether to launch an upload task.
   *
   * @param form
   * @param cc
   * @return
   * @throws ODKDatastoreException
   */
  private synchronized SubmissionMetadata getLastSubmissionMetadata(IForm form, CallingContext cc)
      throws ODKDatastoreException {

    // use cached value -- prevents excessive queries against the form tables
    // during Watchdog verification of streaming publishers.
    SubmissionMetadata metadata = formSubmissionsMap.get(form.getUri());
    if (metadata != null)
      return metadata;

    // compute the upper limit for data we want to process
    // limitDate is the datastore's settle time into the past.
    Date limitDate = new Date(System.currentTimeMillis() - PersistConsts.MAX_SETTLE_MILLISECONDS);
    QueryResumePoint qrp = new QueryResumePoint(
        TopLevelDynamicBase.FIELD_NAME_MARKED_AS_COMPLETE_DATE, WebUtils.iso8601Date(limitDate),
        null, false);

    FilterGroup filterGroup = new FilterGroup(UIConsts.FILTER_NONE, form.getFormId(), null);
    filterGroup.setCursor(qrp.transform());
    filterGroup.setQueryFetchLimit(1);

    // query for the most recent submission that was marked-as-complete for this
    // form
    QueryByUIFilterGroup query = new QueryByUIFilterGroup(form, filterGroup,
        CompletionFlag.ONLY_COMPLETE_SUBMISSIONS, cc);

    List<Submission> submissions = query.getResultSubmissions(cc);
    if (submissions != null && submissions.size() >= 1) {
      Submission lastSubmission = submissions.get(0);
      metadata = new SubmissionMetadata(lastSubmission.getKey().getKey(),
          lastSubmission.getMarkedAsCompleteDate());
      formSubmissionsMap.put(form.getUri(), metadata);
      return metadata;
    }
    return null;
  }

  public void checkTasks(CallingContext cc) throws ODKExternalServiceException,
      ODKFormNotFoundException, ODKDatastoreException, ODKIncompleteSubmissionData {
    logger.info("---------------------BEGIN Watchdog");
    boolean cullThisWatchdog = false;
    boolean activeTasks = true;
    Watchdog wd = null;
    try {
      wd = (Watchdog) cc.getBean(BeanDefs.WATCHDOG);
      cullThisWatchdog = BackendActionsTable.updateWatchdogStart(wd, cc);
      formSubmissionsMap.clear();

      UploadSubmissions uploadSubmissions = (UploadSubmissions) cc
          .getBean(BeanDefs.UPLOAD_TASK_BEAN);
      CsvGenerator csvGenerator = (CsvGenerator) cc.getBean(BeanDefs.CSV_BEAN);
      KmlGenerator kmlGenerator = (KmlGenerator) cc.getBean(BeanDefs.KML_BEAN);
      WorksheetCreator worksheetCreator = (WorksheetCreator) cc.getBean(BeanDefs.WORKSHEET_BEAN);
      FormDelete formDelete = (FormDelete) cc.getBean(BeanDefs.FORM_DELETE_BEAN);
      PurgeOlderSubmissions purgeSubmissions = (PurgeOlderSubmissions) cc
          .getBean(BeanDefs.PURGE_OLDER_SUBMISSIONS_BEAN);
      JsonFileGenerator jsonGenerator = (JsonFileGenerator) cc.getBean(BeanDefs.JSON_FILE_BEAN);
      boolean foundActiveTasks = false;
      // NOTE: do not short-circuit these check actions...
      foundActiveTasks = foundActiveTasks | checkFormServiceCursors(uploadSubmissions, cc);
      foundActiveTasks = foundActiveTasks
          | checkPersistentResults(csvGenerator, kmlGenerator, jsonGenerator, cc);
      foundActiveTasks = foundActiveTasks
          | checkMiscTasks(worksheetCreator, formDelete, purgeSubmissions, cc);
      activeTasks = foundActiveTasks;
    } finally {
      // NOTE: if the above threw an exception, we re-start the watchdog.
      // otherwise, we restart it only if there is work to be done.
      BackendActionsTable.rescheduleWatchdog(activeTasks, cullThisWatchdog, cc);
      logger.info("---------------------END Watchdog");
    }
  }

  private boolean checkFormServiceCursors(UploadSubmissions uploadSubmissions, CallingContext cc)
      throws ODKExternalServiceException, ODKFormNotFoundException, ODKDatastoreException,
      ODKIncompleteSubmissionData {

    Date olderThanDate = new Date(System.currentTimeMillis()
        - BackendActionsTable.PUBLISHING_DELAY_MILLISECONDS);
    List<FormServiceCursor> fscList = FormServiceCursor.queryFormServiceCursorRelation(
        olderThanDate, cc);
    boolean activeTasks = false;
    for (FormServiceCursor fsc : fscList) {
      if (!fsc.isExternalServicePrepared()) {
        // TODO: should handle resume-initiate somehow?
        continue;
      }
      OperationalStatus opStatus = fsc.getOperationalStatus();
      if (opStatus == OperationalStatus.PAUSED || opStatus == OperationalStatus.ACTIVE_PAUSE) {
        // when PAUSED, retry the publisher at most **twice** every idle-watchdog retry interval
        // (every 7.5 minutes). NOTE: This only happens if there are other active publishers and a
        // flow of new submissions into the system (whether or not they submit into the table being
        // published).  If there are no new submissions, this backoff results in the publisher
        // being re-run less often -- i.e., once every idle-watchdog retry interval (every 15 minutes).
        long backoffInterval =
            ( BackendActionsTable.IDLING_WATCHDOG_RETRY_INTERVAL_MILLISECONDS -
              PersistConsts.MAX_SETTLE_MILLISECONDS ) / 2L; // slop
        // but, if ACTIVE_PAUSE, ...
        if (opStatus == OperationalStatus.ACTIVE_PAUSE) {
          // we want the watchdog to run more frequently than its idling interval
          activeTasks = true;
          // and we want a shorter backoff interval (e.g., 60 seconds)
          ExternalServiceType type = fsc.getExternalServiceType();
          switch (type) {
          case GOOGLE_SPREADSHEET:
            backoffInterval = SpreadsheetConsts.BACKOFF_DELAY_MILLISECONDS;
            break;
          case JSON_SERVER:
            backoffInterval = JsonServerConsts.BACKOFF_DELAY_MILLISECONDS;
            break;
          case OHMAGE_JSON_SERVER:
            backoffInterval = OhmageJsonServerConsts.BACKOFF_DELAY_MILLISECONDS;
            break;
          case GOOGLE_FUSIONTABLES:
            backoffInterval = FusionTableConsts.BACKOFF_DELAY_MILLISECONDS;
            break;
          case REDCAP_SERVER:
            backoffInterval = REDCapServerConsts.BACKOFF_DELAY_MILLISECONDS;
            break;
          default:
            backoffInterval = 60000L; // 1 minute
            this.logger.equals("No explicit backoff delay set for ExternalServiceType: "
                + type.name() + " therefore using default");
          }
        }

        if (fsc.getLastUpdateDate().getTime() + backoffInterval < System.currentTimeMillis()) {
          // the paused task needs to be moved into ACTIVE_RETRY
          activeTasks = true;
          opStatus = OperationalStatus.ACTIVE_RETRY;
          fsc.setOperationalStatus(OperationalStatus.ACTIVE_RETRY);
          cc.getDatastore().putEntity(fsc, cc.getCurrentUser());
        }
      }

      if (!(opStatus == OperationalStatus.ACTIVE || opStatus == OperationalStatus.ACTIVE_RETRY)) {
        // TODO: should handle resume-initiate somehow?
        continue;
      }

      switch (fsc.getExternalServicePublicationOption()) {
      case UPLOAD_ONLY:
        if (!fsc.getUploadCompleted()) {
          activeTasks = activeTasks | checkUpload(fsc, uploadSubmissions, cc);
        }
        break;
      case STREAM_ONLY:
        activeTasks = activeTasks | checkStreaming(fsc, uploadSubmissions, cc);
        break;
      case UPLOAD_N_STREAM:
        if (!fsc.getUploadCompleted()) {
          activeTasks = activeTasks | checkUpload(fsc, uploadSubmissions, cc);
        }
        if (fsc.getUploadCompleted()) {
          activeTasks = activeTasks | checkStreaming(fsc, uploadSubmissions, cc);
        }
        break;
      default:
        break;
      }
    }
    return activeTasks;
  }

  private boolean checkUpload(FormServiceCursor fsc, UploadSubmissions uploadSubmissions,
      CallingContext cc) throws ODKExternalServiceException {
    logger.info("Checking upload for " + fsc.getExternalServiceType() + " fsc: " + fsc.getUri());
    boolean activeTask = false;
    if (!fsc.getUploadCompleted()) {
      Date lastUploadDate = fsc.getLastUploadCursorDate();
      Date establishmentDate = fsc.getEstablishmentDateTime();
      if (establishmentDate != null && lastUploadDate == null
          || lastUploadDate.compareTo(establishmentDate) < 0) {
        // there is still work to do
        activeTask = true;
        uploadSubmissions.createFormUploadTask(fsc, true, cc);
      }
    }
    return activeTask;
  }

  /**
   * Determine whether the form has recently-completed submissions that have not
   * yet been published. Use a cache of SubmissionMetadata to minimize queries
   * against the database in the case where there are many publishers for a
   * given table.
   *
   * @param fsc
   * @param uploadSubmissions
   * @param cc
   * @return
   * @throws ODKFormNotFoundException
   * @throws ODKDatastoreException
   * @throws ODKExternalServiceException
   * @throws ODKIncompleteSubmissionData
   */
  private boolean checkStreaming(FormServiceCursor fsc, UploadSubmissions uploadSubmissions,
      CallingContext cc) throws ODKFormNotFoundException, ODKDatastoreException,
      ODKExternalServiceException, ODKIncompleteSubmissionData {
    logger.info("Checking streaming for " + fsc.getExternalServiceType() + " fsc: " + fsc.getUri());
    // get the last submission sent to the external service
    IForm form = FormFactory.retrieveFormByFormId(fsc.getFormId(), cc);
    if (!form.hasValidFormDefinition()) {
      logger.warn("Form definition was ill-formed while checking for streaming for "
          + fsc.getExternalServiceType() + " fsc: " + fsc.getUri());
      return false;
    }

    SubmissionMetadata metadata = getLastSubmissionMetadata(form, cc);

    // determine whether we should make this publisher active
    boolean makeActive = false;
    if (metadata != null
        && metadata.markedAsCompleteDate.compareTo(fsc.getEstablishmentDateTime()) >= 0) {
      // submissions have occurred after the establishment time
      Date limit = fsc.getLastStreamingCursorDate();
      if (limit == null) {
        // streaming hasn't started yet...
        makeActive = true;
      } else {
        // streaming has started
        int cmpDates = metadata.markedAsCompleteDate.compareTo(limit);
        if (cmpDates > 0) {
          // the latest submission is more recent than that last streamed
          makeActive = true;
        } else if (cmpDates == 0) {
          // the latest submission is at the same time as that last streamed
          if (fsc.getLastStreamingKey() == null || !metadata.uri.equals(fsc.getLastStreamingKey())) {
            // the latest submission is not the one last streamed.
            makeActive = true;
          }
        }
      }
    }

    if (makeActive) {
      // there is work to do
      uploadSubmissions.createFormUploadTask(fsc, false, cc);
      return true;
    }

    // go idle unless there is a chance that we have a new submission.
    return BackendActionsTable.mayHaveRecentPublisherRevision(fsc.getUri(), cc);
  }

  private boolean checkPersistentResults(CsvGenerator csvGenerator, KmlGenerator kmlGenerator,
      JsonFileGenerator jsonGenerator, CallingContext cc) throws ODKDatastoreException,
      ODKFormNotFoundException {
    try {
      logger.info("Checking all persistent results");
      boolean activeTasks = false;
      List<PersistentResults> persistentResults = PersistentResults.getStalledRequests(cc);
      for (PersistentResults persistentResult : persistentResults) {
        logger.info("Found stalled request: " + persistentResult.getSubmissionKey());
        long attemptCount = persistentResult.getAttemptCount();
        persistentResult.setAttemptCount(++attemptCount);
        persistentResult.persist(cc);
        IForm form = FormFactory.retrieveFormByFormId(persistentResult.getFormId(), cc);
        if (!form.hasValidFormDefinition()) {
          logger.warn("Form of stalled task is ill-formed: " + persistentResult.getSubmissionKey()
              + " formId: " + persistentResult.getFormId());
          continue; // skip this and move on...
        }
        activeTasks = true;
        switch (persistentResult.getResultType()) {
        case CSV:
          csvGenerator.createCsvTask(form, persistentResult.getSubmissionKey(), attemptCount, cc);
          break;
        case KML:
          kmlGenerator.createKmlTask(form, persistentResult, attemptCount, cc);
          break;
        case JSONFILE:
          jsonGenerator.createJsonFileTask(form, persistentResult.getSubmissionKey(), attemptCount,
              cc);
          break;
        default:
          this.logger.equals("No generator defined for Persisted Result Type: "
              + persistentResult.getResultType().name());
          break;
        }
      }
      return activeTasks;
    } finally {
      logger.info("Done checking persistent results");
    }
  }

  private boolean checkMiscTasks(WorksheetCreator wsCreator, FormDelete formDelete,
      PurgeOlderSubmissions purgeSubmissions, CallingContext cc) throws ODKDatastoreException,
      ODKFormNotFoundException {
    try {
      logger.info("Checking miscellaneous tasks");
      boolean activeTasks = false;
      List<MiscTasks> miscTasks = MiscTasks.getStalledRequests(cc);
      for (MiscTasks aTask : miscTasks) {
        logger.info("Found stalled request: " + aTask.getSubmissionKey());
        long attemptCount = aTask.getAttemptCount();
        aTask.setAttemptCount(++attemptCount);
        aTask.persist(cc);
        IForm form = FormFactory.retrieveFormByFormId(aTask.getFormId(), cc);
        if (!form.hasValidFormDefinition()) {
          logger.warn("Form definition is ill-formed while checking stalled request: "
              + aTask.getSubmissionKey() + " formId: " + aTask.getFormId());
        }
        switch (aTask.getTaskType()) {
        case WORKSHEET_CREATE:
          if (form.hasValidFormDefinition()) {
            activeTasks = true;
            wsCreator.createWorksheetTask(form, aTask, attemptCount, cc);
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
      logger.info("Done checking miscellaneous tasks");
    }
  }

}
