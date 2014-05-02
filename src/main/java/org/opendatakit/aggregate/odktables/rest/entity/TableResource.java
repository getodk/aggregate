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
@Default(value = DefaultType.FIELD, required = true)
public class TableResource extends TableEntry {

  @Element(required = true)
  private String selfUri;

  @Element(required = true)
  private String definitionUri;

  @Element(required = true)
  private String dataUri;

  @Element(required = true)
  private String instanceFilesUri;

  @Element(required = true)
  private String diffUri;

  @Element(required = true)
  private String aclUri;

  public TableResource(TableEntry entry) {
    super(entry.getTableId(), entry.getDataETag(), entry.getSchemaETag());
  }

  @SuppressWarnings("unused")
  private TableResource() {
  }

  public String getSelfUri() {
    return this.selfUri;
  }

  public String getDefinitionUri() {
    return this.definitionUri;
  }

  public String getDataUri() {
    return this.dataUri;
  }

  public String getInstanceFilesUri() {
    return this.instanceFilesUri;
  }

  public String getDiffUri() {
    return this.diffUri;
  }

  public String getAclUri() {
    return this.aclUri;
  }

  public void setSelfUri(final String selfUri) {
    this.selfUri = selfUri;
  }

  public void setDefinitionUri(final String definitionUri) {
    this.definitionUri = definitionUri;
  }

  public void setDataUri(final String dataUri) {
    this.dataUri = dataUri;
  }

  public void setInstanceFilesUri(final String instanceFilesUri) {
    this.instanceFilesUri = instanceFilesUri;
  }

  public void setDiffUri(final String diffUri) {
    this.diffUri = diffUri;
  }

  public void setAclUri(final String aclUri) {
    this.aclUri = aclUri;
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
    if (!(obj instanceof TableResource))
      return false;
    TableResource other = (TableResource) obj;
    if (aclUri == null) {
      if (other.aclUri != null)
        return false;
    } else if (!aclUri.equals(other.aclUri))
      return false;
    if (dataUri == null) {
      if (other.dataUri != null)
        return false;
    } else if (!dataUri.equals(other.dataUri))
      return false;
    if (instanceFilesUri == null) {
      if (other.instanceFilesUri != null)
        return false;
    } else if (!instanceFilesUri.equals(other.instanceFilesUri))
      return false;
    if (diffUri == null) {
      if (other.diffUri != null)
        return false;
    } else if (!diffUri.equals(other.diffUri))
      return false;
    if (selfUri == null) {
      if (other.selfUri != null)
        return false;
    } else if (!selfUri.equals(other.selfUri))
      return false;
    if (definitionUri == null) {
      if (other.definitionUri != null) {
        return false;
      } else if (!definitionUri.equals(other.definitionUri)) {
        return false;
      }
    }
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof TableResource;
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
    result = prime * result + ((dataUri == null) ? 0 : dataUri.hashCode());
    result = prime * result + ((instanceFilesUri == null) ? 0 : instanceFilesUri.hashCode());
    result = prime * result + ((diffUri == null) ? 0 : diffUri.hashCode());
    result = prime * result + ((selfUri == null) ? 0 : selfUri.hashCode());
    result = prime * result + ((definitionUri == null) ? 0 : definitionUri.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("TableResource [selfUri=");
    builder.append(selfUri);
    builder.append(", defintionUri=");
    builder.append(definitionUri);
    builder.append(", dataUri=");
    builder.append(dataUri);
    builder.append(", instanceFilesUri=");
    builder.append(instanceFilesUri);
    builder.append(", diffUri=");
    builder.append(diffUri);
    builder.append(", aclUri=");
    builder.append(aclUri);
    builder.append("]");
    return builder.toString();
  }
}