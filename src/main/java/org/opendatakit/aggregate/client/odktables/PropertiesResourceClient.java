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
 * org.opendatakit.aggregate.odktables.entity.api.PropertiesResource.java. <br>
 * The idea is that this will serve the same function as the server-side object,
 * but for the client. It is possible that a similar object might have to be
 * created on the server-side to handle the non-phone requests, but this will
 * hopefully become apparent.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class PropertiesResourceClient extends TablePropertiesClient implements Serializable {

  /**
   *
   */
  private static final long serialVersionUID = -3016334795208571037L;
  private String selfUri;
  private String tableUri;

  @SuppressWarnings("unused")
  private PropertiesResourceClient() {
    // necessary for gwt serialization
  }

  public PropertiesResourceClient(TablePropertiesClient tableProperties) {
    super();
    setSchemaETag(tableProperties.getSchemaETag());
    setPropertiesETag(tableProperties.getPropertiesETag());
    setTableId(tableProperties.getTableId());
    setKeyValueStoreEntries(tableProperties.getKeyValueStoreEntries());
  }

  public String getSelfUri() {
    return this.selfUri;
  }

  public String getTableUri() {
    return this.tableUri;
  }

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }

  public void setTableUri(final String tableUri) {
    this.tableUri = tableUri;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this)
      return true;
    if (!(o instanceof PropertiesResourceClient))
      return false;
    final PropertiesResourceClient other = (PropertiesResourceClient) o;
    if (!other.canEqual((java.lang.Object) this))
      return false;
    if (!super.equals(o))
      return false;
    if (this.getSelfUri() == null ? other.getSelfUri() != null : !this.getSelfUri().equals(
        (java.lang.Object) other.getSelfUri()))
      return false;
    if (this.getTableUri() == null ? other.getTableUri() != null : !this.getTableUri().equals(
        (java.lang.Object) other.getTableUri()))
      return false;
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof PropertiesResourceClient;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = result * PRIME + super.hashCode();
    result = result * PRIME + (this.getSelfUri() == null ? 0 : this.getSelfUri().hashCode());
    result = result * PRIME + (this.getTableUri() == null ? 0 : this.getTableUri().hashCode());
    return result;
  }

  public String toString() {
    return "PropertiesResource(super=" + super.toString() + ", selfUri=" + this.getSelfUri()
        + ", tableUri=" + this.getTableUri() + ")";
  }

}
