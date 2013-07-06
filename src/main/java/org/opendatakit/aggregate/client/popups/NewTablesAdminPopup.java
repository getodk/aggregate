/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

public final class NewTablesAdminPopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Create User";
  private static final String TOOLTIP_TXT = "Create a new user";
  private static final String HELP_BALLOON_TXT = "Create a new administrative user to edit data.";


  private final TextBox name;
  private final TextBox externalUid;

  public NewTablesAdminPopup() {
    super();

    name = new TextBox();
    externalUid = new TextBox();

    AggregateButton deleteButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new CreateUser());

    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new ClosePopupButton(this));
    layout.setWidget(0, 1, new HTML("Create a New User"));
    layout.setWidget(1, 0, new HTML("Name:"));
    layout.setWidget(1, 1, name);
    layout.setWidget(2, 0, new HTML("User ID:"));
    layout.setWidget(2, 1, externalUid);
    layout.setWidget(3, 1, deleteButton);

    setWidget(layout);
  }

  private class CreateUser implements ClickHandler {
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
          if (result) {
            Window.alert("Successfully added the user");
          } else {
            Window.alert("Error: unable to add the user!");
          }
          AggregateUI.getUI().getTimer().refreshNow();
        }
      };

      // Make the call to the odk tables user admin service.
      OdkTablesAdmin admin = new OdkTablesAdmin(name.getValue(), externalUid.getValue());
      SecureGWT.getOdkTablesAdminService().addAdmin(admin, callback);
      hide();
    }
  }

}