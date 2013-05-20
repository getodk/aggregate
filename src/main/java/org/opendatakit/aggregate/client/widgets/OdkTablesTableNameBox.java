package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.OdkTablesCurrentTablesSubTab;
import org.opendatakit.aggregate.client.popups.OdkTablesAddNewTablePopup;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class OdkTablesTableNameBox extends AggregateTextBox implements
		ValueChangeHandler<String> {

	  private static final int MAX_NUM_LEN = 4;
	  private static final String TOOLTIP_TXT = "Number of Submissions to Display per Page";
	  private static final String HELP_BALLOON_TXT = "Number of Submissions to Display per Page, navigation buttons 'previous' and 'next' buttons allow movement between pages";
	  
	  private final OdkTablesAddNewTablePopup popup;

	  public OdkTablesTableNameBox(OdkTablesAddNewTablePopup popup) {
	    super(TOOLTIP_TXT, HELP_BALLOON_TXT);
	    this.popup = popup;

	    setEnabled(true);
	  }

	  @Override
	  public void onValueChange(ValueChangeEvent<String> event) {
	    super.onValueChange(event);

	    // I don't think i actually need to do anything here...
	  }

	}
