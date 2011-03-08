package org.opendatakit.aggregate.client.submission;

import java.io.Serializable;

public class Column implements Serializable {
  /**
   * Serialization Identifier
   */
  private static final long serialVersionUID = -5276405259406410364L;
  
  private String displayHeader;
  private String columnEncoding;
  
  public Column() {;}
  
  public Column(String displayHeader, String columnName) {
    this.displayHeader = displayHeader;
    this.columnEncoding = columnName;
  }

  public String getDisplayHeader() {
    return displayHeader;
  }

  public String getColumnEncoding() {
    return columnEncoding;
  }
}