/*
  Copyright (C) 2011 University of Washington
  <p>
  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
  in compliance with the License. You may obtain a copy of the License at
  <p>
  http://www.apache.org/licenses/LICENSE-2.0
  <p>
  Unless required by applicable law or agreed to in writing, software distributed under the License
  is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
  or implied. See the License for the specific language governing permissions and limitations under
  the License.
 */
package org.opendatakit.common.persistence.client;

import java.io.Serializable;

/**
 * Wrapper object for communicating a resume point up to the GWT UI layer.
 *
 * @author mitchellsundt@gmail.com
 */
public class UIQueryResumePoint implements Serializable {
  /**
   *
   */
  private static final long serialVersionUID = 7500161085826059616L;
  private String attributeName;
  private String value;
  private String uriLastReturnedValue;
  private Boolean isForwardCursor;

  public UIQueryResumePoint() {
  }

  public String getAttributeName() {
    return attributeName;
  }

  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getUriLastReturnedValue() {
    return uriLastReturnedValue;
  }

  public void setUriLastReturnedValue(String uriLastReturnedValue) {
    this.uriLastReturnedValue = uriLastReturnedValue;
  }

  public Boolean getIsForwardCursor() {
    return isForwardCursor;
  }

  public void setIsForwardCursor(Boolean isForwardCursor) {
    this.isForwardCursor = isForwardCursor;
  }

  ;

}
