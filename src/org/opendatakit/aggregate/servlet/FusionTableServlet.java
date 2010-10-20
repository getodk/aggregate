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
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKExternalServiceAuthenticationError;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKExternalServiceNotAuthenticated;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.externalservice.FusionTable;
import org.opendatakit.aggregate.externalservice.constants.ExternalServiceOption;
import org.opendatakit.aggregate.query.submission.QueryByDate;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

public class FusionTableServlet extends ServletUtilBase {


  
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -8068275263542194677L;

  /**
   * URI from base
   */
  public static final String ADDR = "fusiontables";

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

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    UserService userService = (UserService) ContextFactory.get().getBean(
        ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();

    // get parameters
    String formId = getParameter(req, ServletConsts.ODK_ID);
    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);

    ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);
    
    // store parameters for web redirect
    Map<String, String> params = new HashMap<String, String>();
    params.put(ServletConsts.ODK_ID, formId);
    params.put(ServletConsts.EXTERNAL_SERVICE_TYPE, esTypeString);

    // verify user has been authorized for google fusion tables
    String authToken = null;
    try {
       authToken = verifyGDataAuthorization(req, resp, ServletConsts.FUSION_SCOPE);
    } catch (ODKExternalServiceAuthenticationError e) {
       return; // verifyGDataAuthroization function formats response
    } catch (ODKExternalServiceNotAuthenticated e) {
       // do nothing already set to null
    }

    // need to obtain authorization to fusion table
    if (authToken == null) {
       beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info
       String authButton = generateAuthButton(ServletConsts.FUSION_SCOPE,
             ServletConsts.AUTHORIZE_FUSION_CREATION, params, req, resp);
       resp.getWriter().print(authButton);
       finishBasicHtmlResponse(resp);
       return;
    }


    // get form
    Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);
    FormDefinition fd;
      fd = FormDefinition.getFormDefinition(formId, ds, user);
      if ( fd == null ) {
       	odkIdNotFoundError(resp);
       	return;
      }
    
    FusionTable fusion;
    try {
      fusion = new FusionTable(fd, authToken, esType, ds, user);
    } catch (ODKEntityPersistException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      return;
    } catch (ODKExternalServiceException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      return;
    } catch (ODKDatastoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
		return;
	}
    
    if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
      try {
        QueryByDate query = new QueryByDate(fd, BasicConsts.EPOCH, false,
                  ServletConsts.FETCH_LIMIT, ds, user);
        List<Submission> submissions = query.getResultSubmissions();
        fusion.sendSubmissions(submissions);
      } catch (ODKFormNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
      } catch (ODKIncompleteSubmissionData e) {
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

    resp.sendRedirect(ServletConsts.WEB_ROOT);

  }

}
