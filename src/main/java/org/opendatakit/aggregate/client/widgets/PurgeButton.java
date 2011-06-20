package org.opendatakit.aggregate.client.widgets;

import java.util.Date;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.popups.ConfirmPurgePopup;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PopupPanel;

public class PurgeButton extends AButtonBase implements ClickHandler {
  private String formId;
  private ExternServSummary externServ;

  public PurgeButton(String formId, ExternServSummary externalService) {
    super("Purge Published Data");
    this.formId = formId;
    this.externServ = externalService;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    Date earliest = null;
    switch (externServ.getPublicationOption()) {
    case UPLOAD_ONLY:
      if (externServ.getUploadCompleted()) {
        earliest = externServ.getTimeEstablished();
      } else {
        earliest = externServ.getTimeLastUploadCursor();
      }
      break;
    case UPLOAD_N_STREAM:
      if (externServ.getUploadCompleted()) {
        earliest = externServ.getTimeLastStreamingCursor();
        if (earliest == null) {
          earliest = externServ.getTimeEstablished();
        }
      } else {
        earliest = externServ.getTimeLastUploadCursor();
      }
      break;
    case STREAM_ONLY:
      earliest = externServ.getTimeLastStreamingCursor();
      if (earliest == null) {
        earliest = externServ.getTimeEstablished();
      }
      break;
    }

    StringBuilder b = new StringBuilder();
    if (earliest == null) {
      Window.alert("Data has not yet been published -- no data will be purged");
    } else {
      if (externServ.getPublicationOption() != ExternalServicePublicationOption.UPLOAD_ONLY) {
        b.append("<p><b>Note:</b> Even though the chosen publishing action involves an ongoing streaming"
            + " of data to the external service, this purge action is a one-time event and is "
            + "not automatically ongoing.  You will need to periodically repeat this process.</p>");
      }
      b.append("Click to confirm purge of <b>" + formId + "</b> submissions older than "
          + earliest.toString());

      // TODO: display pop-up with text from b...
      final ConfirmPurgePopup popup = new ConfirmPurgePopup(externServ, earliest, b.toString());
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
}