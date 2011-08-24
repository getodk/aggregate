package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Image;

public class HelpSlidePanelToggleButton extends AbstractImageToggleButton {

  private static final String TOOLTIP_TEXT = "Help Panel";
  
  private static final Image HELP_PANEL_ICON = new Image("images/help_icon.png");

  public HelpSlidePanelToggleButton() {
    super(HELP_PANEL_ICON, TOOLTIP_TEXT);
  }
  
  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    if(getValue()) {
      AggregateUI.getUI().displayHelpPanel();
    } else {
      AggregateUI.getUI().hideHelpPanel();
    }

  }
  
}
