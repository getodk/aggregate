/*
 * Copyright (C) 2016 University of Washington
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

package org.opendatakit.aggregate.format.structure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.KmlConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.RepeatCallbackFormatter;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.KmlElementFormatter;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionField;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 *
 * @author wbrunette@gmail.com
 * @author adam.lerer@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class KmlGeoPointGenerator extends AbstractKmlElementBase implements RepeatCallbackFormatter {

  private static final String LIMITATION_MSG = "limitation: image and title must be in the submission (top-level) or must be in the same repeat group as the gps";

  private static final int APPROX_ITEM_LENGTHS = 100;
  private static final int APPROX_TABLE_FORMATTING_LENGTH = 1000;

  private FormElementModel imgElement;
  private FormElementModel titleElement;

  private ElementFormatter elemFormatter;
  private String baseWebServerUrl;
  private List<GpsRepeatRowData> rowsForGpsInRepeats;

  public KmlGeoPointGenerator(FormElementModel gpsField, FormElementModel titleField,
      FormElementModel imgField, String webServerUrl, FormElementModel rootElement) {
    super(gpsField, rootElement);
    baseWebServerUrl = webServerUrl;
    elemFormatter = new KmlElementFormatter(webServerUrl, true, this);

    // Verify that nesting constraints hold.
    if (verifyElementSameLevel(titleField)) {
      titleElement = titleField;
    } else {
      throw new IllegalStateException(LIMITATION_MSG);
    }
    if (verifyElementSameLevel(titleField)) {
      imgElement = imgField;
    } else {
      throw new IllegalStateException(LIMITATION_MSG);
    }

  }

  boolean childVerifyFieldsArePresent(List<FormElementModel> elements) {
    if (titleElement != null && !elements.contains(titleElement)) {
      return false;
    }
    if (imgElement != null && !elements.contains(imgElement)) {
      return false;
    }
    return true;
  }

  @Override
  String generatePlacemarkSubmission(Submission sub, List<FormElementModel> propertyNames,
      CallingContext cc) throws ODKDatastoreException {

    StringBuilder placemarks = new StringBuilder();

    // check if gps coordinate is in top element, else it's in a repeat
    if (geoParentRootSubmissionElement()) {
      // since both gpsParent equals top element, title & imageURL must be in
      // submission
      String title = getTitle(sub);
      String imageURL = getImageUrl(sub, cc);

      GeoPoint geopoint = getGeoPoint(sub);
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      String id = sub.getKey().getKey();
      placemarks.append(generateFormattedPlacemark(row, StringEscapeUtils.escapeXml10(id),
          StringEscapeUtils.escapeXml10(title), imageURL, geopoint));

    } else {
      // clear previous rows generated
      rowsForGpsInRepeats = new ArrayList<GpsRepeatRowData>();

      // the call back will populate rowsForGpsInRepeats
      sub.getFormattedValuesAsRow(propertyNames, elemFormatter, false, cc);
      for (GpsRepeatRowData repeatData : rowsForGpsInRepeats) {
        String title = repeatData.getTitle();
        String imageURL = repeatData.getImgUrl();
        placemarks.append(generateFormattedPlacemark(repeatData.getRow(),
            StringEscapeUtils.escapeXml10(repeatData.getId()),
            StringEscapeUtils.escapeXml10(title), imageURL, repeatData.getGeoPoint()));
      }
    }
    return placemarks.toString();
  }

  @Override
  public void processRepeatedSubmssionSetsIntoRow(List<SubmissionSet> repeats,
      FormElementModel repeatElement, Row row, CallingContext cc) throws ODKDatastoreException {

    for (SubmissionSet repeatSet : repeats) {
      Row rowFromRepeat = repeatSet.getFormattedValuesAsRow(null, elemFormatter, false, cc);
      if (repeatElement.equals(getGeoElementParent())) {
        Row clonedRow = Row.cloneRowValues(row);
        clonedRow.addDataFromRow(rowFromRepeat);
        GeoPoint geopoint = getGeoPoint(repeatSet);
        String title = getTitle(repeatSet);
        String imageURL = getImageUrl(repeatSet, cc);
        String id = repeatSet.getKey().getKey();
        GpsRepeatRowData repeatData = new GpsRepeatRowData(geopoint, id, title, imageURL, clonedRow);
        rowsForGpsInRepeats.add(repeatData);
      } else {
        row.addDataFromRow(rowFromRepeat);
      }
    }
  }

  private GeoPoint getGeoPoint(SubmissionSet set) {
    SubmissionValue geopointField = set.getElementValue(getGeoElement());
    if (geopointField != null) {
      Object value = ((SubmissionField<?>) geopointField).getValue();
      if (value != null && value instanceof GeoPoint) {
        return (GeoPoint) value;
      }
    }
    return null;
  }

  private String getTitle(SubmissionSet set) {
    SubmissionValue titleField = set.getElementValue(titleElement);
    if (titleField != null) {
      Object titleValue = ((SubmissionField<?>) titleField).getValue();
      if (titleValue != null) {
        return titleValue.toString();
      }
    }
    return null;
  }

  private String getImageUrl(SubmissionSet set, CallingContext cc) throws ODKDatastoreException {
    SubmissionValue imageField = set.getElementValue(imgElement);
    if (imageField != null && imageField instanceof BlobSubmissionType) {
      BlobSubmissionType blobSubmission = (BlobSubmissionType) imageField;
      if ((blobSubmission.getAttachmentCount(cc) == 0)
          || (blobSubmission.getContentHash(1, cc) == null)) {
        return null;
      }

      SubmissionKey value = blobSubmission.getValue();
      if (value == null)
        return null;
      Map<String, String> properties = new HashMap<String, String>();
      properties.put(ServletConsts.BLOB_KEY, value.toString());
      String url = baseWebServerUrl + BasicConsts.FORWARDSLASH + BinaryDataServlet.ADDR;
      return HtmlUtil.createLinkWithProperties(url, properties);
    }
    return null;
  }

  private String generateFormattedPlacemark(Row row, String identifier, String title,
      String imageURL, GeoPoint gp) {

    // make sure no null values slip by
    String id = (identifier == null) ? BasicConsts.EMPTY_STRING : identifier;
    String name = (title == null) ? BasicConsts.EMPTY_STRING : title;
    String titleStr = generateDataElement(KmlConsts.TITLE_VARIABLE, name);

    // create data section
    String dataStr = BasicConsts.EMPTY_STRING;
    if (row.size() > 0) {
      StringBuilder formattedDataStr = new StringBuilder(APPROX_TABLE_FORMATTING_LENGTH
          + row.size() * APPROX_ITEM_LENGTHS);
      formattedDataStr.append(KmlConsts.OPEN_TABLE_W_HEADER_TABLE_FORMAT);
      for (String item : row.getFormattedValues()) {
        formattedDataStr.append(item);
      }
      formattedDataStr.append(HtmlConsts.TABLE_CLOSE);
      dataStr = generateDataElement(KmlConsts.DATA_VARIABLE, formattedDataStr.toString());
    }

    // Create Geopoint
    String geopoint = BasicConsts.EMPTY_STRING;
    if (gp != null) {
      if (gp.getLatitude() != null && gp.getLongitude() != null) {
    	  WrappedBigDecimal altitude;
        if (gp.getAltitude() == null) {
          altitude = WrappedBigDecimal.fromDouble(0.0);
        } else {
          altitude = gp.getAltitude();
        }
        geopoint = String.format(KmlConsts.KML_PLACEMARK_POINT_TEMPLATE, gp.getLongitude()
            + BasicConsts.COMMA + gp.getLatitude() + BasicConsts.COMMA + altitude);
      }
    }

    // create placemark
    if (imageURL == null) {
      return String
          .format(KmlConsts.KML_PLACEMARK_TEMPLATE, id, name, KmlConsts.NO_IMAGE_PLACEMARK_STYLE,
              titleStr, BasicConsts.EMPTY_STRING, dataStr, geopoint);
    } else {
      String imgStr = generateDataElement(KmlConsts.IMAGE_VARIABLE, imageURL);
      return String.format(KmlConsts.KML_PLACEMARK_TEMPLATE, id, name,
          KmlConsts.WITH_IMAGE_PLACEMARK_STYLE, titleStr, imgStr, dataStr, geopoint);
    }
  }

  private final class GpsRepeatRowData {
    private final GeoPoint gps;
    private final String id;
    private final Row row;
    private final String title;
    private final String imgUrl;

    private GpsRepeatRowData(GeoPoint gps, String id, String title, String imgUrl, Row row) {
      this.gps = gps;
      this.id = id;
      this.row = row;
      this.title = title;
      this.imgUrl = imgUrl;
    }

    GeoPoint getGeoPoint() {
      return gps;
    }

    Row getRow() {
      return row;
    }

    String getTitle() {
      return title;
    }

    String getImgUrl() {
      return imgUrl;
    }

    String getId() {
        return id;
    }
  }

}
