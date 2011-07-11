package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.PublishTable;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PublishSubTab extends VerticalPanel implements SubTabInterface {
 
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


  @Override
  public boolean canLeave() {
	  return true;
  }
  
  @Override
  public void update() {
    FormServiceAsync formSvc = SecureGWT.getFormService();

    // Set up the callback object.
    AsyncCallback<FormSummary[]> callback = new AsyncCallback<FormSummary[]>() {
      public void onFailure(Throwable caught) {
        baseUI.reportError(caught);
      }

      public void onSuccess(FormSummary[] forms) {
        baseUI.clearError();
        
        // update the class state with the updated form list
        displayedFormList = UIUtils.updateFormDropDown(formsBox, displayedFormList, forms);
        
        // setup the display with the latest updates
        selectedForm = UIUtils.getFormFromSelection(formsBox, displayedFormList);
        
        // Make the call to get the published services
        updatePublishTable();
      }
    };

    // Make the call to the form service.
    formSvc.getForms(callback);
  }

  public synchronized void updatePublishTable() {
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

    if (selectedForm == null) {
      return;
    }
    
    // request the update if form is not the "none" form (ie id will equal null)
    if (selectedForm.getId() != null) {
    	SecureGWT.getServicesAdminService().getExternalServices(selectedForm.getId(), callback);
    }

  }

  /**
   * Handler to process the change in the form drop down
   * 
   */
  private class ChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      FormSummary form = UIUtils.getFormFromSelection(formsBox, displayedFormList);
      if(form != null) {
        selectedForm = form;
      }
      updatePublishTable();
    }
  }
}
