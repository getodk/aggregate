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

package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.preferences.Preferences.PreferencesCompletionCallback;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class EnableFasterPublishingCheckbox extends AggregateCheckBox implements
    ValueChangeHandler<Boolean> {

  private static final String LABEL = "Enable faster publishing (can increase quota usage on Google AppEngine)";
  private static final String TOOLTIP_TXT = "Enable/Disable Faster Publishing";
  private static final String HELP_BALLOON_TXT = "Check this box if you want to speed the publishing of data.  "
      + "Otherwise leave unchecked. Speeding the publishing of data will prevent the application from sleeping "
      + "and may increase quota usage on Google AppEngine.";

  public EnableFasterPublishingCheckbox(Boolean enabled) {
    super(LABEL, false, TOOLTIP_TXT, HELP_BALLOON_TXT);
    setValue(enabled);
    boolean accessable = AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_SITE_ACCESS_ADMIN);
    setEnabled(accessable);
  }

  public void updateValue(Boolean value) {
    Boolean currentValue = getValue();
    if (currentValue != value) {
      setValue(value);
    }
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    super.onValueChange(event);

    final Boolean enabled = event.getValue();
    SecureGWT.getPreferenceService().setFasterPublishingEnabled(enabled, new AsyncCallback<Void>() {
      @Override
      public void onFailure(Throwable caught) {
        // restore old value
        setValue(Preferences.getFasterPublishingEnabled());
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(Void result) {
        AggregateUI.getUI().clearError();
        Preferences.updatePreferences(new PreferencesCompletionCallback() {
			@Override
			public void refreshFromUpdatedPreferences() {
			}});
      }
    });
  }
}