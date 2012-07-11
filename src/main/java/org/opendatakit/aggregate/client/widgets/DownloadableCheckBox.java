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
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public final class DownloadableCheckBox extends AggregateCheckBox implements
    ValueChangeHandler<Boolean> {

  private static final String TOOLTIP_TXT = "Allow or disallow form to be downloaded";
  private static final String HELP_BALLOON_TXT = "Check this box if you want your form to be "
      + "downloadable.  Otherwise leave unchecked.";

  private final String formId;

  public DownloadableCheckBox(String formId, Boolean downloadable) {
    super(null, false, TOOLTIP_TXT, HELP_BALLOON_TXT);
    this.formId = formId;
    setValue(downloadable);
    boolean enabled = AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_DATA_OWNER);
    setEnabled(enabled);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    super.onValueChange(event);
   
    SecureGWT.getFormAdminService().setFormDownloadable(formId, event.getValue(),
        new AsyncCallback<Void>() {
          @Override
          public void onFailure(Throwable caught) {
            AggregateUI.getUI().reportError(caught);
          }

          @Override
          public void onSuccess(Void v) {
            AggregateUI.getUI().clearError();
          }
        });
  }

}