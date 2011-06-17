package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ExportPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class CreateExportButton extends AButtonBase implements ClickHandler {

  private ExportPopup popup;

  public CreateExportButton(ExportPopup exportPopup) {
    super("<img src=\"images/green_right_arrow.png\" /> Export");
    this.popup = exportPopup;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    popup.createExport();

  }
}
