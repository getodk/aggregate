/*
  Copyright (C) 2011 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */

package org.opendatakit.aggregate.format.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.server.GeopointHeaderIncludes;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

public class UiElementFormatter extends BasicElementFormatter {

  private final String baseWebServerUrl;
  private Map<String, GeopointHeaderIncludes> gpsFormatters;

  /**
   * Construct a UI Element Formatter
   */
  public UiElementFormatter(String webServerUrl, Map<String, GeopointHeaderIncludes> gpsFormatter) {
    super(false, false, false);
    this.baseWebServerUrl = webServerUrl;
    this.gpsFormatters = gpsFormatter;
  }

  @Override
  public void formatGeoPoint(GeoPoint value, FormElementModel element, String ordinalValue, Row row) {
    GeopointHeaderIncludes gpsFormatter = null;

    if (gpsFormatters != null) {
      gpsFormatter = gpsFormatters.get(element.getElementName());
    }

    if (gpsFormatter == null) {
      row.addFormattedValue(Optional.ofNullable((Object) value.getLatitude()).map(Object::toString).orElse(null));
      row.addFormattedValue(Optional.ofNullable((Object) value.getLongitude()).map(Object::toString).orElse(null));
      row.addFormattedValue(Optional.ofNullable((Object) value.getAltitude()).map(Object::toString).orElse(null));
      row.addFormattedValue(Optional.ofNullable((Object) value.getAccuracy()).map(Object::toString).orElse(null));
    } else {
      if (gpsFormatter.includeLatitude()) {
        row.addFormattedValue(Optional.ofNullable((Object) value.getLatitude()).map(Object::toString).orElse(null));
      }

      if (gpsFormatter.includeLongitude()) {
        row.addFormattedValue(Optional.ofNullable((Object) value.getLongitude()).map(Object::toString).orElse(null));
      }

      if (gpsFormatter.includeAltitude()) {
        row.addFormattedValue(Optional.ofNullable((Object) value.getAltitude()).map(Object::toString).orElse(null));
      }

      if (gpsFormatter.includeAccuracy()) {
        row.addFormattedValue(Optional.ofNullable((Object) value.getAccuracy()).map(Object::toString).orElse(null));
      }
    }
  }

  @Override
  public void formatBinary(BlobSubmissionType value, FormElementModel element, String ordinalValue, Row row, CallingContext cc) throws ODKDatastoreException {
    if (value == null ||
        (value.getAttachmentCount(cc) == 0) ||
        (value.getContentHash(1, cc) == null)) {
      row.addFormattedValue(null);
      return;
    }

    SubmissionKey key = value.getValue();
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.BLOB_KEY, key.toString());
    String url = HtmlUtil.createLinkWithProperties(baseWebServerUrl + BasicConsts.FORWARDSLASH + BinaryDataServlet.ADDR, properties);
    //    String html = "<img style='padding:5px' height='144px' src='" + url + "'/>";
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

    row.addFormattedValue(repeat.constructSubmissionKey().toString());
  }
}
