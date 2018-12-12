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

package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.Date;
import org.opendatakit.aggregate.constants.common.ExportStatus;
import org.opendatakit.aggregate.constants.common.ExportType;

public final class ExportSummary implements Serializable {
  /**
   * Serialization Version ID
   */
  private static final long serialVersionUID = -8309784116473729045L;

  private String uri;
  private ExportType fileType;
  private Date timeRequested;
  private ExportStatus status;
  private Date timeLastAction;
  private Date timeCompleted;
  private String resultFile;

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public ExportType getFileType() {
    return fileType;
  }

  public void setFileType(ExportType fileType) {
    this.fileType = fileType;
  }

  public Date getTimeRequested() {
    return timeRequested;
  }

  public void setTimeRequested(Date timeRequested) {
    this.timeRequested = timeRequested;
  }

  public ExportStatus getStatus() {
    return status;
  }

  public void setStatus(ExportStatus status) {
    this.status = status;
  }

  public Date getTimeLastAction() {
    return timeLastAction;
  }

  public void setTimeLastAction(Date timeLastAction) {
    this.timeLastAction = timeLastAction;
  }

  public Date getTimeCompleted() {
    return timeCompleted;
  }

  public void setTimeCompleted(Date timeCompleted) {
    this.timeCompleted = timeCompleted;
  }

  public String getResultFile() {
    return resultFile;
  }

  public void setResultFile(String resultFile) {
    this.resultFile = resultFile;
  }


}
