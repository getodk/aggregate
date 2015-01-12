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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If you use the 'properties' API, please consider switching
 * to the put/get of the properties.csv. It is far more time
 * and space efficient than this interface.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class PropertyEntryXml implements Comparable<PropertyEntryXml> {

  @JsonProperty(required = true)
  private String partition;

  @JsonProperty(required = true)
  private String aspect;

  @JsonProperty(required = true)
  private String key;

  @JsonProperty(required = true)
  private String type;

  @JsonProperty(required = false)
  private String value;

  /**
   * Used by deserializer
   */
  public PropertyEntryXml() {
  }
  
  /**
   * Construct a KeyValueStore entry for insertion. This can be used
   * by a remote client to construct an XML request to update the 
   * properties.csv using a list of properties. It is also used
   * by the server when constructing the XML response to fetch 
   * the properties.csv as a list of properties.
   * 
   * @param partition
   * @param aspect
   * @param key
   * @param type
   * @param value
   */
  public PropertyEntryXml(String partition, String aspect, String key, String type,
      String value) {
    this.partition = partition;
    this.aspect = aspect;
    this.key = key;
    this.type = type;
    this.value = value;
  }

  /**
   * Clone a KeyValueStore entry.
   * 
   * @param r
   */
  protected PropertyEntryXml(PropertyEntryXml r) {
    this.partition = r.partition;
    this.aspect = r.aspect;
    this.key = r.key;
    this.type = r.type;
    this.value = r.value;
  }

  public String getPartition() {
    return partition;
  }

  public String getAspect() {
    return aspect;
  }

  public String getKey() {
    return key;
  }

  public String getType() {
    return type;
  }

  public String getValue() {
    return value;
  }

  public void setPartition(final String partition) {
    this.partition = partition;
  }

  public void setAspect(final String aspect) {
    this.aspect = aspect;
  }

  public void setKey(final String key) {
    this.key = key;
  }

  public void setType(final String type) {
    this.type = type;
  }

  public void setValue(final String value) {
    this.value = value;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((partition == null) ? 0 : partition.hashCode());
    result = prime * result + ((aspect == null) ? 0 : aspect.hashCode());
    result = prime * result + ((key == null) ? 0 : key.hashCode());
    result = prime * result + ((type == null) ? 0 : type.hashCode());
    result = prime * result + ((value == null) ? 0 : value.hashCode());
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
    if (!(obj instanceof PropertyEntryXml)) {
      return false;
    }
    PropertyEntryXml other = (PropertyEntryXml) obj;
    boolean match = (partition == null ? other.partition == null : partition.equals(other.partition))
        && (aspect == null ? other.aspect == null : aspect.equals(other.aspect))
        && (key == null ? other.key == null : key.equals(other.key))
        && (type == null ? other.type == null : type.equals(other.type))
        && (value == null ? other.value == null : value.equals(other.value));
    return match;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("Property [partition=");
    builder.append(partition);
    builder.append(", aspect=");
    builder.append(aspect);
    builder.append(", key=");
    builder.append(key);
    builder.append(", type=");
    builder.append(type);
    builder.append(", value=");
    builder.append(value);
    builder.append("]");
    return builder.toString();
  }

  /**
   * Sort by partition, aspect, key, type and finally value (in order).
   * 
   * In practice, it is invalid to have two or more records with
   * matching (partition, aspect, key). We do not enforce this.
   */
  @Override
  public int compareTo(PropertyEntryXml other) {
    int outcome;
    
    // compare partition
    if (this.partition == null && other.partition == null) {
      outcome = 0;
    } else if (this.partition == null) {
      return -1;
    } else if (other.partition == null) {
      return 1;
    } else {
      outcome = this.partition.compareTo(other.partition);
    }
    if ( outcome != 0 ) {
      return outcome;
    }
    
    // compare aspect
    if (this.aspect == null && other.aspect == null) {
      outcome = 0;
    } else if (this.aspect == null) {
      return -1;
    } else if (other.aspect == null) {
      return 1;
    } else {
      outcome = this.aspect.compareTo(other.aspect);
    }
    if ( outcome != 0 ) {
      return outcome;
    }
    
    // compare key
    if (this.key == null && other.key == null) {
      outcome = 0;
    } else if (this.key == null) {
      return -1;
    } else if (other.key == null) {
      return 1;
    } else {
      outcome = this.key.compareTo(other.key);
    }
    if ( outcome != 0 ) {
      return outcome;
    }
    
    // compare type
    if (this.type == null && other.type == null) {
      outcome = 0;
    } else if (this.type == null) {
      return -1;
    } else if (other.type == null) {
      return 1;
    } else {
      outcome = this.type.compareTo(other.type);
    }
    if ( outcome != 0 ) {
      return outcome;
    }
    
    // compare value
    if (this.value == null && other.value == null) {
      outcome = 0;
    } else if (this.value == null) {
      return -1;
    } else if (other.value == null) {
      return 1;
    } else {
      outcome = this.value.compareTo(other.value);
    }
    if ( outcome != 0 ) {
      return outcome;
    }
    
    return 0;
  }

}