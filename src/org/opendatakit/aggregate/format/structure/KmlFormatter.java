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
package org.opendatakit.aggregate.format.structure;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.opendatakit.aggregate.servlet.BinaryDataServlet;
import org.opendatakit.aggregate.constants.FormatConsts;
import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.aggregate.constants.ServletConsts;
import org.opendatakit.aggregate.datamodel.FormDataModel;
import org.opendatakit.aggregate.datamodel.FormDefinition;
import org.opendatakit.aggregate.format.SubmissionFormatter;
import org.opendatakit.aggregate.format.element.BasicHeaderFormatter;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.format.element.HeaderFormatter;
import org.opendatakit.aggregate.format.element.KmlElementFormatter;
import org.opendatakit.aggregate.format.element.Row;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.aggregate.submission.SubmissionField;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.SubmissionValue;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.constants.BasicConsts;
import org.opendatakit.common.constants.HtmlConsts;
import org.opendatakit.common.persistence.Datastore;
import org.opendatakit.common.persistence.EntityKey;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;

public class KmlFormatter implements SubmissionFormatter {

  public static final String KML_DATA_ELEMENT_TEMPLATE = "<Data name='%s'>\n"
    + "  <value>%s</value>\n" 
    + "</Data>\n";
  
  private static final String KML_PLACEMARK_TEMPLATE = "<Placemark id='%s'>\n"
    + "  <name>%s</name>\n" 
    + "  <styleUrl>#odk_style</styleUrl>\n"
    + "  <Snippet maxLines='0'></Snippet>\n" 
    + "  <ExtendedData>\n" 
    + "  %s</ExtendedData>\n"
    + "%s</Placemark>\n"; // PLACEMARK_POINT_TEMPLATE goes in %s
  
  private static final String IMAGE_VARIABLE = "__imgUrl";

  private static final String NAME_VARIABLE = "__name";

  private static final String VARIABLE_BEGIN = "$[";
  
  private static final String TABLE_DATA_CUSTOM = "<td align='center'>";

  private static final String OPEN_TABLE_W_HEADER_TABLE_FORMAT = "<table border='1' style='border-collapse: collapse;' >";

  private static final String OPEN_TABLE_W_PARENT_TABLE_FORMAT = "<table width='300' cellpadding='0' cellspacing='0'> +";

  private static final String IMAGE_FORMATTING = "<td align='center'><img style='padding:5px' src='$[" + IMAGE_VARIABLE + "]' /></td>";

  private FormDataModel geopointFieldName = null;
  private FormDataModel titleFieldName = null;
  private FormDataModel imageFieldName = null;

  private ElementFormatter elemFormatter;

  private List<FormDataModel> propertyNames;

  private PrintWriter output;

  private FormDefinition formDefinition;
  
  private HeaderFormatter headerFormatter;
  
  private String baseWebServerUrl;

  public KmlFormatter(FormDefinition xform, String webServerUrl, FormDataModel gpsField, FormDataModel titleField,
		  FormDataModel imgField, PrintWriter printWriter, List<FormDataModel> selectedColumnNames,
      Datastore datastore) {
    formDefinition = xform;
    output = printWriter;
    propertyNames = selectedColumnNames;
    elemFormatter = new KmlElementFormatter(webServerUrl, true);
    headerFormatter = new BasicHeaderFormatter(true, true, true);

    geopointFieldName = gpsField;
    titleFieldName = titleField;
    imageFieldName = imgField;
    baseWebServerUrl = webServerUrl;
  }

