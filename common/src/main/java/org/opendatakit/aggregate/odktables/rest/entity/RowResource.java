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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;


@JacksonXmlRootElement(localName="rowResource")
public class RowResource extends Row {

  /**
   * The URL that returns this RowResource.
   */
  private String selfUri;

  @SuppressWarnings("unused")
  private RowResource() {
  }

  public RowResource(Row row) {
    super(row);
  }

  public String getSelfUri() {
    return this.selfUri;
  }

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this)
      return true;
    if (!(o instanceof RowResource))
      return false;
    final RowResource other = (RowResource) o;
    if (!other.canEqual((java.lang.Object) this))
      return false;
    if (!super.equals(o))
      return false;
    if (selfUri == null ? (other.selfUri != null) : !selfUri.equals(other.selfUri))
      return false;
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof RowResource;
  }

  @Override
  public int hashCode() {
    final int PRIME = 31;
    int result = 1;
    result = result * PRIME + super.hashCode();
    result = result * PRIME + (this.getSelfUri() == null ? 0 : this.getSelfUri().hashCode());
    return result;
  }

  public String toString() {
    return "RowResource(super=" + super.toString() + ", selfUri=" + this.getSelfUri() + ")";
  }

}