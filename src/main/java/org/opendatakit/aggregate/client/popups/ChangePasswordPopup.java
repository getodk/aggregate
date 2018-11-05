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
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.PopupPanel;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecuteChangePasswordButton;
import org.opendatakit.common.security.client.UserSecurityInfo;

public class ChangePasswordPopup extends PopupPanel {
  private UserSecurityInfo user;
  private PasswordTextBox password1;
  private PasswordTextBox password2;

  public ChangePasswordPopup(UserSecurityInfo user) {
    super(false);

    this.user = user;

    password1 = new PasswordTextBox();
    password2 = new PasswordTextBox();

    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0,
        new HTML(new SafeHtmlBuilder().appendEscaped("Change Password for " + user.getUsername()).toSafeHtml()));
    layout.setWidget(1, 0, new HTML("Password:"));
    layout.setWidget(1, 1, password1);
    layout.setWidget(2, 0, new HTML("Password (again):"));
    layout.setWidget(2, 1, password2);

    layout.setWidget(3, 0, new ExecuteChangePasswordButton(this));
    layout.setWidget(3, 1, new ClosePopupButton(this));

    setWidget(layout);
  }

  public UserSecurityInfo getUser() {
    return user;
  }

  public PasswordTextBox getPassword1() {
    return password1;
  }

  public PasswordTextBox getPassword2() {
    return password2;
  }
}
