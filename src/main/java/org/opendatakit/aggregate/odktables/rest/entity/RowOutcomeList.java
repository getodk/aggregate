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
 * This holds a list of {@link Row}.
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 *
 */
@JacksonXmlRootElement(localName="rowList")
public class RowOutcomeList {

  /**
   * The URL that returns the TableResource for this table.
   */
  @JsonProperty(required = false)
  private String tableUri;

  /**
   * The entries in the manifest.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(useWrapping=false)
  @JacksonXmlProperty(localName="row")
  private ArrayList<RowOutcome> rows;

  /**
   * The dataETag for the changes made by this request.
   */
  @JsonProperty(required = false)
  private String dataETag;

  /**
   * Constructor used by Jackson
   */
  public RowOutcomeList() {
    this.rows = new ArrayList<RowOutcome>();
    this.dataETag = null;
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public RowOutcomeList(ArrayList<RowOutcome> rows, String dataETag) {
    if ( rows == null ) {
      this.rows = new ArrayList<RowOutcome>();
    } else {
      this.rows = rows;
    }
    this.dataETag = dataETag;
  }

  public ArrayList<RowOutcome> getRows() {
    return rows;
  }

  public void setRows(ArrayList<RowOutcome> rows) {
    this.rows = rows;
  }

  public String getDataETag() {
    return this.dataETag;
  }

  public void setDataETag(final String dataETag) {
    this.dataETag = dataETag;
  }

  public String getTableUri() {
    return this.tableUri;
  }

  public void setTableUri(final String tableUri) {
    this.tableUri = tableUri;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rows == null) ? 0 : rows.hashCode());
    result = prime * result + ((dataETag == null) ? 0 : dataETag.hashCode());
    result = prime * result + ((tableUri == null) ? 0 : tableUri.hashCode());
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
    if (!(obj instanceof RowOutcomeList)) {
      return false;
    }
    RowOutcomeList other = (RowOutcomeList) obj;
    boolean simpleResult = (tableUri == null ? other.tableUri == null : tableUri.equals(other.tableUri)) &&
        (dataETag == null ? other.dataETag == null : dataETag.equals(other.dataETag)) &&
        (rows == null ? other.rows == null : (other.rows != null && rows.size() == other.rows.size()));
    if ( !simpleResult ) {
      return false;
    }
    
    if ( rows == null ) {
      return true;
    }
    
    return rows.containsAll(other.rows);
  }

}
