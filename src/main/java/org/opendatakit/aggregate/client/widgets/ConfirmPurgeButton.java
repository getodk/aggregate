package org.opendatakit.aggregate.client.widgets;

import java.util.Date;

import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.form.FormAdminServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmPurgeButton extends AButtonBase implements ClickHandler {
  private String uri;
  private PopupPanel popup;
  private Date earliest;

  public ConfirmPurgeButton(String uri, Date earliest, PopupPanel popup) {
    super("<img src=\"images/green_right_arrow.png\" /> Purge Data");
    this.uri = uri;
    this.popup = popup;
    this.earliest = earliest;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    // OK -- we are to proceed.
    FormAdminServiceAsync formAdminSvc = SecureGWT.get().createFormAdminService();
    formAdminSvc.purgePublishedData(uri, earliest, new AsyncCallback<Date>() {

      @Override
      public void onFailure(Throwable caught) {
        Window.alert("Failed purge of published data: " + caught.getMessage());

      }

      @Override
      public void onSuccess(Date result) {
        Window.alert("Successful commencement of the purge of " + "\nall data published as of "
            + result.toString());
      }
    });
    popup.hide();
  }

}