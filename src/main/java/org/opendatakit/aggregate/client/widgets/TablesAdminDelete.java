package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ConfirmDeleteTablesAdmin;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class TablesAdminDelete extends AButtonBase implements ClickHandler {
    
    private String aggregateUid;

    public TablesAdminDelete(String aggregateUid) {
      super("<img src=\"images/red_x.png\" />");
      this.aggregateUid = aggregateUid;
      addStyleDependentName("negative");
      addClickHandler(this);
    }

    @Override
    public void onClick(ClickEvent event) {
      super.onClick(event);
      
       // TODO: display pop-up with text from b...
       final ConfirmDeleteTablesAdmin popup = new ConfirmDeleteTablesAdmin(aggregateUid);
       popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
          @Override
          public void setPosition(int offsetWidth, int offsetHeight) {
              int left = ((Window.getScrollLeft() + Window.getClientWidth() - offsetWidth) / 2);
              int top = ((Window.getScrollTop() + Window.getClientHeight() - offsetHeight) / 2);
              popup.setPopupPosition(left, top);
          }
       });
    }

}
