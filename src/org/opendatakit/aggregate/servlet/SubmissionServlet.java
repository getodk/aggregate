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

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKExternalServiceException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.externalservice.ExternalService;
import org.opendatakit.aggregate.externalservice.FormServiceCursor;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.SubmissionParser;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet to process a submission from a form
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class SubmissionServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -9115712148453543651L;

  /**
   * URI from base
   */
  public static final String ADDR = "submission";

  /**
   * Handler for HTTP Get request that processes a form submission
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType(HtmlConsts.RESP_TYPE_XML);

    UserService userService = (UserService) ContextFactory.get().getBean(
        ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();
    
    // get parameter
    String odkId = getParameter(req, ServletConsts.ODK_ID);
    if (odkId == null) {
      errorMissingKeyParam(resp);
      return;
    }

    // get form
    Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);
    Form form;
    String originalText;
    try {
      form = Form.retrieveForm(odkId, ds, user, userService.getCurrentRealm());
      originalText = form.getFormXml();
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    } catch (ODKDatastoreException e) {
    	e.printStackTrace();
    	odkIdNotFoundError(resp);
    	return;
    }

    if (form != null) {
      resp.getWriter().print(originalText);
    } else {
      odkIdNotFoundError(resp);
    }
  }

  /**
   * Handler for HTTP post request that processes a form submission Currently
   * supports plain/xml and multipart
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);

    UserService userService = (UserService) ContextFactory.get().getBean(
        ServletConsts.USER_BEAN);
    User user = userService.getCurrentUser();

    Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);

    String reqUrl = req.getRequestURL().toString();
    
    try {
      SubmissionParser submissionParser = null;
      if (ServletFileUpload.isMultipartContent(req)) {
        submissionParser = new SubmissionParser(new MultiPartFormData(req), ds, user, userService.getCurrentRealm());
      } else {
        // TODO: check that it is the proper types we can deal with
        // XML received
        submissionParser = new SubmissionParser(req.getInputStream(), ds, user, userService.getCurrentRealm());
      }

      if (submissionParser == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.INPUTSTREAM_ERROR);
        return;
      }

      FormDefinition formDefinition = submissionParser.getFormDefinition();
      String appName = this.getServletContext().getInitParameter("application_name");
      Submission submission = submissionParser.getSubmission();
      
      // TODO: Waylon -- I think you're moving this to a background task.
      // Is this still needed here?  If so, 
      Form form = Form.retrieveForm(formDefinition.getFormId(), ds, user, userService.getCurrentRealm());
      List<ExternalService> tmp = FormServiceCursor.getExternalServicesForForm(form.getKey(), 
				formDefinition, ds, user);
      // send information to remote servers that need to be notified
      for (ExternalService rs : tmp) {
        rs.sendSubmission(submission);
      }
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (ODKParseException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
    } catch (ODKEntityPersistException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
    } catch (ODKExternalServiceException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();      
      // TODO: is this the right error?
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.TASK_PROBLEM);
    } catch (ODKIncompleteSubmissionData e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
	} catch (ODKConversionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
	} catch (ODKDatastoreException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
	} catch (FileUploadException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
	}
    resp.setHeader("Location", reqUrl);
  }
}
