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
import org.opendatakit.common.web.CallingContext;

/**
 * Servlet to process a submission from a form
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class UISubmissionServlet extends ServletUtilBase {
  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = -9115712148453543651L;

  /**
   * URI from base
   */
  public static final String ADDR = "ui/submission";

  private static final String TITLE = "Submission Upload";

  /**
   * Script path to include...
   */
  private static final String JQUERY_SCRIPT_HEADER = 
	  "<script src=\"http://ajax.googleapis.com/ajax/libs/jquery/1.5.1/jquery.min.js\"></script>";
  
  private static final String UPLOAD_SCRIPT_RESOURCE = "res/upload_control.js";

  private static final String PAGE_STYLE_RESOURCE = "stylesheets/upload.css";
  
  private static final String NAV_STYLE_RESOURCE = "stylesheets/navigation.css";
  
  private static final String AGGREGATE_STYLE_RESOURCE = "AggregateUI.css";
  
  private static final String BUTTON_STYLE_RESOURCE = "stylesheets/button.css";

  private static final String UPLOAD_PAGE_BODY_START = 

"     <div class=\"gwt-HTML\"><img src=\"../images/odk_color.png\" id=\"odk_aggregate_logo\" style=\"margin-bottom: 4px;\" /></div>" +
"     <table cellspacing=\"0\" cellpadding=\"0\" id=\"layout_panel\"><tr><td>" +
"	  <table cellspacing=\"0\" cellpadding=\"0\" class=\"gwt-DecoratedTabPanel mainNav\"><tr><td>" + 
// Main Nav
"	  <table cellspacing=\"0\" cellpadding=\"0\" class=\"gwt-DecoratedTabBar\" style=\"width: 100%;\"><tr>" +
"	    <td align=\"left\" style=\"vertical-align: bottom;\" height=\"100%\" class=\"gwt-TabBarFirst-wrapper\"><div class=\"gwt-TabBarFirst\"></div></td>" +
"	    <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarItem-wrapper gwt-TabBarItem-wrapper\">" +
"	      <table cellspacing=\"0\" cellpadding=\"0\" class=\"gwt-TabBarItem gwt-TabBarItem\">" +
"	        <tr class=\"tabTop\"><td class=\"tabTopLeft\"><div class=\"tabTopLeftInner\"></div></td><td class=\"tabTopCenter\"><div class=\"tabTopCenterInner\"></div></td><td class=\"tabTopRight\"><div class=\"tabTopRightInner\"></div></td></tr>" +
"	        <tr class=\"tabMidle\"><td class=\"tabMiddleLeft\"><div class=\"tabMiddleLeftInner\"></div></td>" +
"	          <td class=\"tabMiddleCenter\"><div class=\"tabMiddleCenterInner\" onclick=\"window.location='../Aggregate.html#submissions'\">Submissions</div></td>" +
"	          <td class=\"tabMiddleRight\"><div class=\"tabMiddleRightInner\"></div></td></tr>" +
"	      </table>" +
"	    </td>" +
"	    <td class=\"gwt-TabBarItem-wrapper gwt-TabBarItem-wrapper-selected\">" +
"	      <table cellspacing=\"0\" cellpadding=\"0\" class=\"gwt-TabBarItem gwt-TabBarItem-selected\">" +
"	        <tr class=\"tabTop\"><td class=\"tabTopLeft\"><div class=\"tabTopLeftInner\"></div></td><td class=\"tabTopCenter\"><div class=\"tabTopCenterInner\"></div></td><td class=\"tabTopRight\"><div class=\"tabTopRightInner\"></div></td></tr>" +
"	        <tr class=\"tabMidle\"><td class=\"tabMiddleLeft\"><div class=\"tabMiddleLeftInner\"></div></td>" +
"	          <td class=\"tabMiddleCenter\"><div class=\"tabMiddleCenterInner\">Management</div></td>" +
"	          <td class=\"tabMiddleRight\"><div class=\"tabMiddleRightInner\"></div></td></tr>" +
"	      </table>" +
"	    </td>" +
"	    <td align=\"left\" style=\"vertical-align: bottom;\" width=\"100%\" class=\"gwt-TabBarRest-wrapper\"><div></div></td>" +
"	  </tr>" +
"	  </table></td></tr>" +
// Second Level Nav
"	  <tr><td>" +
"	    <table cellspacing=\"0\" cellpadding=\"0\" class=\"gwt-TabBar\" style=\"width: 100%;\">" +
"	      <tr>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarFirst-wrapper\"><div class=\"gwt-TabBarFirst\"></div></td>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarItem-wrapper\"><div class=\"gwt-TabBarItem\" onclick=\"window.location='../Aggregate.html#management'\">Forms</div></td>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarItem-wrapper\"><div class=\"gwt-TabBarItem\" onclick=\"window.location='../Aggregate.html#managements/export'\">Export</div></td>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarItem-wrapper\"><div class=\"gwt-TabBarItem\" onclick=\"window.location='../Aggregate.html#management/publish'\">Publish</div></td>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarItem-wrapper\"><div class=\"gwt-TabBarItem\" onclick=\"window.location='../Aggregate.html#management/permissions'\">Permissions</div></td>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarItem-wrapper\"><div class=\"gwt-TabBarItem\" onclick=\"window.location='../Aggregate.html#management/utilities'\">Utilities</div></td>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" class=\"gwt-TabBarItem-wrapper\"><div class=\"gwt-TabBarItem\" onclick=\"window.location='../Aggregate.html#management/preferences'\">Preferences</div></td>" +
"	        <td align=\"left\" style=\"vertical-align: bottom;\" width=\"100%\" class=\"gwt-TabBarItem gwt-TabBarItemRest-wrapper\"><div class=\"gwt-TabBarRest gwt-TabBarItem\"></div></td>" +
"	      </tr>" +
"	    </table></td></tr>" +
// Page Content
"     <tr><td>" +
"     <h1>" + TITLE + "</h1>" +
"	  <h2>Upload one submission into ODK Aggregate</h2>" +
"	  <p>Submissions are located under the <code>/odk/instances</code> directory on the phone's " +
"     sdcard.  This directory will contain subdirectories with names of the form: <code>formID_yyyy-mm-dd_hh-MM-ss</code><br />" +
"     Within each of these subdirectories are the submission data file (named: <code>formID_yyyy-mm-dd_hh-MM-ss.xml</code>)," +
"     and zero or more associated data files for the images, audio clips, video clips, " +
"     etc. linked with this submission.</p>" +
"	  <!--[if true]><p style=\"color: red;\">For a better user experience, use Chrome, Firefox or Safari</p>" +
"	  <!-- If you specify an empty progress div, it will be expanded with an upload progress region (non-IE) -->" +
"	  <div id=\"progress\"></div><br />" +
"     <form id=\"ie_backward_compatible_form\"" + 
"	                      accept-charset=\"UTF-8\" method=\"POST\" enctype=\"multipart/form-data\"" + 
"	                      action=\"";// emit the ADDR
  private static final String UPLOAD_PAGE_BODY_MIDDLE = "\">" +
