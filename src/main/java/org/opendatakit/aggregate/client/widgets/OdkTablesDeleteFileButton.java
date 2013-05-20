package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateSubTabBase;

import com.google.gwt.event.dom.client.ClickEvent;

public class OdkTablesDeleteFileButton extends AggregateButton {


	  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" />";
	  private static final String TOOLTIP_TXT = "Delete File";
	  private static final String HELP_BALLOON_TXT = "Completely delete this file.";
	 
	  private String rowId;
	  private String tableId;
	  
	  private AggregateSubTabBase basePanel;
	  
	  public OdkTablesDeleteFileButton(AggregateSubTabBase basePanel, 
	      String tableId, String rowId) {
	    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
	    this.basePanel = basePanel;
	    this.tableId = tableId;
	    this.rowId = rowId;
	  }

	  @Override
	  public void onClick(ClickEvent event) {
	    super.onClick(event);
	    
	    OdkTablesConfirmDeleteFilePopup popup = 
	        new OdkTablesConfirmDeleteFilePopup(basePanel, tableId, rowId);
	    popup.setPopupPositionAndShow(popup.getPositionCallBack());
	  }
	
}
