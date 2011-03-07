package org.opendatakit.aggregate.client.filter;

import org.opendatakit.aggregate.constants.common.RowOrCol;
import org.opendatakit.aggregate.constants.common.Visibility;

public class ColumnFilter extends Filter {

	/**
	 * Id for Serialization
	 */
	private static final long serialVersionUID = -1045936241685471645L;
	
	public ColumnFilter() {
		  super();
	  }

	  public ColumnFilter(Visibility keepRemove, String title, Long ordinal) {
		  super(keepRemove, RowOrCol.COLUMN, title, null, "", ordinal);
	  }
}
