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
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.exception.ODKConversionException;
import org.opendatakit.aggregate.exception.ODKFormAlreadyExistsException;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.exception.ODKIncompleteSubmissionData;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.aggregate.parser.FormParserForJavaRosa;
import org.opendatakit.aggregate.parser.MultiPartFormData;
import org.opendatakit.aggregate.parser.MultiPartFormItem;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityPersistException;
import org.opendatakit.common.web.CallingContext;

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
  public static final String ADDR = UIConsts.FORM_UPLOAD_SERVLET_ADDR;

  /**
   * Title for generated webpage
   */
  private static final String TITLE_INFO = "Xform Upload";
  
  /**
   * Script path to include...
   */
  private static final String UPLOAD_SCRIPT_RESOURCE = "javascript/upload_control.js";

  private static final String UPLOAD_PAGE_BODY_START = 

"	  <p><b>Upload one form into ODK Aggregate</b></p>" +
"	  <p>Media files for the form's logo and the icons, images, audio clips and video clips used " +
"     within the form (if any) are" + 
"	  expected to be in a media folder in the same directory as the form definition file (.xml)." + 
"	  If the form definition file is named \"<code>My Form.xml</code>\" then the media folder should" +
"	  be named \"<code>My Form-media</code>\". Please use the form below to upload the form definition" + 
"	  file and the contents of the media folder, if any, into ODK Aggregate.</p>" +
"	  <p>On ODK Collect 1.1.7 and higher, the file named \"<code>form_logo.png</code>\"," + 
"	  if present in the media folder, will be displayed as the form's logo. </p>" +
"	  <!--[if true]><p style=\"color: red;\">For a better user experience, use Chrome, Firefox or Safari</p>" +
"	  <![endif] -->" +
"     <form id=\"ie_backward_compatible_form\"" + 
"	                      accept-charset=\"UTF-8\" method=\"POST\" encoding=\"multipart/form-data\" enctype=\"multipart/form-data\"" + 
"	                      action=\"";// emit the ADDR
  private static final String UPLOAD_PAGE_BODY_MIDDLE = "\">" +
