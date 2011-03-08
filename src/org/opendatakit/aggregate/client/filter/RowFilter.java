package org.opendatakit.aggregate.client.filter;

import java.io.Serializable;

import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

public class RowFilter extends Filter implements Serializable {

  /**
   * Id for serialization
   */
  private static final long serialVersionUID = -482917672621588696L;

  private FilterOperation operation;
  private String input;

  public RowFilter() {
    super();
  }

  // TODO: Kyle I am not sure why RowFilter has keep/remove?
  // TODO: Kyle please fix the fact we should pass a column header instead of a
  // title
  public RowFilter(Visibility keepRemove, String title, FilterOperation compare, String inputParam,
      Long ordinal) {
    super(RowOrCol.ROW, new Column(title, ""), ordinal);
    this.operation = compare;
    this.input = inputParam;
  }

  /**
   * This constructor should only be used by the server
   * 
   * @param uri
   */
  public RowFilter(String uri) {
    super(uri);
  }

  public Visibility getVisibility() {
    // TODO: Kyle I am not sure why RowFilter has keep/remove?
    return Visibility.KEEP;
  }

  public FilterOperation getOperation() {
    return operation;
  }

  public void setOperation(FilterOperation operation) {
    this.operation = operation;
  }

  public String getInput() {
    return input;
  }

  public void setInput(String input) {
    this.input = input;
  }
}
