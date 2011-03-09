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

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceDependencyException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.task.FormDeleteWorkerImpl;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormDeleteTaskServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 8219849865201422548L;

  /**
   * URI from base
   */
  public static final String ADDR = "gae/formDeleteTask";

  /**
   * Handler for HTTP Get request that shows the list of forms
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
    if (formId == null) {
      errorMissingKeyParam(resp);
      return;
    }
    String miscTasksString = getParameter(req, ServletConsts.MISC_TASKS_KEY);
    if ( miscTasksString == null ) {
    	errorBadParam(resp);
    	return;
    }
    SubmissionKey miscTasksKey = new SubmissionKey(miscTasksString);
    String attemptCountString = getParameter(req, ServletConsts.ATTEMPT_COUNT);
    if ( attemptCountString == null ) {
    	errorBadParam(resp);
    	return;
    }
    Long attemptCount = Long.valueOf(attemptCountString);

    Form form;
    try {
      form = Form.retrieveForm(formId, cc);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    }

    FormDeleteWorkerImpl formDelete = new FormDeleteWorkerImpl(form, miscTasksKey, 
    					attemptCount, cc);
      try {
		formDelete.deleteForm();
	} catch (ODKDatastoreException e) {
		e.printStackTrace();
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		return;
	} catch (ODKFormNotFoundException e) {
		odkIdNotFoundError(resp);
		return;
	} catch (ODKExternalServiceDependencyException e) {
		e.printStackTrace();
		resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.toString());
		return;
	}
  }
}
