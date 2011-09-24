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
import java.io.PrintWriter;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.odk.aggregate.EMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.exception.ODKParseException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.remoteserver.RemoteServer;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.parser.SubmissionParser;
import org.odk.aggregate.submission.Submission;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;

/**
 * Servlet to process a submission from a form
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class SubmissionServlet extends ServletUtilBase {
  private static final String DATAFILE = "datafile";

  private static final String TITLE = "Submission Upload";

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
    PrintWriter out = resp.getWriter();
    
    beginBasicHtmlResponse(TITLE, resp, req, true); // header info
    out.write(HtmlUtil.createFormBeginTag(ADDR, ServletConsts.MULTIPART_FORM_DATA, ServletConsts.POST));
    out.write("XML Submission File:" + HtmlConsts.LINE_BREAK );
    out.write(HtmlUtil.createInput(HtmlConsts.INPUT_TYPE_FILE, ServletConsts.XML_SUBMISSION_FILE, null));
    out.write(HtmlConsts.LINE_BREAK + HtmlConsts.LINE_BREAK);
    out.write("Data File(s) that are Part of the Submission (Pictures, Video, etc):" + HtmlConsts.LINE_BREAK); 
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
    resp.setContentType(ServletConsts.RESP_TYPE_HTML);

    PrintWriter out = resp.getWriter();
    Key submissionKey = null;
    String odkId = null;
    EntityManager em = EMFactory.get().createEntityManager();
    try {
      SubmissionParser submissionParser = null;
      if (ServletFileUpload.isMultipartContent(req)) {
        try {
          submissionParser = new SubmissionParser(new MultiPartFormData(req), em);
          odkId = submissionParser.getOdkId();
        } catch (Exception e) {
          throw new IOException(e);
        } 
      } else {
        // TODO: check that it is the proper types we can deal with
        // XML received
        submissionParser = new SubmissionParser(req.getInputStream(), em);
      }

      Form form = submissionParser.getForm();
      String appName = this.getServletContext().getInitParameter("application_name");
      Submission submission = submissionParser.getSubmission();

      List<RemoteServer> tmp = form.getExternalRepos();
      // send information to remote servers that need to be notified
      for (RemoteServer rs : tmp) {
        rs.sendSubmissionToRemoteServer(form, req.getServerName(), em, appName, submission);
      }
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    } catch (ODKParseException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
      return;
    }

    em.close();

    if (ServletConsts.DEBUG) {
      out.println("QUERYING FROM DATASTORE");

      em = EMFactory.get().createEntityManager();
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      try {
        if (odkId == null) {

          // TODO: make better error decision
          return;
        }
        Entity subEntity = ds.get(submissionKey);
        Form form = Form.retrieveForm(em, odkId);
        Submission test = new Submission(subEntity, form);
        test.printSubmission(out);

      } catch (EntityNotFoundException e) {
        e.printStackTrace();
      } catch (ODKFormNotFoundException e) {
        e.printStackTrace();
      } catch (ODKIncompleteSubmissionData e) {
        e.printStackTrace();
      }
      em.close();
    } else {
      resp.setStatus(HttpServletResponse.SC_CREATED);
      resp.setHeader("Location", getServerURL(req));

      // TODO: get an auto redirect going
      // resp.getWriter().print("<html><head><meta HTTP-EQUIV=\"REFRESH\" content=\"0; url=http://"
      // + getServerURL(req) + "\"></head><body></body></html>");
    }

  }
}
