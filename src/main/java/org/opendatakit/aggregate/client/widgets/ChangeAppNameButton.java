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

package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import org.opendatakit.aggregate.client.popups.ChangeAppNamePopup;
import org.opendatakit.aggregate.client.preferences.Preferences.PreferencesCompletionCallback;

public class ChangeAppNameButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/yellow_plus.png\" /> Change ODK 2.0 App Name";
  private static final String TOOLTIP_TXT = "Change the Application Name used by the ODK 2.0 client application.";
  private static final String HELP_BALLOON_TXT = "This is the name of the Android directory under <tt>/sdcard/opendatakit</tt>.";

  private PreferencesCompletionCallback settingsChange;

  public ChangeAppNameButton(PreferencesCompletionCallback settingsChange) {
    super(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.settingsChange = settingsChange;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);

    ChangeAppNamePopup popup = new ChangeAppNamePopup(settingsChange);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }

}
