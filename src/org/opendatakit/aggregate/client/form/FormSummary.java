package org.opendatakit.aggregate.client.form;

import java.io.Serializable;

public class FormSummary implements Serializable {

  private static final long serialVersionUID = 5320217439717436812L;
  private String title;
  private String id;
  private String createdUser;

  public FormSummary() {
    
  }
  
  public FormSummary(String formTitle, String formId, String formCreateUser) {
    this.title = formTitle;
    this.id = formId;
    this.createdUser = formCreateUser;
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

}
