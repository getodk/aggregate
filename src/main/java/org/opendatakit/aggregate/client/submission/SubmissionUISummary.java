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

package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;
import java.util.ArrayList;
import org.opendatakit.aggregate.constants.common.UIDisplayType;
import org.opendatakit.common.persistence.client.UIQueryResumePoint;

public class SubmissionUISummary implements Serializable {

  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = 4067244808385366754L;

  private ArrayList<SubmissionUI> submissions = new ArrayList<SubmissionUI>();

  private ArrayList<Column> headers;

  private UIQueryResumePoint startCursor;

  private UIQueryResumePoint resumeCursor;

  private UIQueryResumePoint backwardCursor;

  private boolean hasMoreResults;

  private boolean hasPriorResults;

  private String formTitle;

  public SubmissionUISummary() {
    headers = new ArrayList<Column>();
  }

  public SubmissionUISummary(String formTitle) {
    this();
    this.formTitle = formTitle;
  }

  public ArrayList<Column> getHeaders() {
    return headers;
  }

  public void addSubmission(SubmissionUI submission) {
    if (submission.getNumberOfFields() == headers.size()) {
      submissions.add(submission);
    } else {
      throw new IllegalArgumentException("Incorrect number of fields contained in submission");
    }
  }

  public void addSubmissionHeader(String displayHeader, String columnName) {
    headers.add(new Column(displayHeader, columnName, UIDisplayType.TEXT));
  }

  public void addBinarySubmissionHeader(String displayHeader, String columnName) {
    headers.add(new Column(displayHeader, columnName, UIDisplayType.BINARY));
  }

  public void addRepeatSubmissionHeader(String displayHeader, String columnName) {
    headers.add(new Column(displayHeader, columnName, UIDisplayType.REPEAT));
  }

  public void addGeopointHeader(String displayHeader, String columnName, Long geopointColumnCode) {
    headers.add(new Column(displayHeader, columnName, geopointColumnCode));
  }

  public ArrayList<SubmissionUI> getSubmissions() {
    return submissions;
  }

  public UIQueryResumePoint getStartCursor() {
    return startCursor;
  }

  public void setStartCursor(UIQueryResumePoint startCursor) {
    this.startCursor = startCursor;
  }

  public UIQueryResumePoint getResumeCursor() {
    return resumeCursor;
  }

  public void setResumeCursor(UIQueryResumePoint resumeCursor) {
    this.resumeCursor = resumeCursor;
  }

  public UIQueryResumePoint getBackwardCursor() {
    return backwardCursor;
  }

  public void setBackwardCursor(UIQueryResumePoint backwardCursor) {
    this.backwardCursor = backwardCursor;
  }

  public String getFormTitle() {
    return formTitle;
  }

  public boolean hasMoreResults() {
    return hasMoreResults;
  }

  public void setHasMoreResults(boolean hasMoreResults) {
    this.hasMoreResults = hasMoreResults;
  }

  public boolean hasPriorResults() {
    return hasPriorResults;
  }

  public void setHasPriorResults(boolean hasPriorResults) {
    this.hasPriorResults = hasPriorResults;
  }

}
