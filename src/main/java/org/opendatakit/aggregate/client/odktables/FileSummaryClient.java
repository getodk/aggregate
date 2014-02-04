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
  private static final long serialVersionUID = -6081623884490673459L;

  private String filename;
  private String contentType;
  private Long contentLength;
  // the list of media files associated with this file. Only media files
  // should be in this list, and they themselves should not have media files.
  private int numMediaFiles;
  private String key;
  // need to add and ID and change mediaFiles to numMediaFiles.
  // also should probably add the table name and table id.
  private String id;
  private String tableId;

  @SuppressWarnings("unused")
  private FileSummaryClient() {
    // necessary for gwt serialization
  }

  public FileSummaryClient(String filename, String contentType, Long contentLength, String key,
      int numMediaFiles, String id, String tableId) {
    this.filename = filename;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.key = key;
    this.numMediaFiles = numMediaFiles;
    this.id = id;
    this.tableId = tableId;
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

  public int getNumMediaFiles() {
    return numMediaFiles;
  }

  public String getKey() {
    return key;
  }

  public String getId() {
    return id;
  }

  public String getTableId() {
    return tableId;
  }
}