"	  <![endif] -->" +
"	  <table>" +
"	  	<tr>" +
"	  		<td><label for=\"xml_submission_file\">Submission data file:</label></td>" +
"	  		<td><input id=\"xml_submission_file\" type=\"file\" size=\"80\"" +
"	  			name=\"form_def_file\" class=\"gwt-Button\" /></td>" +
"	  	</tr>" +
"	  	<tr>" +
"	  		<td><label for=\"mediaFiles\">Associated data file(s):</label></td>" +
"	  		<td><input id=\"mediaFiles\" type=\"file\" class=\"gwt-Button\" size=\"80,20\" name=\"datafile\" multiple /><button id=\"clear_media_files\" type=\"button\" class=\"gwt-Button gwt-Button-negative clearButton\" onClick=\"clearMediaInputField('mediaFiles')\"><img src=\"/images/red_x.png\"> Clear</button></td>" +
"	  	</tr>" +
"	  	<!--[if true]>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles2\">Associated data file #2:</label></td>" +
"	          <td><input id=\"mediaFiles2\" type=\"file\" class=\"gwt-Button\" size=\"80\" name=\"datafile\" /><button id=\"clear_media_files2\" type=\"button\" class=\"gwt-Button gwt-Button-negative clearButton\" onClick=\"clearMediaInputField('mediaFiles2')\"><img src=\"/images/red_x.png\"> Clear</button></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles3\">Associated data file #3:</label></td>" +
"	          <td><input id=\"mediaFiles3\" type=\"file\" class=\"gwt-Button\" size=\"80\" name=\"datafile\" /><button id=\"clear_media_files3\" type=\"button\" class=\"gwt-Button gwt-Button-negative clearButton\" onClick=\"clearMediaInputField('mediaFiles3')\"><img src=\"/images/red_x.png\"> Clear</button></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles4\">Associated data file #4:</label></td>" +
"	          <td><input id=\"mediaFiles4\" type=\"file\" class=\"gwt-Button\" size=\"80\" name=\"datafile\" /><button id=\"clear_media_files4\" type=\"button\" class=\"gwt-Button gwt-Button-negative clearButton\" onClick=\"clearMediaInputField('mediaFiles4')\"><img src=\"/images/red_x.png\"> Clear</button></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles5\">Associated data file #5:</label></td>" +
"	          <td><input id=\"mediaFiles5\" type=\"file\" class=\"gwt-Button\" size=\"80\" name=\"datafile\" /><button id=\"clear_media_files5\" type=\"button\" class=\"gwt-Button gwt-Button-negative clearButton\" onClick=\"clearMediaInputField('mediaFiles5')\"><img src=\"/images/red_x.png\"> Clear</button></td>" +
"	      </tr>" +
"	      <tr>" +
"	          <td><label for=\"mediaFiles6\">Associated data file #6:</label></td>" +
"	          <td><input id=\"mediaFiles6\" type=\"file\" class=\"gwt-Button\" size=\"80\" name=\"datafile\" /><button id=\"clear_media_files6\" type=\"button\" class=\"gwt-Button gwt-Button-negative clearButton\" onClick=\"clearMediaInputField('mediaFiles6')\"><img src=\"/images/red_x.png\"> Clear</button></td>" +
"	      </tr>" +
"	      <![endif]-->" +
"	  	<tr>" +
"	  		<td><button name=\"button\" class=\"gwt-Button\" id=\"uploadButton\"" +
"	  			onClick=\"submitButton(document,'";
  private static final String UPLOAD_PAGE_BODY_REMAINDER = "','xml_submission_file','mediaFiles')\"><img src=\"/images/blue_up_arrow.png\"> Upload Form</button></td>" +
