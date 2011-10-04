package org.opendatakit.aggregate.client.table;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.BinaryPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class BinaryPopupClickHandler implements ClickHandler {
  private final String value;
  private final boolean larger;

  public BinaryPopupClickHandler(String value, boolean larger) {
    this.value = value;
    this.larger = larger;
  }

  @Override
  public void onClick(ClickEvent event) {
    BinaryPopup popup = new BinaryPopup(value, larger);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
    AggregateUI.getUI().getTimer().restartTimer();
  }
}