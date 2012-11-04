package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesManageTableFilesSubTab;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class OdkTablesShowTableFilesButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = 
      "<img src=\"images/green_right_arrow.png\" /> Files Associated with Table";
  private static final String TOOLTIP_TXT = "Display Files for this Table";
  private static final String HELP_BALLOON_TXT = "View the files that have " +
  		"been uploaded for this table.";
  
  // the table id of the button this table is tied to.
  private String tableId;
  // the parent table to which this table belongs
  private OdkTablesTableList parentTable;
  
  public OdkTablesShowTableFilesButton(OdkTablesTableList parentTable,
      String tableId) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.parentTable = parentTable;
    this.tableId = tableId;
  }
  
  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    OdkTablesManageTableFilesSubTab fileSubTab = 
        (OdkTablesManageTableFilesSubTab) AggregateUI.getUI()
        .getSubTab(SubTabs.MANAGEFILES);
    fileSubTab.setCurrentTable(tableId);
    fileSubTab.update();
    AggregateUI.getUI().redirectToSubTab(SubTabs.MANAGEFILES);
  }
}
