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

package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.Date;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;

public final class FormSummary implements Serializable {

  private static final long serialVersionUID = 5320217439717436812L;
  private String title;
  private String id;
  // creation date is the date the form was uploaded to Aggregate...
  private Date creationDate;
  private String createdUser;
  private boolean download;
  private boolean receiveSubmissions;
  private String viewURL;
  // number of media files associated with this form
  private int mediaFileCount = 0;
  // if the form is marked for deletion
  private FormActionStatusTimestamp mostRecentDeletionRequestStatus = null;
  // if the form has a purge-submissions action in progress.
  private FormActionStatusTimestamp mostRecentPurgeSubmissionsRequestStatus = null;


  public FormSummary() {

  }

  public FormSummary(String formTitle, String formId, Date creationDate, String formCreateUser,
                     boolean download, boolean receiveSubmissions, String viewableURL, int mediaFileCount) {
    this.title = formTitle;
    this.id = formId;
    this.creationDate = creationDate;
    this.createdUser = formCreateUser;
    this.download = download;
    this.receiveSubmissions = receiveSubmissions;
    this.viewURL = viewableURL;
    this.mediaFileCount = mediaFileCount;
  }

  public String getTitle() {
    return title;
  }

  public String getId() {
    return id;
  }

  public Date getCreationDate() {
    return creationDate;
  }

  public String getCreatedUser() {
    return createdUser;
  }

  public boolean isDownloadable() {
    return download;
  }

  public boolean receiveSubmissions() {
    return receiveSubmissions;
  }

  public String getViewableURL() {
    return viewURL;
  }

  public int getMediaFileCount() {
    return mediaFileCount;
  }

  public FormActionStatusTimestamp getMostRecentDeletionRequestStatus() {
    return mostRecentDeletionRequestStatus;
  }

  public void setMostRecentDeletionRequestStatus(
      FormActionStatusTimestamp mostRecentDeletionRequestStatus) {
    this.mostRecentDeletionRequestStatus = mostRecentDeletionRequestStatus;
  }

  public FormActionStatusTimestamp getMostRecentPurgeSubmissionsRequestStatus() {
    return mostRecentPurgeSubmissionsRequestStatus;
  }

  public void setMostRecentPurgeSubmissionsRequestStatus(
      FormActionStatusTimestamp mostRecentPurgeSubmissionsRequestStatus) {
    this.mostRecentPurgeSubmissionsRequestStatus = mostRecentPurgeSubmissionsRequestStatus;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof FormSummary)) {
      return false;
    }

    FormSummary other = (FormSummary) obj;
    return (title == null ? (other.title == null) : (title.equals(other.title)))
        && (id == null ? (other.id == null) : (id.equals(other.id)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 101;
    if (title != null)
      hashCode += title.hashCode();
    if (id != null)
      hashCode += id.hashCode();
    return hashCode;
  }

}
