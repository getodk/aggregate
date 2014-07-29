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
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

/**
 * This holds a list of {@link TableResource}. Proper XML documents can contain
 * only one root node. This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="tableResourceList")
public class TableResourceList {

  /**
   * pass this in to return this same result set.
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
  @JacksonXmlProperty(localName="tableResource")
  private ArrayList<TableResource> tables;

  /**
   * Constructor used by Jackson
   */
  public TableResourceList() {
    this.tables = new ArrayList<TableResource>();
    this.webSafeResumeCursor = null;
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public TableResourceList(ArrayList<TableResource> tables,
      String refetchCursor, String backCursor, String resumeCursor, boolean hasMore, boolean hasPrior) {
    if (tables == null) {
      this.tables = new ArrayList<TableResource>();
    } else {
      this.tables = tables;
    }
    this.webSafeRefetchCursor = refetchCursor;
    this.webSafeBackwardCursor = backCursor;
    this.webSafeResumeCursor = resumeCursor;
    this.hasMoreResults = hasMore;
    this.hasPriorResults = hasPrior;
  }

  public List<TableResource> getTables() {
    return tables;
  }

  public void setTables(ArrayList<TableResource> tables) {
    this.tables = tables;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tables == null) ? 0 : tables.hashCode());
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
    if (!(obj instanceof TableResourceList)) {
      return false;
    }
    TableResourceList other = (TableResourceList) obj;
    return ((tables == null ? other.tables == null : (tables.size() == other.tables.size() && tables.containsAll(other.tables)))) &&
            (webSafeRefetchCursor == null ? other.webSafeRefetchCursor == null : (webSafeRefetchCursor.equals(other.webSafeRefetchCursor))) &&
            (webSafeBackwardCursor == null ? other.webSafeBackwardCursor == null : (webSafeBackwardCursor.equals(other.webSafeBackwardCursor))) &&
            (webSafeResumeCursor == null ? other.webSafeResumeCursor == null : (webSafeResumeCursor.equals(other.webSafeResumeCursor))) &&
            (hasMoreResults == other.hasMoreResults) &&
            (hasPriorResults == other.hasPriorResults);
  }
}
