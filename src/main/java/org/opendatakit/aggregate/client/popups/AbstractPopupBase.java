package org.opendatakit.aggregate.client.popups;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class AbstractPopupBase extends PopupPanel {

  public AbstractPopupBase() {
    super(false);
    setModal(true);
    
  }
  
  public PopupPanel.PositionCallback getPositionCallBack() {
    return new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
          int left = ((Window.getScrollLeft() + Window.getClientWidth() - offsetWidth) / 2);
          int top = ((Window.getScrollTop() + Window.getClientHeight() - offsetHeight) / 2);
          setPopupPosition(left, top);
      }
    };
  }
  
}
