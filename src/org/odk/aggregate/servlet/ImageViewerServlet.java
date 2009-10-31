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
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.submission.SubmissionBlob;

import com.google.appengine.api.datastore.Blob;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;

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

      
    // TOOD: figure out how to do picture security
    // verify user is logged in
//    if (!verifyCredentials(req, resp)) {
//      return;
//    }

    // verify parameters are present
    String keyString =  getParameter(req, ServletConsts.BLOB_KEY);
     if (keyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    try {
        SubmissionBlob blobStore = new SubmissionBlob(KeyFactory.stringToKey(keyString));
        Blob imageBlob = blobStore.getBlob();
        if(imageBlob != null) {
          resp.setContentType(blobStore.getContentType());
          OutputStream os = resp.getOutputStream();
          os.write(imageBlob.getBytes());
          os.close();
          return;
        }

    } catch (EntityNotFoundException e) {
      // TODO: consider better error handling, right now defaulting to simple error message
    }
    resp.setContentType(ServletConsts.RESP_TYPE_PLAIN);
    resp.getWriter().print(ErrorConsts.NO_IMAGE_EXISTS);

  }

}
