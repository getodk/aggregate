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
