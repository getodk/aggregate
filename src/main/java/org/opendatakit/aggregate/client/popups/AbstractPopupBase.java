package org.opendatakit.aggregate.client.popups;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class AbstractPopupBase extends PopupPanel {

  public AbstractPopupBase() {
    super(false);
    setModal(true); // things not in the popup are inactive
    
    // Set glass behind the popup so that the things behind it are grayed out.
    this.setGlassEnabled(true);
    this.setGlassStyleName("gwt-PopupPanelGlassAggregate");
    
  }
  
  public PopupPanel.PositionCallback getPositionCallBack() {
    return new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
          int left = ((Window.getClientWidth() - offsetWidth) / 2);
          int top = ((Window.getClientHeight() - offsetHeight) / 2);
          setPopupPosition(left, top);
      }
    };
  }
  
}
