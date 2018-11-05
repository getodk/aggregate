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


public enum ExportConsts implements HelpSliderConsts {
  EXPORT("After running \"Export\" on Submissions -> Filter Submissions, you should have a list of files you have exported.",
      "Understanding the table:<br>" +
          "1.  File Type - CSV or KML file.<br>" +
          "2.  Status - This will state whether the file being made is in progress, or is now available for viewing.<br>" +
          "3.  Time Requested - this shows the time when you finished filling out the \"Export\" form.<br>" +
          "4.  Time Completed - this shows the time when the \"Export\" task is complete and the file is ready.<br>" +
          "5.  Last Retry - this shows the time when the file was last attempted to be made.<br>" +
          "6.  Download File - click the link to see your exported file.");

  private String title;
  private String content;

  private ExportConsts(String titleString, String contentString) {
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
