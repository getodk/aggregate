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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opendatakit.aggregate.CallingContext;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.parser.FormParserForJavaRosa;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;

/**
 * Servlet to upload, parse, and save an XForm
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class FormUploadServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -3784460108221008112L;

  /**
   * URI from base
   */
  public static final String ADDR = "admin/upload";

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Xform Upload";

  /**
   * Title for generated webpage to obtain title
   */
  private static final String OBTAIN_TITLE_INFO = "Xform Title Entry";

  /**
   * Text to display to user to obtain title
   */
  private static final String TITLE_OF_THE_XFORM = "Title of the Xform:";

  private static final String DATAFILE = "datafile";

  private static final String LOCATION_OF_XFORM_DEFINITION = "Location of Xform definition to be uploaded:";

  private static final String DATA_FILES_DESCRIPTION = "Data File(s) that are Part of the Form Definition (Pictures, Video, etc):";

  private static final String UPLOAD_BUTTON_TEXT = "Upload Form";

  /**
   * Handler for HTTP Get request to create xform upload page
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

	CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

	PrintWriter out = resp.getWriter();

    beginBasicHtmlResponse(TITLE_INFO, resp, true, cc); // header info
    out.write(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(ADDR), HtmlConsts.MULTIPART_FORM_DATA, HtmlConsts.POST));
    out.write(LOCATION_OF_XFORM_DEFINITION + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, ServletConsts.FORM_DEF_PRAM, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(DATA_FILES_DESCRIPTION + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, DATAFILE, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, UPLOAD_BUTTON_TEXT));
    out.write(HtmlConsts.FORM_CLOSE);
    finishBasicHtmlResponse(resp);
  }

  /**
   * Handler for HTTP Post request that takes an xform, parses, and saves a
   * parsed version in the datastore
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, ADDR, req);

    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);

	/*
	 * OAuth application-layer support for ODK Build publishing.
	 * This is broken with spring security (which is outside the app layer).
	 *  
    User user = cc.getCurrentUser();
	if (user instanceof org.opendatakit.common.security.gae.UserImpl) {
      // We are in app engine

      String authParam = getParameter(req, ServletConsts.AUTHENTICATION);

      if (authParam != null && authParam.equalsIgnoreCase(ServletConsts.AUTHENTICATION_OAUTH)) {
        // Try OAuth authentication
        try {
          user = ((org.opendatakit.common.security.gae.UserServiceImpl) cc.getUserService()).getCurrentOAuthUser();
          if (user.isAnonymous()) {
            resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorConsts.OAUTH_ERROR);
            return;
          }
        } catch (OAuthRequestException e) {
          resp.sendError(HttpServletResponse.SC_UNAUTHORIZED, ErrorConsts.OAUTH_ERROR
              + "\n Reason: " + e.getLocalizedMessage());
          return;
        }
      }
    }
    */

    // verify request is multipart
    if (!ServletFileUpload.isMultipartContent(req)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
      return;
    }

    boolean bOk = false;
    // TODO Add in form title process so it will update the changes in the XML
    // of form

    try {
      // process form
      MultiPartFormData uploadedFormItems = new MultiPartFormData(req);

      FormParserForJavaRosa parser = null;

      MultiPartFormItem formNameData = uploadedFormItems
          .getFormDataByFieldName(ServletConsts.FORM_NAME_PRAM);
      MultiPartFormItem formXmlData = uploadedFormItems
          .getFormDataByFieldName(ServletConsts.FORM_DEF_PRAM);

      String formName = null;
      String inputXml = null;
      String xmlFileName = "default.xml";

      if (formNameData != null) {
        formName = formNameData.getStream().toString(HtmlConsts.UTF8_ENCODE);
      }
      if (formXmlData != null) {
        // TODO: changed added output stream writer. probably something better
        // exists
        inputXml = formXmlData.getStream().toString(HtmlConsts.UTF8_ENCODE);
        xmlFileName = formXmlData.getFilename();
      }

      try {
        parser = new FormParserForJavaRosa(formName, formXmlData, inputXml, xmlFileName,
            uploadedFormItems, cc);

        Form form = Form.retrieveForm(parser.getFormId(), cc);
        form.printDataTree(System.out);
        bOk = true;

      } catch (ODKFormAlreadyExistsException e) {
        resp.sendError(HttpServletResponse.SC_CONFLICT, ErrorConsts.FORM_WITH_ODKID_EXISTS);
        return;
      } catch (ODKIncompleteSubmissionData e) {
        switch (e.getReason()) {
        case TITLE_MISSING:
          createTitleQuestionWebpage(resp, inputXml, xmlFileName, cc);
          return;
        case ID_MALFORMED:
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.JAVA_ROSA_PARSING_PROBLEM
              + e.getMessage());
          return;
        case ID_MISSING:
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_ID);
          return;
        case MISSING_XML:
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
          return;
        case BAD_JR_PARSE:
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.JAVA_ROSA_PARSING_PROBLEM
              + e.getMessage());
          return;
        default:
          // just move on
        }
      } catch (ODKEntityPersistException e) {
        // TODO NEED TO FIGURE OUT PROPER ACTION FOR ERROR
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
      } catch (ODKDatastoreException e) {
        // TODO NEED TO FIGURE OUT PROPER ACTION FOR ERROR
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
      } catch (ODKConversionException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PARSING_PROBLEM
            + "\n" + e.getMessage());
      } catch (ODKFormNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.ODKID_NOT_FOUND);
      } catch (ODKParseException e) {
        // unfortunately, the underlying javarosa utility swallows the parsing
        // error.
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM + "\n"
            + e.getMessage());
      }

    } catch (FileUploadException e) {
      e.printStackTrace(resp.getWriter());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.UPLOAD_PROBLEM);
    }

    if (bOk) {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.setHeader("Location", cc.getServerURL());
      resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
      resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
      PrintWriter out = resp.getWriter();
      out.write(HtmlConsts.HTML_OPEN);
      out.write(HtmlConsts.BODY_OPEN);
      out.write("Successful form upload.  Click ");
      out.write(HtmlUtil.createHref(cc.getWebApplicationURL(FormsServlet.ADDR), "here"));
      out.write(" to return to forms page.");
      out.write(HtmlConsts.BODY_CLOSE);
      out.write(HtmlConsts.HTML_CLOSE);
    }
  }

  private void createTitleQuestionWebpage(HttpServletResponse resp,
      String formXml, String xmlFileName, CallingContext cc) throws IOException {
    beginBasicHtmlResponse(OBTAIN_TITLE_INFO, resp, true, cc); // header info

    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(cc.getWebApplicationURL(FormUploadServlet.ADDR), HtmlConsts.MULTIPART_FORM_DATA,
        HtmlConsts.POST));
    out.write(TITLE_OF_THE_XFORM + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_TEXT, ServletConsts.FORM_NAME_PRAM, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.encodeFormInHiddenInput(formXml, xmlFileName));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, "Submit"));
    out.write(HtmlConsts.FORM_CLOSE);
    finishBasicHtmlResponse(resp);
  }

}
