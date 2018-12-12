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
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.CsvWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class CsvGeneratorTaskServlet extends ServletUtilBase {
  /**
   * URI from base
   */
  public static final String ADDR = "gae/csvGeneratorTask";
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 5552217246831515463L;
  private static final Logger logger = LoggerFactory.getLogger(CsvGeneratorTaskServlet.class);

  /**
   * Handler for HTTP Get request to create xform upload page
   *
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *     javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    cc.setAsDaemon(true);

    logger.info("GAE servlet for CSV generation begins");

    // get parameter
    final String formId = getParameter(req, ServletConsts.FORM_ID);
    if (formId == null) {
      logger.error("Missing " + ServletConsts.FORM_ID + " key");
      errorMissingKeyParam(resp);
      return;
    }
    final String persistentResultsString = getParameter(req, ServletConsts.PERSISTENT_RESULTS_KEY);
    if (persistentResultsString == null) {
      logger.error("Missing " + ServletConsts.PERSISTENT_RESULTS_KEY + " key");
      errorBadParam(resp);
      return;
    }
    SubmissionKey persistentResultsKey = new SubmissionKey(persistentResultsString);
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
      logger.error("Invalid " + ServletConsts.ATTEMPT_COUNT +
          " value: " + attemptCountString + " exception: " + e.toString());
      errorBadParam(resp);
      return;
    }

    IForm form = null;
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

    CsvWorkerImpl impl = new CsvWorkerImpl(form, persistentResultsKey, attemptCount, cc);

    impl.generateCsv();
  }
}
