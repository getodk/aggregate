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

import static org.opendatakit.aggregate.client.LayoutUtils.buildVersionNote;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.externalserv.ExternServSummary;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.PublishTable;
import org.opendatakit.aggregate.client.widgets.FormListBox;
import org.opendatakit.aggregate.constants.common.HelpSliderConsts;
import org.opendatakit.aggregate.constants.common.PublishConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;

public class PublishSubTab extends AggregateSubTabBase {
 
  // ui elements
  private PublishTable publishTable;
  private FormListBox formsBox;

  // state
  private FormSummary selectedForm;

  public PublishSubTab() {
    // vertical
    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
     
    formsBox = new FormListBox(new ChangeDropDownHandler());
    publishTable = new PublishTable();

    FlexTable navTable = new FlexTable();
    navTable.setWidget(0, 0, new Label("Form: "));
    navTable.setWidget(0, 1, formsBox);
    
    // add tables to panels
    add(navTable);
    add(publishTable);
    add(buildVersionNote());
  }


  @Override
  public boolean canLeave() {
      return true;
  }

  private class UpdateAction implements AsyncCallback<ArrayList<FormSummary>> {
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    public void onSuccess(ArrayList<FormSummary> formsFromService) {
      AggregateUI.getUI().clearError();
      
      // setup the display with the latest updates
      formsBox.updateFormDropDown(formsFromService);
      
      // update the class state with the currently displayed form
      selectedForm = formsBox.getSelectedForm();
      
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
        AggregateUI.getUI().reportError(caught);
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
      FormSummary form = formsBox.getSelectedForm();
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
