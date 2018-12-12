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

public enum PublishConsts implements HelpSliderConsts {
  PUBLISH("This is a view of the published data you have created for a particular form.",
      "Select the form corresponding to the published data."),
  TABLE("Understanding the table:",
      "1.  \"Purge Published Data\" - Read the message that appears and press Purge Data.<br>" +
          "2.  Created By - The email of the user who created the published file.<br>" +
          "3.  Status<br>" +
          "   a.  ACTIVE - the file is ready to view.<br>" +
          "   b.  ESTABLISHED - something went wrong in the process of exporting.<br>" +
          "4.  Start Date - This shows the time when you finished filling out the \"Publish\" form.<br>" +
          "5.  Action - This is based on your selection of upload only, stream only, or both in the \"Publish\" form.<br>" +
          "6.  Type - This is either a Google Spreadsheet or a Google Fusion Table.<br>" +
          "7.  Name<br>" +
          "   a.  If this is a Google Spreadsheet, it will be the name you chose.<br>" +
          "   b.  If this is a Google Fusion Table, click on the link to view the Fusion Table.<br>" +
          "8.  Delete - Complete this if you want to delete your published file.");

  private String title;
  private String content;

  private PublishConsts(String titleString, String contentString) {
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