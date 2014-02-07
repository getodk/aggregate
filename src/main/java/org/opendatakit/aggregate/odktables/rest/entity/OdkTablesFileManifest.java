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

import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Root;


/**
 * This holds a list of {@link OdkTablesFileManifestEntry}.
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@Root
public class OdkTablesFileManifest {

  /**
   * The entries in the manifest.
   */
  @ElementList(inline = true, required = false)
  private ArrayList<OdkTablesFileManifestEntry> entries;

  /**
   * Constructor used by Jackson
   */
  public OdkTablesFileManifest() {
    this.entries = new ArrayList<OdkTablesFileManifestEntry>();
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public OdkTablesFileManifest(ArrayList<OdkTablesFileManifestEntry> entries) {
    if ( entries == null ) {
      this.entries = new ArrayList<OdkTablesFileManifestEntry>();
    } else {
      this.entries = entries;
    }
  }

  public ArrayList<OdkTablesFileManifestEntry> getEntries() {
    return entries;
  }

  public void setEntries(ArrayList<OdkTablesFileManifestEntry> entries) {
    this.entries = entries;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entries == null) ? 0 : entries.hashCode());
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
    return (entries == null ? other.entries == null :
      ( entries.size() == other.entries.size() &&
        entries.containsAll(other.entries) &&
        other.entries.containsAll(entries)));
  }

}
