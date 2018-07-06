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

package org.opendatakit.aggregate.client.popups;

import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public final class ConfirmFormDeletePopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete Data and Form";
  private static final String TOOLTIP_TXT = "Delete data and form";
  private static final String HELP_BALLOON_TXT = "This will delete the form and all of the contained " +
        "data.";

  private final String formId;

  public ConfirmFormDeletePopup(String formId) {
    super();

    this.formId = formId;

    AggregateButton deleteButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new DeleteHandler());
    
    FlexTable layout = new FlexTable();

    HTML message = new HTML(new SafeHtmlBuilder()
        .appendEscaped("Delete all data and the form definition for ")
        .appendHtmlConstant("<b>"+formId+"</b><br/>")
        .appendEscaped("Do you wish to delete all uploaded data and the form definition for this form?")
        .toSafeHtml());
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  private class DeleteHandler implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      // OK -- we are to proceed.
      // Set up the callback object.
      AsyncCallback<Void> callback = new AsyncCallback<Void>() {
        @Override
        public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
        }

        @Override
        public void onSuccess(Void result) {
          AggregateUI.getUI().clearError();
          Window.alert("Successfully scheduled this form's deletion.\n"
                + "It may take several minutes to delete all the "
                + "data submissions\nfor this form -- after which the "
                + "form definition itself will be deleted.");
          AggregateUI.getUI().getTimer().refreshNow();
        }
      };
      // Make the call to the form service.
      SecureGWT.getFormAdminService().deleteForm(formId, callback);
      hide();
    }
  }
}