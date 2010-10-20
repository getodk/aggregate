/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.table;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.commons.lang.StringEscapeUtils;
import org.odk.aggregate.constants.BasicConsts;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.PersistConsts;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.constants.TableConsts;
import org.odk.aggregate.exception.ODKFormNotFoundException;
import org.odk.aggregate.exception.ODKIncompleteSubmissionData;
import org.odk.aggregate.form.Form;
import org.odk.aggregate.form.FormElement;
import org.odk.aggregate.servlet.ImageViewerServlet;
import org.odk.aggregate.submission.Submission;
import org.odk.aggregate.submission.SubmissionField;
import org.odk.aggregate.submission.SubmissionFieldType;
import org.odk.aggregate.submission.SubmissionRepeat;
import org.odk.aggregate.submission.SubmissionSet;
import org.odk.aggregate.submission.SubmissionValue;
import org.odk.aggregate.submission.type.GeoPoint;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;

public class SubmissionKml {
  
  private static final String LIMITATION_MSG = "limitation: image and title must be in the submission (top-level) or must be in the same repeat group";

  private class ColumnNamePair {
    private String columnName;
    private String displayName;
    
    public ColumnNamePair(String columnName, String displayName) {
      this.columnName = columnName;
      this.displayName = displayName;
    }

    public String getColumnName() {
      return columnName;
    }

    public String getDisplayName() {
      return displayName;
    }
  }
  
  private class DisplayPair {
    private String columnName;
    private String columnValue;
    
    public DisplayPair(String columnName, String displayName) {
      this.columnName = columnName;
      this.columnValue = displayName;
    }

    public String getColumnName() {
      return columnName;
    }

    public String getColumnValue() {
      return columnValue;
    }
  }
  
  private static final String IMAGE_VARIABLE = "__imgUrl";
  private static final String TITLE_VARIABLE = "__title";
  private static final String DATA_VARIABLE = "__data";
  private static final String VARIABLE_BEGIN = "$[";
  
