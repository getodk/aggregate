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

public enum BookHelpConsts {

  XFORM("Forms",
      "https://www.youtube.com/embed/Zsa-4pUzULo",
      "Forms are tables which organize your data into rows and columns.  You can view your " +
          "forms on \"Submissions\" -> \"Filter Submissions\".<br><br>" +
          "What is \"Downloadable\"? (\"Form Management\" -> \"Forms List\")<br>" +
          "&nbsp;This setting allows remote clients to download the form. <br><br>" +
          "What is \"Accept Submissions\"? (\"Form Management\" -> \"Forms List\")<br>" +
          "&nbsp;This setting allows the form to accept data while it is in Aggregate.", "How do I add a new form? (Press \"New Form\" on \"Form Management\" -> \"Forms List\")<br>" +
      "&nbsp;1.  Form Definition - required.  Please choose the .xml file that will be used.<br>" +
      "&nbsp;2.  Media File(s) - optional.  Choose the appropriate media files for the form."),

  SUBMISSIONS("Submissions",
      null,
      "Submissions are the data contained in a form.  One submission corresponds to one row in the " +
          "table.", "How do I upload data? (Press \"Upload Data\" on \"Form Management\" -> \"Forms List\")<br>" +
      "&nbsp;1.  Form Definition - required.  Please choose the .xml file that will be used.<br>" +
      "&nbsp;2.  Media File(s) - optional.  Choose the appropriate media files for the form."),

  FILTER("Filters",
      "https://www.youtube.com/embed/d4RquOGfcI0",
      "Filters give you the ability to see a subset of your data.  For example, if you wanted to " +
          "only look at males over the age of 35, you would include 2 filters: <br>" +
          "&nbsp;1.  Display Rows where column Gender EQUAL male <br>" +
          "&nbsp;2.  Display Rows where column Age GREATER_THAN 35<br>" +
          "If you have multiple filters applied at once, then you have a filter group. <br><br>" +
          "If you were more curious about viewing the distribution of locations, you could do one " +
          "filter:<br>" +
          "&nbsp;1.  Display columns location", "How do I add a filter? (Press \"Add Filter\" on " +
      "\"Submissions\" -> \"Filter Submissions\") <br>" +
      "&nbsp;1.  Display/Hide - Will you be selecting data to show or hide?<br>" +
      "&nbsp;2.  Rows/Columns - Choose whether you will be working with the rows or columns of the table.<br>" +
      "&nbsp;3a.  If you selected Rows:<br>" +
      "&nbsp;&nbsp;a.  Pick the column that you want to evaluate for all rows.<br>" +
      "&nbsp;&nbsp;b.  Pick the operation you would like to use.<br>" +
      "&nbsp;&nbsp;c.  Pick a value to use for the evaluation.<br>" +
      "&nbsp;3b.  If you selected Columns:<br>" +
      "&nbsp;&nbsp;a.  Pick the columns you want to either display or hide.<br>" +
      "&nbsp;4.  Click \"Apply Filter\".<br>" +
      "Unless you save it, it will be temporary.<br><br>" +
      "How do I save the filter group? (Press \"Save\" on \"Submissions\" -> \"Filter Submissions\") <br>" +
      "&nbsp;1.  Please fill in the desired name.<br>" +
      "&nbsp;2.  Press \"OK\"."),

  METADATA("Metadata",
      null,
      "Metadata provides information about the submissions being submitted.  There will be " +
          "information such as date submitted, if the data is complete, version numbers, " +
          "and id numbers", "You can toggle whether or not you view metadata by clicking the \"Display Metadata\"" +
      "checkbox on \"Submissions\" -> \"Filter Submissions\"."),

