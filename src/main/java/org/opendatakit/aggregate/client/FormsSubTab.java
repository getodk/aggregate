package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.FormTable;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FormsSubTab extends VerticalPanel implements SubTabInterface {
  // button text & styling
  private static final String SUBMISSION_TXT = "Upload Data";
  private static final String NEW_FORM_TXT = "New Form";
  private static final String UPLOAD_SUBMISSION_BUTTON_TEXT = "<img src=\"images/blue_up_arrow.png\" /> " + SUBMISSION_TXT;
  private static final String NEW_FORM_BUTTON_TEXT = "<img src=\"images/yellow_plus.png\" /> " + NEW_FORM_TXT;

  private FormTable listOfForms;
  private FlexTable navTable;

  public FormsSubTab(AggregateUI baseUI) {

    // create navigation buttons to servlet
    navTable = new FlexTable();
    navTable.setWidget(0, 0, new ServletPopupButton(NEW_FORM_BUTTON_TEXT, NEW_FORM_TXT,
        UIConsts.FORM_UPLOAD_SERVLET_ADDR, this));
    navTable.setWidget(0, 1, new ServletPopupButton(UPLOAD_SUBMISSION_BUTTON_TEXT, SUBMISSION_TXT,
        UIConsts.SUBMISSION_SERVLET_ADDR, this));

    // create form panel
    listOfForms = new FormTable();

    // add tables to panels
    add(navTable);
    add(listOfForms);
  }


  @Override
  public boolean canLeave() {
	  return true;
  }
  
  @Override
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
