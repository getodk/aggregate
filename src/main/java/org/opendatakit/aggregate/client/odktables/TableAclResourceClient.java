/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.odktables;

import java.io.Serializable;

/**
 * This is the client-side version of
 * org.opendatakit.aggregate.odktables.entity.api.TableAclResource.java. <br>
 * The idea is that this will perform the same function but on the client side.
 * Usual caveat that it is currently unclear if this is necessary and if a new
 * non-phone object has to be created for the server.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableAclResourceClient extends TableAclClient implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = 7456564501408206546L;

  private String selfUri;
  private String aclUri;
  private String tableUri;

  @SuppressWarnings("unused")
  private TableAclResourceClient() {
  }

  public TableAclResourceClient(TableAclClient tableAcl) {
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
    if (!(obj instanceof TableAclResourceClient))
      return false;
    TableAclResourceClient other = (TableAclResourceClient) obj;
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
