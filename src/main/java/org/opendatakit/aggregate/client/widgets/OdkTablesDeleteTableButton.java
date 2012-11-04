package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.NewTablesAdminPopup;
import org.opendatakit.aggregate.client.popups.OdkTablesConfirmDeleteTablePopup;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class OdkTablesDeleteTableButton extends AggregateButton implements
		ClickHandler {

	  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" />";
	  private static final String TOOLTIP_TXT = "Delete Table";
	  private static final String HELP_BALLOON_TXT = "Completely delete this table.";
	 
	  /**
	   * This is the parent table that contains the elements which this button
	   * is responsible for deleting. This is so you can call refresh on it
	   * without needing to refresh the whole page.
	   */
	  private OdkTablesTableList parentTable;
	  
	  private String tableId;
	  
	  public OdkTablesDeleteTableButton(OdkTablesTableList parentTable, 
	      String tableId) {
	    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
	    this.parentTable = parentTable;
	    this.tableId = tableId;
	  }

	  @Override
	  public void onClick(ClickEvent event) {
	    super.onClick(event);
	    
	    OdkTablesConfirmDeleteTablePopup popup = 
	        new OdkTablesConfirmDeleteTablePopup(parentTable, tableId);
	    popup.setPopupPositionAndShow(popup.getPositionCallBack());
	  }
	
}
