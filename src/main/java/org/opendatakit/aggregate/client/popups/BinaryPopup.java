package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;

public class BinaryPopup extends PopupPanel {

  public BinaryPopup(final String url) {
    super(false);
    setTitle("Binary");
    
    Frame frame = new Frame(url);
    frame.setPixelSize((Window.getClientWidth() / 2),(Window.getClientHeight() / 2)); 

    DockLayoutPanel panel = new DockLayoutPanel(Unit.EM);
    panel.setPixelSize((Window.getClientWidth() / 2)+6,(Window.getClientHeight() / 2)+30);
    panel.addNorth(new ClosePopupButton(this), 2);   
    panel.add(frame);      
    setWidget(panel);
  }
}
