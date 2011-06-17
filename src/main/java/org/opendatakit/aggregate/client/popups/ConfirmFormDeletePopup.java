package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.form.FormAdminServiceAsync;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmFormDeletePopup extends PopupPanel implements ClickHandler {
  /**
   * 
   */
  private final String formId;

  public ConfirmFormDeletePopup(String formid) {
    super(false);
    formId = formid;

    FlexTable layout = new FlexTable();

    layout
        .setWidget(
            0,
            0,
            new HTML(
                "Delete all data and the form definition for <b>"
                    + formId
                    + "</b>?<br/>Do you wish to delete all uploaded data and the form definition for this form?"));

    Button deleteButton = new Button(
        "<img src=\"images/green_right_arrow.png\" /> Delete Data and Form");
    deleteButton.addClickHandler(this);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  @Override
  public void onClick(ClickEvent event) {
    // OK -- we are to proceed.
    // Set up the callback object.
    AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(Boolean result) {
        AggregateUI.getUI().clearError();
        Window.alert("Successfully scheduled this form's deletion.\n"
            + "It may take several minutes to delete all the "
            + "data submissions\nfor this form -- after which the "
            + "form definition itself will be deleted.");
        AggregateUI.getUI().getTimer().refreshNow();
      }
    };
    // Make the call to the form service.
    FormAdminServiceAsync formAdminSvc = SecureGWT.get().createFormAdminService();
    formAdminSvc.deleteForm(formId, callback);
    hide();
    AggregateUI.getUI().getTimer().restartTimer();
  }
}