"	  		<td />" +
"	  	</tr>" +
"	  </table>" +
"	  <!--[if true]></form>" +
"	  <![endif] --></td></tr></table></td></tr></table>";

  /**
   * Handler for HTTP Get request that processes a form submission
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);

	StringBuilder headerString = new StringBuilder();
	headerString.append(JQUERY_SCRIPT_HEADER);
	headerString.append("<script src=\"");
	headerString.append(cc.getWebApplicationURL(UPLOAD_SCRIPT_RESOURCE));
	headerString.append("\"></script>");
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(PAGE_STYLE_RESOURCE));
	headerString.append("\" />");
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(NAV_STYLE_RESOURCE));
	headerString.append("\" />");
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(AGGREGATE_STYLE_RESOURCE));
	headerString.append("\" />");
	headerString.append("<link type=\"text/css\" rel=\"stylesheet\" href=\"");
	headerString.append(cc.getWebApplicationURL(BUTTON_STYLE_RESOURCE));
	headerString.append("\" />");
	
	resp.addHeader(HOST_HEADER, cc.getServerURL());
   resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
   resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);

   PrintWriter out = resp.getWriter();
   out.write(HtmlConsts.HTML_OPEN);
   out.write(HtmlUtil.wrapWithHtmlTags(HtmlConsts.HEAD, headerString.toString() + HtmlUtil.wrapWithHtmlTags(
       HtmlConsts.TITLE, "AGGREGATE")));
   out.write(HtmlConsts.BODY_OPEN);
	
	out.write(UPLOAD_PAGE_BODY_START);
	out.write(cc.getWebApplicationURL(ADDR));
	out.write(UPLOAD_PAGE_BODY_MIDDLE);
	out.write(cc.getWebApplicationURL(ADDR));
	out.write(UPLOAD_PAGE_BODY_REMAINDER);
   resp.getWriter().write(HtmlConsts.BODY_CLOSE + HtmlConsts.HTML_CLOSE);
	
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
	CallingContext cc = ContextFactory.getCallingContext(this, req);

    resp.setContentType(HtmlConsts.RESP_TYPE_HTML);

    try {
      SubmissionParser submissionParser = null;
      if (ServletFileUpload.isMultipartContent(req)) {
    	MultiPartFormData uploadedSubmissionItems = new MultiPartFormData(req);
        String isIncompleteFlag = uploadedSubmissionItems.getSimpleFormField(ServletConsts.TRANSFER_IS_INCOMPLETE);
        submissionParser = new SubmissionParser(uploadedSubmissionItems, cc);
      } else {
        // TODO: check that it is the proper types we can deal with
        // XML received, we hope...
        submissionParser = new SubmissionParser(req.getInputStream(), cc);
      }

      // TODO: mitch are we assuming the submissionParser always persists?
      Form form = Form.retrieveForm(submissionParser.getSubmissionFormId(), cc);
      Submission submission = submissionParser.getSubmission();

      // send information to remote servers that need to be notified
      List<ExternalService> tmp = FormServiceCursor.getExternalServicesForForm(form, cc);
      UploadSubmissions uploadTask = (UploadSubmissions) cc.getBean(BeanDefs.UPLOAD_TASK_BEAN);

  	  CallingContext ccDaemon = ContextFactory.getCallingContext(this, req);
	  ccDaemon.setAsDaemon(true);
      for (ExternalService rs : tmp) {
        uploadTask.createFormUploadTask(rs.getFormServiceCursor(), ccDaemon);
      }

      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.setHeader("Location", cc.getServerURL());

      resp.setContentType(HtmlConsts.RESP_TYPE_HTML);
      resp.setCharacterEncoding(HtmlConsts.UTF8_ENCODE);
      PrintWriter out = resp.getWriter();
      out.write(HtmlConsts.HTML_OPEN);
      out.write(HtmlConsts.BODY_OPEN);
      out.write("Successful submission upload.  Click ");
      out.write(HtmlUtil.createHref(cc.getWebApplicationURL(FormsServlet.ADDR), "here"));
      out.write(" to return to forms page.");
      out.write(HtmlConsts.BODY_CLOSE);
      out.write(HtmlConsts.HTML_CLOSE);
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