  private static final int APPROX_ITEM_LENGTHS = 100;
  private static final int APPROX_TABLE_FORMATTING_LENGTH = 1000;
  private static final String OPEN_TABLE_W_HEADER_TABLE_FORMAT = "<table border='1' style='border-collapse: collapse;' >";
  private static final String OPEN_TABLE_W_PARENT_TABLE_FORMAT = "<table width='300' cellpadding='0' cellspacing='0'>";
  private static final String IMAGE_FORMAT = "<td align='center'><img style='padding:5px' src='" + VARIABLE_BEGIN + IMAGE_VARIABLE + BasicConsts.RIGHT_BRACKET + "'/></td>";   
  private static final String DATA_ITEM_TEMPLATE = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, HtmlUtil.wrapWithHtmlTags(HtmlConsts.B, " %s ")) + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, " %s ");
  private static final String DATA_ROW_TEMPLATE = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, DATA_ITEM_TEMPLATE);
  
  
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

  // TODO: Can add KML styling elements in BallonStyle
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
  
  
  private final String PLACEMARK_TEMPLATE = 
    "<Placemark id='%s'>\n" +
    "  <name>%s</name>\n" +
    "  <styleUrl>#odk_style</styleUrl>\n" +
    "  <Snippet maxLines='0'></Snippet>\n" +
    "  <ExtendedData>\n" + 
    "  %s" +
    "  %s" +
    "  %s" +
    "</ExtendedData>\n" +
    "%s</Placemark>\n"; //PLACEMARK_POINT_TEMPLATE goes in %s
  
  private final String PLACEMARK_POINT_TEMPLATE = 
    "  <Point>\n" +
    "    <coordinates>%s</coordinates>\n" +
    "  </Point>\n";
  
  public static final String KML_DATA_ELEMENT_TEMPLATE = "<Data name='%s'>\n"
    + "  <value>%s</value>\n" 
    + "</Data>\n";
  
  private String gpField;
  private String titleField;
  private String imgField;

  private String gpSubmissionSet;
  private String imgSubmissionSet;
  private String titleSubmissionSet;
  
  private String odkId;
  private EntityManager em;
  private Form form;
  private int fetchLimit;
  private String baseServerUrl;

  private Map<String, List<ColumnNamePair>> submissionSetColumns;
  
  private boolean gpsInRepeat;
  
  
  public SubmissionKml(String odkIdentifier, String webServerName, EntityManager entityManager,
      String geopointField, String nameField, String imageField, int maxQuery) throws ODKFormNotFoundException {
    odkId = odkIdentifier;
    em = entityManager;
    form = Form.retrieveForm(em, odkId);
    baseServerUrl = HtmlUtil.createUrl(webServerName);

    if(maxQuery > 0) {
      fetchLimit = maxQuery;
    } else {
      fetchLimit = TableConsts.QUERY_ROWS_MAX;
    }

    gpsInRepeat = geopointField.contains(BasicConsts.COLON);

    gpField = getColumnName(geopointField);
    titleField = getColumnName(nameField);
    imgField = getColumnName(imageField);

    gpSubmissionSet = getSubmissionSet(geopointField);
    imgSubmissionSet = getSubmissionSet(imageField);
    titleSubmissionSet = getSubmissionSet(nameField);

    submissionSetColumns = new HashMap<String, List<ColumnNamePair>>();

    List<ColumnNamePair> currentColumnPairs = new ArrayList<ColumnNamePair>();
    addSubmissionSetColumn(odkId, currentColumnPairs);

    processElementForColumnNames(form.getElementTreeRoot(), form.getElementTreeRoot(),
        BasicConsts.EMPTY_STRING, currentColumnPairs);

    if (!imgSubmissionSet.equals(odkId) && !titleSubmissionSet.equals(odkId)
        && !imgSubmissionSet.equals(titleSubmissionSet)) {
      throw new IllegalStateException(LIMITATION_MSG);
    }

  }

  private String getColumnName(String field) {
    int lastIndex = field.lastIndexOf(BasicConsts.COLON);
    String remaining = field;
    
    if(lastIndex > 0){    
      remaining = field.substring(lastIndex+1);
    }
    
    lastIndex = remaining.lastIndexOf(BasicConsts.DASH);
    if(lastIndex > 0){
      return remaining = remaining.substring(lastIndex+1);
    } 
    return remaining;
  }
  
  private String getSubmissionSet(String field) {
    String [] parts = field.split(BasicConsts.COLON);
    if(parts.length < 2) {
      return odkId;
    } else {
      return parts[parts.length-2]; // want the next to last as the last is the column name
    }
  }
  
  public void generateKml(Writer w) throws IOException, ODKIncompleteSubmissionData {
    List<Entity> submissionEntities = getEntities(TableConsts.EPOCH, false);

    w.write(String.format(PREAMBLE_TEMPLATE, odkId, odkId));
    w.write(generateStyle(imgField!=null));
    for (Entity subEntity : submissionEntities) {
      w.write(generatePlacemarks(subEntity));
    }
    w.write(POSTAMBLE_TEMPLATE);
  }


  private String generateStyle(boolean hasImage) {
    String styleHtml = OPEN_TABLE_W_PARENT_TABLE_FORMAT;
    styleHtml += wrapInBothRowNData(HtmlUtil.wrapWithHtmlTags(HtmlConsts.H2, wrapVariable(TITLE_VARIABLE)));
    if (hasImage) {
      styleHtml += HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, IMAGE_FORMAT);
    }
    styleHtml += wrapInBothRowNData(wrapVariable(DATA_VARIABLE));
    styleHtml += HtmlConsts.TABLE_CLOSE;

    return String.format(STYLE_TEMPLATE, "odk_style", styleHtml);
  }
  
  private String generatePlacemarks(Entity subEntity) throws ODKIncompleteSubmissionData {
    Submission sub = new Submission(subEntity, form);

    // data needed for the placemark
    GeoPoint gp = null;
    String title = null;
    String imageUrl = null;
    String id = null;

    // process submission base
    ArrayList<DisplayPair> data = new ArrayList<DisplayPair>();
    data.add(new DisplayPair(TableConsts.SUBMISSION_DATE_HEADER, sub.getSubmittedTime().toString()));
    processSubmissionSet(sub, submissionSetColumns.get(odkId), data, false);

    if (titleSubmissionSet.equals(odkId)) {
      title = getTitle(sub);
    }

    if (imgSubmissionSet.equals(odkId)) {
      imageUrl = getImageUrl(sub);
    }

    // process repeats
    if (gpsInRepeat) {
      // holder for the multiple generated placemarks
      StringBuilder placemarks = new StringBuilder();

      Map<String, SubmissionRepeat> repeatSets = getSubmissionSetRepeats(sub);

      // TODO: Should we really ignore any repeats that are at an equal level
      // FOR THE INITIAL IMPLEMENTATION REPEATS AT AN EQUAL LEVEL ARE THROWN
      // AWAY
      for (Map.Entry<String, SubmissionRepeat> entry : repeatSets.entrySet()) {
        SubmissionRepeat repeats = entry.getValue();
        String repeatName = entry.getKey();
        if (gpSubmissionSet.equals(repeatName)) {
          for (SubmissionSet repeat : repeats.getSubmissionSets()) {
            @SuppressWarnings("unchecked")
            List<DisplayPair> clonedData = (ArrayList<DisplayPair>) data.clone();
            // by definition of if statement the geopoint must be in this repeat
            gp = getGeoPoint(repeat);
            id = KeyFactory.keyToString(repeat.getKey());
            processSubmissionSet(repeat, submissionSetColumns.get(repeatName), clonedData, true);
            if (titleSubmissionSet.equals(repeatName)) {
              title = getTitle(repeat);
            }

            if (imgSubmissionSet.equals(repeatName)) {
              imageUrl = getImageUrl(repeat);
            }
            placemarks.append(generateFormattedPlacemark(clonedData, id, title, imageUrl, gp));
          }
        }
      }
      return placemarks.toString();
    } else {
      // by definition of if statement the geopoint must be in submission
      gp = getGeoPoint(sub);
      id = KeyFactory.keyToString(sub.getKey());

      Map<String, SubmissionRepeat> repeatSets = getSubmissionSetRepeats(sub);
      for (Map.Entry<String, SubmissionRepeat> entry : repeatSets.entrySet()) {
        SubmissionRepeat repeats = entry.getValue();
        String repeatName = entry.getKey();

        // TODO: this obviously causes a problem because there a multiple
        // possible values
        // INITIAL HACK IS TO ONLY THE VALUE FROM THE FIRST REPEAT
        if (titleSubmissionSet.equals(repeatName)) {
          title = getTitle(repeats.getSubmissionSets().first());
        }

        if (imgSubmissionSet.equals(repeatName)) {
          imageUrl = getImageUrl(repeats.getSubmissionSets().first());
        }

        for (SubmissionSet repeat : repeats.getSubmissionSets()) {
          processSubmissionSet(repeat, submissionSetColumns.get(repeatName), data, true);
        }
      }
      return generateFormattedPlacemark(data, id, title, imageUrl, gp);
    }
  }
  
  private Map<String, SubmissionRepeat> getSubmissionSetRepeats(SubmissionSet sub) {
    Map<String, SubmissionRepeat> repeatSets = new HashMap<String, SubmissionRepeat>();
    
    Map<String, SubmissionValue> valueMap = sub.getSubmissionValuesMap();
    for(Map.Entry<String, SubmissionValue> entry: valueMap.entrySet()) {
      SubmissionValue value = entry.getValue();
      if(value instanceof SubmissionRepeat) {
        repeatSets.put(entry.getKey(), (SubmissionRepeat)value);
      }
    }
    return repeatSets;
  }

  
  private String getTitle(SubmissionSet set) {
    Map<String, SubmissionField<?>> fieldMap = set.getSubmissionFieldsMap();
    SubmissionField<?> field = fieldMap.get(titleField);
    if(field == null) {
      return null;
    }
    
    Object value = field.getValue();
    if(value == null) {
      return null;
    }
    
    return value.toString();
  }
  
  private GeoPoint getGeoPoint(SubmissionSet set) {
    Map<String, SubmissionField<?>> fieldMap = set.getSubmissionFieldsMap();
    SubmissionField<?> field = fieldMap.get(gpField);
    if(field == null) {
      return null;
    }
    
    Object value = field.getValue();
    if(value instanceof GeoPoint) {
      return (GeoPoint)value;
    } else {
      return null;
    }
  }

  private String getImageUrl(SubmissionSet set) {
    Map<String, SubmissionField<?>> fieldMap = set.getSubmissionFieldsMap();
    SubmissionField<?> field = fieldMap.get(imgField);
    if(field == null) {
      return null;
    }
    
    if (field.isBinary()) {
      Object value = field.getValue();
      if (value instanceof Key) {
        Key blobKey = (Key) value;
        Map<String, String> properties = createViewLinkProperties(blobKey);
        return HtmlUtil.createLinkWithProperties(baseServerUrl + ImageViewerServlet.ADDR, properties);     
      } else {
        System.err.println(ErrorConsts.NOT_A_KEY);
      }
    }
    return null;
  }
  
  private void processSubmissionSet(SubmissionSet set, List<ColumnNamePair> columns, List<DisplayPair> data, boolean repeated) {
    Map<String, SubmissionField<?>> fieldMap = set.getSubmissionFieldsMap();
    for (ColumnNamePair column : columns) {
      SubmissionField<?> field = fieldMap.get(column.getColumnName());
      String displayName = column.getDisplayName() + (repeated ? set.getOrder() : BasicConsts.EMPTY_STRING);
      if (field == null) {
        data.add(new DisplayPair(displayName, BasicConsts.EMPTY_STRING));
        continue;
      }
      Object value = field.getValue();
      if (value == null) {
        data.add(new DisplayPair(displayName, BasicConsts.EMPTY_STRING));
      } else if(value instanceof GeoPoint) {
        GeoPoint geoPoint = (GeoPoint)value;
        
        Double latitude = geoPoint.getLatitude();
        String latName = displayName + BasicConsts.DASH + BasicConsts.LATITUDE;
        data.add(new DisplayPair(latName, (latitude != null ? Double.toString(latitude) : BasicConsts.EMPTY_STRING)));
        
        Double longitude = geoPoint.getLongitude();
        String longName = displayName + BasicConsts.DASH + BasicConsts.LONGITUDE;
        data.add(new DisplayPair(longName, (longitude != null ? Double.toString(longitude) : BasicConsts.EMPTY_STRING)));
        
        Double altitude = geoPoint.getAltitude();
        String altName = displayName + BasicConsts.DASH + BasicConsts.ALTITUDE;
        data.add(new DisplayPair(altName, (altitude != null ? Double.toString(altitude) : BasicConsts.EMPTY_STRING)));

        Double accuracy = geoPoint.getAccuracy();
        String accName = displayName + BasicConsts.DASH + BasicConsts.ACCURACY;
        data.add(new DisplayPair(accName, (accuracy != null ? Double.toString(accuracy) : BasicConsts.EMPTY_STRING)));
      } else if (field.isBinary()) {
        if (value instanceof Key) {
          Key blobKey = (Key) value;
          Map<String, String> properties = createViewLinkProperties(blobKey);
          String url = HtmlUtil.createHrefWithProperties(baseServerUrl + ImageViewerServlet.ADDR, properties, "View");
          data.add(new DisplayPair(displayName, url));
        } else {
          System.err.println(ErrorConsts.NOT_A_KEY);
        }
      } else {
        data.add(new DisplayPair(displayName, value.toString()));
      }
    }
  }
  
  private String generateFormattedPlacemark(List<DisplayPair> items, String identifier,
      String title, String imageURL, GeoPoint gp) {

    // make sure no null values slip by
    String id = (identifier == null) ? BasicConsts.EMPTY_STRING : identifier;
    String name = (title == null) ? BasicConsts.EMPTY_STRING : title;

    // determine what data values to create
    String titleStr = (title == null) ? BasicConsts.EMPTY_STRING : generateDataElement(TITLE_VARIABLE, title);
    String imgStr = (imageURL == null) ? BasicConsts.EMPTY_STRING : generateDataElement(IMAGE_VARIABLE, imageURL);
    
    
    // create data section
    String dataStr = BasicConsts.EMPTY_STRING;
    if(!items.isEmpty()) {
      StringBuilder formattedDataStr = new StringBuilder(APPROX_TABLE_FORMATTING_LENGTH
        + items.size() * APPROX_ITEM_LENGTHS);   
      createFormattedDataTable(formattedDataStr, items);
      dataStr = generateDataElement(DATA_VARIABLE, formattedDataStr.toString());    
    }
    
    // Create Geopoint
    String geopoint = BasicConsts.EMPTY_STRING;
    if (gp != null) {
      if (gp.getLatitude() != null && gp.getLongitude() != null) {
        Double altitude = 0.0;
        if (gp.getAltitude() != null) {
          altitude = gp.getAltitude();
        }
        geopoint = String.format(PLACEMARK_POINT_TEMPLATE, gp.getLongitude() + BasicConsts.COMMA
            + gp.getLatitude() + BasicConsts.COMMA + altitude);
      }
    }

    return String.format(PLACEMARK_TEMPLATE, StringEscapeUtils.escapeXml(id), StringEscapeUtils
        .escapeXml(name), titleStr, imgStr, dataStr, geopoint);
  }

  private void createFormattedDataTable(StringBuilder out, List<DisplayPair> items) {
    out.append(OPEN_TABLE_W_HEADER_TABLE_FORMAT);
    for (DisplayPair item : items) {
      String tmp = String.format(DATA_ROW_TEMPLATE, item.getColumnName(), item.getColumnValue());
      out.append(tmp);
    }
    out.append(HtmlConsts.TABLE_CLOSE);
  }

  private Map<String, String> createViewLinkProperties(Key subKey) {
    Map<String, String> properties = new HashMap<String, String>();
    properties.put(ServletConsts.BLOB_KEY, KeyFactory.keyToString(subKey));
    return properties;
  }

  /**
   * Generates a result table that contains all the submission data of the form
   * specified by the ODK ID
   * 
   * @return a result table containing submission data
   * 
   * @throws ODKIncompleteSubmissionData
   */
  protected List<Entity> getEntities(Date lastDate, boolean backward) {

    // retrieve submissions
    Query surveyQuery = new Query(odkId);
    if (backward) {
      surveyQuery
          .addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.DESCENDING);
      surveyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG,
          Query.FilterOperator.LESS_THAN, lastDate);
    } else {
      surveyQuery.addSort(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG, Query.SortDirection.ASCENDING);
      surveyQuery.addFilter(PersistConsts.SUBMITTED_TIME_PROPERTY_TAG,
          Query.FilterOperator.GREATER_THAN, lastDate);
    }

    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    List<Entity> submissionEntities = ds.prepare(surveyQuery).asList(
        FetchOptions.Builder.withLimit(fetchLimit));

    return submissionEntities;
  }
  

  /**
   * Helper function to recursively go through the element tree and create
   * the column headings
   * 
   */
  private void processElementForColumnNames(FormElement node, FormElement root, String parentName, List<ColumnNamePair> columnPairs) {    
    if (node == null) return;

    List<ColumnNamePair> currentColumnPairs = columnPairs;
    
    if (!node.getSubmissionFieldType().equals(SubmissionFieldType.UNKNOWN)) {
      ColumnNamePair pair = new ColumnNamePair(node.getElementName(), parentName + node.getElementName());
      currentColumnPairs.add(pair);
    } else if(node.isRepeatable()) {
      parentName = parentName + node.getElementName() + BasicConsts.COLON + BasicConsts.SPACE;
      currentColumnPairs = new ArrayList<ColumnNamePair>();
      addSubmissionSetColumn(node.getElementName(), currentColumnPairs);
    } else {
      if(node != root) { 
        parentName = parentName + node.getElementName() + BasicConsts.DASH;
      }
    }
    
    List<FormElement> childDataElements = node.getChildren();
    if (childDataElements == null) {
      return;
    }
    for (FormElement child : childDataElements) {
      processElementForColumnNames(child, root, parentName, currentColumnPairs);
    }
  }
  
  private void addSubmissionSetColumn(String submissionSetID, List<ColumnNamePair> columnNamePairs) {
    submissionSetColumns.put(submissionSetID, columnNamePairs);
  }
  
  private String generateDataElement(String name, String value){
    return String.format(KML_DATA_ELEMENT_TEMPLATE, StringEscapeUtils.escapeXml(name), StringEscapeUtils.escapeXml(value));
  }
  
  private String wrapVariable(String variable) {
    return VARIABLE_BEGIN + variable + BasicConsts.RIGHT_BRACKET;
  }
  
  private String wrapInBothRowNData(String value) {
    return HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, HtmlUtil.wrapWithHtmlTags(
        HtmlConsts.TABLE_DATA, value));
  }
}
