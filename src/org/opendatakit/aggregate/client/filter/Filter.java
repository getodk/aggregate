package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.constants.common.ColumnVisibility;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.UIConsts;

public class Filter implements Serializable {

  /**
   * Id for serialization
   */
  private static final long serialVersionUID = -482917672621588696L;

  private String uri; // unique identifier
  private ColumnVisibility kr;
  private String col;
  private FilterOperation operation;
  private String input;
  private Long ordinal; // order to display in filter group

  public Filter() {

  }

  public Filter(ColumnVisibility keepRemove, String colName, FilterOperation compare,
      String inputParam, Long ordinal) {
    this.uri = UIConsts.URI_DEFAULT;
    this.kr = keepRemove;
    this.col = colName;
    this.operation = compare;
    this.input = inputParam;
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

  public void setVisibility(ColumnVisibility kr) {
    this.kr = kr;
  }

  public Long getOrdinal() {
    return ordinal;
  }

  public void setOrdinal(Long ordinal) {
    this.ordinal = ordinal;
  }

  public void setCol(String col) {
    this.col = col;
  }

  public void setOperation(FilterOperation operation) {
    this.operation = operation;
  }

  public void setInput(String input) {
    this.input = input;
  }

  public ColumnVisibility getVisibility() {
    return kr;
  }

  public String getCol() {
    return col;
  }

  public FilterOperation getOperation() {
    return operation;
  }

  public String getInput() {
    return input;
  }

}
