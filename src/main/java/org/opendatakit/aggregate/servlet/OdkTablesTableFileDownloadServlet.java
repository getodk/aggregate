/*
 * Copyright (C) 2012-2013 University of Washington
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
import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.odktables.relation.DbTableFiles;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * The servlet that handles the downloading of table files. Based mostly on
 * XFormsDownloadServlet.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class OdkTablesTableFileDownloadServlet extends ServletUtilBase {

  /**
	 *
	 */
  private static final long serialVersionUID = -4681728139240108291L;

  private static final Log logger =
      LogFactory.getLog(OdkTablesTableFileDownloadServlet.class);

  /**
   * URI from base.
   */
  public static final String ADDR = UIConsts.TABLE_FILE_DOWNLOAD_SERVLET_ADDR;

  /**
   * Handler for the HTTP Get request.
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp)
      throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // verify the parameters you expect are there
    String keyString = getParameter(req, ServletConsts.BLOB_KEY);
    String downloadAsAttachmentString =
        getParameter(req, ServletConsts.AS_ATTACHMENT);

    if (keyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    // here I am diverging from XFormsDownloadServlet. They have a Submission
    // Key.
    // I am planning on the key passed in being the key to the file in the
    // datastore.
    // So, let's hope that works.

    byte[] imageBlob;
    String unrootedFileName;
    String contentType;
    // THIS COULD BE A PROBLEM--MAYBE SHOULD BE SETTING TYPE WITH
    // AN HTML TYPE AS FROM HtmlConsts.RESP_TYPE...
    Long contentLength;

    try {

      DbTableFiles files = new DbTableFiles(cc);
      BlobEntitySet blobSet = files.getBlobEntitySet(keyString, cc);
      // should only ever be one, b/c each of the files is in an
      // entity set of 1.
      if (blobSet.getAttachmentCount(cc) != 1) {
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Unexpectedly non-unary attachment count");
      }
      imageBlob = blobSet.getBlob(1, cc);
      unrootedFileName = files.getBlobEntitySet(keyString, cc)
          .getUnrootedFilename(1, cc);
      contentType = files.getBlobEntitySet(keyString, cc)
          .getContentType(1, cc);
      contentLength = files.getBlobEntitySet(keyString, cc)
          .getContentLength(1, cc);
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
          "Unable to retrieve attachment and access attributes");
      return;
    }

    if (imageBlob != null) {
      // not sure why they set to jpeg
      if (contentType == null) {
        resp.setContentType(HtmlConsts.RESP_TYPE_IMAGE_JPEG);
      } else {
        resp.setContentType(contentType);
      }
      if (contentLength != null) {
        resp.setContentLength(contentLength.intValue());
      }

      if (downloadAsAttachmentString != null
          && !"".equals(downloadAsAttachmentString)) {
        // set the file name if we're downloading to the disk
        if (unrootedFileName != null) {
          resp.addHeader(HtmlConsts.CONTENT_DISPOSITION,
              "attachment; filename=\""
              + unrootedFileName + "\"");
        }
      }
      OutputStream os = resp.getOutputStream();
      os.write(imageBlob);
    } else {
      resp.setContentType(HtmlConsts.RESP_TYPE_PLAIN);
      resp.getWriter().print(ErrorConsts.NO_IMAGE_EXISTS);
    }

  }

}
