package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.user.client.ui.Image;

public class HelpDialogsToggleButton extends AbstractImageToggleButton {
  
  private static final String TOOLTIP_TEXT = "Help Balloons";
  
  private static final Image HELP_DIALOG_ICON = new Image("images/help_dialog.jpg");
  
  public HelpDialogsToggleButton() {
    super(HELP_DIALOG_ICON, TOOLTIP_TEXT);
  }
  
}
