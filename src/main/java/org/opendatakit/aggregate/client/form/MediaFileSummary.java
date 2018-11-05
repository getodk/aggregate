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

public class MediaFileSummary implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -6087879867808529452L;

  private String filename;
  private String contentType;
  private Long contentLength;

  public MediaFileSummary() {
  }

  public MediaFileSummary(String filename, String contentType, Long contentLength) {
    this.filename = filename;
    this.contentType = contentType;
    this.contentLength = contentLength;
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
}
