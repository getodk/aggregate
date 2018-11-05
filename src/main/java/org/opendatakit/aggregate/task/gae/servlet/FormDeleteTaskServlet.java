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
import org.opendatakit.aggregate.task.FormDeleteWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class FormDeleteTaskServlet extends ServletUtilBase {

  /**
   * URI from base
   */
  public static final String ADDR = "gae/formDeleteTask";
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 8219849865201422548L;
  private static final Logger logger = LoggerFactory.getLogger(FormDeleteTaskServlet.class);

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

    String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      errorMissingKeyParam(resp);
      logger.error("missing " + ServletConsts.FORM_ID);
      return;
    }
    String miscTasksString = getParameter(req, ServletConsts.MISC_TASKS_KEY);
    if (miscTasksString == null) {
      errorBadParam(resp);
      logger.error("missing " + ServletConsts.MISC_TASKS_KEY);
      return;
    }
    SubmissionKey miscTasksKey = new SubmissionKey(miscTasksString);
    String attemptCountString = getParameter(req, ServletConsts.ATTEMPT_COUNT);
    if (attemptCountString == null) {
      errorBadParam(resp);
      logger.error("missing " + ServletConsts.ATTEMPT_COUNT);
      return;
    }

    Long attemptCount;
    try {
      attemptCount = Long.valueOf(attemptCountString);
    } catch (NumberFormatException e) {
      errorBadParam(resp);
      logger.error("parsing failed: " + ServletConsts.ATTEMPT_COUNT);
      return;
    }

    IForm form;
    try {
      form = FormFactory.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      odkIdNotFoundError(resp);
      logger.error("fetching form failed: " + e.toString());
      return;
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      logger.error("fetching form failed: " + e.toString());
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      datastoreError(resp);
      logger.error("fetching form failed: " + e.toString());
      return;
    } catch (Exception e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      logger.error("fetching form failed: " + e.toString());
      return;
    }

    try {
      FormDeleteWorkerImpl formDelete = new FormDeleteWorkerImpl(form, miscTasksKey, attemptCount,
          cc);
      formDelete.deleteForm();
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      logger.error("delete form failed: " + e.toString());
      return;
    } catch (ODKExternalServiceDependencyException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      logger.error("delete form failed: " + e.toString());
      return;
    } catch (Exception e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
      logger.error("delete form failed: " + e.toString());
      return;
    }
  }
}
