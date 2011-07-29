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
