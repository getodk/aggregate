package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.UIConsts;

public class Filter implements Serializable {

  private static final long serialVersionUID = -5453093733004634508L;
  private String uri; // unique identifier
  private RowOrCol rc;
  private Column column;
  private Long ordinal; // order to display in filter group

  public Filter() {

  }

  public Filter(RowOrCol rowcol, Column column, long ordinal) {
    this.uri = UIConsts.URI_DEFAULT;
    this.rc = rowcol;
    this.column = column;
    this.ordinal = ordinal;
  }

  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   */
  public Filter(String uri) {
    this.uri = uri;
  }

  public String getUri() {
    return uri;
  }

  public RowOrCol getRc() {
    return rc;
  }

  public void setRc(RowOrCol rc) {
    this.rc = rc;
  }

  public String getTitle() {
    return column.getDisplayHeader();
  }

  public Long getOrdinal() {
    return ordinal;
  }

  public void setOrdinal(Long ordinal) {
    this.ordinal = ordinal;
  }

  public void setColumn(Column column) {
    this.column = column;
  }
  
  public Column getColumn() {
    return this.column;
  }
}