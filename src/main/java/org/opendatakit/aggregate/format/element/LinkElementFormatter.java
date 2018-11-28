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
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
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
public class LinkElementFormatter extends BasicElementFormatter {
  private final String baseWebServerUrl;
  private final String repeatServlet;


  /**
   * Construct a Html Link Element Formatter
   *
   * @param baseWebServerUrl       base url for the web app (e.g., http://localhost:8080/ODKAggregatePlatform)
   * @param repeatServlet          name of the repeat servlet for repeat data.
   * @param separateGpsCoordinates separate the GPS coordinates of latitude and longitude into
   *                               columns
   * @param includeGpsAltitude     include GPS altitude data
   * @param includeGpsAccuracy     include GPS accuracy data
   */
  public LinkElementFormatter(String baseWebServerUrl, String repeatServlet,
                              boolean separateGpsCoordinates, boolean includeGpsAltitude, boolean includeGpsAccuracy, boolean googleDocsDate) {
    super(separateGpsCoordinates, includeGpsAltitude, includeGpsAccuracy);
    this.baseWebServerUrl = baseWebServerUrl;
    this.repeatServlet = repeatServlet;
  }

  public void addFormattedLink(SubmissionKey key, String servletPath, String urlParameterName, Row row) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(urlParameterName, key.toString());
    String url = HtmlUtil.createLinkWithProperties(baseWebServerUrl + BasicConsts.FORWARDSLASH + servletPath, properties);
    row.addFormattedValue(url);
  }

  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element, String ordinalValue, Row row, CallingContext cc)
      throws ODKDatastoreException {
    if (blobSubmission == null ||
        (blobSubmission.getAttachmentCount(cc) == 0) ||
        (blobSubmission.getContentHash(1, cc) == null)) {
      row.addFormattedValue(null);
      return;
    }

    addFormattedLink(blobSubmission.getValue(), BinaryDataServlet.ADDR,
        ServletConsts.BLOB_KEY, row);
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

    addFormattedLink(repeat.constructSubmissionKey(), repeatServlet,
        ServletConsts.FORM_ID, row);
  }
}
