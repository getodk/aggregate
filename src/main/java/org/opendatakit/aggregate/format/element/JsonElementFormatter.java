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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.format.RepeatCallbackFormatter;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.submission.SubmissionKey;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.utils.WebUtils;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 *
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 *
 */
public class JsonElementFormatter implements ElementFormatter {
  private static final String JSON_NULL = "null";
  private static final String JSON_TRUE = "true";
  private static final String JSON_FALSE = "false";

  private static ObjectMapper mapper = new ObjectMapper();

  private RepeatCallbackFormatter callbackFormatter;

  private String baseWebServerUrl;

  /**
   * separate the GPS coordinates of latitude and longitude into columns
   */
  private boolean separateCoordinates;

  /**
   * include GPS altitude data
   */
  private boolean includeAltitude;

  /**
   * include GPS accuracy data
   */
  private boolean includeAccuracy;

  /**
   * express multiple-choice values as an array of strings.
   */
  private boolean expressMultipleChoiceListsAsArrays;

  /**
   * Construct a JSON Element Formatter
   *
   * @param separateGpsCoordinates
   *          separate the GPS coordinates of latitude and longitude into
   *          columns
   * @param includeGpsAltitude
   *          include GPS altitude data
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   * @param expressMultipleChoiceListsAsArrays
   *          if true, express the multiple-choice fields as arrays of strings
   */
  public JsonElementFormatter(boolean separateGpsCoordinates, boolean includeGpsAltitude,
      boolean includeGpsAccuracy, boolean expressMultipleChoiceListsAsArrays,
      RepeatCallbackFormatter formatter) {
    separateCoordinates = separateGpsCoordinates;
    includeAltitude = includeGpsAltitude;
    includeAccuracy = includeGpsAccuracy;
    this.expressMultipleChoiceListsAsArrays = expressMultipleChoiceListsAsArrays;
    callbackFormatter = formatter;
    baseWebServerUrl = null;
  }

  /**
   * Construct a JSON Element Formatter with links
   *
   * @param webServerUrl
   *          base url for the web app (e.g.,
   *          localhost:8080/ODKAggregatePlatform)
   * @param separateGpsCoordinates
   *          separate the GPS coordinates of latitude and longitude into
   *          columns
   * @param includeGpsAltitude
   *          include GPS altitude data
   * @param includeGpsAccuracy
   *          include GPS accuracy data
   * @param expressMultipleChoiceListsAsArrays
   *          if true, express the multiple-choice fields as arrays of strings
   */
  public JsonElementFormatter(String webServerUrl, boolean separateGpsCoordinates,
      boolean includeGpsAltitude, boolean includeGpsAccuracy,
      boolean expressMultipleChoiceListsAsArrays, RepeatCallbackFormatter formatter) {
    this(separateGpsCoordinates, includeGpsAltitude, includeGpsAccuracy,
        expressMultipleChoiceListsAsArrays, formatter);
    baseWebServerUrl = webServerUrl;
  }

  @Override
  public void formatUid(String uri, String propertyName, Row row) {
    // unneeded so unimplemented
  }

  @Override
  public void formatBinary(BlobSubmissionType blobSubmission, FormElementModel element,
      String ordinalValue, Row row, CallingContext cc) throws ODKDatastoreException {
    if (blobSubmission == null || (blobSubmission.getAttachmentCount(cc) == 0)
        || (blobSubmission.getContentHash(1, cc) == null)) {
      addToJsonValueToRow(null, true, element.getElementName(), row);
      return;
    }

    byte[] imageBlob = null;
    if (blobSubmission.getAttachmentCount(cc) == 1) {
      imageBlob = blobSubmission.getBlob(1, cc);
    }
    if (imageBlob != null && imageBlob.length > 0) {
      Map<String,String> obj = new HashMap<String,String>();
      obj.put("filename", blobSubmission.getUnrootedFilename(1, cc));
      obj.put("type", blobSubmission.getContentType(1, cc));
      if (baseWebServerUrl == null) {
        // embed the binary
        obj.put("bytes", new String(Base64.encodeBase64(imageBlob)));
      } else {
        // create a link to the binary
        SubmissionKey key = blobSubmission.getValue();
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(ServletConsts.BLOB_KEY, key.toString());
        String url = HtmlUtil.createLinkWithProperties(baseWebServerUrl + BasicConsts.FORWARDSLASH
            + BinaryDataServlet.ADDR, properties);
        obj.put("url", url);
      }
      String serialized = null;
      try {
        serialized = mapper.writeValueAsString(obj);
      } catch (JsonGenerationException e) {
        e.printStackTrace();
      } catch (JsonMappingException e) {
        e.printStackTrace();
      } catch (IOException e) {
        e.printStackTrace();
      }
      addToJsonValueToRow(serialized, false, element.getElementName(), row);
    }

  }

  @Override
  public void formatBoolean(Boolean bool, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(((bool == null) ? null : (bool ? JSON_TRUE : JSON_FALSE)), false,
        element.getElementName(), row);
  }

