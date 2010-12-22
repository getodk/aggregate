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
import java.io.PrintWriter;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
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
import org.opendatakit.aggregate.task.UploadSubmissions;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;

/**
 * Servlet to process a submission from a form
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class SubmissionServlet extends ServletUtilBase {

  private static final String XML_FILE_DESC = "XML Submission File:";

  private static final String ATTACHMENT_FILE_DESC = "Data File(s) that are Part of the Submission (Pictures, Video, etc):";

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -9115712148453543651L;

  /**
   * URI from base
   */
  public static final String ADDR = "submission";

  private static final String TITLE = "Submission Upload";

  private static final String DATAFILE = "datafile";

  /**
   * Handler for HTTP Get request that processes a form submission
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();

    beginBasicHtmlResponse(TITLE, resp, req, true); // header info
    out.write(HtmlUtil.createFormBeginTag(ADDR, HtmlConsts.MULTIPART_FORM_DATA, HtmlConsts.POST));
    out.write(XML_FILE_DESC + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, ServletConsts.XML_SUBMISSION_FILE,
        null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(ATTACHMENT_FILE_DESC + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, "Upload"));
    out.write(HtmlConsts.FORM_CLOSE);

    finishBasicHtmlResponse(resp);

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
	CallingContext cc = ContextFactory.getCallingContext(getServletContext());

    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);

    try {
      SubmissionParser submissionParser = null;
      if (ServletFileUpload.isMultipartContent(req)) {
        submissionParser = new SubmissionParser(new MultiPartFormData(req), cc);
      } else {
        // TODO: check that it is the proper types we can deal with
        // XML received
        submissionParser = new SubmissionParser(req.getInputStream(), cc);
      }

      if (submissionParser == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.INPUTSTREAM_ERROR);
        return;
      }

      // TODO: mitch are we assuming the submissionParser always persists?
      Form form = Form.retrieveForm(submissionParser.getSubmissionFormId(), cc);
      Submission submission = submissionParser.getSubmission();

      // send information to remote servers that need to be notified
      List<ExternalService> tmp = FormServiceCursor.getExternalServicesForForm(form,
          getServerURL(req), cc);
      UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);
      try {
    	  cc.setAsDaemon(true);
	      for (ExternalService rs : tmp) {
	        uploadTask.createFormUploadTask(rs.getFormServiceCursor(), getServerURL(req), cc);
	      }
      } finally {
    	  cc.setAsDaemon(false);
      }

      resp.setStatus(HttpServletResponse.SC_CREATED);
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
    } catch (ODKParseException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
    } catch (ODKEntityPersistException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PARSING_PROBLEM);
    } catch (ODKExternalServiceException e) {
      e.printStackTrace();
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
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PARSING_PROBLEM);
    } catch (FileUploadException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
    }
  }
}
