package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExecuteDeleteSubmissionButton extends AbstractButtonBase implements ClickHandler {
 
  private static final String TOOLTIP_TEXT = "Delete Submission from database";
  
  private String submissionKeyAsString;
  private PopupPanel popup;
  
  public ExecuteDeleteSubmissionButton(String submissionKeyAsString, PopupPanel popup) {
    super("<img src=\"images/green_right_arrow.png\" /> Delete Submission", TOOLTIP_TEXT);
    this.submissionKeyAsString = submissionKeyAsString;
    this.popup = popup;
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
        AggregateUI.getUI().getTimer().refreshNow();
      }
    };
    // Make the call to the form service.
    SecureGWT.getFormAdminService().deleteSubmission(submissionKeyAsString, callback);
    popup.hide();
  }  
}
