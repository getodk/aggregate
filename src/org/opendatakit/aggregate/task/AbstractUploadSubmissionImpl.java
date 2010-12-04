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

import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.TaskLockType;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKTaskLockException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.query.submission.QueryByDateRange;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.TaskLock;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public abstract class AbstractUploadSubmissionImpl implements UploadSubmissions {

  private static final int DELAY_BETWEEN_RELEASE_RETRIES = 1000;
  private static final int MAX_NUMBER_OF_RELEASE_RETRIES = 10;

  public abstract void createFormUploadTask(FormServiceCursor fsc, User user)
      throws ODKExternalServiceException;

  @Override
  public void uploadSubmissions(FormServiceCursor fsc, String baseServerWebUrl, Datastore ds,
      User user) throws ODKEntityNotFoundException, ODKExternalServiceException,
      ODKFormNotFoundException, ODKTaskLockException {

    ExternalServiceOption esOption = fsc.getExternalServiceOption();
    ExternalService es = fsc.getExternalService(baseServerWebUrl, ds, user);
    Form form = Form.retrieveForm(fsc.getFormId(), ds, user);

    TaskLock taskLock = ds.createTaskLock();
    String lockId = UUID.randomUUID().toString();
    if (!taskLock.obtainLock(lockId, form, TaskLockType.UPLOAD_SUBMISSION)) {
      createFormUploadTask(fsc, user);
    }
    taskLock = null;
    try {
      switch (esOption) {
      case UPLOAD_ONLY:
        if (fsc.getUploadCompleted()) {
          es.delete();
        } else {
          uploadSubmissions(fsc, es, form, lockId, ds, user);
        }
        break;
      case STREAM_ONLY:
        streamSubmissions(fsc, es, form, lockId, ds, user);
        break;
      case UPLOAD_N_STREAM:
        if (!fsc.getUploadCompleted()) {
          uploadSubmissions(fsc, es, form, lockId, ds, user);
        }
        streamSubmissions(fsc, es, form, lockId, ds, user);
        break;
      case NONE:
        break;
      }
    } catch (Exception e) {
      // TODO: do something smarter with exceptions
      throw new ODKExternalServiceException(e);
    } finally {
      taskLock = ds.createTaskLock();
      for (int i = 0; i < MAX_NUMBER_OF_RELEASE_RETRIES; i++) {
        if (taskLock.releaseLock(lockId, form, TaskLockType.UPLOAD_SUBMISSION))
          break;
        try {
          Thread.sleep(DELAY_BETWEEN_RELEASE_RETRIES);
        } catch (InterruptedException e) {
          // just move on, this retry mechanism is to only make things nice
        }
      }
    }
  }

  private void uploadSubmissions(FormServiceCursor fsc, ExternalService es, Form form,
      String lockId, Datastore ds, User user) throws Exception {

    Date startDate = null;
    Date endDate = fsc.getEstablishmentDateTime();
    String lastUploadKey = fsc.getLastUploadKey();

    // check cursor for last upload date
    String lastUploadCursor = fsc.getLastUploadPersistenceCursor();
    if (lastUploadCursor != null && !lastUploadCursor.isEmpty()) {
      startDate = new Date(Long.parseLong(lastUploadCursor));
    } else {
      startDate = BasicConsts.EPOCH;
    }

    boolean keepChecking = true;
    while (keepChecking) {
      // query for next set of submissions
      QueryByDateRange query = new QueryByDateRange(form, ServletConsts.SUBMISSIONS_FETCH_LIMIT,
          startDate, endDate, ds, user);
      List<Submission> submissions = query.getResultSubmissions();

      if (submissions == null || submissions.size() == 0) {
        // there are no submissions so uploading is complete
        fsc.setUploadCompleted(true);
        keepChecking = false;

        // create another task to either start streaming OR to delete if upload
        // ONLY
        createFormUploadTask(fsc, user);
      } else {
        if (lastUploadKey == null) {
          sendSubmissionsForUpload(fsc, es, submissions, ds, user);
        } else {
          // find the last submission sent, so we don't resend it
          int indexOfLastSubmission = -1;
          for (int i = 0; i < submissions.size(); i++) {
            if (submissions.get(i).getKey().getKey().equals(lastUploadKey))
              indexOfLastSubmission = i;
          }
          if (indexOfLastSubmission > -1) {
            // we found the last submission that was sent, so now we can send
            // all the submission after that one
            List<Submission> submissionsToSend = new ArrayList<Submission>();
            submissionsToSend = submissions.subList(indexOfLastSubmission + 1, submissions.size());
            sendSubmissionsForUpload(fsc, es, submissionsToSend, ds, user);
          } else {
            // didn't find the last sent submission, so need to query again
            startDate = submissions.get(submissions.size() - 1).getSubmittedTime();
          }
        }
      }
    }
  }

  private void sendSubmissionsForUpload(FormServiceCursor fsc, ExternalService es,
      List<Submission> submissionsToSend, Datastore ds, User user)
      throws ODKExternalServiceException {
    String lastDateSent = null;
    String lastKeySent = null;
    try {
      for (Submission submission : submissionsToSend) {
        es.sendSubmission(submission);
        lastDateSent = String.valueOf(submission.getSubmittedTime().getTime());
        lastKeySent = submission.getKey().getKey();
      }
      fsc.setLastUploadPersistenceCursor(lastDateSent);
      fsc.setLastUploadKey(lastKeySent);
      ds.putEntity(fsc, user);
    } catch (Exception e) {
      if (lastDateSent != null && lastKeySent != null) {
        fsc.setLastUploadPersistenceCursor(lastDateSent);
        fsc.setLastUploadKey(lastKeySent);
        // TODO: figure out what to do when datastore throws exception here
        try {
          ds.putEntity(fsc, user);
        } catch (ODKEntityPersistException e1) {
          // we were trying to save things... but just move on if things are in
          // bad shape
          // TODO: decide how to move submissions forward
        }
        throw new ODKExternalServiceException(e);
      }
    }
  }

  private void streamSubmissions(FormServiceCursor fsc, ExternalService es, Form form,
      String lockId, Datastore ds, User user) {

  }

}
