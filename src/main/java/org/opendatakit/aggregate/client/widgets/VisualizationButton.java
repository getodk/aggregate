package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.popups.VisualizationPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class VisualizationButton extends AButtonBase implements ClickHandler {
  private FilterSubTab filterSubTab;
  
  public VisualizationButton(FilterSubTab filterSubTab) {
    super("<img src=\"images/bar_chart.png\" /> Visualize");
    this.filterSubTab = filterSubTab;
    addClickHandler(this);
  }

  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    final PopupPanel vizPopup = new VisualizationPopup(filterSubTab.getCurrentlyDisplayedFilter());
    vizPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
      @Override
      public void setPosition(int offsetWidth, int offsetHeight) {
        int left = (Window.getClientWidth() - offsetWidth) / 2;
        int top = (Window.getClientHeight() - offsetHeight) / 2;
        vizPopup.setPopupPosition(left, top);
      }
    });
  }
}
