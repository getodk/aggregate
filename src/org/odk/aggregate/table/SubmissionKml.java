/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.table;

import java.io.IOException;
import java.io.Writer;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringEscapeUtils;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.servlet.ImageViewerServlet;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionField;
import org.odk.aggregate.submission.type.GeoPoint;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class SubmissionKml extends SubmissionResultBase{
  
  private final String PREAMBLE_TEMPLATE = 
    "<?xml version='1.0' encoding='UTF-8'?>\n" +
    "<kml xmlns='http://www.opengis.net/kml/2.2' xmlns:gx='http://www.google.com/kml/ext/2.2' xmlns:kml='http://www.opengis.net/kml/2.2' xmlns:atom='http://www.w3.org/2005/Atom'>\n" +
    "<Document id='odk_kml'>\n" +
    "  <name>%s</name>\n" +
    "  <open>1</open>\n" +
    "  <Snippet maxLines='0'></Snippet>\n" +
    "  <description>KML file showing results from ODK form: %s</description>\n";
  
  private final String POSTAMBLE_TEMPLATE = 
    "  </Document>\n" + 
    "</kml>";
  
  private static final String STYLE_TEMPLATE =
    "<Style id='%s'>\n" +
    "  <BalloonStyle>\n" +
    "    <text>\n" +
    "      <![CDATA[\n" +
    "        %s\n" +
    "      ]]>\n" +
    "    </text>\n" +
    "  </BalloonStyle>\n" +
    "</Style>\n";
  
  private final String DATA_ELEMENT_TEMPLATE = 
    "<Data name='%s'>\n" + 
    "  <value>%s</value>\n" +
    "</Data>\n";
  
  private final String PLACEMARK_TEMPLATE = 
    "<Placemark id='%s'>\n" +
    "  <name>%s</name>\n" +
    "  <styleUrl>#odk_style</styleUrl>\n" +
    "  <Snippet maxLines='0'></Snippet>\n" +
    "  <ExtendedData>\n" + 
    "  %s</ExtendedData>\n" +
    "%s</Placemark>\n"; //PLACEMARK_POINT_TEMPLATE goes in %s
  
  private final String PLACEMARK_POINT_TEMPLATE = 
    "  <Point>\n" +
    "    <coordinates>%s</coordinates>\n" +
    "  </Point>\n";
  
  private String geopointField = null;
  private String nameField = null;
  private String imageField = null;
  
  /**
   * 
   * @param odkIdentifier
   * @param serverName
   * @param entityManager
   */
  public SubmissionKml(String odkIdentifier, String serverName, EntityManager entityManager, String geopointField, String nameField, String imageField) throws ODKFormNotFoundException{
    super(serverName, odkIdentifier, entityManager, TableConsts.QUERY_ROWS_MAX);
    this.geopointField = geopointField;
    this.nameField = nameField;
    this.imageField = imageField;
  }
  
  public void generateKml(Writer w) throws IOException, ODKIncompleteSubmissionData {
    generatePropertyNamesAndHeaders(form.getElementTreeRoot(), true);
    
    List<Entity> submissionEntities = getEntities(TableConsts.EPOCH, false);

    w.write(getPreamble(odkId));
    w.write(generateStyle(headers, imageField != null));
    for (Entity subEntity : submissionEntities) {
      w.write(generatePlacemark(subEntity));
    }
    w.write(getPostamble());
  }
  
  private String generateStyle(List<String> headers, boolean hasImage){
    String styleHtml = "";
    styleHtml += 
      "<table width='300' cellpadding='0' cellspacing='0'>" + 
      "  <tr><td><h2>$[__name]</h2></td></tr>" +
      (hasImage ? "  <tr><td align='center'><img style='padding:5px' src='$[__imgUrl]' /></td></tr>":"") +
      "  <tr><td align='center'><table border='1' style='border-collapse: collapse;' >";
    for (String header: headers){
      String escapedHeader = StringEscapeUtils.escapeHtml(header);
      styleHtml +=
        "<tr><td><b>" + escapedHeader + "</b></td><td>$[" + escapedHeader + "]</td></tr>\n";
    }
    styleHtml +=
      "</table></td></tr></table>";
    
    return String.format(STYLE_TEMPLATE, "odk_style", styleHtml);
  }
  
  private boolean moreRecords;
  /**
   * Generates a result table that contains all the submission data 
   * of the form specified by the ODK ID
   * 
   * @return
   *    a result table containing submission data
   *
   * @throws ODKIncompleteSubmissionData 
   */
  protected List<Entity> getEntities(Date lastDate, boolean backward) {
    
    // create results table
    generatePropertyNamesAndHeaders(form.getElementTreeRoot(), true);

    // retrieve submissions
    Query surveyQuery = new Query(odkId);
    if(backward) {
      surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.DESCENDING);
      surveyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.FilterOperator.LESS_THAN, lastDate);
    } else {
      surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.ASCENDING);
      surveyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.FilterOperator.GREATER_THAN, lastDate);
    }
    
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<Entity> submissionEntities =
        ds.prepare(surveyQuery).asList(FetchOptions.Builder.withLimit(fetchLimit + 1));
    
    if(submissionEntities.size() > fetchLimit) {
      moreRecords = true;
      submissionEntities.remove(fetchLimit);
    }
    return submissionEntities;
  }
  
  private String generatePlacemark(Entity subEntity) throws ODKIncompleteSubmissionData {
    GeoPoint gp = null;
    String name=null, imageUrl=null;
    Map<String, String> data = new HashMap<String, String>();
    
    Submission sub = new Submission(subEntity, form);
    Map<String, SubmissionField<?>> fieldMap = sub.getSubmissionFieldsMap();

    data.put(TableConsts.SUBMISSION_DATE_HEADER, sub.getSubmittedTime().toString());
    int headerPointer = 0; // 0 is SUBMISSION_DATE
    for (String property: propertyNames) {
      SubmissionField<?> field = fieldMap.get(property);
      if (field == null) {
        System.out.println("Null field: " + property + ", " + headers.get(headerPointer) + "((" + headerPointer);
        data.put(headers.get(headerPointer++), null);
        continue;
      }
      Object value = field.getValue();
      String fieldString = getFieldString(field);
      if (property.equals(geopointField) && value instanceof GeoPoint) {
        gp = (GeoPoint) value;
      }
      if (property.equals(nameField)) {
        name = fieldString;
      }
      if (property.equals(imageField)) {
        imageUrl = fieldString;
      }
      // here's the nasty hack to accomodate the fact that GeoPoint => two rows
      if (value != null && value instanceof GeoPoint) {
        System.out.println("Lat: " + property + ", " + headers.get(headerPointer) + "((" + headerPointer);
        data.put(headers.get(headerPointer++), String.valueOf(((GeoPoint) value).getLatitude()));
        System.out.println("Long: " + property + ", " + headers.get(headerPointer) + "((" + headerPointer);
        data.put(headers.get(headerPointer++), String.valueOf(((GeoPoint) value).getLongitude()));
      } else if (field.isBinary()) {
        data.put(headers.get(headerPointer++), "<a href='" + fieldString + "'>View</a>");
      } else {
        System.out.println("Normal: " + property + ", " + headers.get(headerPointer) + "((" + headerPointer);
        data.put(headers.get(headerPointer++), fieldString);
      }
    }
    String id = KeyFactory.keyToString(sub.getKey());
    return generatePlacemark(id, name, gp, imageUrl, data);
  }
  
  private String generatePlacemark(String id, String name, GeoPoint gp, String imageUrl, Map<String, String> data){
    String dataString = BasicConsts.EMPTY_STRING;
    dataString += generateDataElement("__name", name);
    dataString += generateDataElement("__imgUrl", imageUrl);
    for (String n: data.keySet()) {
      dataString += generateDataElement(n, data.get(n));
    }
    String point = (gp==null || gp.getLatitude()==null || gp.getLongitude()==null)?"":String.format(PLACEMARK_POINT_TEMPLATE, gp.getLongitude() + "," + gp.getLatitude() + ",0");
    return String.format(PLACEMARK_TEMPLATE, StringEscapeUtils.escapeXml(id), StringEscapeUtils.escapeXml(name), dataString, point);
  }
  
  private String getFieldString(SubmissionField<?> field) {
    if (field == null) {
      return null;
    }
    Object value = field.getValue();
    if (value == null) {
      return null;
    }
    if (field.isBinary()) {
      if (value instanceof Key) {
        Key blobKey = (Key) value;
        Map<String, String> properties = createViewLinkProperties(blobKey);
        return HtmlUtil.createLinkWithProperties(super.getBaseServerUrl() + ImageViewerServlet.ADDR, properties);
      } else {
        System.err.println(ErrorConsts.NOT_A_KEY);
        return null;
      }
//    } else if(value instanceof GeoPoint) {
//      GeoPoint coordinate = (GeoPoint) value;
//      if (coordinate.getLatitude() == null || coordinate.getLongitude() == null) {
//        return null;
//      }
//      return coordinate.getLongitude() + "," + coordinate.getLatitude();
    } else {
      return value.toString();
    }
  }
  
  private String generateDataElement(String name, String value){
    return String.format(DATA_ELEMENT_TEMPLATE, StringEscapeUtils.escapeXml(name), StringEscapeUtils.escapeXml(value));
  }
 
  
  private String getPreamble(String title){
    return String.format(PREAMBLE_TEMPLATE, title, title);
  }
  
  private String getPostamble(){
    return POSTAMBLE_TEMPLATE;
  }

  protected Map<String, String> createViewLinkProperties(Key subKey) {
    Map<String, String> properties = new HashMap<String,String>();
    properties.put(ServletConsts.BLOB_KEY, KeyFactory.keyToString(subKey)); 
    return properties;
  }

}