  VISUALIZE("Visualize",
      "https://www.youtube.com/embed/WyJD2vDc5Cg",
      "Aggregate provides a simple web interface for basic data " +
          "visualization.  <br>You can view your data in a bar graph, pie chart," +
          " or on a map.  <br>This Visualize functionality is meant to provide a" +
          " quick means to view early data results <rb>in meaningful ways but is" +
          " not meant to provide complex data analysis functionality. ",
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

  EXPORT("Export",
      "https://www.youtube.com/embed/2VzgWVnM8lM",
      "Exporting your data allows you to view the information in another software tool.  This" +
          "can allow you to do more comprehensive work with your data.  You can export your file to" +
          "a CSV file (Excel) or a KML file (Google Maps).", "How do I export a file? (Press \"Export\" on \"Submissions\" -> \"Filter Submissions\" " +
      "or on \"Form Management\" -> \"Forms List\")<br>" +
      "&nbsp;1.  Choose whether you want to export to a .csv file, or a .kml file.<br>" +
      "&nbsp;2a.  If you choose CSV, just press \"Export\".<br>" +
      "&nbsp;2b.  If you choose KML:<br>" +
      "&nbsp;&nbsp;a.  Select the type of geographical data you are using.<br>" +
      "&nbsp;&nbsp;b.  Select the column that you want to map.<br>" +
      "&nbsp;&nbsp;c.  Choose the type of picture for your map.  This will be displayed in the balloon on the map.<br>" +
      "&nbsp;&nbsp;d.  Press \"Export\"."),

  PUBLISH("Publish",
      "https://www.youtube.com/embed/Vfl0WxR-xcw",
      "When you publish your data, it will be uploaded to a software tool: Google Fusion Tables or " +
          "Google Spreadsheets.  You can display data before a certain time, after a certain time, or " +
          "stream all of the data.  This will allow others to work with the data to do analysis.",
      "How do I publish my data? (Press \"Publish\" on \"Submissions\" -> \"Filter Submissions\" " +
          "or on \"Form Management\" -> \"Forms List\")<br>" +
          "&nbsp;1.  Choose whether you want your data in a Google Fusion Table or a Google Spreadsheet.<br>" +
          "&nbsp;2a.  If you chose Google Fusion Table:<br>" +
          "&nbsp;&nbsp;a.  Choose whether you would like to upload only, stream only, or both.<br>" +
          "&nbsp;&nbsp;&nbsp;1.  Upload only - This will take the current table and send it to the service.  No new data will be sent.<br>" +
          "&nbsp;&nbsp;&nbsp;2.  Stream only - This will only send new data after the service is created.  No old data will be sent." +
          "&nbsp;&nbsp;&nbsp;3.  Both will send both old and new data.<br>" +
          "&nbsp;&nbsp;b.  Press \"Publish\".<br>" +
          "&nbsp;&nbsp;c.  Press \"Grant Access\" so that ODK Aggregate is allowed to make the file.<br>" +
          "&nbsp;&nbsp;d.  You can view your published document in Google Fusion Tables.<br>" +
          "&nbsp;2b.  If you chose Google Spreadsheet:<br>" +
          "&nbsp;&nbsp;a.  Enter the desired name of the spreadsheet.<br>" +
          "&nbsp;&nbsp;b.  Choose whether you would like to upload only, stream only, or both.<br>" +
          "&nbsp;&nbsp;&nbsp;1.  Upload only - This will take the current table and send it to the service.  No new data will be sent.<br>" +
          "&nbsp;&nbsp;&nbsp;2.  Stream only - This will only send new data after the service is created.  No old data will be sent.<br>" +
          "&nbsp;&nbsp;&nbsp;3.  Both will send both old and new data.<br>" +
          "&nbsp;&nbsp;c.  Press \"Publish\".<br>" +
          "&nbsp;&nbsp;d.  Press \"Grant Access\" so that ODK Aggregate is allowed to make the file.<br>" +
          "&nbsp;&nbsp;e.  You can view your published document in Google Docs."),
  HELP("Help",
      "https://www.youtube.com/embed/Y9kmJeMpY38",
      "Aggregate provides three kinds of help accessible by pressing one of three buttons in the upper righthand corner.",
      "&nbsp;1.  The red question mark will give you instructions for the tab you are currently viewing. When you click the button, a help panel will appear at the bottom of the screen. To hide the help panel, simply click the red question mark again. <br>" +
          "&nbsp;2.  The green book will give you the most comprehensive help.  When you click the button, a popup will appear providing detailed information as well as video instruction.<br>" +
          "&nbsp;3.  The blue balloon increases the amount of detail that appears that describes the button's functionality when you hover over most buttons.<br>"),
  PERMISSIONS("Permissions",
      null,
      "", "");

  private String title;
  private String concept;
  private String procedures;
  private String videoUrl;

  private BookHelpConsts(String title, String helpVidUrl, String concept, String procedures) {
    this.title = title;
    this.videoUrl = helpVidUrl;
    this.concept = concept;
    this.procedures = procedures;
  }

  public String getTitle() {
    return title;
  }

  public String getConcept() {
    return concept;
  }

  public String getProcedures() {
    return procedures;
  }

  public String getVideoUrl() {
    return videoUrl;
  }
}
