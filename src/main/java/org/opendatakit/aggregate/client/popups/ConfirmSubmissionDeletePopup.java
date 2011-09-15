package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public final class ConfirmSubmissionDeletePopup extends AbstractPopupBase {
  
  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete Submission";
  private static final String TOOLTIP_TXT = "Delete Submission from database";
  private static final String HELP_BALLOON_TXT = "This will delete the submission from the database.";

  private static final String DELETE_SUBMISSION_WARNING = "Are you sure you want to Delete the submission? Once delete the submission will be permanently removed from Aggregate's database";

  private final String submissionKeyAsString;

  public ConfirmSubmissionDeletePopup(String submissionKeyAsString) {
    super();

    this.submissionKeyAsString = submissionKeyAsString;

    FlexTable layout = new FlexTable();

    AggregateButton deleteButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new ExecuteDelete());
    
    HTML message = new HTML(DELETE_SUBMISSION_WARNING);
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  private class ExecuteDelete implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
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
      hide();
    }
  }
}
