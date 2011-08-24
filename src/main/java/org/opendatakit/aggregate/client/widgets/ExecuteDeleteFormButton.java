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

package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExecuteDeleteFormButton extends AbstractButtonBase implements ClickHandler {
 
  private static final String TOOLTIP_TEXT = UIConsts.EMPTY_STRING;
  
  private String formId;
  private PopupPanel popup;
  
  public ExecuteDeleteFormButton(String formId, PopupPanel popup) {
    super("<img src=\"images/green_right_arrow.png\" /> Delete Data and Form", TOOLTIP_TEXT);
    this.formId = formId;
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
        if ( result ) {
	        Window.alert("Successfully scheduled this form's deletion.\n"
	            + "It may take several minutes to delete all the "
	            + "data submissions\nfor this form -- after which the "
	            + "form definition itself will be deleted.");
        } else {
        	Window.alert("Error: unable to delete this form!");
        }
        AggregateUI.getUI().getTimer().refreshNow();
      }
    };
    // Make the call to the form service.
    SecureGWT.getFormAdminService().deleteForm(formId, callback);
    popup.hide();
  }  
}
