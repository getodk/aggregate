package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.services.admin.ServicesAdminServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ImagePopup extends PopupPanel {

  public ImagePopup(final String url, final ServicesAdminServiceAsync servicesAdminSvc) {
    super(false);
    FlexTable layout = new FlexTable();

    layout.setWidget(0, 0, new HTML("<img src='" + url + "'/>"));
    Button closeButton = new Button("<img src=\"images/red_x.png\" />");
    closeButton.addStyleDependentName("close");
    closeButton.addStyleDependentName("negative");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
      }
    });
    layout.setWidget(0, 2, closeButton);

    setWidget(layout);
  }
}
