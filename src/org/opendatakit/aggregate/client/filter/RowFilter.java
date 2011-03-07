package org.opendatakit.aggregate.client.filter;

import org.opendatakit.aggregate.constants.common.FilterOperation;
import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

public class RowFilter extends Filter {

  /**
   * Id for serialization
   */
  private static final long serialVersionUID = -482917672621588696L;

  public RowFilter() {
	  super();
  }

  public RowFilter(Visibility keepRemove, String title, 
		  FilterOperation compare, String inputParam, Long ordinal) {
	  super(keepRemove, RowOrCol.ROW, title, compare, inputParam, ordinal);
  }
}