  @Override
  public void formatChoices(List<String> choices, FormElementModel element, String ordinalValue,
      Row row) {
    StringBuilder b = new StringBuilder();

    if (choices.size() == 0) {
      addToJsonValueToRow(null, true, element.getElementName(), row);
    } else {
      if (expressMultipleChoiceListsAsArrays) {
        b.append(BasicConsts.LEFT_BRACKET);
        boolean first = true;
        for (String s : choices) {
          if (!first) {
            b.append(BasicConsts.COMMA);
          }
          first = false;
          try {
            b.append(mapper.writeValueAsString(s));
          } catch (JsonGenerationException e) {
            e.printStackTrace();
          } catch (JsonMappingException e) {
            e.printStackTrace();
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
        b.append(BasicConsts.RIGHT_BRACKET);
        addToJsonValueToRow(b.toString(), false, element.getElementName(), row);
      } else {
        boolean first = true;
        for (String s : choices) {
          if (!first) {
            b.append(BasicConsts.SPACE);
          }
          first = false;
          b.append(s);
        }
        addToJsonValueToRow(b.toString(), true, element.getElementName(), row);
      }
    }
  }

  @Override
  public void formatDate(Date date, FormElementModel element, String ordinalValue, Row row) {
    // date in ISO8601 Javarosa format
    addToJsonValueToRow((date == null) ? null : WebUtils.asSubmissionDateOnlyString(date), true,
        element.getElementName(), row);

  }

  @Override
  public void formatDateTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    // dateTime in ISO8601 Javarosa format
    addToJsonValueToRow((date == null) ? null : WebUtils.asSubmissionDateTimeString(date), true,
        element.getElementName(), row);

  }

  @Override
  public void formatTime(Date date, FormElementModel element, String ordinalValue, Row row) {
    // time in ISO8601 Javarosa format
    addToJsonValueToRow((date == null) ? null : WebUtils.asSubmissionTimeOnlyString(date), true,
        element.getElementName(), row);
  }

  @Override
  public void formatDecimal(BigDecimal dub, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(dub, false, element.getElementName(), row);

  }

  @Override
  public void formatGeoPoint(GeoPoint coordinate, FormElementModel element, String ordinalValue,
      Row row) {
    if (separateCoordinates) {
      addToJsonValueToRow(coordinate.getLatitude(), false, element.getElementName()
          + FormatConsts.HEADER_CONCAT + GeoPoint.LATITUDE, row);
      addToJsonValueToRow(coordinate.getLongitude(), false, element.getElementName()
          + FormatConsts.HEADER_CONCAT + GeoPoint.LONGITUDE, row);

      if (includeAltitude) {
        addToJsonValueToRow(coordinate.getAltitude(), false, element.getElementName()
            + FormatConsts.HEADER_CONCAT + GeoPoint.ALTITUDE, row);
      }

      if (includeAccuracy) {
        addToJsonValueToRow(coordinate.getAccuracy(), false, element.getElementName()
            + FormatConsts.HEADER_CONCAT + GeoPoint.ACCURACY, row);
      }
    } else {
      if (coordinate.getLongitude() != null && coordinate.getLatitude() != null) {
        String coordVal = coordinate.getLatitude().toString() + BasicConsts.COMMA
            + BasicConsts.SPACE + coordinate.getLongitude().toString();
        addToJsonValueToRow(coordVal, true, element.getElementName(), row);
        if (includeAltitude) {
          addToJsonValueToRow(coordinate.getAltitude(), false, element.getElementName()
              + FormatConsts.HEADER_CONCAT + GeoPoint.ALTITUDE, row);
        }
        if (includeAccuracy) {
          addToJsonValueToRow(coordinate.getAccuracy(), false, element.getElementName()
              + FormatConsts.HEADER_CONCAT + GeoPoint.ACCURACY, row);
        }
      } else {
        addToJsonValueToRow(null, false, element.getElementName(), row);
      }
    }

  }

  @Override
  public void formatLong(Long longInt, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(longInt, false, element.getElementName(), row);
  }

  @Override
  public void formatRepeats(SubmissionRepeat repeat, FormElementModel repeatElement, Row row,
      CallingContext cc) throws ODKDatastoreException {
    callbackFormatter.processRepeatedSubmssionSetsIntoRow(repeat.getSubmissionSets(),
        repeatElement, row, cc);
  }

  @Override
  public void formatString(String string, FormElementModel element, String ordinalValue, Row row) {
    addToJsonValueToRow(string, true, element.getElementName(), row);
  }

  private void addToJsonValueToRow(Object value, boolean quoted, String propertyName, Row row) {
    StringBuilder jsonString = new StringBuilder();
    jsonString.append(BasicConsts.QUOTE);
    jsonString.append(propertyName);
    jsonString.append(BasicConsts.QUOTE + BasicConsts.COLON);

    if (value != null) {
      if (quoted) {
        try {
          jsonString.append(mapper.writeValueAsString(value.toString()));
        } catch (JsonGenerationException e) {
          e.printStackTrace();
        } catch (JsonMappingException e) {
          e.printStackTrace();
        } catch (IOException e) {
          e.printStackTrace();
        }
      } else {
        jsonString.append(value.toString());
      }
    } else {
      jsonString.append(JSON_NULL);
    }

    row.addFormattedValue(jsonString.toString());
  }

}
