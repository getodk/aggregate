package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.OdkTablesViewTableSubTab;
import org.opendatakit.aggregate.client.table.OdkTablesTableList;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

/**
 * This button should move the UI to the view table subtab and display the
 * contents of that table.
 * <p>
 * It is written for the use case of looking at a list of all the tables in the
 * datastore, and wanting to view the contents of a table without having to
 * click the view table sub tab and then select the table from a dropdown. This
 * button should handle all of that.
 *
 * @author sudars
 *
 */
/*
 * Started by copying from OdkTablesDeleteTableButton
 */
public class OdkTablesShowTableButton extends AggregateButton
    implements ClickHandler {

  private static final String BUTTON_TXT =
      "<img src=\"images/green_right_arrow.png\" /> Table Data";
  private static final String TOOLTIP_TXT = "Display Table Contents";
  private static final String HELP_BALLOON_TXT = "View the contents of this" +
  		" table.";

  /**
   * This is the parent table to which this button belongs.
   */
  private OdkTablesTableList parentTable;

  private String tableId;

  public OdkTablesShowTableButton(OdkTablesTableList parentTable,
      String tableId, String tableName) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.parentTable = parentTable;
    this.tableId = tableId;
    this.setWidth("100%");
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    OdkTablesViewTableSubTab viewTableSubTab =
        (OdkTablesViewTableSubTab) AggregateUI.getUI()
        .getSubTab(SubTabs.VIEWTABLE);
    viewTableSubTab.setCurrentTable(tableId);
    viewTableSubTab.update();
    AggregateUI.getUI().redirectToSubTab(SubTabs.VIEWTABLE);
  }


}
