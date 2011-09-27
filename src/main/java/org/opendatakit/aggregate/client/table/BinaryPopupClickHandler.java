package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.BinaryPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

class BinaryPopupClickHandler implements ClickHandler {
  private final String value;

  public BinaryPopupClickHandler(String value) {
    this.value = value;
  }

  @Override
  public void onClick(ClickEvent event) {
    final PopupPanel popup = new BinaryPopup(value);
    popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = ((Window.getScrollLeft() + Window.getClientWidth() - offsetWidth) / 2);
        int top = ((Window.getScrollTop() + Window.getClientHeight() - offsetHeight) / 2);
        popup.setPopupPosition(left, top);
      }
    });
    AggregateUI.getUI().getTimer().restartTimer();
  }
}