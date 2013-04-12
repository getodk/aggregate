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
import java.io.OutputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.FormFactory;
import org.opendatakit.aggregate.form.FormInfo;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.common.datamodel.BinaryContentManipulator;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet to download the manifest-declared files of a form.
 * 
 * Largely copied from BinaryDataServlet. Restricts the form to be the FormInfo
 * form.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class XFormsDownloadServlet extends ServletUtilBase {

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 7917801268158165832L;

  /**
   * URI from base
   */
  public static final String ADDR = "xformsDownload";

  /**
   * Handler for HTTP Get request that responds with an Image
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    CallingContext cc = ContextFactory.getCallingContext(this, req);
    addOpenRosaHeaders(resp);

    // verify parameters are present
    String keyString = getParameter(req, ServletConsts.BLOB_KEY);
    String downloadAsAttachmentString = getParameter(req, ServletConsts.AS_ATTACHMENT);

    if (keyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }
    SubmissionKey key = new SubmissionKey(keyString);

    List<SubmissionKeyPart> parts = key.splitSubmissionKey();

    boolean isXformDefinitionRequest = false;

    // verify that the submission key is well-formed
    if (FormInfo.validFormManifestKey(parts)) {
      isXformDefinitionRequest = false;
    } else if (FormInfo.validFormXformDefinitionKey(parts)) {
      isXformDefinitionRequest = true;
    } else {
      errorBadParam(resp);
      return;
    }

    // get this form's definition
    IForm form;
    try {
      form = FormFactory.retrieveForm(parts, cc);
    } catch (ODKFormNotFoundException e) {
      e.printStackTrace();
      odkIdNotFoundError(resp);
      return;
    } catch (ODKOverQuotaException e) {
      e.printStackTrace();
      quotaExceededError(resp);
      return;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      datastoreError(resp);
      return;
    }

    // the binary content selection is the 4th part
    // (formInfo/row/fileset/[xform|manifest])
    SubmissionKeyPart part = parts.get(3);

    byte[] imageBlob;
    String unrootedFileName;
    String contentType;
    Long contentLength;

    if (isXformDefinitionRequest) {
      BinaryContentManipulator xform = form.getXformDefinition();
      try {
        if (xform.getAttachmentCount(cc) != 1) {
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              "Unexpectedly non-unary attachment count");
          return;
        }
        unrootedFileName = xform.getUnrootedFilename(1, cc);
        contentType = xform.getContentType(1, cc);
        contentLength = xform.getContentLength(1, cc);
        imageBlob = xform.getBlob(1, cc);
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Unable to retrieve attachment");
        return;
      }
    } else {
      BinaryContentManipulator manifest = form.getManifestFileset();
      if (part.getOrdinalNumber() == null) {
        resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
            "attachment request must be fully qualified");
        return;
      }
      int idx = part.getOrdinalNumber().intValue();
      try {
        if (manifest.getAttachmentCount(cc) < idx) {
          errorBadParam(resp);
          return;
        }
        unrootedFileName = manifest.getUnrootedFilename(idx, cc);
        contentType = manifest.getContentType(idx, cc);
        contentLength = manifest.getContentLength(idx, cc);
        imageBlob = manifest.getBlob(idx, cc);
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Unable to retrieve attachment");
        return;
      }
    }

    if (imageBlob != null) {
      if (contentType == null) {
        resp.setContentType(HtmlConsts.RESP_TYPE_IMAGE_JPEG);
      } else {
        resp.setContentType(contentType);
      }
      if (contentLength != null) {
        resp.setContentLength(contentLength.intValue());
      }

      if (downloadAsAttachmentString != null && !"".equals(downloadAsAttachmentString)) {
        // set filename if we are downloading to disk...
        // need this for manifest fetch logic...
        if (unrootedFileName != null) {
          resp.addHeader(HtmlConsts.CONTENT_DISPOSITION, "attachment; filename=\"" + unrootedFileName
              + "\"");
        }
      }

      OutputStream os = resp.getOutputStream();
      os.write(imageBlob);
      os.close();
    } else {
      resp.setContentType(HtmlConsts.RESP_TYPE_PLAIN);
      resp.getWriter().print(ErrorConsts.NO_IMAGE_EXISTS);
    }
  }

}
