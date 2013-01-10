package org.opendatakit.aggregate.servlet;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.externalservice.OAuthExternalService;
import org.opendatakit.aggregate.server.ServerPreferencesProperties;
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;

public class OAuth2Servlet extends ServletUtilBase {

  private static final String NOT_OAUTH2_ERROR = "Somehow got a non OAuth2 external service in the OAuth2 servlet";

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -2220857379519391127L;

  /**
   * URI from base
   */
  public static final String ADDR = "auth/auth2";
  private static final String BAD_STATE_OBJECT = "Bad state argument";
  private static final ObjectMapper mapper = new ObjectMapper();

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
    String state = getParameter(req, "state");
    String code = getParameter(req, "code");

    if ( state == null || state.length() < 16 ) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, BAD_STATE_OBJECT);
      return;
    }
    
    StringReader reader = new StringReader(state);

    try {
      @SuppressWarnings("unchecked")
      Map<String,Object> stateData = mapper.readValue(reader, Map.class);
     
      ServerPreferencesProperties.setServerPreferencesProperty(cc, (String) stateData.get("key"), code);

      String uri = (String) stateData.get("uri");

      FormServiceCursor fsc = FormServiceCursor.getFormServiceCursor(uri, cc);
      ExternalService es = fsc.getExternalService(cc);
      ExternalServicePublicationOption esOption = fsc.getExternalServicePublicationOption();

      if (!(es instanceof OAuthExternalService)) {
        throw new ODKExternalServiceException(NOT_OAUTH2_ERROR);
      }

      OAuthExternalService externalService = (OAuthExternalService) es;
      externalService.authenticate2AndCreate(code, cc);

      // upload data to external service
      if (!esOption.equals(ExternalServicePublicationOption.STREAM_ONLY)) {

        UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
        CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
        ccDaemon.setAsDaemon(true);
        uploadTask.createFormUploadTask(fsc, ccDaemon);

      }
    } catch (JsonParseException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, BAD_STATE_OBJECT + e.getMessage());
      e.printStackTrace();
      return;
    } catch (JsonMappingException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, BAD_STATE_OBJECT + e.getMessage());
      e.printStackTrace();
      return;
    } catch (IOException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, BAD_STATE_OBJECT + e.getMessage());
      e.printStackTrace();
      return;
    } catch (ODKEntityNotFoundException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      e.printStackTrace();
      return;
    } catch (ODKOverQuotaException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      e.printStackTrace();
      return;
    } catch (ODKDatastoreException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      e.printStackTrace();
      return;
    } catch (ODKFormNotFoundException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      e.printStackTrace();
      return;
    } catch (ODKExternalServiceException e) {
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getMessage());
      e.printStackTrace();
      return;
    }

    resp.sendRedirect(cc.getWebApplicationURL(AggregateHtmlServlet.ADDR));
  }
}
