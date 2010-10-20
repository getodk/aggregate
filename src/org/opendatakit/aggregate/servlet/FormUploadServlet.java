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
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.security.User;
import org.opendatakit.common.security.UserService;

/**
 * Servlet to upload, parse, and save an XForm
 * 
 * @author wbrunette@gmail.com
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
  public static final String ADDR = "upload";

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

  
  /**
   * Handler for HTTP Get request to create xform upload page
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

    beginBasicHtmlResponse(TITLE_INFO, resp, req, true); // header info

    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(ADDR, HtmlConsts.MULTIPART_FORM_DATA,
        HtmlConsts.POST));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write("Location of Xform definition to be uploaded:" + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, ServletConsts.FORM_DEF_PRAM, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_SUBMIT, null, "Upload"));
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

    // verify user is logged in
    if (!verifyCredentials(req, resp)) {
      return;
    }

    // verify request is multipart
    if (!ServletFileUpload.isMultipartContent(req)) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.NO_MULTI_PART_CONTENT);
      return;
    }

    boolean bOk = false;
    // TODO Add in form title process so it will update the changes in the XML of form

    try {
      // process form
      MultiPartFormData uploadedFormItems = new MultiPartFormData(req);

      MultiPartFormItem formNameData = uploadedFormItems
          .getFormDataByFieldName(ServletConsts.FORM_NAME_PRAM);
      MultiPartFormItem formXmlData = uploadedFormItems
          .getFormDataByFieldName(ServletConsts.FORM_DEF_PRAM);

      FormParserForJavaRosa parser = null;
      String formName = null;
      String formXml = null;
      String xmlFileName = "default.xml";

      if (formNameData != null) {
        formName = formNameData.getStream().toString("UTF-8");
      }
      if (formXmlData != null) {
        // TODO: changed added output stream writer. probably something better
        // exists
        formXml = formXmlData.getStream().toString("UTF-8");
        xmlFileName = formXmlData.getFilename();
      }

      // persist form
      Datastore ds = (Datastore) ContextFactory.get().getBean(ServletConsts.DATASTORE_BEAN);

      if (formXml == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
        return;
      }

      try {
        UserService userService = (UserService) ContextFactory.get().getBean(
            ServletConsts.USER_BEAN);
        User user = userService.getCurrentUser();
        parser = new FormParserForJavaRosa(formName, formXml, xmlFileName, ds, user,
        		userService.getCurrentRealm().getRootDomain());
        
        Form form = Form.retrieveForm(parser.getFormId(), ds, user, userService.getCurrentRealm());
        // form.persist(ds, uriUser);
        form.printDataTree(System.out);
        bOk = true;
        
      } catch (ODKFormAlreadyExistsException e) {
        resp.sendError(HttpServletResponse.SC_CONFLICT, ErrorConsts.FORM_WITH_ODKID_EXISTS);
        return;
      } catch (ODKIncompleteSubmissionData e) {
        switch (e.getReason()) {
        case TITLE_MISSING:
          createTitleQuestionWebpage(req, resp, formXml, xmlFileName); 
          return;
        case ID_MISSING:
          resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_ID);
          return;
        default:
          // just move on
        }
      } catch (ODKEntityPersistException e) {
        // TODO NEED TO FIGURE OUT PROPER ACTION FOR ERROR
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
      } catch (ODKDatastoreException e) {
        // TODO NEED TO FIGURE OUT PROPER ACTION FOR ERROR
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
      } catch (ODKConversionException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PARSING_PROBLEM + "\n" + e.getMessage());
	} catch (ODKFormNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.ODKID_NOT_FOUND);
	} catch (ODKParseException e) {
		// unfortunately, the underlying javarosa utility swallows the parsing error.
		e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM + "\n" + e.getMessage());
	}

    } catch (FileUploadException e) {
      e.printStackTrace(resp.getWriter());
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.UPLOAD_PROBLEM);
    }

    if ( bOk ) resp.sendRedirect(FormsServlet.ADDR);
  }

  private void createTitleQuestionWebpage(HttpServletRequest req, HttpServletResponse resp,
      String formXml, String xmlFileName) throws IOException {
    beginBasicHtmlResponse(OBTAIN_TITLE_INFO, resp, req, true); // header info

    PrintWriter out = resp.getWriter();
    out.write(HtmlUtil.createFormBeginTag(FormUploadServlet.ADDR, HtmlConsts.MULTIPART_FORM_DATA, HtmlConsts.POST));
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
