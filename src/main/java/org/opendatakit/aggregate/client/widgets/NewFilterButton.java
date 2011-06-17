package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.popups.FilterPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class NewFilterButton extends AButtonBase implements ClickHandler {

  private FilterSubTab basePanel;

  public NewFilterButton(FilterSubTab panel) {
    super("<img src=\"images/yellow_plus.png\" /> New Filter");
    basePanel = panel;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    final FilterPopup filterPopup = new FilterPopup(basePanel.getSubmissionTable(), basePanel.getCurrentlyDisplayedFilter());
    filterPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = (Window.getClientWidth() - offsetWidth) / 2;
        int top = (Window.getClientHeight() - offsetHeight) / 2;
        filterPopup.setPopupPosition(left, top);
      }
    });
    filterPopup.addCloseHandler(new CloseHandler<PopupPanel>() {

      @Override
      public void onClose(CloseEvent<PopupPanel> event) {
        basePanel.update();
      }

    });
  }

}