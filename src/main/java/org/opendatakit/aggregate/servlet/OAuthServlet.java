package org.opendatakit.aggregate.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.exception.ODKExternalServiceAuthenticationError;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKExternalServiceNotAuthenticated;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.OAuthExternalService;
import org.opendatakit.aggregate.externalservice.OAuthToken;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.common.web.CallingContext;

public class OAuthServlet extends ServletUtilBase {

  private static final String NOT_OAUTH_ERROR = "Somehow got a non OAuth external service in the OAuth servlet";

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -2220857379519391127L;

  /**
   * URI from base
   */
  public static final String ADDR = "auth/auth";

  /**
   * Callback from external service acknowledging acceptable authentication
   * token.
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // get parameters
    String uri = getParameter(req, UIConsts.FSC_URI_PARAM);

    // get authToken
    OAuthToken authToken = null;
    try {
      authToken = verifyGDataAuthorization(req, resp);
    } catch (ODKExternalServiceAuthenticationError e) {
      e.printStackTrace();
      return; // verifyGDataAuthroization function formats response
    } catch (ODKExternalServiceNotAuthenticated e) {
      e.printStackTrace();
      // do nothing already set to null
    }

    try {
      if (authToken != null) {
        FormServiceCursor fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
        ExternalService es = fsc.getExternalService(cc);
        ExternalServicePublicationOption esOption = fsc.getExternalServicePublicationOption();

        if (!(es instanceof OAuthExternalService)) {
          throw new ODKExternalServiceException(NOT_OAUTH_ERROR);
        }

        OAuthExternalService externalService = (OAuthExternalService) es;
        externalService.authenticateAndCreate(authToken, cc);

        // upload data to external service
        if (!esOption.equals(ExternalServicePublicationOption.STREAM_ONLY)) {

          UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
          CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
          ccDaemon.setAsDaemon(true);
          uploadTask.createFormUploadTask(fsc, ccDaemon);

        }
      }
    } catch (Exception e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      e.printStackTrace();
      return;
    }

    resp.sendRedirect(cc.getWebApplicationURL(AggregateHtmlServlet.ADDR));
  }
}
