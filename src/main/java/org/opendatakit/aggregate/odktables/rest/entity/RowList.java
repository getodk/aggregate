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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import java.util.ArrayList;

/**
 * This holds a list of {@link Row}.
 * Proper XML documents can contain only one root node.
 * This wrapping class provides that root node.
 *
 * @author mitchellsundt@gmail.com
 */
@JacksonXmlRootElement(localName = "rowList")
public class RowList {

  /**
   * The entries in the manifest.
   */
  @JsonProperty(required = false)
  @JacksonXmlElementWrapper(useWrapping = false)
  @JacksonXmlProperty(localName = "row")
  private ArrayList<Row> rows;

  /**
   * The dataETag of the table at the START of this request.
   */
  @JsonProperty(required = false)
  private String dataETag;

  /**
   * Constructor used by Jackson
   */
  public RowList() {
    this.rows = new ArrayList<Row>();
  }

  /**
   * Constructor used by our Java code
   *
   * @param entries
   */
  public RowList(ArrayList<Row> rows, String dataETag) {
    if (rows == null) {
      this.rows = new ArrayList<Row>();
    } else {
      this.rows = rows;
    }
    this.dataETag = dataETag;
  }

  public ArrayList<Row> getRows() {
    return rows;
  }

  public void setRows(ArrayList<Row> rows) {
    this.rows = rows;
  }

  public String getDataETag() {
    return dataETag;
  }

  public void setDataETag(String dataETag) {
    this.dataETag = dataETag;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((rows == null) ? 0 : rows.hashCode());
    result = prime * result + ((dataETag == null) ? 0 : dataETag.hashCode());
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
    if (!(obj instanceof RowList)) {
      return false;
    }
    RowList other = (RowList) obj;
    boolean simpleResult =
        (rows == null ? other.rows == null : (other.rows != null && rows.size() == other.rows.size())) &&
            (dataETag == null ? other.dataETag == null : (dataETag.equals(other.dataETag)));
    if (!simpleResult) {
      return false;
    }

    if (rows == null) {
      return true;
    }

    return rows.containsAll(other.rows);
  }

}
