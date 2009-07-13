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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.odk.aggregate.PMFactory;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.exception.ODKParseException;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.parser.MultiPartFormData;
import org.odk.aggregate.parser.SubmissionParser;
import org.odk.aggregate.submission.Submission;

import java.io.IOException;
import java.io.PrintWriter;

import javax.jdo.PersistenceManager;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
    resp.setContentType(ServletConsts.RESP_TYPE_XML);

    // get parameter
    String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
    if(odkFormKey == null) {
      errorMissingKeyParam(resp);
      return;
    }
    
    // get form
    PersistenceManager pm = PMFactory.get().getPersistenceManager();
    Key formKey = KeyFactory.stringToKey(odkFormKey);
    Form form = pm.getObjectById(Form.class, formKey);

    if (form != null) {
      resp.getWriter().print(form.getOriginalForm());
    } else {
      odkIdNotFoundError(resp);
    }
    pm.close();
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
    resp.setContentType(ServletConsts.RESP_TYPE_PLAIN);
    
    PrintWriter out = resp.getWriter();
    Key submissionKey = null;
    PersistenceManager pm = PMFactory.get().getPersistenceManager();
    try {
      SubmissionParser submissionParser = null;
      if (ServletFileUpload.isMultipartContent(req)) {
        try {
          submissionParser = new SubmissionParser(new MultiPartFormData(req), pm);
        } catch (FileUploadException e) {
          e.printStackTrace();
        }

      } else {
        // TODO: check that it is the proper types we can deal with
        // XML received
        submissionParser = new SubmissionParser(req.getInputStream(), pm);
      }

      if (submissionParser == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.INPUTSTREAM_ERROR);
        return;
      }

      submissionKey = submissionParser.getSubmission().getKey();
    } catch (ODKFormNotFoundException e) {
      odkIdNotFoundError(resp);
      return;
    } catch (ODKParseException e) {
      resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.PARSING_PROBLEM);
      return;
    }

    pm.close();
    
    if (ServletConsts.DEBUG) {
      out.println("QUERYING FROM DATASTORE");

      pm = PMFactory.get().getPersistenceManager();
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      try {
        Entity subEntity = ds.get(submissionKey);
        Submission test = new Submission(subEntity, pm);
        test.printSubmission(out);
        
      } catch (EntityNotFoundException e) {
        e.printStackTrace();
      } catch (ODKFormNotFoundException e) {
        e.printStackTrace();
      } catch (ODKIncompleteSubmissionData e) {
        e.printStackTrace();
      }
      pm.close();
    } else {
      resp.sendRedirect(ServletConsts.WEB_ROOT);
    }

  }


}
