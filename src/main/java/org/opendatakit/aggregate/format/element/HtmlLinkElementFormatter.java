/*
 * Copyright (C) 2010 University of Washington
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
package org.opendatakit.aggregate.format.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.FormTableConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.servlet.FormMultipleValueServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 */
public class HtmlLinkElementFormatter extends BasicElementFormatter {

  private final String baseWebServerUrl;
  private final boolean binariesAsDownloadLink;

  /**
   * Construct a Html Link Element Formatter
   *
   * @param webServerUrl           base url for the web app (e.g., localhost:8080/ODKAggregatePlatform)
   * @param separateGpsCoordinates separate the GPS coordinates of latitude and longitude into
   *                               columns
   * @param includeGpsAltitude     include GPS altitude data
   * @param includeGpsAccuracy     include GPS accuracy data
   */
  public HtmlLinkElementFormatter(String webServerUrl, boolean separateGpsCoordinates,
                                  boolean includeGpsAltitude, boolean includeGpsAccuracy) {
    this(webServerUrl, separateGpsCoordinates, includeGpsAltitude, includeGpsAccuracy, false);
  }

  public HtmlLinkElementFormatter(String webServerUrl, boolean separateGpsCoordinates,
                                  boolean includeGpsAltitude, boolean includeGpsAccuracy, boolean binariesAsDownloadLink) {
    super(separateGpsCoordinates, includeGpsAltitude, includeGpsAccuracy);
    baseWebServerUrl = webServerUrl;
    this.binariesAsDownloadLink = binariesAsDownloadLink;
  }

  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue, Row row, CallingContext cc) throws ODKDatastoreException {
    if (blobSubmission == null ||
        (blobSubmission.getAttachmentCount(cc) == 0) ||
        (blobSubmission.getContentHash(1, cc) == null)) {
      row.addFormattedValue(null);
      return;
    }

    SubmissionKey key = blobSubmission.getValue();
    String linkText;
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.BLOB_KEY, key.toString());
    if (binariesAsDownloadLink) {
      properties.put(ServletConsts.AS_ATTACHMENT, "yes");
      linkText = FormTableConsts.DOWNLOAD_LINK_TEXT;
      if (blobSubmission.getAttachmentCount(cc) == 1) {
        linkText = blobSubmission.getUnrootedFilename(1, cc);
        if (linkText == null || linkText.length() == 0) {
          linkText = FormTableConsts.DOWNLOAD_LINK_TEXT;
        }
      }
    } else {
      linkText = FormTableConsts.VIEW_LINK_TEXT;
    }
    String url = HtmlUtil.createHrefWithProperties(baseWebServerUrl + BasicConsts.FORWARDSLASH + BinaryDataServlet.ADDR, properties, linkText, !binariesAsDownloadLink);
    row.addFormattedValue(url);
  }


  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row, CallingContext cc) {
    if (repeat == null) {
      row.addFormattedValue(null);
      return;
    }

    List<SubmissionSet> sets = repeat.getSubmissionSets();
    if (sets.size() == 0) {
      row.addFormattedValue(null);
      return;
    }

    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.FORM_ID, repeat.constructSubmissionKey().toString());

    String url = HtmlUtil.createHrefWithProperties(baseWebServerUrl + BasicConsts.FORWARDSLASH + FormMultipleValueServlet.ADDR, properties, FormTableConsts.VIEW_LINK_TEXT, false);
    row.addFormattedValue(url);
  }


}
