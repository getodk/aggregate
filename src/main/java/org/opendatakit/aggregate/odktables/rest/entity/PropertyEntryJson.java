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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * If you use the 'properties' API, please consider switching
 * to the put/get of the properties.csv. It is far more time
 * and space efficient than this interface.
 * 
 * This holds one KeyValueStore entry that will be returned to the 
 * user. The value may be a Javascript array or object and will
 * be emitted as such to the user -- it is not a JSON serialization
 * of that array or object, but is the array or object itself.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class PropertyEntryJson implements Comparable<PropertyEntryJson> {
  
  @JsonProperty(required = true)
  private String partition;

  @JsonProperty(required = true)
  private String aspect;

  @JsonProperty(required = true)
  private String key;

  @JsonProperty(required = true)
  private String type;
  
  @JsonProperty(required = false)
  private Object value;

  /**
   * Construct a KeyValueStore entry. This can be used to construct
   * a JSON serialization of the entry, but cannot be used to deserialize
   * that entry. 
   * 
   * The server processes this via a native object deserialization into
   * an ArrayList<Map<String,Object>>, and traverses and parses that.
   * 
   * @param partition
   * @param aspect
   * @param key
   * @param type
   * @param value
   */
  public PropertyEntryJson(String partition, String aspect, String key,
      String type, Object value) {
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
  protected PropertyEntryJson(PropertyEntryJson r) {
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

  public Object getValue() {
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

  
  public void setValue(final Object value) {
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
    if (!(obj instanceof PropertyEntryJson)) {
      return false;
    }
    PropertyEntryJson other = (PropertyEntryJson) obj;
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
  public int compareTo(PropertyEntryJson other) {
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
      int hash1 = this.value.hashCode();
      int hash2 = other.value.hashCode();
      if ( hash1 == hash2 && this.value.equals(other.value)) {
        outcome = 0;
      } else if ( hash1 < hash2 ) {
        outcome = -1;
      } else if ( hash1 > hash2 ) {
        outcome = 1;
      } else {
        // hopefully this produces a non-zero outcome...
        outcome = this.value.toString().compareTo(other.value.toString());
        if ( outcome == 0 ) {
          // don't know what else to do...
          outcome = 1;
        }
      }
    }
    if ( outcome != 0 ) {
      return outcome;
    }
    
    return 0;
  }

}