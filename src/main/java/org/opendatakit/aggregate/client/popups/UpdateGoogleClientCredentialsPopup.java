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

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.AggregateBaseHandlers;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

public final class UpdateGoogleClientCredentialsPopup extends AbstractPopupBase {

  private static final String CLIENT_ID_TOOLTIP = "Client ID assigned to application by Google API Console";
  private static final String CLIENT_ID_HELP_BALLOON = "This updates the Google Client ID for OAuth2 access to Google services.";

  private static final String CLIENT_SECRET_TOOLTIP = "Client Secret assigned to application by Google API Console";
  private static final String CLIENT_SECRET_HELP_BALLOON = "This updates the Google Client Secret for OAuth2 access to Google services.";

  private static final String UPDATE_BUTTON_TEXT = "<img src=\"images/green_right_arrow.png\" /> Update";
  private static final String UPDATE_BUTTON_TOOLTIP = "Update Google Client Credentials";
  private static final String UPDATE_BUTTON_HELP_BALLOON = "This updates the Google Client ID and Client Secret for OAuth2 access to Google services.";

  // this will be the main flex table for the popups
  private final FlexTable layout;

  private final AggregateButton updateButton;

  private final TextBox clientId = new TextBox();
  private final TextBox clientSecret = new TextBox();
  private final UpdateClientCredentialsHandler handler = new UpdateClientCredentialsHandler();

  public UpdateGoogleClientCredentialsPopup(String clientId) {
    super();
    this.clientId.setText(clientId);
    this.clientId.setWidth("50em");
    this.clientSecret.setWidth("50em");
    this.clientId.addChangeHandler(handler);
    this.clientSecret.addChangeHandler(handler);
    this.clientSecret.addKeyUpHandler(handler);
    AggregateBaseHandlers handlers = new AggregateBaseHandlers(this, CLIENT_ID_TOOLTIP, CLIENT_ID_HELP_BALLOON);  
    this.clientId.addMouseOverHandler(handlers);
    this.clientId.addMouseOutHandler(handlers);
    handlers = new AggregateBaseHandlers(this, CLIENT_SECRET_TOOLTIP, CLIENT_SECRET_HELP_BALLOON);  
    this.clientSecret.addMouseOverHandler(handlers);
    this.clientSecret.addMouseOutHandler(handlers);
    
    updateButton = new AggregateButton(UPDATE_BUTTON_TEXT, UPDATE_BUTTON_TOOLTIP,
        UPDATE_BUTTON_HELP_BALLOON);
    updateButton.addClickHandler(handler);

    // set the standard header widgets
    layout = new FlexTable();
    layout.addStyleName("stretch_header");
    layout.setWidget(0, 0, new HTML("<h2> Client ID:</h2>"));
    layout.setWidget(0, 1, this.clientId);
    layout.setWidget(1, 0, new HTML("<h2>Client Secret:</h2>"));
    layout.setWidget(1, 1, this.clientSecret);
    layout.setWidget(2, 0, updateButton);
    layout.setWidget(2, 1, new ClosePopupButton(this));
    
    updateUIOptions();

    setWidget(layout);
  }


  public void updateUIOptions() {
    updateButton.setEnabled( clientId.getText().length() != 0 
                              && clientSecret.getText().length() != 0 );
  }

  private class UpdateClientCredentialsCallback implements AsyncCallback<Void> {

    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(Void unused) {
      hide();
    }
  }

  private class UpdateClientCredentialsHandler implements ClickHandler, ChangeHandler, KeyUpHandler {
    @Override
    public void onClick(ClickEvent event) {

      SecureGWT.getPreferenceService().setGoogleApiClientCredentials(clientId.getText(), clientSecret.getText(),
            new UpdateClientCredentialsCallback());

    }

    @Override
    public void onChange(ChangeEvent event) {
      updateUIOptions();     
    }

    @Override
    public void onKeyUp(KeyUpEvent event) {
      updateUIOptions();    
    }
  }
  
}
