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
  private static final long serialVersionUID = 762805275766667473L;

  private String uriUser;

  private String odkTablesUserId;

  private String name;

  public OdkTablesAdmin() {

  }

  public String getUriUser() {
    return uriUser;
  }

  public void setUriUser(String uriUser) {
    this.uriUser = uriUser;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getOdkTablesUserId() {
    return odkTablesUserId;
  }

  public void setOdkTablesUserId(String odkTablesUserId) {
    this.odkTablesUserId = odkTablesUserId;
  }
}
