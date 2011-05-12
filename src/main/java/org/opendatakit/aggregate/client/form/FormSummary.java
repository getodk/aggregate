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

public class FormSummary implements Serializable {

  private static final long serialVersionUID = 5320217439717436812L;
  private String title;
  private String id;
  private String createdUser;
  private boolean download;
  private boolean receiveSubmissions;

  public FormSummary() {

  }

  public FormSummary(String formTitle, String formId, String formCreateUser, boolean download,
      boolean receiveSubmissions) {
    this.title = formTitle;
    this.id = formId;
    this.createdUser = formCreateUser;
    this.download = download;
    this.receiveSubmissions = receiveSubmissions;
  }

  public String getTitle() {
    return title;
  }

  public String getId() {
    return id;
  }

  public String getCreatedUser() {
    return createdUser;
  }

  public boolean isDownloadable() {
    return download;
  }

  public boolean receiveSubmissions() {
    return receiveSubmissions;
  }

  
  
}
