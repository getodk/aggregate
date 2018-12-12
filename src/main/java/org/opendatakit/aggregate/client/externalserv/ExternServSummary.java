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

package org.opendatakit.aggregate.client.externalserv;

import java.io.Serializable;
import java.util.Date;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;
import org.opendatakit.aggregate.constants.common.OperationalStatus;

public final class ExternServSummary implements Serializable {

  /**
   * Serialization identifier
   */
  private static final long serialVersionUID = 29897237349781615L;

  private String uri; // NOTE: Do not display to the user, for internal
  // service only
  private String user;
  private OperationalStatus operationalStatus;
  private Date timeEstablished;
  private ExternalServicePublicationOption externalServicePublicationOption;
  private Boolean uploadCompleted;
  private Date timeLastUploadCursor;
  private Date timeLastStreamingCursor;
  private ExternalServiceType externalServiceType;
  private String ownership;
  private String name;

  public ExternServSummary() {

  }

  public ExternServSummary(String uri, String user, OperationalStatus status,
                           Date established,
                           ExternalServicePublicationOption externalServicePublicationOption,
                           Boolean uploadCompleted,
                           Date timeLastUploadCursor, Date timeLastStreamingCursor,
                           ExternalServiceType externalServiceTypeName, String ownership, String name) {
    this.uri = uri;
    this.user = user;
    this.operationalStatus = status;
    this.timeEstablished = established;
    this.externalServicePublicationOption = externalServicePublicationOption;
    this.uploadCompleted = uploadCompleted;
    this.timeLastUploadCursor = timeLastUploadCursor;
    this.timeLastStreamingCursor = timeLastStreamingCursor;
    this.externalServiceType = externalServiceTypeName;
    this.ownership = ownership;
    this.name = name;
  }

  public String getUser() {
    return user;
  }

  public OperationalStatus getStatus() {
    return operationalStatus;
  }

  public Date getTimeEstablished() {
    return timeEstablished;
  }

  public ExternalServicePublicationOption getPublicationOption() {
    return externalServicePublicationOption;
  }

  public Boolean getUploadCompleted() {
    return uploadCompleted;
  }

  public Date getTimeLastUploadCursor() {
    return timeLastUploadCursor;
  }

  public Date getTimeLastStreamingCursor() {
    return timeLastStreamingCursor;
  }

  public ExternalServiceType getExternalServiceType() {
    return externalServiceType;
  }

  public String getOwnership() {
    return ownership;
  }

  public String getName() {
    return name;
  }

  public String getUri() {
    return uri;
  }
}
