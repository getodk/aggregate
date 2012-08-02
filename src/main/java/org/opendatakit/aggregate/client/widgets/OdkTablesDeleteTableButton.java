package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.NewTablesAdminPopup;
import org.opendatakit.aggregate.client.popups.OdkTablesConfirmDeleteTablePopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class OdkTablesDeleteTableButton extends AggregateButton implements
		ClickHandler {

	  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete Table";
	  private static final String TOOLTIP_TXT = "Delete Table";
	  private static final String HELP_BALLOON_TXT = "Completely delete this table.";
	 
	  
	  private String tableId;
	  
	  public OdkTablesDeleteTableButton(String tableId) {
	    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
	    this.tableId = tableId;
	  }

	  @Override
	  public void onClick(ClickEvent event) {
	    super.onClick(event);
	    
	    OdkTablesConfirmDeleteTablePopup popup = new OdkTablesConfirmDeleteTablePopup(tableId);
	    popup.setPopupPositionAndShow(popup.getPositionCallBack());
	  }
	
}
