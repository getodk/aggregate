package org.opendatakit.aggregate.client.form;

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
