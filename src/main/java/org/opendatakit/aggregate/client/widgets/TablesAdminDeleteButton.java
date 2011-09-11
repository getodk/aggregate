package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ConfirmDeleteTablesAdminPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public final class TablesAdminDeleteButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" />";
  private static final String TOOLTIP_TXT = "Delete user";
  private static final String HELP_BALLOON_TXT = "Remove the administrative user from being able to edit data.";

  private final String aggregateUid;

  public TablesAdminDeleteButton(String aggregateUid) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.aggregateUid = aggregateUid;
    addStyleDependentName("negative");
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    ConfirmDeleteTablesAdminPopup popup = new ConfirmDeleteTablesAdminPopup(aggregateUid);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }
}
