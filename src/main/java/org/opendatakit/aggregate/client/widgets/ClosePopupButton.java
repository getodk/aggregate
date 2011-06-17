package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.PopupPanel;

public class ClosePopupButton extends AButtonBase implements ClickHandler {
  private PopupPanel popup;

  public ClosePopupButton(PopupPanel popup) {
    super("<img src=\"images/red_x.png\" />");
    this.popup = popup;
    addStyleDependentName("close");
    addStyleDependentName("negative");
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    popup.hide();
  }

}