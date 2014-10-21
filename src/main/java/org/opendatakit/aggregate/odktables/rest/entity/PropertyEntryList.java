/*
 * Copyright (C) 2014 University of Washington
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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This holds a list of {@link Row}.
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="propertyList")
public class PropertyEntryList {

  /**
   * The entries in the manifest.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="property")
  private ArrayList<PropertyEntry> properties;

  /**
   * Constructor used by Jackson
   */
  public PropertyEntryList() {
    this.properties = new ArrayList<PropertyEntry>();
  }

  /**
   * Constructor used by our Java code.
   * 
   * The list cannot contain any nulls.
   * 
   * Sorts the passed-in array and 
   * takes ownership of it.
   *
   * @param entries
   */
  public PropertyEntryList(ArrayList<PropertyEntry> properties) {
    if ( properties == null ) {
      this.properties = new ArrayList<PropertyEntry>();
    } else {
      this.properties = properties;
      // throws an exception if there are any nulls in the list...
      Collections.sort(this.properties);
    }
  }

  /**
   * Get the sorted list of properties.
   * 
   * @return
   */
  public ArrayList<PropertyEntry> getProperties() {
    return properties;
  }

  /**
   * The list cannot contain any nulls.
   * 
   * Sorts the passed-in array and 
   * takes ownership of it.
   *
   * @param properties
   */
  public void setProperties(ArrayList<PropertyEntry> properties) {
    this.properties = properties;
    // throws an exception if there are any nulls in the list...
    Collections.sort(this.properties);
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((properties == null) ? 0 : properties.hashCode());
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
    if (!(obj instanceof PropertyEntryList)) {
      return false;
    }
    PropertyEntryList other = (PropertyEntryList) obj;
    boolean simpleResult = (properties == null ? other.properties == null : (other.properties != null && properties.size() == other.properties.size()));
    if ( !simpleResult ) {
      return false;
    }
    
    if ( properties == null ) {
      return true;
    }

    // the properties are an ordered list. 
    // Do an O(n) compare of the lists.
    for ( int i = 0 ; i < properties.size() ; ++i ) {
      PropertyEntry left = this.properties.get(i);
      PropertyEntry right = other.properties.get(i);
      if ( ! left.equals(right) ) {
        return false;
      }
    }
    return true;
  }

}