"	  <table>" +
"	  	<tr>" +
"	  		<td><label for=\"form_def_file\">Form definition:</label></td>" +
"	  		<td><input id=\"form_def_file\" type=\"file\" size=\"80\"" +
"	  			name=\"form_def_file\" /></td>" +
"	  	</tr>" +
"	  	<tr>" +
"	  		<td><label for=\"mediaFiles\">Media file(s):</label></td>" +
"	  		<td><input id=\"mediaFiles\" type=\"file\" size=\"80,20\" name=\"datafile\" multiple /><input id=\"clear_media_files\" type=\"button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles')\" /></td>" +
"	  	</tr>" +
"	  	<!--[if true]>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles2\">Media file #2:</label></td>" +
"	          <td><input id=\"mediaFiles2\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files2\" type=\"button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles2')\" /></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles3\">Media file #3:</label></td>" +
"	          <td><input id=\"mediaFiles3\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files3\" type=\"button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles3')\" /></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles4\">Media file #4:</label></td>" +
"	          <td><input id=\"mediaFiles4\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files4\" type=\"button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles4')\" /></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles5\">Media file #5:</label></td>" +
"	          <td><input id=\"mediaFiles5\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files5\" type=\"button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles5')\" /></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles6\">Media file #6:</label></td>" +
"	          <td><input id=\"mediaFiles6\" type=\"file\" size=\"80\" name=\"datafile\" /><input id=\"clear_media_files6\" type=\"button\" value=\"Clear\" onClick=\"clearMediaInputField('mediaFiles6')\" /></td>" +
"	      </tr>" +
"	      <![endif]-->" +
"	  	<tr>" +
"	  		<td><input type=\"submit\" name=\"button\" value=\"Upload Form\" /></td>" +
"	  		<td />" +
"	  	</tr>" +
"	  </table>" +
"	  </form>";

  /**
   * Title for generated webpage to obtain title
   */
  private static final String OBTAIN_TITLE_INFO = "Xform Title Entry";

  /**
   * Text to display to user to obtain title
   */
  private static final String TITLE_OF_THE_XFORM = "Title of the Xform:";

  private static final Logger logger = Logger.getLogger(FormUploadServlet.class.getName());
  /**
   * Handler for HTTP Get request to create xform upload page
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

	CallingContext cc = ContextFactory.getCallingContext(this, req);

	StringBuilder headerString = new StringBuilder();
	headerString.append("<script type=\"application/javascript\" src=\"");
	headerString.append(cc.getWebApplicationURL(UPLOAD_SCRIPT_RESOURCE));
	headerString.append("\"></script>");
	beginBasicHtmlResponse(TITLE_INFO, headerString.toString(), resp, cc );// header info
	PrintWriter out = resp.getWriter();
	out.write(UPLOAD_PAGE_BODY_START);
	out.write(cc.getWebApplicationURL(ADDR));
	out.write(UPLOAD_PAGE_BODY_MIDDLE);
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
	CallingContext cc = ContextFactory.getCallingContext(this, req);

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
    StringBuilder warnings = new StringBuilder();
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
            uploadedFormItems, warnings, cc);

        logger.info("Upload form: " + parser.getFormId());
        Form form = Form.retrieveForm(parser.getFormId(), cc);
        String isIncompleteFlag = uploadedFormItems.getSimpleFormField(ServletConsts.TRANSFER_IS_INCOMPLETE);
        if ( isIncompleteFlag != null && isIncompleteFlag.trim().length() != 0 ) {
        	// not complete yet...
        	form.setDownloadEnabled(false);
        	form.persist(cc);
        } else {
        	form.setDownloadEnabled(true);
        	form.persist(cc);
        }
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
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            ErrorConsts.PERSISTENCE_LAYER_PROBLEM);
      } catch (ODKConversionException e) {
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.PARSING_PROBLEM
            + "\n" + e.getMessage());
      } catch (ODKFormNotFoundException e) {
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ErrorConsts.ODKID_NOT_FOUND);
      } catch (ODKParseException e) {
        // unfortunately, the underlying javarosa utility swallows the parsing error.
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
      resp.setHeader("Location", cc.getServerURL() + BasicConsts.FORWARDSLASH + ADDR);
      resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
      resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
      PrintWriter out = resp.getWriter();
      out.write(HtmlConsts.HTML_OPEN);
      out.write(HtmlConsts.BODY_OPEN);
      if ( warnings.length() != 0 ) {
    	  out.write("<p>Form uploaded with warnings. There are value fields in the form that do not " +
    	  		"have <code>&gt;bind/&lt;</code> declarations or those <code>&gt;bind/&lt;</code> " +
    	  		"declarations do not have a <code>type</code> attribute that " +
    	  		"identifies the data type of that field (e.g., boolean, int, decimal, date, dateTime, time, string, " +
    	  		"select1, select, barcode, geopoint or binary).</p>" +
    	  		"<p><b>All these value fields have been declared as string values.</b></p>" +
        	  	"<p>If these value fields hold date, dateTime, time or numeric data (e.g., decimal or int), then ODK Aggregate will " +
    	  		"produce erroneous sortings and filtering results against those value fields.  It will use " +
    	  		"lexical ordering on those fields.  I.e., the value 100 will be considered less than 11.</p>" +
    	  		"<table><th><td>Field Name</td></th>");
    	  out.write(warnings.toString());
    	  out.write("</table>");
      } else {
    	  out.write("<p>Successful form upload.</p>");
      }
      out.write("<p>Click ");
    	  
      out.write(HtmlUtil.createHref(cc.getWebApplicationURL(ADDR), "here"));
      out.write(" to return to add new form page.</p>");
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
