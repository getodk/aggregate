package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ExportPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExportButton extends AButtonBase implements ClickHandler {
  private String formId;

  public ExportButton(String formId) {
    super("<img src=\"images/green_right_arrow.png\" /> Export");
    this.formId = formId;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    final PopupPanel popup = new ExportPopup(formId);
    popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = ((Window.getClientWidth() - offsetWidth) / 2);
        int top = ((Window.getClientHeight() - offsetHeight) / 2);
        popup.setPopupPosition(left, top);
      }
    });
  }

}