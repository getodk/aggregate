/*
 * Copyright (C) 2012-2013 University of Washington
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

package org.opendatakit.aggregate.odktables.rest.entity;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Root;

/**
 * This represents information about a file so that a phone running ODKTables
 * will be able to check to see if it has the most recent version of the file,
 * and if not will be able to download the file. It is meant to be mostly a
 * struct that is parsed into and recovered from JSON.
 *
 * @author sudar.sam@gmail.com
 *
 */
@Root
public class OdkTablesFileManifestEntry {

  /**
   * This is the name of the file.
   */
  @Attribute(required = true)
  public String filename;

  @Attribute(required = false)
  public Long contentLength;

  @Attribute(required = false)
  public String contentType;

  /**
   * This is the md5hash of the file, which will be used for checking whether or
   * not the version of the file on the phone is current.
   */
  @Attribute(required = true)
  public String md5hash;

  /**
   * This is the url from which the current version of the file can be
   * downloaded.
   */
  @Attribute(required = true)
  public String downloadUrl;

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((filename == null) ? 0 : filename.hashCode());
    result = prime * result + ((contentLength == null) ? 0 : contentLength.hashCode());
    result = prime * result + ((contentType == null) ? 0 : contentType.hashCode());
    result = prime * result + ((md5hash == null) ? 0 : md5hash.hashCode());
    result = prime * result + ((downloadUrl == null) ? 0 : downloadUrl.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof OdkTablesFileManifestEntry)) {
      return false;
    }
    OdkTablesFileManifestEntry other = (OdkTablesFileManifestEntry) obj;
    return (filename == null ? other.filename == null : filename.equals(other.filename))
        && (contentLength == null ? other.contentLength == null : contentLength.equals(other.contentLength))
        && (contentType == null ? other.contentType == null : contentType.equals(other.contentType))
        && (md5hash == null ? other.md5hash == null : md5hash.equals(other.md5hash))
        && (downloadUrl == null ? other.downloadUrl == null : downloadUrl.equals(other.downloadUrl));
  }

}
