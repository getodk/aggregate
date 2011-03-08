package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

public class ColumnFilter extends Filter implements Serializable {

  /**
   * Id for Serialization
   */
  private static final long serialVersionUID = -1045936241685471645L;

  private Visibility kr;

  public ColumnFilter() {
    super();
  }

  // TODO: Kyle please fix the fact we should pass a column header instead of a
  // title
  public ColumnFilter(Visibility keepRemove, String title, Long ordinal) {
    super(RowOrCol.COLUMN, new Column(title, ""), ordinal);
    this.kr = keepRemove;
  }

  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   */
  public ColumnFilter(String uri) {
    super(uri);
  }
  
  public Visibility getVisibility() {
    return kr;
  }

  public void setVisibility(Visibility kr) {
    this.kr = kr;
  }

}
