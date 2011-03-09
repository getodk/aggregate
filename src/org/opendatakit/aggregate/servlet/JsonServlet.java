/*
 * Copyright (C) 2009 Google Inc. 
 * Copyright (C) 2010 University of Washington.
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

package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.JsonServer;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class JsonServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 456146061385437109L;
  /**
   * URI from base
   */
  public static final String ADDR = "extern/json";

  /**
   * Handler for HTTP Get request to create a google spreadsheet
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

    // TODO: rename params so not spreadsheet

    // get parameter
    String serverUrl = "bogusUrl"; // TODO: hook this up...
    String formId = getParameter(req, ServletConsts.FORM_ID);
    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    
    ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);

    if (serverUrl == null || formId == null || esTypeString == null || esType == null) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
      return;
    }

    Form form;
    JsonServer jsonServer;
    try {
      form = Form.retrieveForm(formId, cc);
      jsonServer = new JsonServer(form, serverUrl, esType, cc);
    } catch (ODKFormNotFoundException e) {
  	  odkIdNotFoundError(resp);
	  return;
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
      return;
    } catch (ODKEntityPersistException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      return;
    } catch (ODKDatastoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}

    if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {

      try {
    	QueryByDate query = new QueryByDate(form, BasicConsts.EPOCH, false,
                  ServletConsts.FETCH_LIMIT, cc);

        List<Submission> submissions = query.getResultSubmissions(cc);
        jsonServer.sendSubmissions(submissions, cc);

      } catch (ODKFormNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ODKExternalServiceException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ODKDatastoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
    }

    resp.sendRedirect(cc.getWebApplicationURL(FormsServlet.ADDR));
  }

}
