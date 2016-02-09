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
package org.opendatakit.aggregate.constants.format;

import org.opendatakit.aggregate.constants.HtmlUtil;
import org.opendatakit.common.web.constants.BasicConsts;
import org.opendatakit.common.web.constants.HtmlConsts;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public final class KmlConsts {

  public static final String OPEN_TABLE_W_HEADER_TABLE_FORMAT = "<table border='1' style='border-collapse: collapse;' >";

  public static final String DATA_VARIABLE = "__data";
  public static final String TITLE_VARIABLE = "__title";
  public static final String IMAGE_VARIABLE = "__imgUrl";

  public static final String NO_IMAGE_PLACEMARK_STYLE = "odk_no_image_placemark_style";
  public static final String WITH_IMAGE_PLACEMARK_STYLE = "odk_with_image_placemark_style";
  public static final String GEOSHAPE_STYLE = "odk_geoshape_style";
  
  public static final String KML_PREAMBLE_TEMPLATE = 
  "<?xml version='1.0' encoding='UTF-8'?>\n" +
  "<kml xmlns='http://www.opengis.net/kml/2.2' xmlns:gx='http://www.google.com/kml/ext/2.2' xmlns:kml='http://www.opengis.net/kml/2.2' xmlns:atom='http://www.w3.org/2005/Atom'>\n" +
  "<Document id='%s'>\n" +
  "  <name>%s</name>\n" +
  "  <open>1</open>\n" +
  "  <Snippet maxLines='0'></Snippet>\n" +
  "  <description>KML file showing results from ODK form: %s</description>\n";
  
  public static final String KML_POSTAMBLE_TEMPLATE = 
  "  </Document>\n" + 
  "</kml>";
  
  private static final String VARIABLE_BEGIN = "$[";

  private static final String TITLE_VAR_DEFN = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, 
      HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, 
      HtmlUtil.wrapWithHtmlTags(HtmlConsts.H2, VARIABLE_BEGIN + TITLE_VARIABLE + BasicConsts.RIGHT_BRACKET)));
 
  private static final String IMAGE_FORMAT = "<td align='center'><img style='padding:5px' height='144px' src='" + KmlConsts.VARIABLE_BEGIN + KmlConsts.IMAGE_VARIABLE + BasicConsts.RIGHT_BRACKET + "'/></td>";

  private static final String IMG_VAR_DEFN = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, KmlConsts.IMAGE_FORMAT);
  
  private static final String DATA_VAR_DEFN = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW, 
      HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, VARIABLE_BEGIN + DATA_VARIABLE + BasicConsts.RIGHT_BRACKET));
  
  private static final String KML_BALLON_STYLE_TEMPLATE =
  "<Style id='%s'>\n" +
  "  <BalloonStyle>\n" +
  "    <text>\n" +
  "      <![CDATA[" +  "<table width='300' cellpadding='0' cellspacing='0'>" + TITLE_VAR_DEFN + "%s" + DATA_VAR_DEFN + HtmlConsts.TABLE_CLOSE +"]]>\n" +
  "    </text>\n" +
  "  </BalloonStyle>\n" +
  "</Style>\n";
  
  public static final String KML_BALLON_NO_IMAGE_STYLE_DEFN = String.format(KML_BALLON_STYLE_TEMPLATE, NO_IMAGE_PLACEMARK_STYLE, BasicConsts.EMPTY_STRING);
  
  public static final String KML_BALLON_WITH_IMAGE_STYLE_DEFN = String.format(KML_BALLON_STYLE_TEMPLATE, WITH_IMAGE_PLACEMARK_STYLE, IMG_VAR_DEFN);
  
  public static final String KML_GEOSHAPE_STYLE_TEMPLATE =
  "<Style id='" + GEOSHAPE_STYLE + "'>\n" +
  "  <LineStyle>\n" +
  "    <color>7f00ffff</color>\n" +
  "    <width>4</width>\n" +
  "  </LineStyle>\n" +
  "  <PolyStyle>\n" +
  "    <color>7f00ff00</color>\n" +
  "  </PolyStyle>\n" +
  "</Style>\n";
  
  public static final String KML_PLACEMARK_TEMPLATE = 
  "<Placemark id='%s'>\n" +
  "  <name>%s</name>\n" +
  "  <styleUrl>#%s</styleUrl>\n" +
  "  <Snippet maxLines='0'></Snippet>\n" +
  "  <ExtendedData>\n" + 
  "  %s" +
  "  %s" +
  "  %s" +
  "</ExtendedData>\n" +
  "%s</Placemark>\n"; //PLACEMARK_POINT_TEMPLATE goes in %s
  
  public static final String KML_PLACEMARK_POINT_TEMPLATE = 
  "  <Point>\n" +
  "    <coordinates>%s</coordinates>\n" +
  "  </Point>\n";
  
  public static final String KML_DATA_TEMPLATE = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA,
      HtmlUtil.wrapWithHtmlTags(HtmlConsts.B, " %s ")) + HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_DATA, " %s ");
  
  public static final String KML_DATA_ITEM_TEMPLATE = HtmlUtil.wrapWithHtmlTags(HtmlConsts.TABLE_ROW,
      KML_DATA_TEMPLATE);
  
  public static final String KML_DATA_ELEMENT_TEMPLATE = "<Data name='%s'>\n"
    + "  <value>%s</value>\n" 
    + "</Data>\n";

  public static final String KML_LINE_STRING_PLACEMARK_TEMPLATE = "<Placemark id='%s'>\n" +
      "  <name>%s</name>\n" +
      "  <styleUrl>#" + GEOSHAPE_STYLE + "</styleUrl>\n" +
      "  <LineString>\n" +
      "    <extrude>1</extrude>\n" +
      "    <altitudeMode>clampToGround</altitudeMode>\n" +
      "    <coordinates>\n" +
      "     %s\n" +
      "    </coordinates>\n" +
      "  </LineString>\n " +
      "</Placemark>\n"; 

}
