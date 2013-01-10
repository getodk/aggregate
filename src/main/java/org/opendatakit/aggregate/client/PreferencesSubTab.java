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

import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.widgets.EnableOdkTablesCheckbox;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.client.widgets.UpdateGMapsKeyButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextBox;

public class PreferencesSubTab extends AggregateSubTabBase {

  // Preferences tab
  private static final String GOOGLE_MAPS_API_KEY_LABEL = "See <a href=\"http://opendatakit.org/use/aggregate/oauth2-service-account/\"> for instructions on obtaining and supplying these values.<h2>Google Simple API Access API Key</h2></a>";
  private static final String GOOGLE_API_CLIENT_ID_LABEL = "<h2>Google OAuth2 Credentials</h2>";
  private static final String FEATURES_LABEL = "<h2>Aggregate Features</h2>";


  private static final String NEW_SERVICE_ACCOUNT_TXT = "Change Credentials";
  private static final String NEW_SERVICE_ACCOUNT_TOOLTIP_TXT = "Upload NEW Google Oauth2 Service Account information.";
  private static final String NEW_SERVICE_ACCOUNT_BALLOON_TXT = "Upload a NEW Google Oauth2 Service Account information to Aggregate.";
  private static final String NEW_SERVICE_ACCOUNT_BUTTON_TEXT = "<img src=\"images/yellow_plus.png\" /> "
      + NEW_SERVICE_ACCOUNT_TXT;

  private TextBox mapsApiKey;
  private TextBox googleApiClientId;
  private EnableOdkTablesCheckbox odkTablesEnable;

  public PreferencesSubTab() {
    // vertical
    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    HTML labelVersion = new HTML("<h2>Version Information</h2>");
    add(labelVersion);
    Label version = new Label();
    version.setStylePrimaryName("app_version_string");
    version.setText(UIConsts.VERSION_STRING);
    add(version);

    HTML labelMapsKey = new HTML(GOOGLE_MAPS_API_KEY_LABEL);
    add(labelMapsKey);

    mapsApiKey = new TextBox();
    mapsApiKey.setText(Preferences.getGoogleMapsApiKey());
    add(mapsApiKey);
    add(new UpdateGMapsKeyButton(mapsApiKey));

    HTML labelApiClientId = new HTML(GOOGLE_API_CLIENT_ID_LABEL);
    add(labelApiClientId);

    googleApiClientId = new TextBox();
    googleApiClientId.setText(Preferences.getGoogleApiClientId());
    googleApiClientId.setWidth("50em");
    add(googleApiClientId);


    ServletPopupButton newCredential = new ServletPopupButton(NEW_SERVICE_ACCOUNT_BUTTON_TEXT, NEW_SERVICE_ACCOUNT_TXT,
        UIConsts.SERVICE_ACCOUNT_PRIVATE_KEY_UPLOAD_ADDR, this, NEW_SERVICE_ACCOUNT_TOOLTIP_TXT, NEW_SERVICE_ACCOUNT_BALLOON_TXT);

    add(newCredential);
    // add(new UpdateGoogleClientCredentialsButton(googleApiClientId.getText()));

    HTML features = new HTML(FEATURES_LABEL);
    add(features);

    odkTablesEnable = new EnableOdkTablesCheckbox(Preferences.getOdkTablesEnabled());
    add(odkTablesEnable);
  }

  @Override
  public boolean canLeave() {
	  return true;
  }

  @Override
  public void update() {
    Preferences.updatePreferences();
    mapsApiKey.setText(Preferences.getGoogleMapsApiKey());
    googleApiClientId.setText(Preferences.getGoogleApiClientId());
    odkTablesEnable.updateValue(Preferences.getOdkTablesEnabled());
  }

}
