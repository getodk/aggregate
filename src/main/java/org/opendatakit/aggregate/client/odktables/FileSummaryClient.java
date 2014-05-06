/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;

/**
 * Represents a summary of a file that has been uploaded to be associated with a
 * table.
 * <p>
 * Modeled on (keep this fully qualified!)
 * {@link org.opendatakit.aggregate.client.form.MediaFileSummary}
 *
 * @author sudar.sam@gmail.com
 *
 */
public class FileSummaryClient implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -608162388449067349L;

  private String filename;
  private String contentType;
  private Long contentLength;
  // need to add and ID and change mediaFiles to numMediaFiles.
  // also should probably add the table name and table id.
  private String id;
  private String odkClientVersion;
  private String tableId;
  private String downloadUrl;

  @SuppressWarnings("unused")
  private FileSummaryClient() {
    // necessary for gwt serialization
  }

  public FileSummaryClient(String filename, String contentType, Long contentLength,
      String id, String odkClientVersion, String tableId, String downloadUrl) {
    this.filename = filename;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.id = id;
    this.odkClientVersion = odkClientVersion;
    this.tableId = tableId;
    this.downloadUrl = downloadUrl;
  }

  public String getFilename() {
    return filename;
  }

  public String getContentType() {
    return contentType;
  }

  public Long getContentLength() {
    return contentLength;
  }

  public String getId() {
    return id;
  }

  public String getOdkClientVersion() {
    return odkClientVersion;
  }

  public String getTableId() {
    return tableId;
  }

  public String getDownloadUrl() {
    return downloadUrl;
  }
}
