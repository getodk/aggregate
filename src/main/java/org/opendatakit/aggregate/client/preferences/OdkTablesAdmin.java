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

package org.opendatakit.aggregate.client.preferences;

import java.io.Serializable;

public class OdkTablesAdmin implements Serializable{

  /**
   *
   */
  private static final long serialVersionUID = 7628052757666367474L;

  private String aggregateUid;

  private String name;

  private String externalUid;

  public OdkTablesAdmin() {

  }

  public OdkTablesAdmin(String name, String externalUid) {
    this.aggregateUid = null;
    this.name = name;
    this.externalUid = externalUid;
  }


  public OdkTablesAdmin(String aggregateUid, String name, String externalUid) {
    this.aggregateUid = aggregateUid;
    this.name = name;
    this.externalUid = externalUid;
  }

  public String getAggregateUid() {
    return aggregateUid;
  }

  public void setAggregateUid(String aggregateUid) {
    this.aggregateUid = aggregateUid;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getExternalUid() {
    return externalUid;
  }

  public void setExternalUid(String externalUid) {
    this.externalUid = externalUid;
  }
}
