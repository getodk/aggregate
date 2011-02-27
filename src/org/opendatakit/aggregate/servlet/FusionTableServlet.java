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
package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceConsts;
import org.opendatakit.aggregate.constants.externalservice.ExternalServiceOption;
import org.opendatakit.aggregate.constants.externalservice.FusionTableConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceAuthenticationError;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKExternalServiceNotAuthenticated;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.externalservice.OAuthToken;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FusionTableServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -8068275263542194677L;

  /**
   * URI from base
   */
  public static final String ADDR = "extern/fusiontables";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Create Google Fusion Table";

  /**
   * Handler for HTTP Get request to create a google spreadsheet
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

    // get parameters
    String formId = getParameter(req, ServletConsts.FORM_ID);
    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);

    ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);

    // store parameters for web redirect
    Map<String, String> params = new HashMap<String, String>();
    params.put(ServletConsts.FORM_ID, formId);
    params.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esTypeString);

    // verify user has been authorized for google fusion tables
    OAuthToken authToken = null;
    try {
      authToken = verifyGDataAuthorization(req, resp);
    } catch (ODKExternalServiceAuthenticationError e) {
      return; // verifyGDataAuthroization function formats response
    } catch (ODKExternalServiceNotAuthenticated e) {
      // do nothing already set to null
    }

    // need to obtain authorization to fusion table
    if (authToken == null) {
      beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
      String authButton = generateAuthButton(ExternalServiceConsts.AUTHORIZE_FUSION_CREATION,
          params, req, resp, FusionTableConsts.FUSION_SCOPE);
      resp.getWriter().print(authButton);
      finishBasicHtmlResponse(resp);
      return;
    }

    // create fusion table
    FusionTable fusion;

    try {
      Form form = Form.retrieveForm(formId, cc);
      fusion = FusionTable.createFusionTable(form, authToken, esType, cc);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    } catch (ODKExternalServiceException e1) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, e1.getMessage());
      e1.printStackTrace();
      return;
    } catch (ODKDatastoreException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      e.printStackTrace();
      return;
    }

    // upload data to fusion table
    if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
      try {
        UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
        uploadTask.createFormUploadTask(fusion.getFormServiceCursor(), cc);
      } catch (Exception e) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
        e.printStackTrace();
        return;
      }
    }

    resp.sendRedirect(cc.getWebApplicationURL(FormsServlet.ADDR));

  }

}
