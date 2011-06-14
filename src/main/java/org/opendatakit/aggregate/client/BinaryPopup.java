package org.opendatakit.aggregate.client;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.PopupPanel;

public class BinaryPopup extends PopupPanel {

  public BinaryPopup(final String url) {
    super(false);
    setTitle("Binary");
    
    Frame frame = new Frame(url);
    frame.setPixelSize((Window.getClientWidth() / 2),(Window.getClientHeight() / 2)); 

    Button closeButton = new Button("<img src=\"images/red_x.png\" />");
    closeButton.addStyleDependentName("close");
    closeButton.addStyleDependentName("negative");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });

    DockLayoutPanel panel = new DockLayoutPanel(Unit.EM);
    panel.setPixelSize((Window.getClientWidth() / 2)+6,(Window.getClientHeight() / 2)+30);
    panel.addNorth(closeButton, 2);   
    panel.add(frame);      
    setWidget(panel);
  }
}
