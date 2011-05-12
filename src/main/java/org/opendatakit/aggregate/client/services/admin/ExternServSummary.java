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

package org.opendatakit.aggregate.client.services.admin;

import java.io.Serializable;
import java.util.Date;

import org.opendatakit.aggregate.constants.common.OperationalStatus;

public class ExternServSummary implements Serializable{
  
  /**
   * Serialization identifier 
   */
  private static final long serialVersionUID = 29897237349781615L;
   
  private String uri; // NOTE: Do not display to the user, for internal service only
  private String user;
  private OperationalStatus status;
  private Date established;
  private String action;
  private String type;
  private String name;
  
  public ExternServSummary() {
    
  }

  public ExternServSummary(String uri, String user, OperationalStatus status, Date established, String action, String type, String name) {
    this.uri = uri;
    this.user = user;
    this.status = status;
    this.established = established;
    this.action = action;
    this.type = type;
    this.name = name;
  }

  public String getUser() {
    return user;
  }

  public OperationalStatus getStatus() {
    return status;
  }

  public Date getEstablished() {
    return established;
  }

  public String getAction() {
    return action;
  }

  public String getType() {
    return type;
  }

  public String getName() {
    return name;
  }

  public String getUri() {
    return uri;
  }
}
