/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.PublishTable;
import org.opendatakit.aggregate.constants.common.HelpSliderConsts;
import org.opendatakit.aggregate.constants.common.PublishConsts;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.ListBox;

public class PublishSubTab extends AggregateSubTabBase {
 
  private AggregateUI baseUI;

  // ui elements
  private PublishTable publishTable;
  private ListBox formsBox;

  // state
  private ArrayList<FormSummary> displayedFormList;
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

  private class UpdateAction implements AsyncCallback<ArrayList<FormSummary>> {
    public void onFailure(Throwable caught) {
      baseUI.reportError(caught);
    }

    public void onSuccess(ArrayList<FormSummary> forms) {
      baseUI.clearError();
      
      // update the class state with the updated form list
      displayedFormList = UIUtils.updateFormDropDown(formsBox, displayedFormList, forms);
      
      // setup the display with the latest updates
      selectedForm = UIUtils.getFormFromSelection(formsBox, displayedFormList);
      
      // Make the call to get the published services
      updatePublishTable();
    }
  };

  @Override
  public void update() {

    if ( AggregateUI.getUI().getUserInfo().getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_DATA_OWNER)) {
    	formsBox.setVisible(true);
    	publishTable.setVisible(true);
	    FormServiceAsync formSvc = SecureGWT.getFormService();
	
	    // Make the call to the form service.
	    formSvc.getForms(new UpdateAction());
    } else {
    	formsBox.setVisible(false);
    	publishTable.setVisible(false);
    }
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
  
  @Override
  public HelpSliderConsts[] getHelpSliderContent() {
    return PublishConsts.values();
  }
}
