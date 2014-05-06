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
 * org.opendatakit.aggregate.odktables.entity.api. <br>
 * The idea is that this will perform the same function on the client. Usual
 * caveat that it's not yet clear if this is necessary or if a new non-phone
 * class has to be created on the server.
 *
 * @author sudar.sam@gmail.com
 *
 */
public class TableResourceClient extends TableEntryClient implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 81481336521796890L;
  private String selfUri;
  private String definitionUri;
  private String dataUri;
  private String instanceFilesUri;
  private String diffUri;
  private String aclUri;

  public TableResourceClient(TableEntryClient entry) {
    super(entry.getTableId(), entry.getDataETag(), entry.getSchemaETag());
  }

  @SuppressWarnings("unused")
  private TableResourceClient() {
    // necessary for gwt serialization
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
    if (!(obj instanceof TableResourceClient))
      return false;
    TableResourceClient other = (TableResourceClient) obj;
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
    return true;
  }

  public boolean canEqual(final Object other) {
    return other instanceof TableResourceClient;
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
