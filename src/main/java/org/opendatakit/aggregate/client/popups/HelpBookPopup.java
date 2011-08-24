package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HelpBookPopup extends PopupPanel {
  private VerticalPanel panel;
  
  public HelpBookPopup() {
    super(false);
    panel = new VerticalPanel();
   
    // populate the panel
    panel.add(new ClosePopupButton(this));
    panel.add(new Label("NO HELP CONTENT YET THE BOOK ON HELP NEEDS TO BE WRITTEN"));
    
    ScrollPanel scroll = new ScrollPanel(panel);
    scroll.setPixelSize((Window.getClientWidth() / 2),(Window.getClientHeight() / 2));
    setWidget(scroll);
  }
}
