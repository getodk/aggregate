package org.opendatakit.aggregate.client.form;

import java.io.Serializable;
import java.util.Date;

public class ExternServSummary implements Serializable{
  
  /**
   * Serialization identifier 
   */
  private static final long serialVersionUID = 29897237349781615L;
  
  
  private String user;
  private String status;
  private Date established;
  private String action;
  private String type;
  private String name;
  
  public ExternServSummary() {
    
  }

  public ExternServSummary(String user, String status, Date established, String action, String type, String name) {
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

  public String getStatus() {
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
  
  
}
