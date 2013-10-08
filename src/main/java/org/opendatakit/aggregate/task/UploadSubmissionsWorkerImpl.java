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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.OperationalStatus;
import org.opendatakit.aggregate.exception.ODKExternalServiceCredentialsException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.ExternalServiceUtils;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.query.submission.QueryByDateRange;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Common worker implementation for the publishing of data to an external
 * service.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class UploadSubmissionsWorkerImpl {

  // UploadSubmissions are run in the frontend (website) thread on GAE
  // unless fast publishing is enabled, in which case they are thrown
  // onto the background process where there is 10-minute processing limit.
  //
  // Frontend tasks are still limited to a 1-minute request time-out.
  // Timing against Fusion Tables indicates that on a good day, it takes
  // about 900 ms per published submission (no repeats). This means that
  // in 10 minutes (600,000 ms), you should be able to submit 666
  // records into fusion tables. If we give a 6-fold factor for a
  // combination of multiple repeat groups within a submission and
  // the slowness of submissions on a bad day, this brings the fetch
  // limit down to about 10 records (for a 60-second request time-out).
  private static final int MAX_FOREGROUND_QUERY_LIMIT = 10;
  private static final int DELAY_BETWEEN_RELEASE_RETRIES = 1000;
  private static final int MAX_NUMBER_OF_RELEASE_RETRIES = 10;

  private final Log logger = LogFactory.getLog(UploadSubmissionsWorkerImpl.class);
  private final String lockId;
  private final CallingContext cc;
  private final boolean useLargerBatchSize;
  private final FormServiceCursor pFsc;
  private final ExternalServicePublicationOption pEsOption;
  private ExternalService pExtService;
  private IForm form;
  private long lastUpdateTimestamp = System.currentTimeMillis();

  private int getQueryLimit() {
    if (useLargerBatchSize) {
      // we are running in the background...
      return MAX_FOREGROUND_QUERY_LIMIT * 10;
    } else {
      return MAX_FOREGROUND_QUERY_LIMIT;
    }
  }

  public UploadSubmissionsWorkerImpl(FormServiceCursor fsc, boolean useLargerBatchSize,
      CallingContext cc) {
    pFsc = fsc;
    this.useLargerBatchSize = useLargerBatchSize;
    this.cc = cc;
    pEsOption = fsc.getExternalServicePublicationOption();
    lockId = UUID.randomUUID().toString();
  }

  public String getUploadSubmissionsTaskLockName() {
    return pFsc.getUri();
  }

  public void uploadAllSubmissions() throws ODKEntityNotFoundException,
      ODKExternalServiceException, ODKFormNotFoundException {
    // by default, don't requeue the request on failures or exceptions.
    // In those cases, let the watchdog restart the activity because
    // the remedy will likely take longer than the requeue delay.
    boolean reQueue = false;
    logger.info("Beginning UPLOAD service: " + pFsc.getAuriService() + " form " + pFsc.getFormId());

    try {
      pExtService = pFsc.getExternalService(cc);
      form = FormFactory.retrieveFormByFormId(pFsc.getFormId(), cc);
      if (!form.hasValidFormDefinition()) {
        logger.error("Upload not performed -- ill-formed form definition.");
        return;
      }
    } catch (ODKOverQuotaException e) {
      logger.warn("Quota exceeded.");
      return;
    } catch (ODKDatastoreException e) {
      logger.error("Persistence layer problem: " + e.getMessage());
      return;
    } catch (Exception e) {
      logger.error("Unexpected exception: " + e.getMessage());
      throw new ODKExternalServiceException(e);
    }

    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    TaskLock taskLock = ds.createTaskLock(user);

    boolean locked = false;
    try {
      if (taskLock.obtainLock(lockId, getUploadSubmissionsTaskLockName(),
          TaskLockType.UPLOAD_SUBMISSION)) {
        locked = true;
      }
      taskLock = null;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
    }

    if (!locked) {
      logger.warn("Unable to acquire lock");
      return;
    }

    try {
      if (!pFsc.isExternalServicePrepared()) {
        logger.warn("Upload invoked before external service is prepared");
        return;
      }

      OperationalStatus opStatus = pFsc.getOperationalStatus();
      if (opStatus == OperationalStatus.ACTIVE_RETRY ||
          opStatus == OperationalStatus.ACTIVE) {
        logger.info("Upload invoked when operational status is " + opStatus.name());
      } else {
        logger.warn("Upload IGNORED when operational status is " + opStatus.name());
        return;
      }

      // opStatus is one of ACTIVE, ACTIVE_RETRY
      switch (pEsOption) {
      case UPLOAD_ONLY:
        if (pFsc.getUploadCompleted()) {
          // leave the record so we know action has occurred.
          logger
              .warn("Upload completed for UPLOAD_ONLY but formServiceCursor operational status slow to be revised");
          // update this value here, but it should have already been set...
          pFsc.setOperationalStatus(OperationalStatus.COMPLETED);
          ds.putEntity(pFsc, user);
        } else {
          reQueue = uploadSubmissions();
        }
        break;
      case STREAM_ONLY:
        reQueue = streamSubmissions();
        break;
      case UPLOAD_N_STREAM:
        if (!pFsc.getUploadCompleted()) {
          reQueue = uploadSubmissions();
        } else {
          reQueue = streamSubmissions();
        }
        break;
      default:
        throw new IllegalStateException("Unexpected ExternalServiceOption: " + pEsOption.name());
      }
    } catch (ODKExternalServiceException e) {
      Writer writer = new StringWriter();
      PrintWriter printWriter = new PrintWriter(writer);
      e.printStackTrace(printWriter);
      logger.error(writer.toString());
      e.printStackTrace();
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      logger.error("Unexpected exception: " + e.getMessage());
      throw new ODKExternalServiceException(e);
    } finally {
      taskLock = ds.createTaskLock(user);
      try {
        for (int i = 0; i < MAX_NUMBER_OF_RELEASE_RETRIES; i++) {
          if (taskLock.releaseLock(lockId, getUploadSubmissionsTaskLockName(),
              TaskLockType.UPLOAD_SUBMISSION))
            break;
          try {
            Thread.sleep(DELAY_BETWEEN_RELEASE_RETRIES);
          } catch (InterruptedException e) {
            // just move on, this retry mechanism is to make things nice
          }
        }
      } catch (ODKTaskLockException e) {
        // if release fails, it will eventually be cleared...
        e.printStackTrace();
        logger.error("Failed trying to release lock");
      }
    }

   
    if (reQueue) {
      // create another task to continue upload
      UploadSubmissions uploadSubmissionsBean = (UploadSubmissions) cc
          .getBean(BeanDefs.UPLOAD_TASK_BEAN);
      // schedule it on the background thread only if we are not disabling
      // background activities and it started on the background thread.
      boolean disableFasterProcessing = true;
      try {
        disableFasterProcessing = ServerPreferencesProperties
            .getFasterBackgroundActionsDisabled(cc);
      } catch (ODKOverQuotaException e) {
        e.printStackTrace();
      }
      uploadSubmissionsBean.createFormUploadTask(pFsc, useLargerBatchSize
          && !disableFasterProcessing, cc);
    }
  }

  private boolean uploadSubmissions() throws Exception {

    Date startDate = pFsc.getLastUploadCursorDate();
    if (startDate == null) {
      startDate = BasicConsts.EPOCH;
    }

    Date endDate = pFsc.getEstablishmentDateTime();
    // submissions are queried by the markedAsCompleteDate, since the
    // submissionDate
    // marks the initiation of the upload, but it may not have completed and
    // been marked as completely uploaded until later. This is particularly
    // significant for briefcase-uploaded data, which preserves the
    // submissionDate,
    // but would have a much-later markedAsCompleteDate, creationDate and
    // lastUpdatedDate.
    String lastUploadKey = pFsc.getLastUploadKey();
    List<Submission> submissions = querySubmissionsDateRange(startDate, endDate, lastUploadKey);

    if (submissions.isEmpty()) {
      logger.info("There are no submissions available for upload");
      // there are no submissions so uploading is complete
      // this persists pFsc
      pExtService.setUploadCompleted(cc);
      return (pEsOption == ExternalServicePublicationOption.UPLOAD_N_STREAM);
    } else {
      logger.info("There are " + submissions.size() + " submissions available for upload");
      // this persists pFsc
      sendSubmissions(submissions, false);
    }

    return true;
  }

  private boolean streamSubmissions() throws ODKFormNotFoundException, ODKIncompleteSubmissionData,
      ODKDatastoreException, ODKExternalServiceException {

    Date startDate = pFsc.getLastStreamingCursorDate();
    if (startDate == null) {
      startDate = pFsc.getEstablishmentDateTime();
    }

    String lastStreamedKey = pFsc.getLastStreamingKey();
    List<Submission> submissions = querySubmissionsStartDate(startDate, lastStreamedKey);

    if (submissions.isEmpty()) {
      logger.info("There are no submissions available for streaming");
    } else {
      logger.info("There are " + submissions.size() + " submissions available for streaming");
      // this persists pFsc
      sendSubmissions(submissions, true);
      return true;
    }
    return false;
  }

  private void sendSubmissions(List<Submission> submissionsToSend, boolean streaming)
      throws ODKExternalServiceException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();
    try {
      // check if publisher is capable of batching transmission
      if (pExtService.canBatchSubmissions()) {
        pExtService.sendSubmissions(submissionsToSend, streaming, cc);

      } else { // publisher not capable of batching
        int counter = 0;
        for (Submission submission : submissionsToSend) {
          pExtService.sendSubmission(submission, cc);
          ++counter;

          // persist updated last send date
          ExternalServiceUtils.updateFscToSuccessfulSubmissionDate(pFsc, submission, streaming);
          ds.putEntity(pFsc, user);

          counter = renewTaskLock(counter);
        }
      }
    } catch (ODKExternalServiceCredentialsException e) {
      e.printStackTrace();
      // The main goal of this catch is to avoid
      // silently transitioning BAD_CREDENTIALS into
      // the PAUSED state.
      if (pFsc.getOperationalStatus() != OperationalStatus.BAD_CREDENTIALS) {
        // An authorization failure should have set
        // the BAD_CREDENTIALS state. Log warning and do it now.
        logger.warn("ODKExternalServiceCredentialsException but not yet in BAD_CREDENTIALS state!");
        pFsc.setOperationalStatus(OperationalStatus.BAD_CREDENTIALS);
        try {
          ds.putEntity(pFsc, user);
        } catch (ODKEntityPersistException ex) {
          // ignore -- important exception is the external service exception
          ex.printStackTrace();
        } catch (ODKOverQuotaException ex) {
          // ignore -- important exception is the external service exception
          ex.printStackTrace();
        }
      }
      throw e;
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      ExternalServiceUtils.pauseFscOperationalStatus(pFsc);
      try {
        ds.putEntity(pFsc, user);
      } catch (ODKEntityPersistException ex) {
        // ignore -- important exception is the external service exception
        ex.printStackTrace();
      } catch (ODKOverQuotaException ex) {
        // ignore -- important exception is the external service exception
        ex.printStackTrace();
      }
      throw e;
    } catch (Exception e) {
      e.printStackTrace();
      ExternalServiceUtils.pauseFscOperationalStatus(pFsc);
      try {
        ds.putEntity(pFsc, user);
      } catch (ODKEntityPersistException ex) {
        // ignore -- important exception is the external service exception
        ex.printStackTrace();
      } catch (ODKOverQuotaException ex) {
        // ignore -- important exception is the external service exception
        ex.printStackTrace();
      }
      throw new ODKExternalServiceException(e);
    }

  }

  private int renewTaskLock(int counter) throws ODKTaskLockException, ODKExternalServiceException {
    Datastore ds = cc.getDatastore();
    User user = cc.getCurrentUser();

    // renew the lock whenever we've consumed more than 33% of the time
    // budget for the lock. This adjusts for very slow external service
    // response times, though if the response time is more than the lock
    // expiration timeout, we can still get into trouble.
    if ((System.currentTimeMillis() - lastUpdateTimestamp + 1) > (TaskLockType.UPLOAD_SUBMISSION
        .getLockExpirationTimeout() / 3)) {
      // renew lock
      TaskLock taskLock = ds.createTaskLock(user);
      // TODO: figure out what to do if this returns false
      if (!taskLock.renewLock(lockId, getUploadSubmissionsTaskLockName(),
          TaskLockType.UPLOAD_SUBMISSION)) {
        logger.error("UploadSubmission task lock -- FAILED renewal -- records transmitted: "
            + counter);
        throw new ODKExternalServiceException("UploadSubmission TaskLock renewal failed");
      } else {
        taskLock = null;
        logger.info("UploadSubmission task lock renewed -- records transmitted: " + counter);
        counter = 0;
        lastUpdateTimestamp = System.currentTimeMillis();
      }
    }
    return counter;
  }

  private List<Submission> querySubmissionsDateRange(Date startDate, Date endDate, String uriLast)
      throws ODKFormNotFoundException, ODKIncompleteSubmissionData, ODKDatastoreException {
    // query for next set of submissions
    QueryByDateRange query = new QueryByDateRange(form, getQueryLimit(), startDate, endDate,
        uriLast, cc);
    List<Submission> submissions = query.getResultSubmissions(cc);
    return submissions;
  }

  private List<Submission> querySubmissionsStartDate(Date startDate, String uriLast)
      throws ODKFormNotFoundException, ODKIncompleteSubmissionData, ODKDatastoreException {
    // query for next set of submissions
    // (excluding the very recent submissions that haven't settled yet).
    QueryByDateRange query = new QueryByDateRange(form, getQueryLimit(), startDate, uriLast, cc);
    List<Submission> submissions = query.getResultSubmissions(cc);
    return submissions;
  }
}
