/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.constants.common;


public enum FormConsts implements HelpSliderConsts {
  NEW("New Form",
      "1.  Form Definition - required.  Please choose the .xml file that will be used.<br>" +
          "2.  Media File(s) - optional.  Choose the appropriate media files for the form."),
  UPLOAD("Upload Data",
      "1.  Form Definition - required.  Please choose the .xml file that will be used.<br>" +
          "2.  Media File(s) - optional.  Choose the appropriate media files for the form."),
  TABLE("Understanding the Table:",
      "1.  Title - Click on the link to view the XML for the form.<br>" +
          "2.  Form Id - This is the unique name for the form.<br>" +
          "3.  Media Files - This displays the count of media files you have uploaded for the form.<br>" +
          "4.  User - This is the user who uploaded the form.<br>" +
          "5.  Downloadable - This enables/disables the Aggregate from displaying the form to remote clients so that they can download the " +
          "form<br>" +
          "6.  Accept Submissions - This enables/disables Aggregate ability to accept submissions for the particular form. This is helpful " +
          "if you want to prevent users from submitting more data."),
  PUBLISH("Publish", "This allows you to view your data in a Google Fusion Table or a Google Spreadsheet.<br>" +
      "1.  Choose whether you want your data in a Google Fusion Table or a Google Spreadsheet.<br>" +
      "2a.  If you chose Google Fusion Table:<br>" +
      "   a.  Choose whether you would like to upload only, stream only, or both.<br>" +
      "       1.  Upload only - This will take the current table and send it to the service.  No new data will be sent.<br>" +
      "       2.  Stream only - This will only send new data after the service is created.  No old data will be sent." +
      "       3.  Both will send both old and new data.<br>" +
      "   b.  Press \"Publish\".<br>" +
      "   c.  Press \"Grant Access\" so that ODK Aggregate is allowed to make the file.<br>" +
      "   d.  You can view your published document in Google Fusion Tables.<br>" +
      "2b.  If you chose Google Spreadsheet:<br>" +
      "   a.  Enter the desired name of the spreadsheet.<br>" +
      "   b.  Choose whether you would like to upload only, stream only, or both.<br>" +
      "       1.  Upload only - This will take the current table and send it to the service.  No new data will be sent.<br>" +
      "       2.  Stream only - This will only send new data after the service is created.  No old data will be sent.<br>" +
      "       3.  Both will send both old and new data.<br>" +
      "   c.  Press \"Publish\".<br>" +
      "   d.  Press \"Grant Access\" so that ODK Aggregate is allowed to make the file.<br>" +
      "   e.  You can view your published document in Google Docs."),
  EXPORT("Export", "This allows you to view your data in either Microsoft Excel, or in a Google Map.<br>" +
      "1.  Choose whether you want to export to a .csv file, or a .kml file.<br>" +
      "2a.  If you choose CSV, just press \"Export\".<br>" +
      "2b.  If you choose KML:<br>" +
      "   a.  Select the type of geographical data you are using.<br>" +
      "   b.  Select the column that you want to map.<br>" +
      "   c.  Choose the type of picture for your map.  This will be displayed in the balloon on the map.<br>" +
      "   d.  Press \"Export\"."),
  DELETE("Delete", "Press this if you choose to remove the form.");

  private String title;
  private String content;

  private FormConsts(String titleString, String contentString) {
    title = titleString;
    content = contentString;
  }

  public String getTitle() {
    return title;
  }

  public String getContent() {
    return content;
  }
}