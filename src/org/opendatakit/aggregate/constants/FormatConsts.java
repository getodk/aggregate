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

package org.opendatakit.aggregate.constants;

import org.opendatakit.common.constants.BasicConsts;


/**
 * Constants used for formatting data
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class FormatConsts {
  
  // formatting constants
  public static final String CSV_DELIMITER = BasicConsts.COMMA;
  public static final String HEADER_CONCAT = BasicConsts.COLON;
  public static final String JSON_VALUE_DELIMITER = BasicConsts.COMMA;

  // gps coordinate constants
  public static final String GEO_POINT = "GeoPoint";
  public static final String ACCURACY = "Accuracy";
  public static final String ALTITUDE = "Altitude";
  public static final String LONGITUDE = "Longitude";
  public static final String LATITUDE = "Latitude";

  // button text
  public static final String SUBMISSION_BUTTON_TXT = "Create Test Submission";
  public static final String XML_BUTTON_TXT = "View XML";
  public static final String CSV_BUTTON_TXT = "Download CSV";
  public static final String RESULTS_BUTTON_TXT = "View Submissions";
  public static final String EXTERNAL_SERVICE_BUTTON_TXT = "External Service Connection";
  public static final String KML_BUTTON_TXT = "Create KML File";

  // form table headers
  public static final String FT_HEADER_SUBMISSION = "Test Entry/Submission";
  public static final String FT_HEADER_XFORM = "Xform Definition";
  public static final String FT_HEADER_CSV = "Submissions in CSV";
  public static final String FT_HEADER_RESULTS = "Submission Results";
  public static final String FT_HEADER_USER = "User";
  public static final String FT_HEADER_ODKID = "Identifier";
  public static final String FT_HEADER_NAME = "Name";
  public static final String FT_HEADER_KML = "KML File";
  public static final String FT_HEADER_EXTERNAL_SERVICE = "Send Submissions to External Service";

  // submission headers
  public static final String SUBMISSION_DATE_HEADER = "SubmissionDate";
  public static final String SUBMISSION_ID_HEADER = "SubmissionId";

  // link text
  public static final String VIEW_LINK_TEXT = "View";

  // xml form list tags
  public static final String URL_ATTR = "url";
  public static final String FORMS_TAG = "forms";
  public static final String FORM_TAG = "form";
  public static final String BEGIN_FORMS_TAG = HtmlUtil.createBeginTag(FORMS_TAG);
  public static final String END_FORMS_TAG = HtmlUtil.createEndTag(FORMS_TAG);
  public static final String END_FORM_TAG = HtmlUtil.createEndTag(FORM_TAG);
  public static final int QUERY_ROWS_MAX = 990;

  public static final String FT_HEADER_QUERY = "Query Results";
  public static final String QUERY_BUTTON_TXT = "Query";

  public static final String TO_STRING_DELIMITER = ": ";

  public static final String KML_PREAMBLE_TEMPLATE = "<?xml version='1.0' encoding='UTF-8'?>\n"
      + "<kml xmlns='http://www.opengis.net/kml/2.2' xmlns:gx='http://www.google.com/kml/ext/2.2' xmlns:kml='http://www.opengis.net/kml/2.2' xmlns:atom='http://www.w3.org/2005/Atom'>\n"
      + "<Document id='odk_kml'>\n" 
      + "  <name>%s</name>\n" 
      + "  <open>1</open>\n"
      + "  <Snippet maxLines='0'></Snippet>\n"
      + "  <description>KML file showing results from ODK form: %s</description>\n";
  
  public static final String KML_POSTAMBLE_TEMPLATE = "  </Document>\n" 
      + "</kml>";

  public static final String KML_STYLE_TEMPLATE = "<Style id='%s'>\n" 
      + "  <BalloonStyle>\n"
      + "    <text>\n" 
      + "      <![CDATA[\n" 
      + "        %s\n" 
      + "      ]]>\n" 
      + "    </text>\n"
      + "  </BalloonStyle>\n" 
      + "</Style>\n";

  public static final String KML_PLACEMARK_POINT_TEMPLATE = "  <Point>\n"
      + "    <coordinates>%s</coordinates>\n" 
      + "  </Point>\n";
  
  public static final String KML_PLACEMARK_TEMPLATE = "<Placemark id='%s'>\n"
      + "  <name>%s</name>\n" 
      + "  <styleUrl>#odk_style</styleUrl>\n"
      + "  <Snippet maxLines='0'></Snippet>\n" 
      + "  <ExtendedData>\n" 
      + "  %s</ExtendedData>\n"
      + "%s</Placemark>\n"; // PLACEMARK_POINT_TEMPLATE goes in %s

}
