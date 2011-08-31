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
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.CsvWorkerImpl;
import org.opendatakit.common.web.CallingContext;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class CsvGeneratorTaskServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 5552217246831515463L;

  /**
   * URI from base
   */
  public static final String ADDR = "gae/csvGeneratorTask";

  /**
   * Handler for HTTP Get request to create xform upload page
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // TODO: talk to MITCH about the fact the user will be incorrect
	CallingContext cc = ContextFactory.getCallingContext(this, req);
	cc.setAsDaemon(true);

    // get parameter
    String formId = getParameter(req, ServletConsts.FORM_ID);
    String persistentResultsString = getParameter(req, ServletConsts.PERSISTENT_RESULTS_KEY);
    if ( persistentResultsString == null ) {
    	errorBadParam(resp);
    	return;
    }
    SubmissionKey persistentResultsKey = new SubmissionKey(persistentResultsString);
    String attemptCountString = getParameter(req, ServletConsts.ATTEMPT_COUNT);
    if ( attemptCountString == null ) {
    	errorBadParam(resp);
    	return;
    }
    Long attemptCount = Long.valueOf(attemptCountString);

    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    Form form = null;
    try {
      form = Form.retrieveFormByFormId(formId, cc);
    } catch (ODKFormNotFoundException e1) {
      odkIdNotFoundError(resp);
      return;
    }
    
    if ( form.getFormDefinition() == null ) {
	  errorRetreivingData(resp);
	  return; // ill-formed definition
    }

    CsvWorkerImpl impl = new CsvWorkerImpl(form, persistentResultsKey, attemptCount, cc);
    
    impl.generateCsv();
  }
}