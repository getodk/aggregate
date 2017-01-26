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
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.apache.commons.lang3.StringEscapeUtils;
import org.opendatakit.aggregate.constants.format.KmlConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.exception.ODKParseException;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionField;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.WrappedBigDecimal;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 *
 * @author wbrunette@gmail.com
 *
 */
public class KmlGeoTraceNGeoShapeGenerator extends AbstractKmlElementBase {
  private static final String LIMITATION_MSG = "limitation: name must be in the submission (top-level) or must be in the same repeat group as the trace/shape";

  private static final String PARSE_PROBLEM_GEOTRACE_OR_GEOSHAPES_STRING = "Problem with GPS Coordinates parsed from geotrace or geoshapes. This is not properly formatted coordinate:";

  private FormElementModel nameElement;

  public KmlGeoTraceNGeoShapeGenerator(FormElementModel geoField, FormElementModel nameField,
      FormElementModel rootElement) {
    super(geoField, rootElement);

    // Verify that nesting constraints hold.
    if (verifyElementSameLevel(nameField)) {
      nameElement = nameField;
    } else {
      throw new IllegalStateException(LIMITATION_MSG);
    }

  }

  boolean childVerifyFieldsArePresent(List<FormElementModel> elements) {
    if (nameElement != null && !elements.contains(nameElement)) {
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
      placemarks.append(generatePlacemark(sub));
    } else {
      Queue<SubmissionSet> submissionSetLevelsToExamine = new LinkedList<SubmissionSet>();
      submissionSetLevelsToExamine.add(sub);
      while (!submissionSetLevelsToExamine.isEmpty()) {
        SubmissionSet submissionSet = submissionSetLevelsToExamine.remove();
        recursiveElementSearchToFindRepeats(submissionSet, submissionSetLevelsToExamine, placemarks);
      }
    }

    return placemarks.toString();
  }

  private void recursiveElementSearchToFindRepeats(SubmissionSet submissionSet,
      Queue<SubmissionSet> submissionSetLevelsToExamine, StringBuilder placemarks) {
    List<SubmissionValue> values = submissionSet.getSubmissionValues();
    if (values == null || values.isEmpty()) {
      return;
    }

    for (SubmissionValue value : values) {
      if (value instanceof SubmissionRepeat) {
        SubmissionRepeat repeat = (SubmissionRepeat) value;
        List<SubmissionSet> repeatSets = repeat.getSubmissionSets();
        if (getGeoElementParent().equals(repeat.getElement())) {
          // found the correct repeat, generate placemarks
          for (SubmissionSet set : repeatSets) {
            placemarks.append(generatePlacemark(set));
          }
        } else {
          submissionSetLevelsToExamine.addAll(repeatSets);
        }
      }
    }

  }

  private String generatePlacemark(SubmissionSet sub) {
    try {
      // parse value into geopoints
      SubmissionValue value = sub.getElementValue(getGeoElement());
      List<GeoPoint> points = getGeoLineCoordinates(value);

      // generate KML placemark
      if (!points.isEmpty()) {
        StringBuilder coordinateString = new StringBuilder(BasicConsts.EMPTY_STRING);
        for (GeoPoint gp : points) {
          if (gp != null) {
            if (gp.getLatitude() != null && gp.getLongitude() != null) {
              coordinateString.append(gp.getLongitude());
              coordinateString.append(BasicConsts.COMMA);
              coordinateString.append(gp.getLatitude());
              if (gp.getAltitude() != null) {
                coordinateString.append(BasicConsts.COMMA);
                coordinateString.append(gp.getAltitude());
              }
            }
            coordinateString.append(BasicConsts.NEW_LINE);
          }
        }
        String id = sub.getKey().getKey();
        String idStr = (id == null) ? BasicConsts.EMPTY_STRING : id;
        String name = getName(sub);
        String nameStr = (name == null) ? BasicConsts.EMPTY_STRING : name;
        return String.format(KmlConsts.KML_LINE_STRING_PLACEMARK_TEMPLATE,
            StringEscapeUtils.escapeXml10(idStr), StringEscapeUtils.escapeXml10(nameStr),
            coordinateString.toString().trim());
      }
    } catch (ODKParseException e) {
      // TODO: CHANGE SO THE ERROR GOES TO THE UI
      e.printStackTrace();
    }
    return BasicConsts.EMPTY_STRING;
  }

  private String getName(SubmissionSet set) {
    SubmissionValue nameField = set.getElementValue(nameElement);
    if (nameField != null) {
      Object nameValue = ((SubmissionField<?>) nameField).getValue();
      if (nameValue != null) {
        return nameValue.toString();
      }
    }
    return null;
  }

  private List<GeoPoint> getGeoLineCoordinates(SubmissionValue subValue) throws ODKParseException {
    if (subValue != null) {
      Object value = ((SubmissionField<?>) subValue).getValue();
      if (value != null && value instanceof String) {
        return KmlGeoTraceNGeoShapeGenerator.parseGeoLineCoordinates((String) value);
      }
    }
    return new ArrayList<GeoPoint>(); // return empty array list
  }

  static List<GeoPoint> parseGeoLineCoordinates(String stringWithCoordinates)
      throws ODKParseException {
    List<GeoPoint> points = new ArrayList<GeoPoint>();
    if (stringWithCoordinates != null) {
      String[] geoPointStrings = stringWithCoordinates.split(BasicConsts.SEMI_COLON);
      for (String gpsCoordinate : geoPointStrings) {
        try {

          String[] values = gpsCoordinate.trim().split("\\s+");
          GeoPoint coordinates;
          if (values.length == 1) {
            if (BasicConsts.EMPTY_STRING.equals(values[0])) {
              continue;
            } else {
              throw new ODKParseException(PARSE_PROBLEM_GEOTRACE_OR_GEOSHAPES_STRING
                  + gpsCoordinate);
            }
          } else if (values.length == 2) {
            coordinates = new GeoPoint(new WrappedBigDecimal(values[0]), new WrappedBigDecimal(values[1]));
          } else if (values.length == 3) {
            coordinates = new GeoPoint(new WrappedBigDecimal(values[0]), new WrappedBigDecimal(values[1]),
                new WrappedBigDecimal(values[2]));
          } else if (values.length == 4) {
            coordinates = new GeoPoint(new WrappedBigDecimal(values[0]), new WrappedBigDecimal(values[1]),
                new WrappedBigDecimal(values[2]), new WrappedBigDecimal(values[3]));
          } else {
            throw new ODKParseException(PARSE_PROBLEM_GEOTRACE_OR_GEOSHAPES_STRING);
          }
          points.add(coordinates);
        } catch (Throwable e) {
          throw new ODKParseException(PARSE_PROBLEM_GEOTRACE_OR_GEOSHAPES_STRING + gpsCoordinate, e);
        }
      }
    }
    return points;
  }

}
