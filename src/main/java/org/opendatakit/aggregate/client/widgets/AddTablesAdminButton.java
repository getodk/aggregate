package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.NewTablesAdminPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public final class AddTablesAdminButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Add User";
  private static final String TOOLTIP_TXT = "Add administrative user";
  private static final String HELP_BALLOON_TXT = "Add an administrative user with their phone id.";

  public AddTablesAdminButton() {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    NewTablesAdminPopup popup = new NewTablesAdminPopup();
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }
}