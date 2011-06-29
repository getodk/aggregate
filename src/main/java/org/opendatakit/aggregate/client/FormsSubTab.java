package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.FormTable;
import org.opendatakit.aggregate.client.widgets.RedirectButton;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FormsSubTab extends VerticalPanel implements SubTabInterface {

  // servlet URLS
  private static final String UPLOAD_SUBMISSION_URL = "ui/submission";
  private static final String FORM_UPLOAD_URL = "ui/upload";

  // button text & styling
  private static final String UPLOAD_SUBMISSION_BUTTON_TEXT = "<img src=\"images/blue_up_arrow.png\" /> Upload Data";
  private static final String NEW_FORM_BUTTON_TEXT = "<img src=\"images/yellow_plus.png\" /> New Form";

  private FormTable listOfForms;
  private FlexTable navTable;

  public FormsSubTab(AggregateUI baseUI) {

    // create navigation buttons to servlet
    navTable = new FlexTable();
    navTable.setWidget(0, 0, new RedirectButton(NEW_FORM_BUTTON_TEXT, FORM_UPLOAD_URL));
    navTable.setWidget(0, 1, new RedirectButton(UPLOAD_SUBMISSION_BUTTON_TEXT,
        UPLOAD_SUBMISSION_URL));

    // create form panel
    listOfForms = new FormTable();

    // add tables to panels
    add(navTable);
    add(listOfForms);
  }

  public void update() {
    // Set up the callback object.
    AsyncCallback<FormSummary[]> callback = new AsyncCallback<FormSummary[]>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(FormSummary[] forms) {
        AggregateUI.getUI().clearError();
        listOfForms.updateFormTable(forms);
      }
    };

    // Make the call to the form service.
    SecureGWT.getFormService().getForms(callback);

  }

}
