/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.servlet;

import java.io.IOException;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ExternalServiceOption;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.GoogleSpreadsheetOAuth;
import org.odk.aggregate.form.remoteserver.OAuthToken;
import org.odk.aggregate.table.SubmissionSpreadsheetTable;

import com.google.gdata.client.authn.oauth.GoogleOAuthHelper;
import com.google.gdata.client.authn.oauth.GoogleOAuthParameters;
import com.google.gdata.client.authn.oauth.OAuthException;
import com.google.gdata.client.authn.oauth.OAuthHmacSha1Signer;
import com.google.gdata.client.spreadsheet.SpreadsheetService;
import com.google.gdata.data.spreadsheet.WorksheetEntry;
import com.google.gdata.util.ServiceException;

public class WorksheetServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 3054003683995535651L;

  /**
   * URI from base
   */
  public static final String ADDR = "worksheet";

 
  /**
   * Handler for HTTP Get request to create xform upload page
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    
    // get parameter
    String odkIdParam = getParameter(req, ServletConsts.ODK_ID);
    if (odkIdParam == null) {
      errorMissingKeyParam(resp);
      return;
    }

    String spreadsheetName = getParameter(req, ServletConsts.SPREADSHEET_NAME_PARAM);
    if (spreadsheetName == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);
    if (esTypeString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }
    
    try {
        // get form
        EntityManager em = EMFactory.get().createEntityManager();
        Form form;
        try {
          form = Form.retrieveForm(em, odkIdParam);
        } catch (ODKFormNotFoundException e) {
          odkIdNotFoundError(resp);
          return;
        }

        GoogleSpreadsheetOAuth spreadsheet = form.getGoogleSpreadsheetWithName(spreadsheetName);
        if(spreadsheet == null) {
          return;
        }
        OAuthToken authToken = spreadsheet.getAuthToken();
        
        // verify form has a spreadsheet element
        if (spreadsheet == null) {
          errorRetreivingData(resp);
          return;
        }

        SpreadsheetService service = new SpreadsheetService(this
            .getServletContext().getInitParameter("application_name"));
        try {
          GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
          oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
          oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
          oauthParameters.setOAuthToken(authToken.getToken());
          oauthParameters.setOAuthTokenSecret(authToken.getTokenSecret());
          service.setOAuthCredentials(oauthParameters, new OAuthHmacSha1Signer());
        } catch (OAuthException e) {
          // TODO: handle OAuth failure
          e.printStackTrace();
        }

        // TODO: REMOVE after bug is fixed
        // http://code.google.com/p/gdata-java-client/issues/detail?id=103
        service.setProtocolVersion(SpreadsheetService.Versions.V1);

        ExternalServiceOption esType = ExternalServiceOption.valueOf(esTypeString);
        
        try {
          SubmissionSpreadsheetTable subResults =
              new SubmissionSpreadsheetTable(form, req.getServerName(), em, this
                  .getServletContext().getInitParameter("application_name"));

          // TODO: make more robust (currently assuming nothing has touched the
          // sheet)
          // get worksheet
          WorksheetEntry worksheet =
              subResults.getWorksheet(service, spreadsheet.getSpreadsheetKey(), "Sheet 1");

          // verify worksheet was found
          if (worksheet == null) {
            resp
                .sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "COULD NOT FIND WORKSHEET");
            return;
          }
          subResults.generateWorksheet(service, worksheet);
          
          if (!esType.equals(ExternalServiceOption.STREAM_ONLY)) {
            subResults.uploadSubmissionDataToSpreadsheet(service, worksheet);
          }
          
        } catch (ODKIncompleteSubmissionData e1) {
          errorRetreivingData(resp);
          return;
        }

        if (!esType.equals(ExternalServiceOption.UPLOAD_ONLY)) {
          spreadsheet.updateReadyValue();
        } else {
          form.removeGoogleSpreadsheet(spreadsheet);

          // remove spreadsheet permission as no longer needed
          try {
            GoogleOAuthParameters oauthParameters = new GoogleOAuthParameters();
            oauthParameters.setOAuthConsumerKey(ServletConsts.OAUTH_CONSUMER_KEY);
            oauthParameters.setOAuthConsumerSecret(ServletConsts.OAUTH_CONSUMER_SECRET);
            oauthParameters.setOAuthToken(authToken.getToken());
            oauthParameters.setOAuthTokenSecret(authToken.getTokenSecret());
            GoogleOAuthHelper oauthHelper = new GoogleOAuthHelper(new OAuthHmacSha1Signer());
            oauthHelper.revokeToken(oauthParameters);
          } catch (OAuthException e) {
            e.printStackTrace();
          }

        }
 
        em.close();


    } catch (ServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
