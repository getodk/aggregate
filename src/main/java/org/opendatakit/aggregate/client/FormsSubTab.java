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

import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.FormTable;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.FormConsts;
import org.opendatakit.aggregate.constants.common.HelpSliderConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.rpc.AsyncCallback;

public class FormsSubTab extends AggregateSubTabBase {
  // button text & styling

  private static final String NEW_FORM_TXT = "Add New Form";
  private static final String NEW_FORM_TOOLTIP_TXT = "Upload NEW form";
  private static final String NEW_FORM_BALLOON_TXT = "Upload a NEW form to Aggregate.";
  private static final String NEW_FORM_BUTTON_TEXT = "<img src=\"images/yellow_plus.png\" /> "
      + NEW_FORM_TXT;


  private FormTable listOfForms;

  public FormsSubTab(AggregateUI baseUI) {
    // vertical
    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);
    
    // create navigation buttons to servlet
    
    ServletPopupButton newForm = new ServletPopupButton(NEW_FORM_BUTTON_TEXT, NEW_FORM_TXT,
        UIConsts.FORM_UPLOAD_SERVLET_ADDR, this, NEW_FORM_TOOLTIP_TXT, NEW_FORM_BALLOON_TXT);
    
    // create form panel
    listOfForms = new FormTable();

    // add tables to panels
    add(newForm);
    add(listOfForms);
  }

  @Override
  public boolean canLeave() {
    return true;
  }

  @Override
  public void update() {
    // Set up the callback object.
    AsyncCallback<ArrayList<FormSummary>> callback = new AsyncCallback<ArrayList<FormSummary>>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(ArrayList<FormSummary> forms) {
        AggregateUI.getUI().clearError();
        listOfForms.updateFormTable(forms);
      }
    };

    // Make the call to the form service.
    SecureGWT.getFormService().getForms(callback);

  }

  @Override
  public HelpSliderConsts[] getHelpSliderContent() {
    return FormConsts.values();
  }

}
