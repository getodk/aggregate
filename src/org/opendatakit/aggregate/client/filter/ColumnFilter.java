package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

public class ColumnFilter extends Filter implements Serializable {

  /**
   * Id for Serialization
   */
  private static final long serialVersionUID = -1045936241685471645L;

  private List<ColumnFilterHeader> columns;
  private Visibility kr;

  public ColumnFilter() {
    super();
  }

  // TODO: Kyle please fix the fact we should pass a column header instead of a
  // title
  public ColumnFilter(Visibility keepRemove, List<ColumnFilterHeader> columns, Long ordinal) {
    super(RowOrCol.COLUMN, ordinal);
    this.kr = keepRemove;
    this.columns = columns;
  }

  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   */
  public ColumnFilter(String uri) {
    super(uri);
    this.columns = new ArrayList<ColumnFilterHeader>();
  }
  
  public Visibility getVisibility() {
    return kr;
  }

  public void setVisibility(Visibility kr) {
    this.kr = kr;
  }

  public List<ColumnFilterHeader> getColumnFilterHeaders() {
    return columns;
  }

  public void setColumnFilterHeaders(List<ColumnFilterHeader> columns) {
    this.columns = columns;
  }

  public void addColumnFilterHeader(ColumnFilterHeader column) {
    this.columns.add(column);
  }
  
}
