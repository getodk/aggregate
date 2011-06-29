package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExecuteDeleteFormButton extends AButtonBase implements ClickHandler {
 
  private String formId;
  private PopupPanel popup;
  
  public ExecuteDeleteFormButton(String formId, PopupPanel popup) {
    super("<img src=\"images/green_right_arrow.png\" /> Delete Data and Form");
    this.formId = formId;
    this.popup = popup;
    addClickHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
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
    SecureGWT.getFormAdminService().deleteForm(formId, callback);
    popup.hide();
  }

}
