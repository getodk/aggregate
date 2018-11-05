/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.task.gae.servlet;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceDependencyException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.PurgeOlderSubmissionsWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple servlet for the GAE invokation of the task action that scans through
 * and removes all of a form's submissions older than a given date.
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class PurgeOlderSubmissionsTaskServlet extends ServletUtilBase {

  /**
   * URI from base
   */
  public static final String ADDR = "gae/purgeOlderSubmissionsTask";
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 8219849865201422548L;
  private static final Logger logger = LoggerFactory.getLogger(PurgeOlderSubmissionsTaskServlet.class);

  /**
   * Handler for HTTP Get request that shows the list of forms
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    cc.setAsDaemon(true);

    // get parameter

    final String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      logger.error("Missing " + ServletConsts.FORM_ID + " key");
      errorMissingKeyParam(resp);
      return;
    }
    final String miscTasksString = getParameter(req, ServletConsts.MISC_TASKS_KEY);
    if (miscTasksString == null) {
      logger.error("Missing " + ServletConsts.MISC_TASKS_KEY + " key");
      errorBadParam(resp);
      return;
    }
    SubmissionKey miscTasksKey = new SubmissionKey(miscTasksString);
    final String attemptCountString = getParameter(req, ServletConsts.ATTEMPT_COUNT);
    if (attemptCountString == null) {
      logger.error("Missing " + ServletConsts.ATTEMPT_COUNT + " key");
      errorBadParam(resp);
      return;
    }
    Long attemptCount = 1L;
    try {
      attemptCount = Long.valueOf(attemptCountString);
    } catch (Exception e) {
      logger.error("Invalid " + ServletConsts.ATTEMPT_COUNT + " value: " + attemptCountString
          + " exception: " + e.toString());
      errorBadParam(resp);
      return;
    }

    IForm form;
    try {
      form = FormFactory.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e) {
      logger.error("Unable to retrieve formId: " + formId + " exception: " + e.toString());
      e.printStackTrace();
      odkIdNotFoundError(resp);
      return;
    } catch (ODKOverQuotaException e) {
      logger.error("Unable to retrieve formId: " + formId + " exception: " + e.toString());
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    } catch (ODKDatastoreException e) {
      logger.error("Unable to retrieve formId: " + formId + " exception: " + e.toString());
      e.printStackTrace();
      datastoreError(resp);
      return;
    }

    if (!form.hasValidFormDefinition()) {
      logger.error("Unable to retrieve formId: " + formId + " invalid form definition");
      errorRetreivingData(resp);
      return; // ill-formed definition
    }

    PurgeOlderSubmissionsWorkerImpl formDelete = new PurgeOlderSubmissionsWorkerImpl(form,
        miscTasksKey, attemptCount, cc);
    try {
      formDelete.purgeOlderSubmissions();
    } catch (ODKDatastoreException e) {
      logger.error("Unable to purge older submissions formId: " + formId + " exception: "
          + e.toString());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    } catch (ODKFormNotFoundException e) {
      logger.error("Unable to purge older submissions formId: " + formId + " exception: "
          + e.toString());
      odkIdNotFoundError(resp);
      return;
    } catch (ODKExternalServiceDependencyException e) {
      logger.error("Unable to purge older submissions formId: " + formId + " exception: "
          + e.toString());
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      return;
    }
  }
}
