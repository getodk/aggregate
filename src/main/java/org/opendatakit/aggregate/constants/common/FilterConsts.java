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


public enum FilterConsts implements HelpSliderConsts {
  ADD("Add Filter", "This adds a filter to the data.  Unless you save it, it will be temporary.<br>" +
      "1.  Display/Hide - Will you be selecting data to show or hide?<br>" +
      "2.  Rows/Columns - Choose whether you will be working with the rows or columns of the table.<br>" +
      "3a.  If you selected Rows:<br>" +
      "   a.  Pick the column that you want to evaluate for all rows.<br>" +
      "   b.  Pick the operation you would like to use.<br>" +
      "   c.  Pick a value to use for the evaluation.<br>" +
      "3b.  If you selected Columns:<br>" +
      "   a.  Pick the columns you want to either display or hide.<br>" +
      "4.  Click \"Apply Filter\"."),
  SAVE("Save", "This will save a filter for future use.  You must have at least one filter in order to save.<br>" +
      "1.  Please fill in the desired name.<br>" +
      "2.  Press \"OK\"."),
  VISUALIZE("Visualize",
      "Aggregate provides a simple web interface for basic data " +
          "visualization.<br>You can view your data in a bar graph, pie chart," +
          " or on a map.<br>This Visualize functionality is meant to provide a" +
          " quick means to view early data results <br>in meaningful ways but is" +
          " not meant to provide complex data analysis functionality.<br><br>" +
          "How do I visualize the data? (" +
          "Press \"Visualize\" on \"Submissions\" -> \"Filter Submissions\")" +
          "<br>" +
          "&nbsp;1.  Choose whether you want to view a Pie Chart, Bar Graph," +
          " or a Map.<br>" +
          "&nbsp;2a.  If you choose Pie Chart:<br>" +
          "&nbsp;&nbsp;a.  Choose whether you would like to Count or Sum data." +
          "<br>" +
          "&nbsp;&nbsp;&nbsp;Count - Counts the number of times a unique" +
          " answer occurs in the specified column.<br>" +
          "&nbsp;&nbsp;&nbsp;1. Select the column that you want to count.<br>" +
          "&nbsp;&nbsp;&nbsp;Sum - Sums the values in one column grouped" +
          " together by a value in another column.<br>" +
          "&nbsp;&nbsp;&nbsp;1.  Select the column of values that you want to" +
          "add.<br>" +
          "&nbsp;&nbsp;&nbsp;2.  Select the column of values that you want to" +
          "use to group the numbers.<br>" +
          "&nbsp;&nbsp;c.  Press \"Pie It\".<br>" +
          "&nbsp;2b.  If you choose Bar Graph:<br>" +
          "&nbsp;&nbsp;a.  Choose whether you would like to Count or Sum data." +
          "<br>" +
          "&nbsp;&nbsp;&nbsp;Count - Counts the number of times a unique" +
          " answer occurs in the specified column.<br>" +
          "&nbsp;&nbsp;&nbsp;1. Select the column that you want to count.<br>" +
          "&nbsp;&nbsp;&nbsp;Sum - Sums the values in one column grouped" +
          " together by a value in another column.<br>" +
          "&nbsp;&nbsp;&nbsp;1.  Select the column of values that you want to" +
          "add.<br>" +
          "&nbsp;&nbsp;&nbsp;2.  Select the column of values that you want to" +
          "use to group the numbers.<br>" +
          "&nbsp;&nbsp;c.  Press \"Bar It\".<br>" +
          "&nbsp;2c.  If you choose Map:<br>" +
          "&nbsp;&nbsp;a.  Select the column that you want to map.<br>" +
          "&nbsp;&nbsp;b.  Press \"Map It\"." +
          "&nbsp;&nbsp;c.  You can click on a point to view a balloon with the" +
          " other information supplied in the table."),
  EXPORT("Export", "This allows you to view your data in either Microsoft Excel, or in a Google Map.<br>" +
      "1.  Choose whether you want to export to a .csv file, or a .kml file.<br>" +
      "2a.  If you choose CSV, just press \"Export\".<br>" +
      "2b.  If you choose KML:<br>" +
      "   a.  Select the type of geographical data you are using.<br>" +
      "   b.  Select the column that you want to map.<br>" +
      "   c.  Choose the type of picture for your map.  This will be displayed in the balloon on the map.<br>" +
      "   d.  Press \"Export\"."),
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
      "   e.  You can view your published document in Google Docs.");

  private String title;
  private String content;

  private FilterConsts(String titleString, String contentString) {
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