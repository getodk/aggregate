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

import org.simpleframework.xml.Default;
import org.simpleframework.xml.DefaultType;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@Root
@Default(DefaultType.FIELD)
public class TableAclResource extends TableAcl {

  @Element(required = true)
  private String selfUri;
  @Element(required = false)
  private String aclUri;
  @Element(required = false)
  private String tableUri;

  @SuppressWarnings("unused")
  private TableAclResource() {
  }

  public TableAclResource(TableAcl tableAcl) {
    super();
    setScope(tableAcl.getScope());
    setRole(tableAcl.getRole());
  }

  /**
   * @return the selfUri
   */
  public String getSelfUri() {
    return selfUri;
  }

  /**
   * @param selfUri
   *          the selfUri to set
   */
  public void setSelfUri(String selfUri) {
    this.selfUri = selfUri;
  }

  /**
   * @return the aclUri
   */
  public String getAclUri() {
    return aclUri;
  }

  /**
   * @param aclUri
   *          the aclUri to set
   */
  public void setAclUri(String aclUri) {
    this.aclUri = aclUri;
  }

  /**
   * @return the tableUri
   */
  public String getTableUri() {
    return tableUri;
  }

  /**
   * @param tableUri
   *          the tableUri to set
   */
  public void setTableUri(String tableUri) {
    this.tableUri = tableUri;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = super.hashCode();
    result = prime * result + ((aclUri == null) ? 0 : aclUri.hashCode());
    result = prime * result + ((selfUri == null) ? 0 : selfUri.hashCode());
    result = prime * result + ((tableUri == null) ? 0 : tableUri.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (!super.equals(obj))
      return false;
    if (!(obj instanceof TableAclResource))
      return false;
    TableAclResource other = (TableAclResource) obj;
    if (aclUri == null) {
      if (other.aclUri != null)
        return false;
    } else if (!aclUri.equals(other.aclUri))
      return false;
    if (selfUri == null) {
      if (other.selfUri != null)
        return false;
    } else if (!selfUri.equals(other.selfUri))
      return false;
    if (tableUri == null) {
      if (other.tableUri != null)
        return false;
    } else if (!tableUri.equals(other.tableUri))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TableAclResource [selfUri=");
    builder.append(selfUri);
    builder.append(", aclUri=");
    builder.append(aclUri);
    builder.append(", tableUri=");
    builder.append(tableUri);
    builder.append("]");
    return builder.toString();
  }

}
