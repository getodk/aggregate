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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.constants.BeanDefs;
import org.opendatakit.aggregate.constants.ErrorConsts;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.exception.ODKFormNotFoundException;
import org.opendatakit.aggregate.form.PersistentResults;
import org.opendatakit.aggregate.form.PersistentResults.ResultFileInfo;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionElement;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionKeyPart;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.util.ImageUtil;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKOverQuotaException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * Servlet to display the binary data from a submission
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class BinaryDataServlet extends ServletUtilBase {

  private static final String NOT_BINARY_OBJECT = "Requested element is not a binary object";

  private static final Log logger = LogFactory.getLog(BinaryDataServlet.class);

  /**
   * Serial number for serialization
   */
  private static final long serialVersionUID = 7917801268158165832L;

  /**
   * URI from base
   */
  public static final String ADDR = "view/binaryData";

  /**
   * Handler for HTTP Get request that responds with an Image
   * 
   * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
   *      javax.servlet.http.HttpServletResponse)
   */
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    CallingContext cc = ContextFactory.getCallingContext(this, req);

    // verify parameters are present
    String keyString = getParameter(req, ServletConsts.BLOB_KEY);
    String downloadAsAttachmentString = getParameter(req, ServletConsts.AS_ATTACHMENT);
    String previewSizeString = getParameter(req, UIConsts.PREVIEW_PARAM);

    boolean previewSize = false;
    if (previewSizeString != null) {
      previewSize = Boolean.valueOf(previewSizeString);
    }

    if (keyString == null) {
      sendErrorNotEnoughParams(resp);
      return;
    }

    SubmissionKey key = new SubmissionKey(keyString);

    Date lastUpdateDate = null;
    byte[] imageBlob = null;
    String unrootedFileName = null;
    String contentType = null;
    Long contentLength = null;

    List<SubmissionKeyPart> parts = key.splitSubmissionKey();
    if (parts.get(0).getElementName().equals(PersistentResults.FORM_ID_PERSISTENT_RESULT)) {
      // special handling for persistent results data...
      try {
        PersistentResults p = new PersistentResults(key, cc);
        ResultFileInfo info = p.getResultFileInfo(cc);
        if (info == null) {
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              "Unable to retrieve attachment");
          return;
        }
        unrootedFileName = info.unrootedFilename;
        contentType = info.contentType;
        contentLength = info.contentLength;
        imageBlob = p.getResultFileContents(cc);
        lastUpdateDate = p.getCompletionDate();
      } catch (ODKOverQuotaException e) {
        e.printStackTrace();
        quotaExceededError(resp);
        return;
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Unable to retrieve attachment");
        return;
      }

    } else {
      Submission sub = null;
      try {
        sub = Submission.fetchSubmission(parts, cc);
      } catch (ODKFormNotFoundException e1) {
        odkIdNotFoundError(resp);
        return;
      } catch (ODKOverQuotaException e) {
        e.printStackTrace();
        quotaExceededError(resp);
        return;
      } catch (ODKDatastoreException e) {
        e.printStackTrace();
        resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
            "Unable to retrieve attachment");
        return;
      }

      if (sub != null) {
        BlobSubmissionType b = null;

        try {
          SubmissionElement v = null;
          v = sub.resolveSubmissionKey(parts);
          if (v instanceof BlobSubmissionType) {
            b = (BlobSubmissionType) v;
          } else {
            String path = getKeyPath(parts);
            logger.error(NOT_BINARY_OBJECT + ": " + path);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, NOT_BINARY_OBJECT);
            return;
          }
        } catch (Exception e) {
          e.printStackTrace();
          String path = getKeyPath(parts);
          logger.error("Unable to retrieve part identified by path: " + path);
          errorBadParam(resp);
          return;
        }

        try {
          // ordinal should be 1 if there is just 1 attachment...
          int ordinal = b.getAttachmentCount(cc);
          if ( ordinal != 1 ) {
            // we have multiple attachments
            // -- use submissionKey to determine which one we want
            SubmissionKeyPart p = parts.get(parts.size() - 1);
            Long ord = p.getOrdinalNumber();
            if (ord == null) {
              resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                  "attachment request must be fully qualified");
              return;
            }
            // OK. This is the attachment we want...
            ordinal = ord.intValue();
          }
          imageBlob = b.getBlob(ordinal, cc);
          lastUpdateDate = b.getLastUpdateDate(ordinal, cc);
          unrootedFileName = b.getUnrootedFilename(ordinal, cc);
          contentType = b.getContentType(ordinal, cc);
          contentLength = b.getContentLength(ordinal, cc);
        } catch (ODKOverQuotaException e) {
          e.printStackTrace();
          quotaExceededError(resp);
          return;
        } catch (ODKDatastoreException e) {
          e.printStackTrace();
          resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
              "Unable to retrieve attachment");
          return;
        }
      }
    }

    if (imageBlob != null) {
      if (contentType == null) {
        contentType = HtmlConsts.RESP_TYPE_IMAGE_JPEG;
      }

      if (previewSize) {
        // cache for 1 hour...
        resp.setHeader("Expires:", 
              WebUtils.rfc1123Date(new Date(System.currentTimeMillis() + 3600000L)));
        resp.setHeader("Last-Modified:",
              WebUtils.rfc1123Date(lastUpdateDate));
        resp.setContentType(HtmlConsts.RESP_TYPE_IMAGE_JPEG);
        if (contentType.equals(HtmlConsts.RESP_TYPE_IMAGE_JPEG)) {
          // resize
          ImageUtil imageUtil = (ImageUtil) cc.getBean(BeanDefs.IMAGE_UTIL);
          imageBlob = imageUtil.resizeImage(imageBlob, 64, 48);
        } else {
          // display not-able-to-resize image...
          imageBlob = playJPG;
        }
        resp.setContentLength(imageBlob.length);
      } else {
        resp.setHeader("Last-Modified:",
            WebUtils.rfc1123Date(lastUpdateDate));
        resp.setContentType(contentType);
        if (contentLength != null) {
          resp.setContentLength(contentLength.intValue());
        }
      }

      if (downloadAsAttachmentString != null && !"".equals(downloadAsAttachmentString)) {
        // set filename if we are downloading to disk...
        // need this for manifest fetch logic...
        if (unrootedFileName != null) {
          resp.addHeader(HtmlConsts.CONTENT_DISPOSITION, "attachment; filename=\""
              + unrootedFileName + "\"");
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

  private final String getKeyPath(List<SubmissionKeyPart> parts) {
    StringBuilder b = new StringBuilder();
    for (SubmissionKeyPart p : parts) {
      b.append("/");
      b.append(p.toString());
    }
    return b.toString();
  }

  private static byte[] playJPG = { -1, -40, -1, -32, 0, 16, 74, 70, 73, 70, 0, 1, 1, 1, 0, 96, 0,
      96, 0, 0, -1, -31, 0, 78, 69, 120, 105, 102, 0, 0, 73, 73, 42, 0, 8, 0, 0, 0, 4, 0, 1, 3, 5,
      0, 1, 0, 0, 0, 62, 0, 0, 0, 16, 81, 1, 0, 1, 0, 0, 0, 1, 0, -128, 63, 17, 81, 4, 0, 1, 0, 0,
      0, -62, 14, 0, 0, 18, 81, 4, 0, 1, 0, 0, 0, -62, 14, 0, 0, 0, 0, 0, 0, -96, -122, 1, 0, -113,
      -79, 0, 0, -1, -37, 0, 67, 0, 8, 6, 6, 7, 6, 5, 8, 7, 7, 7, 9, 9, 8, 10, 12, 20, 13, 12, 11,
      11, 12, 25, 18, 19, 15, 20, 29, 26, 31, 30, 29, 26, 28, 28, 32, 36, 46, 39, 32, 34, 44, 35,
      28, 28, 40, 55, 41, 44, 48, 49, 52, 52, 52, 31, 39, 57, 61, 56, 50, 60, 46, 51, 52, 50, -1,
      -37, 0, 67, 1, 9, 9, 9, 12, 11, 12, 24, 13, 13, 24, 50, 33, 28, 33, 50, 50, 50, 50, 50, 50,
      50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50,
      50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, 50, -1, -64,
      0, 17, 8, 0, 48, 0, 59, 3, 1, 34, 0, 2, 17, 1, 3, 17, 1, -1, -60, 0, 31, 0, 0, 1, 5, 1, 1, 1,
      1, 1, 1, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1, -60, 0, -75, 16, 0,
      2, 1, 3, 3, 2, 4, 3, 5, 5, 4, 4, 0, 0, 1, 125, 1, 2, 3, 0, 4, 17, 5, 18, 33, 49, 65, 6, 19,
      81, 97, 7, 34, 113, 20, 50, -127, -111, -95, 8, 35, 66, -79, -63, 21, 82, -47, -16, 36, 51,
      98, 114, -126, 9, 10, 22, 23, 24, 25, 26, 37, 38, 39, 40, 41, 42, 52, 53, 54, 55, 56, 57, 58,
      67, 68, 69, 70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 88, 89, 90, 99, 100, 101, 102, 103, 104,
      105, 106, 115, 116, 117, 118, 119, 120, 121, 122, -125, -124, -123, -122, -121, -120, -119,
      -118, -110, -109, -108, -107, -106, -105, -104, -103, -102, -94, -93, -92, -91, -90, -89,
      -88, -87, -86, -78, -77, -76, -75, -74, -73, -72, -71, -70, -62, -61, -60, -59, -58, -57,
      -56, -55, -54, -46, -45, -44, -43, -42, -41, -40, -39, -38, -31, -30, -29, -28, -27, -26,
      -25, -24, -23, -22, -15, -14, -13, -12, -11, -10, -9, -8, -7, -6, -1, -60, 0, 31, 1, 0, 3, 1,
      1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, -1, -60, 0, -75,
      17, 0, 2, 1, 2, 4, 4, 3, 4, 7, 5, 4, 4, 0, 1, 2, 119, 0, 1, 2, 3, 17, 4, 5, 33, 49, 6, 18,
      65, 81, 7, 97, 113, 19, 34, 50, -127, 8, 20, 66, -111, -95, -79, -63, 9, 35, 51, 82, -16, 21,
      98, 114, -47, 10, 22, 36, 52, -31, 37, -15, 23, 24, 25, 26, 38, 39, 40, 41, 42, 53, 54, 55,
      56, 57, 58, 67, 68, 69, 70, 71, 72, 73, 74, 83, 84, 85, 86, 87, 88, 89, 90, 99, 100, 101,
      102, 103, 104, 105, 106, 115, 116, 117, 118, 119, 120, 121, 122, -126, -125, -124, -123,
      -122, -121, -120, -119, -118, -110, -109, -108, -107, -106, -105, -104, -103, -102, -94, -93,
      -92, -91, -90, -89, -88, -87, -86, -78, -77, -76, -75, -74, -73, -72, -71, -70, -62, -61,
      -60, -59, -58, -57, -56, -55, -54, -46, -45, -44, -43, -42, -41, -40, -39, -38, -30, -29,
      -28, -27, -26, -25, -24, -23, -22, -14, -13, -12, -11, -10, -9, -8, -7, -6, -1, -38, 0, 12,
      3, 1, 0, 2, 17, 3, 17, 0, 63, 0, -13, -65, 50, -113, 51, -125, 85, -53, -44, -74, -47, 75,
      119, 115, 21, -68, 8, 94, 89, 88, 34, 40, -18, 77, 125, 51, 105, 30, 23, 43, 36, 82, -50,
      -31, 17, 75, 49, -32, 5, 25, 38, -70, 11, 127, 4, -8, -94, -22, 33, 36, 58, 37, -39, 66, 50,
      11, 38, -36, -2, 120, -81, 70, -48, 116, 93, 59, -63, -10, 104, 66, 36, -38, -93, 46, 102,
      -71, 97, -99, -89, -5, -87, -24, 7, -81, 83, 83, -49, -30, 25, 75, -27, -99, -71, -25, 39,
      -67, 121, -107, 51, 6, -99, -96, -114, -22, 120, 45, 61, -10, 120, -2, -93, -92, 106, -102,
      67, 5, -44, 108, 46, 109, -77, -48, -53, 25, 0, -3, 13, 103, -105, 56, -81, 119, -117, 91,
      -114, -14, 22, -74, -69, 68, -98, 7, 24, 104, -91, 27, -108, -41, -103, -8, -21, -62, -80,
      -24, -109, -90, -95, -90, -18, 58, 109, -61, 96, 35, 28, -104, 95, -5, -71, -18, 61, 63, 42,
      -37, 15, -115, 85, 31, 44, -107, -103, -99, 108, 43, -126, -26, -117, -70, 57, 50, -39, -17,
      77, -33, 81, -18, -30, -109, 113, -82, -37, -100, -36, -93, 3, 87, 75, -32, 89, -96, -73,
      -15, 68, 55, 23, 12, -118, -79, 35, -107, 103, 56, 1, -79, -59, 114, -64, -5, -2, -107, -95,
      -91, 16, -41, -98, 81, 63, -21, 1, 81, -11, -19, 92, -8, -117, -5, 41, 88, -34, -107, -67,
      -94, -71, -23, 58, -105, -120, 45, 100, -111, -128, -71, 87, 63, -20, -28, -41, 77, 17, -75,
      -67, -46, -83, 86, 100, -34, -90, 4, 32, -29, -111, -14, -114, 65, -19, 94, 93, 13, -93, 55,
      24, -82, -46, 29, 65, 45, 116, -5, 117, -55, 103, 88, -108, 108, 94, 78, 64, 3, -16, -81, 4,
      -11, 73, 47, 44, -97, 79, 13, 60, 82, 25, 32, 81, -106, -56, -7, -108, 127, 90, -93, -84,
      106, -10, 122, -121, -124, 111, -19, -51, -60, 78, -27, 1, 69, -35, -50, -32, 70, 56, -21,
      89, -6, -99, -27, -3, -22, 20, 102, 49, -60, 127, -127, 15, 95, -87, -17, 92, -35, -36, 126,
      69, -84, -46, -73, 0, 13, -93, -22, 106, -23, -33, -99, 88, -103, -4, 46, -26, 5, 45, 20, 96,
      -6, 87, -47, 30, 57, 31, 52, -86, 88, 48, 32, -32, -114, 65, 20, 119, -89, 119, -87, 25, -36,
      120, 123, 91, -45, -17, 54, -59, 126, -23, 111, 117, -48, -69, -16, -110, -5, -25, -79, -3,
      13, 119, -112, -24, 66, 104, -61, -58, 85, -108, -12, 41, -13, 3, -8, -118, -16, -50, -11,
      44, 87, 19, -58, 54, -92, -46, 34, -6, 43, -111, 92, 21, 48, 9, -69, -63, -40, -21, -122, 45,
      -91, 105, 35, -42, 117, -108, -80, -46, 98, 38, -10, -22, 40, -65, -39, -50, 92, -3, 23, -83,
      121, -90, -81, -87, 13, 70, 124, 69, 25, -118, -39, 15, -18, -48, -100, -109, -2, -45, 123,
      -97, -45, -91, 80, 44, -50, 119, 49, 44, -57, -71, 57, 52, -107, -83, 12, 36, 105, 62, 102,
      -18, -52, -86, -30, 92, -43, -106, -120, 49, -19, 75, -74, -99, -34, -118, -22, 57, 110, 127,
      -1, -39 };

}
