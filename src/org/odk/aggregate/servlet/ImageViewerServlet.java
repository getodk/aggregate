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

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionField;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Servlet to display the image from a submission
 * 
 * @author wbrunette@gmail.com
 *
 */
public class ImageViewerServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 7124099259931611219L;
  
  /**
   * URI from base
   */
  public static final String ADDR = "imageViewer";

  /**
   * Handler for HTTP Get request that responds with an Image
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

    // verify parameters are present
    String keyString =  getParameter(req, ServletConsts.SUBMISSION_KEY);
    String propertyName =  getParameter(req, ServletConsts.PROPERTY_NAME);
    if (keyString == null || propertyName == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    // retrieve submission based on key passed as a parameter
    Entity sub = null;
    try {
      DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
      sub = ds.get(KeyFactory.stringToKey(keyString));
    } catch (EntityNotFoundException e) {
      resp.sendError(HttpServletResponse.SC_NOT_FOUND, ErrorConsts.SUBMISSION_NOT_FOUND);
      return;
    }

    // extra data from submission
    if (sub != null) {
      Submission submission;
      try {
        submission = new Submission(sub);
      } catch (ODKFormNotFoundException e) {
        odkIdNotFoundError(resp);
        return;
      } catch (ODKIncompleteSubmissionData e) {
        errorRetreivingData(resp);
        return;
      }


      resp.setContentType(ServletConsts.RESP_TYPE_IMAGE_JPEG);
      OutputStream os = resp.getOutputStream();
      Map<String, SubmissionField<?>> fieldMap = submission.getSubmissionFieldsMap();
      SubmissionField<?> image = fieldMap.get(propertyName);
      Blob imageBlog = (Blob) image.getValue();
      os.write(imageBlog.getBytes());
      os.close();
    }

  }

}
