/*
 * Copyright (C) 2011 University of Washington
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

package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.UIDisplayType;

public class Column implements Serializable {
  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -5276405259406410364L;

  // unique identifier for DB if column filter
  // NOTE: unneeded for row filter
  private String uri;

  private String displayHeader;
  private String columnEncoding;
  private Long childColumnCode;
  private UIDisplayType uiDisplayType;

  public Column() {
    this.uri = UIConsts.URI_DEFAULT;
    this.uiDisplayType = UIDisplayType.TEXT;
  }

  public Column(String uri, String displayHeader, String columnName, UIDisplayType displayType) {
    this.uri = uri;
    this.displayHeader = displayHeader;
    this.columnEncoding = columnName;
    this.uiDisplayType = displayType;
  }

  public Column(String displayHeader, String columnName, UIDisplayType displayType) {
    this(UIConsts.URI_DEFAULT, displayHeader, columnName, displayType);
  }

  public Column(String uri, String displayHeader, String columnName, Long childColumnCode) {
    this.uri = uri;
    this.displayHeader = displayHeader;
    this.columnEncoding = columnName;
    this.childColumnCode = childColumnCode;
    this.uiDisplayType = UIDisplayType.TEXT;
  }

  public Column(String displayHeader, String columnName, Long childColumnCode) {
    this(UIConsts.URI_DEFAULT, displayHeader, columnName, childColumnCode);
  }

  /**
   * Used to clear the URI in the elements so it can be Saved As properly in the
   * server, as the server creates a new entity when uri is set to URI_DEFAULT
   */
  public void resetUriToDefault() {
    uri = UIConsts.URI_DEFAULT;
  }

  public String getUri() {
    return uri;
  }

  public String getDisplayHeader() {
    return displayHeader;
  }

  public String getColumnEncoding() {
    return columnEncoding;
  }

  public Long getChildColumnCode() {
    return childColumnCode;
  }

  public UIDisplayType getUiDisplayType() {
    return uiDisplayType;
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Column)) {
      return false;
    }

    Column other = (Column) obj;
    return (uri == null ? (other.uri == null) : (uri.equals(other.uri)))
        && (displayHeader == null ? (other.displayHeader == null) : (displayHeader
        .equals(other.displayHeader)))
        && (columnEncoding == null ? (other.columnEncoding == null) : (columnEncoding
        .equals(other.columnEncoding)))
        && (childColumnCode == null ? (other.childColumnCode == null) : (childColumnCode
        .equals(other.childColumnCode)))
        && (uiDisplayType == null ? (other.uiDisplayType == null) : (uiDisplayType
        .equals(other.uiDisplayType)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 11;
    if (uri != null)
      hashCode += uri.hashCode();
    if (displayHeader != null)
      hashCode += displayHeader.hashCode();
    if (columnEncoding != null)
      hashCode += columnEncoding.hashCode();
    if (childColumnCode != null)
      hashCode += childColumnCode.hashCode();
    if (uiDisplayType != null)
      hashCode += uiDisplayType.hashCode();
    return hashCode;
  }
}