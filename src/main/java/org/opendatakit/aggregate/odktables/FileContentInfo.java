/*
 * Copyright (C) 2016 University of Washington
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
package org.opendatakit.aggregate.odktables;

public class FileContentInfo {
  public final String partialPath;
  public final String contentType;
  public final Long contentLength;
  public final String contentHash;
  public final byte[] fileBlob;

  public FileContentInfo(String partialPath, String contentType, Long contentLength, String contentHash, byte[] blob) {
    this.partialPath = partialPath;
    this.contentType = contentType;
    this.contentLength = contentLength;
    this.contentHash = contentHash;
    this.fileBlob = blob;
  }
}