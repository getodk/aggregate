package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.OdkTablesAddNewTablePopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class OdkTablesAddTableButton extends AggregateButton implements
		ClickHandler {

	  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Add Table";
	  private static final String TOOLTIP_TXT = "Add a new table";
	  private static final String HELP_BALLOON_TXT = "Add a new table to the database.";

	  public OdkTablesAddTableButton() {
	    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
	  }

	  @Override
	  public void onClick(ClickEvent event) {
	    super.onClick(event);
	    
	    OdkTablesAddNewTablePopup popup = new OdkTablesAddNewTablePopup();
	    popup.setPopupPositionAndShow(popup.getPositionCallBack());
	  }
}