  @Override
  public void processSubmissions(List<Submission> submissions) throws ODKDatastoreException {
    // output preamble
    output.append(String.format(FormatConsts.KML_PREAMBLE_TEMPLATE, formDefinition.getFormName(), formDefinition.getFormName()));

    // create headers
    List<String> headers = headerFormatter.generateHeaders(formDefinition, formDefinition.getTopLevelGroup(), propertyNames);
    output.append(generateStyle(headers, imageFieldName != null));
    
    // format row elements
    for (SubmissionSet sub : submissions) {
      String point = BasicConsts.EMPTY_STRING;
      String name = BasicConsts.EMPTY_STRING;
      String formattedData = BasicConsts.EMPTY_STRING;

      SubmissionValue nameField = sub.getElementValue(titleFieldName);
      if (nameField != null) {
        Object nameValue = ((SubmissionField<?>) nameField).getValue();
        if(nameValue != null){
          name = nameValue.toString();
        }
      }
      
      formattedData += generateDataElement(NAME_VARIABLE, name);
      
      SubmissionValue geopointField = sub.getElementValue(geopointFieldName);
      if (geopointField != null) {
        Object value = ((SubmissionField<?>) geopointField).getValue();
        if (value != null && value instanceof GeoPoint) {

          GeoPoint gp = (GeoPoint) value;
          if (gp != null && gp.getLatitude() != null && gp.getLongitude() != null) {
            BigDecimal altitude = BigDecimal.ZERO;
            if (gp.getAltitude() != null) {
              altitude = gp.getAltitude();
            }
            point = String.format(FormatConsts.KML_PLACEMARK_POINT_TEMPLATE, gp.getLongitude()
                + BasicConsts.COMMA + gp.getLatitude() + BasicConsts.COMMA + altitude);
          }
        }
      } 
      
      String imageUrl = BasicConsts.EMPTY_STRING;
      SubmissionValue imageField = sub.getElementValue(imageFieldName);
      if (imageField != null) {
        Object value = ((SubmissionField<?>) imageField).getValue();
        if (value != null && value instanceof EntityKey) {
          EntityKey key = (EntityKey) value;
          Map<String, String> properties = new HashMap<String, String>();
          properties.put(ServletConsts.BLOB_KEY, key.getKey());
          imageUrl = HtmlUtil.createHrefWithProperties(baseWebServerUrl + BinaryDataServlet.ADDR, properties, FormatConsts.VIEW_LINK_TEXT);
        }
      } 
      formattedData += generateDataElement(IMAGE_VARIABLE, imageUrl);
      
      Row row = sub.getFormattedValuesAsRow(propertyNames, elemFormatter);
      for(String formattedString : row.getFormattedValues()) {
        formattedData += formattedString;
      }
     
       
      
      String id = sub.getKey().getKey();
      output.append(String.format(KML_PLACEMARK_TEMPLATE, StringEscapeUtils.escapeXml(id), StringEscapeUtils.escapeXml(name), formattedData, point));

    }

    // output postamble
    output.append(FormatConsts.KML_POSTAMBLE_TEMPLATE);

  }

  @Override
  public void processRepeatedSubmssionSets(FormDataModel repeatGroup, List<SubmissionSet> repeats)
      throws ODKDatastoreException {
    // TODO: Figure out how to incorporate repeats into KML. Large problem is
    // how to deal with the GPS coordinate being in the repeat.

  }

  private String generateDataElement(String name, String value){
    return String.format(KML_DATA_ELEMENT_TEMPLATE, StringEscapeUtils.escapeXml(name), StringEscapeUtils.escapeXml(value));
  }

  
  private String generateStyle(List<String> headers, boolean hasImage) {
    String styleHtml = OPEN_TABLE_W_PARENT_TABLE_FORMAT;
    styleHtml += BasicConsts.SPACE + BasicConsts.SPACE;
    styleHtml += wrapInBothRowNData(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H2, VARIABLE_BEGIN
        + NAME_VARIABLE + BasicConsts.RIGHT_BRACKET));
    styleHtml += generateImageStyle(hasImage);
    styleHtml += BasicConsts.SPACE + BasicConsts.SPACE;
    styleHtml += HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, createHeaderFormat(headers));
    styleHtml += HtmlConsts.TABLE_CLOSE;

    return String.format(FormatConsts.KML_STYLE_TEMPLATE, "odk_style", styleHtml);
  }

  private String createHeaderFormat(List<String> headers) {
    String tmp = TABLE_DATA_CUSTOM;
    tmp += OPEN_TABLE_W_HEADER_TABLE_FORMAT;
    for (String header : headers) {
      tmp += wrapRowHeaderStyle(StringEscapeUtils.escapeHtml(header));

    }
    tmp += HtmlConsts.TABLE_CLOSE;
    tmp += HtmlConsts.TABLE_DATA_CLOSE;
    return tmp;
  }

  private String generateImageStyle(boolean hasImage) {
    if (hasImage) {
      return BasicConsts.SPACE + BasicConsts.SPACE
          + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, IMAGE_FORMATTING);
    } else {
      return BasicConsts.EMPTY_STRING;
    }
  }

  private String wrapRowHeaderStyle(String escapedHeader) {
    String data = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, HtmlUtil.wrapWithHtmlTags(
        HtmlConsts.B, escapedHeader));
    data += HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, VARIABLE_BEGIN + escapedHeader
        + BasicConsts.RIGHT_BRACKET);
    return HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, data);
  }

  private String wrapInBothRowNData(String value) {
    return HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, HtmlUtil.wrapWithHtmlTags(
        HtmlConsts.TABLE_DATA, value));
  }
}
