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
 * This holds a list of dataETag (changeSet) values.
 * It also supplies a sequenceValue that can be used
 * to get all dataETag (changeSet) values made after
 * this request.
 * 
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="changeSetList")
public class ChangeSetList {

  /**
   * The dataETag values.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="changeSet")
  private ArrayList<String> changeSets;

  /**
   * The dataETag value of the table at the START of this request.
   */
  @JsonProperty(required = false)
  private String dataETag;

  /**
   * The sequenceValue of the server at the START of this request.
   * A monotonically increasing string.
   */
  @JsonProperty(required = false)
  private String sequenceValue;

  /**
   * Constructor used by Jackson
   */
  public ChangeSetList() {
    this.changeSets = new ArrayList<String>();
    this.dataETag = null;
    this.sequenceValue = null;
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public ChangeSetList(ArrayList<String> changeSets, String dataETag, String sequenceValue) {
    if ( changeSets == null ) {
      this.changeSets = new ArrayList<String>();
    } else {
      this.changeSets = changeSets;
      Collections.sort(this.changeSets);
    }
    this.dataETag = dataETag;
    this.sequenceValue = sequenceValue;
  }

  public ArrayList<String> getChangeSets() {
    return changeSets;
  }

  public void setChangeSets(ArrayList<String> changeSets) {
    this.changeSets = changeSets;
  }

  public String getDataETag() {
    return dataETag;
  }

  public void setDataETag(String dataETag) {
    this.dataETag = dataETag;
  }

  public String getSequenceValue() {
    return sequenceValue;
  }

  public void setSequenceValue(String sequenceValue) {
    this.sequenceValue = sequenceValue;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((changeSets == null) ? 0 : changeSets.hashCode());
    result = prime * result + ((dataETag == null) ? 0 : dataETag.hashCode());
    result = prime * result + ((sequenceValue == null) ? 0 : sequenceValue.hashCode());
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
    if (!(obj instanceof ChangeSetList)) {
      return false;
    }
    ChangeSetList other = (ChangeSetList) obj;
    boolean simpleResult =
        (dataETag == null ? other.dataETag == null : dataETag.equals(other.dataETag)) &&
        (sequenceValue == null ? other.sequenceValue == null : sequenceValue.equals(other.sequenceValue)) &&
        (changeSets == null ? other.changeSets == null : (other.changeSets != null && changeSets.size() == other.changeSets.size())); 
    
    if ( !simpleResult ) {
      return false;
    }
    
    if ( changeSets == null ) {
      return true;
    }
    
    return changeSets.containsAll(other.changeSets);
  }

}
