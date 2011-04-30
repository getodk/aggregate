package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.UIConsts;

public class ColumnFilterHeader implements Serializable {

  private static final long serialVersionUID = -6420599052382340424L;

  private String uri; // unique identifier
  private Column column;

  public ColumnFilterHeader() {

  }

  public ColumnFilterHeader(Column column) {
    this.uri = UIConsts.URI_DEFAULT;
    this.column = column;
  }
  
  public ColumnFilterHeader(String displayHeader, String columnName, Long geopointCode) {
    this(new Column(displayHeader, columnName, geopointCode));
  }
  
  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   * @param column
   */
  public ColumnFilterHeader(String uri, Column column) {
    this.uri = uri;
    this.column = column;
  }
  
  public String getUri() {
    return uri;
  }
  
  public Column getColumn() {
    return column;
  }
  
}
