/*
 * Copyright (C) 2014 University of Washington
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

import static org.opendatakit.aggregate.client.security.SecurityUtils.secureRequest;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.preferences.Preferences.PreferencesCompletionCallback;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

public class ChangeAppNamePopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Change ODK 2.0 App Name";
  private static final String TOOLTIP_TXT = "Change the Application Name used by the ODK 2.0 client application.";
  private static final String HELP_BALLOON_TXT = "This is the name of the Android directory under <tt>/sdcard/opendatakit</tt>.";

  private TextBox appNameBox;

  public ChangeAppNamePopup(PreferencesCompletionCallback settingsChange) {
    super();

    appNameBox = new TextBox();
    appNameBox.setText(Preferences.getAppName());

    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new HTML("Change ODK 2.0 App Name"));
    layout.setWidget(1, 0, new HTML("App Name:"));
    layout.setWidget(1, 1, appNameBox);

    AggregateButton changeAppNameButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    changeAppNameButton.addClickHandler(event -> {
      TextBox appNameBox = getAppName();

      String appName = appNameBox.getText();
      if (appName == null || appName.length() == 0) {
        Window.alert("ODK 2.0 App Name cannot be blank");
      } else if (!appName.equals(Preferences.getAppName())) {
        secureRequest(
            SecureGWT.getPreferenceService(),
            (rpc, sessionCookie, cb) -> rpc.setOdkAppName(appName, cb),
            () -> onSuccess(settingsChange),
            this::onError
        );
      }
    });

    layout.setWidget(3, 0, changeAppNameButton);
    layout.setWidget(3, 1, new ClosePopupButton(this));

    setWidget(layout);
  }

  private void onSuccess(PreferencesCompletionCallback settingsChange) {
    hide();
    AggregateUI.getUI().clearError();
    Preferences.updatePreferences(settingsChange);
  }

  private void onError(Throwable cause) {
    hide();
    AggregateUI.getUI().reportError(cause);
  }

  private TextBox getAppName() {
    return appNameBox;
  }

}
