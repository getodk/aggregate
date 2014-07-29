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

import java.util.ArrayList;
import java.util.Collections;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


/**
 * This holds a list of {@link OdkTablesFileManifestEntry}.
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * Removed all JAXB annotations -- these cause issues on Android 4.2 and earlier.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="manifest")
public class OdkTablesFileManifest {

  /**
   * The entries in the manifest.
   * Ordered by filename and md5hash.
   */
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="file")
  private ArrayList<OdkTablesFileManifestEntry> files;

  /**
   * Constructor used by Jackson
   */
  public OdkTablesFileManifest() {
    this.files = new ArrayList<OdkTablesFileManifestEntry>();
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public OdkTablesFileManifest(ArrayList<OdkTablesFileManifestEntry> files) {
    if ( files == null ) {
      this.files = new ArrayList<OdkTablesFileManifestEntry>();
    } else {
      this.files = files;
      Collections.sort(this.files);
    }
  }

  public ArrayList<OdkTablesFileManifestEntry> getFiles() {
    return files;
  }

  public void setFiles(ArrayList<OdkTablesFileManifestEntry> files) {
    this.files = files;
    Collections.sort(this.files);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((files == null) ? 0 : files.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if ( obj == null ) {
      return false;
    }
    if ( obj == this ) {
      return true;
    }
    if (!(obj instanceof OdkTablesFileManifest)) {
      return false;
    }
    OdkTablesFileManifest other = (OdkTablesFileManifest) obj;
    boolean simpleResult = (files == null) ? (other.files == null) :
      ( other.files != null &&  files.size() == other.files.size());
    if ( !simpleResult ) {
      return false;
    }
    if ( files == null ) {
      return true;
    }
    
    // files are in sorted order -- compare linearly
    for ( int i = 0 ; i < files.size() ; ++i ) {
      if ( !files.get(i).equals(other.files.get(i)) ) {
        return false;
      }
    }
    return true;
  }

}
