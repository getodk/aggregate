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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This holds a list of {@link RowResource}.
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="rowResourceList")
public class RowResourceList {

  /**
   * The entries in the manifest.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="rowResource")
  private ArrayList<RowResource> rows;

  /**
   * The dataETag of the table at the START of this request. 
   */
  @JsonProperty(required = false)
  private String dataETag;

  /**
   * The URL that returns the TableResource for this table.
   */
  private String tableUri;

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
   * Constructor used by Jackson
   */
  public RowResourceList() {
    this.rows = new ArrayList<RowResource>();
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public RowResourceList(ArrayList<RowResource> rows, String dataETag, String tableUri,
      String refetchCursor, String backCursor, String resumeCursor, boolean hasMore, boolean hasPrior) {
    if ( rows == null ) {
      this.rows = new ArrayList<RowResource>();
    } else {
      this.rows = rows;
    }
    this.dataETag = dataETag;
    this.tableUri = tableUri;
    this.webSafeRefetchCursor = refetchCursor;
    this.webSafeBackwardCursor = backCursor;
    this.webSafeResumeCursor = resumeCursor;
    this.hasMoreResults = hasMore;
    this.hasPriorResults = hasPrior;
  }

  public ArrayList<RowResource> getRows() {
    return rows;
  }

  public void setRows(ArrayList<RowResource> rows) {
    this.rows = rows;
  }

  public String getDataETag() {
    return dataETag;
  }

  public void setDataETag(String dataETag) {
    this.dataETag = dataETag;
  }

  public String getTableUri() {
    return this.tableUri;
  }

  public void setTableUri(final String tableUri) {
    this.tableUri = tableUri;
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
    result = prime * result + ((rows == null) ? 0 : rows.hashCode());
    result = prime * result + ((dataETag == null) ? 0 : dataETag.hashCode());
    result = prime * result + ((tableUri == null) ? 0 : tableUri.hashCode());
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
    if (!(obj instanceof RowResourceList)) {
      return false;
    }
    RowResourceList other = (RowResourceList) obj;
    boolean simpleResult = (rows == null ? other.rows == null : (other.rows != null && rows.size() == other.rows.size())) &&
        (dataETag == null ? other.dataETag == null : (dataETag.equals(other.dataETag))) &&
        (tableUri == null ? other.tableUri == null : (tableUri.equals(other.tableUri))) &&
        (webSafeRefetchCursor == null ? other.webSafeRefetchCursor == null : (webSafeRefetchCursor.equals(other.webSafeRefetchCursor))) &&
        (webSafeBackwardCursor == null ? other.webSafeBackwardCursor == null : (webSafeBackwardCursor.equals(other.webSafeBackwardCursor))) &&
        (webSafeResumeCursor == null ? other.webSafeResumeCursor == null : (webSafeResumeCursor.equals(other.webSafeResumeCursor))) &&
        (hasMoreResults == other.hasMoreResults) &&
        (hasPriorResults == other.hasPriorResults);
    if ( !simpleResult ) {
      return false;
    }
    
    if ( rows == null ) {
      return true;
    }
    
    return rows.containsAll(other.rows);
  }

}
