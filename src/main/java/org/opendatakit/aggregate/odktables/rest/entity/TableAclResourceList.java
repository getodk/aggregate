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
import java.util.Comparator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

/**
 * This holds a list of {@link TableAclResource}.
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class TableAclResourceList {

  /**
   * together with the initial query, pass this in to
   * return this same result set.
   */
  @JsonProperty(required = false)
  private String webSafeRefetchCursor;

  /**
   * Alternatively, the user can obtain the elements preceding the contents of the
   * result set by constructing a 'backward query' with the same filter criteria
   * but all sort directions inverted and pass the webSafeBackwardCursor
   * to obtain the preceding elements.
   */
  @JsonProperty(required = false)
  private String webSafeBackwardCursor;

  /**
   * together with the initial query, pass this in to
   * return the next set of results
   */
  @JsonProperty(required = false)
  private String webSafeResumeCursor;

  @JsonProperty(required = false)
  private boolean hasMoreResults;

  @JsonProperty(required = false)
  private boolean hasPriorResults;

  /**
   * The entries in the manifest.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="aclResource")
  private ArrayList<TableAclResource> orderedAcls;

  /**
   * Constructor used by Jackson
   */
  public TableAclResourceList() {
    this.orderedAcls = new ArrayList<TableAclResource>();
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public TableAclResourceList(ArrayList<TableAclResource> acls,
      String refetchCursor, String backCursor, String resumeCursor, boolean hasMore, boolean hasPrior) {
    if ( acls == null ) {
      this.orderedAcls = new ArrayList<TableAclResource>();
    } else {
      this.orderedAcls = acls;
      Collections.sort(orderedAcls, new Comparator<TableAclResource>(){

        @Override
        public int compare(TableAclResource arg0, TableAclResource arg1) {
          return arg0.compareTo(arg1);
        }});
    }
    this.webSafeRefetchCursor = refetchCursor;
    this.webSafeBackwardCursor = backCursor;
    this.webSafeResumeCursor = resumeCursor;
    this.hasMoreResults = hasMore;
    this.hasPriorResults = hasPrior;
  }

  @JsonIgnore
  public ArrayList<TableAclResource> getAcls() {
    return orderedAcls;
  }

  @JsonIgnore
  public void setAcls(ArrayList<TableAclResource> acls) {
    this.orderedAcls = acls;
    Collections.sort(orderedAcls, new Comparator<TableAclResource>(){

      @Override
      public int compare(TableAclResource arg0, TableAclResource arg1) {
        return arg0.compareTo(arg1);
      }});
  }

  public String getWebSafeRefetchCursor() {
    return webSafeRefetchCursor;
  }

  public void setWebSafeRefetchCursor(String webSafeRefetchCursor) {
    this.webSafeRefetchCursor = webSafeRefetchCursor;
  }

  public String getWebSafeBackwardCursor() {
    return webSafeBackwardCursor;
  }

  public void setWebSafeBackwardCursor(String webSafeBackwardCursor) {
    this.webSafeBackwardCursor = webSafeBackwardCursor;
  }

  public String getWebSafeResumeCursor() {
    return webSafeResumeCursor;
  }

  public void setWebSafeResumeCursor(String webSafeResumeCursor) {
    this.webSafeResumeCursor = webSafeResumeCursor;
  }

  public boolean isHasMoreResults() {
    return hasMoreResults;
  }

  public void setHasMoreResults(boolean hasMoreResults) {
    this.hasMoreResults = hasMoreResults;
  }

  public boolean isHasPriorResults() {
    return hasPriorResults;
  }

  public void setHasPriorResults(boolean hasPriorResults) {
    this.hasPriorResults = hasPriorResults;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((orderedAcls == null) ? 0 : orderedAcls.hashCode());
    result = prime * result + ((webSafeRefetchCursor == null) ? 0 : webSafeRefetchCursor.hashCode());
    result = prime * result + ((webSafeBackwardCursor == null) ? 0 : webSafeBackwardCursor.hashCode());
    result = prime * result + ((webSafeResumeCursor == null) ? 0 : webSafeResumeCursor.hashCode());
    result = prime * result + (hasMoreResults ? 0 : 1);
    result = prime * result + (hasPriorResults ? 0 : 1);
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
    if (!(obj instanceof TableAclResourceList)) {
      return false;
    }
    TableAclResourceList other = (TableAclResourceList) obj;
    boolean simpleResult = (orderedAcls == null ? other.orderedAcls == null : 
            (other.orderedAcls != null && orderedAcls.size() == other.orderedAcls.size())) &&
    (webSafeRefetchCursor == null ? other.webSafeRefetchCursor == null : (webSafeRefetchCursor.equals(other.webSafeRefetchCursor))) &&
    (webSafeBackwardCursor == null ? other.webSafeBackwardCursor == null : (webSafeBackwardCursor.equals(other.webSafeBackwardCursor))) &&
    (webSafeResumeCursor == null ? other.webSafeResumeCursor == null : (webSafeResumeCursor.equals(other.webSafeResumeCursor))) &&
    (hasMoreResults == other.hasMoreResults) &&
    (hasPriorResults == other.hasPriorResults);
    
    if ( !simpleResult ) {
      return false;
    }
    
    if ( orderedAcls == null ) {
      return true;
    }
    
    // acls are ordered... compare in order
    for ( int i = 0 ; i < orderedAcls.size(); ++i ) {
      if ( ! orderedAcls.get(i).equals(other.orderedAcls.get(i)) ) {
        return false;
      }
    }
    return true;
  }

}
