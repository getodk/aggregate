package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;

public class Column implements Serializable {
  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -5276405259406410364L;
  
  private String displayHeader;
  private String columnEncoding;
  private Long geopointColumnCode;
  
  public Column() {;}
  
  public Column(String displayHeader, String columnName) {
    this.displayHeader = displayHeader;
    this.columnEncoding = columnName;
  }

  public Column(String displayHeader, String columnName, Long geopointColumnCode) {
    this.displayHeader = displayHeader;
    this.columnEncoding = columnName;
    this.geopointColumnCode = geopointColumnCode;
  }
  
  public String getDisplayHeader() {
    return displayHeader;
  }

  public String getColumnEncoding() {
    return columnEncoding;
  }
  
  public Long getGeopointColumnCode() {
    return geopointColumnCode;
  }
}