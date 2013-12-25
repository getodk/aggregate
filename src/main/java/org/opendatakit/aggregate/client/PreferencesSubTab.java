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
import org.opendatakit.aggregate.client.preferences.Preferences.PreferencesCompletionCallback;
import org.opendatakit.aggregate.client.widgets.DisableFasterBackgroundActionsCheckbox;
import org.opendatakit.aggregate.client.widgets.EnableOdkTablesCheckbox;
import org.opendatakit.aggregate.client.widgets.ServletPopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class PreferencesSubTab extends AggregateSubTabBase {

  // Preferences tab
  private static final String INDENTED_STYLE = "indentedRegion";
  private static final String INDENTED_ENTRY_STYLE = "indentedEntryRegion";
  private static final String UNDEFINED_STYLE = "undefinedValue";
  private static final String DEFINED_STYLE = "definedValue";

  private static final String GOOGLE_API_CREDENTIALS_LABEL = "<h2>Google API Credentials</h2>";
  private static final String GOOGLE_API_CREDENTIALS_INFO = "<p>See <a href=\"http://opendatakit.org/use/aggregate/oauth2-service-account/\" target=\"_blank\">http://opendatakit.org/use/aggregate/oauth2-service-account/</a> for instructions on obtaining and supplying these values.</p>";
  private static final String GOOGLE_API_KEY_LABEL = "<h3>Simple API Access Key</h3>";
  private static final String GOOGLE_API_KEY_INFO = "<p>Recommended for accessing Google Maps.</p>";
  private static final String GOOGLE_API_CLIENT_ID_LABEL = "<h3>Google OAuth2 Credentials</h3>";
  private static final String GOOGLE_API_CLIENT_ID_INFO = "<p>Necessary for publishing to Google Spreadsheets and Fusion Tables</p>";
  private static final String FEATURES_LABEL = "<h2>Aggregate Features</h2>";

  private static final String NEW_SERVICE_ACCOUNT_TXT = "Change Google API Credentials";
  private static final String NEW_SERVICE_ACCOUNT_TOOLTIP_TXT = "Upload NEW Google Simple API Key and Oauth2 Service Account information.";
  private static final String NEW_SERVICE_ACCOUNT_BALLOON_TXT = "Upload a NEW Google Simple API Key and Oauth2 Service Account information to Aggregate.";
  private static final String NEW_SERVICE_ACCOUNT_BUTTON_TEXT = "<img src=\"images/yellow_plus.png\" /> "
      + NEW_SERVICE_ACCOUNT_TXT;

  private Label simpleApiKey;
  private Label googleApiClientId;
  private EnableOdkTablesCheckbox odkTablesEnable;
  private DisableFasterBackgroundActionsCheckbox disableFasterBackgroundActions;

  private PreferencesCompletionCallback settingsChange = new PreferencesCompletionCallback() {
    @Override
    public void refreshFromUpdatedPreferences() {
      setCredentialValues();
      disableFasterBackgroundActions.updateValue(Preferences.getFasterBackgroundActionsDisabled());
      odkTablesEnable.updateValue(Preferences.getOdkTablesEnabled());
    }

    @Override
    public void failedRefresh() {
      // Error message is displayed. Leave everything as-is.
    }
  };

  public PreferencesSubTab() {
    // vertical
    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    HTML labelVersion = new HTML("<h2>Version Information</h2>");
    add(labelVersion);
    Label version = new Label();
    version.setStylePrimaryName("app_version_string");
    version.setText(UIConsts.VERSION_STRING);
    add(version);

    HTML labelCredentialsSection = new HTML(GOOGLE_API_CREDENTIALS_LABEL);
    add(labelCredentialsSection);

    HTML labelCredentialsInfo = new HTML(GOOGLE_API_CREDENTIALS_INFO);
    labelCredentialsInfo.setStylePrimaryName(INDENTED_STYLE);
    add(labelCredentialsInfo);

    HTML labelApiKeyHeading = new HTML(GOOGLE_API_KEY_LABEL);
    labelApiKeyHeading.setStylePrimaryName(INDENTED_STYLE);
    add(labelApiKeyHeading);

    simpleApiKey = new Label();
    simpleApiKey.setStylePrimaryName(INDENTED_ENTRY_STYLE);
    add(simpleApiKey);

    HTML labelApiKeyInfo = new HTML(GOOGLE_API_KEY_INFO);
    labelApiKeyInfo.setStylePrimaryName(INDENTED_STYLE);
    add(labelApiKeyInfo);

    HTML labelApiClientIdHeading = new HTML(GOOGLE_API_CLIENT_ID_LABEL);
    labelApiClientIdHeading.setStylePrimaryName(INDENTED_STYLE);
    add(labelApiClientIdHeading);

    googleApiClientId = new Label();
    googleApiClientId.setStylePrimaryName(INDENTED_ENTRY_STYLE);
    add(googleApiClientId);

    HTML labelApiClientIdInfo = new HTML(GOOGLE_API_CLIENT_ID_INFO);
    labelApiClientIdInfo.setStylePrimaryName(INDENTED_STYLE);
    add(labelApiClientIdInfo);

    setCredentialValues();

    ServletPopupButton newCredential = new ServletPopupButton(NEW_SERVICE_ACCOUNT_BUTTON_TEXT,
        NEW_SERVICE_ACCOUNT_TXT, UIConsts.SERVICE_ACCOUNT_PRIVATE_KEY_UPLOAD_ADDR, this,
        NEW_SERVICE_ACCOUNT_TOOLTIP_TXT, NEW_SERVICE_ACCOUNT_BALLOON_TXT);
    newCredential.setStylePrimaryName(INDENTED_STYLE);
    add(newCredential);
    // add(new UpdateGMapsKeyButton(mapsApiKey));
    // add(new
    // UpdateGoogleClientCredentialsButton(googleApiClientId.getText()));

    HTML features = new HTML(FEATURES_LABEL);
    add(features);

    disableFasterBackgroundActions = new DisableFasterBackgroundActionsCheckbox(
        Preferences.getFasterBackgroundActionsDisabled(), settingsChange);
    add(disableFasterBackgroundActions);

    HTML br = new HTML("<br>");
    add(br);

    odkTablesEnable = new EnableOdkTablesCheckbox(Preferences.getOdkTablesEnabled(), settingsChange);
    add(odkTablesEnable);
  }

  @Override
  public boolean canLeave() {
    return true;
  }

  private void setCredentialValues() {
    String value;

    value = SafeHtmlUtils.fromString(Preferences.getGoogleSimpleApiKey()).asString();
    if (value.length() == 0) {
      value = "undefined";
      simpleApiKey.setStyleName(UNDEFINED_STYLE, true);
      simpleApiKey.setStyleName(DEFINED_STYLE, false);
    } else {
      simpleApiKey.setStyleName(UNDEFINED_STYLE, false);
      simpleApiKey.setStyleName(DEFINED_STYLE, true);
    }
    simpleApiKey.setText(value);

    value = SafeHtmlUtils.fromString(Preferences.getGoogleApiClientId()).asString();
    if (value.length() == 0) {
      value = "undefined";
      googleApiClientId.setStyleName(UNDEFINED_STYLE, true);
      googleApiClientId.setStyleName(DEFINED_STYLE, false);
    } else {
      googleApiClientId.setStyleName(UNDEFINED_STYLE, false);
      googleApiClientId.setStyleName(DEFINED_STYLE, true);
    }
    googleApiClientId.setText(value);
  }

  @Override
  public void update() {
    Preferences.updatePreferences(settingsChange);
  }

}
