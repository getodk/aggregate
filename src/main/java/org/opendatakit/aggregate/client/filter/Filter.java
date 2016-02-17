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

package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.aggregate.constants.common.Visibility;

public abstract class Filter implements Serializable {

  private static final long serialVersionUID = -5453093733004634508L;
  protected String uri; // unique identifier
  private RowOrCol rc;
  private Long ordinal; // order to display in filter group
  private Visibility visibility;
  
  public Filter() {

  }

  public Filter(Visibility visibility, RowOrCol rowcol, long ordinal) {
    this.uri = UIConsts.URI_DEFAULT;
    this.rc = rowcol;
    this.ordinal = ordinal;
    this.visibility = visibility;
  }

  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   */
  public Filter(String uri) {
    this.uri = uri;
  }
  
  /**
   * Used to clear the URI in the elements so it can be Saved As properly in the
   * server, as the server creates a new entity when uri is set to URI_DEFAULT
   */
  public abstract void resetUriToDefault();
  
  public String getUri() {
    return uri;
  }

  public RowOrCol getRc() {
    return rc;
  }

  public void setRc(RowOrCol rc) {
    this.rc = rc;
  }

  public Long getOrdinal() {
    return ordinal;
  }

  public void setOrdinal(Long ordinal) {
    this.ordinal = ordinal;
  }
  
  public Visibility getVisibility() {
    return visibility;
  }

  public void setVisibility(Visibility kr) {
    this.visibility = kr;
  }
  
  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof Filter)) {
      return false;
    }
    
    Filter other = (Filter) obj;
    return (visibility == null ? (other.visibility == null) : (visibility.equals(other.visibility)))
        && (rc == null ? (other.rc == null) : (rc == other.rc))
        && (ordinal == null ? (other.ordinal == null) : (ordinal.equals(other.ordinal)));
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    int hashCode = 131;
    if(visibility != null)
      hashCode += visibility.hashCode();
    if(rc != null)
      hashCode += rc.hashCode();
    if (ordinal != null)
      hashCode += ordinal.hashCode();
    return hashCode;
  }
}