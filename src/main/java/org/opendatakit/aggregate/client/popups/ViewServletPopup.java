package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;

public class ViewServletPopup extends PopupPanel {

  public ViewServletPopup(String buttonText, String url) {
    super(false);
    setTitle(buttonText);
    
    // so we can play with the dimensions...
    int innerWidth = Window.getClientWidth()*2 / 3;
    int innerHeight = Window.getClientHeight()*2 /3;
    
    Frame frame = new Frame(url);
    frame.setPixelSize(innerWidth,innerHeight); 

    DockLayoutPanel panel = new DockLayoutPanel(Unit.EM);
    panel.setPixelSize(innerWidth+6,innerHeight+30);
    panel.addNorth(new ClosePopupButton(this), 2);   
    panel.add(frame);      
    setWidget(panel);
  }
}
