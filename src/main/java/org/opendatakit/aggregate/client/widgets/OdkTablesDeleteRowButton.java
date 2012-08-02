package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.OdkTablesConfirmDeleteRowPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class OdkTablesDeleteRowButton extends AggregateButton implements
		ClickHandler {

	  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" />";
	  private static final String TOOLTIP_TXT = "Delete Table";
	  private static final String HELP_BALLOON_TXT = "Completely delete this table.";
	 
	  private String rowId;
	  private String tableId;
	  
	  public OdkTablesDeleteRowButton(String tableId, String rowId) {
	    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
	    this.tableId = tableId;
	    this.rowId = rowId;
	  }

	  @Override
	  public void onClick(ClickEvent event) {
	    super.onClick(event);
	    
	    OdkTablesConfirmDeleteRowPopup popup = new OdkTablesConfirmDeleteRowPopup(tableId,
	    		rowId);
	    popup.setPopupPositionAndShow(popup.getPositionCallBack());
	  }
	
}
