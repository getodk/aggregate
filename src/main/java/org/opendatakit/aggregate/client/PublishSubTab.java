package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.permissions.ExternServSummary;
import org.opendatakit.aggregate.client.permissions.ServicesAdminServiceAsync;
import org.opendatakit.aggregate.client.table.PublishTable;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PublishSubTab extends VerticalPanel implements SubTabInterface {
  private static final String NO_FORM = "none";

  private AggregateUI baseUI;

  // ui elements
  private PublishTable publishTable;
  private ListBox formsBox;

  // state
  private FormSummary[] displayedFormList;
  private FormSummary selectedForm;

  public PublishSubTab(AggregateUI baseUI) {
    this.baseUI = baseUI;

    formsBox = new ListBox();
    formsBox.addChangeHandler(new ChangeDropDownHandler());
    publishTable = new PublishTable();

    // add tables to panels
    add(formsBox);
    add(publishTable);
  }

  public void update() {
    FormServiceAsync formSvc = SecureGWT.get().createFormService();

    // Set up the callback object.
    AsyncCallback<FormSummary[]> callback = new AsyncCallback<FormSummary[]>() {
      public void onFailure(Throwable caught) {
        baseUI.reportError(caught);
      }

      public void onSuccess(FormSummary[] forms) {
        baseUI.clearError();
        updateFormDropDown(forms);
      }
    };

    // Make the call to the form service.
    formSvc.getForms(callback);

    // Make the call to get the published services
    updatePublishTable();
  }

  public synchronized void updateFormDropDown(FormSummary[] formsFromService) {

    FormSummary currentFormSelected = null;

    FormSummary[] forms = formsFromService;

    if (forms == null || forms.length == 0) {
      forms = new FormSummary[1];
      forms[0] = new FormSummary(NO_FORM, null, null, false, false, null);
    } else {
      // get the previously selected form, and verify it matches
      int currentSelectionIndex = formsBox.getSelectedIndex();
      if (currentSelectionIndex >= 0) {
        String currentFormTitle = formsBox.getItemText(currentSelectionIndex);
        currentFormSelected = displayedFormList[currentSelectionIndex];
        if (currentFormSelected != null) {
          // double check that the titles match,
          // otherwise this would be a not fun bug to track down
          if (!currentFormTitle.equals(currentFormSelected.getTitle())) {
            currentFormSelected = null;
          }
        }
      }
    }

    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available

    formsBox.clear();
    
    // populate the form box
    for (int i = 0; i < forms.length; i++) {
      FormSummary form = forms[i];
      formsBox.insertItem(form.getTitle(), i);
      if (form.equals(currentFormSelected)) {
        selectedIndex = i;
      }
    }

    // update the panel to display the right form
    formsBox.setItemSelected(selectedIndex, true);

    // set the class state to the newly created form list
    displayedFormList = forms;
  }

  public synchronized void updatePublishTable() {
    ServicesAdminServiceAsync servicesAdminSvc = SecureGWT.get().createServicesAdminService();
    AsyncCallback<ExternServSummary[]> callback = new AsyncCallback<ExternServSummary[]>() {
      @Override
      public void onFailure(Throwable caught) {
        baseUI.reportError(caught);
      }

      @Override
      public void onSuccess(ExternServSummary[] result) {
        publishTable.updatePublishPanel(selectedForm.getId(), result);
      }
    };

    // request the update
    if (selectedForm == null) {
      return;
    }
    if (selectedForm.getId() != null) {
      servicesAdminSvc.getExternalServices(selectedForm.getId(), callback);
    }

  }

  /**
   * Handler to process the change in the form drop down
   * 
   */
  private class ChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      for (FormSummary form : displayedFormList) {
        if (form.getTitle().compareTo(formsBox.getValue(formsBox.getSelectedIndex())) == 0) {
          selectedForm = form;
          break;
        }
      }
      updatePublishTable();
    }
  }
}
