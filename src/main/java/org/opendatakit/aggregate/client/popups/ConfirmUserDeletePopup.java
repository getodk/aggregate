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

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import org.opendatakit.aggregate.client.permissions.AccessConfigurationSheet;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.common.security.client.UserSecurityInfo;

public final class ConfirmUserDeletePopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete User";
  private static final String TOOLTIP_TXT = "Remove this user";
  private static final String HELP_BALLOON_TXT = "Remove this user from the server.";

  private final UserSecurityInfo user;
  private final AccessConfigurationSheet accessSheet;

  public ConfirmUserDeletePopup(UserSecurityInfo userToDelete, AccessConfigurationSheet sheet) {
    super();
    this.user = userToDelete;
    this.accessSheet = sheet;

    AggregateButton deleteButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        accessSheet.deleteUser(user);
        hide();
      }
    });

    FlexTable layout = new FlexTable();

    HTML message;
    if (sheet.isUiOutOfSyncWithServer()) {
      message = new HTML(new SafeHtmlBuilder()
          .appendEscaped("Unsaved changes exist.")
          .appendHtmlConstant("<br/>")
          .appendHtmlConstant("<p>Proceeding will save all pending changes and<br/>permanently delete user <b>")
          .appendEscaped(userToDelete.getCanonicalName())
          .appendHtmlConstant("</b> on the server.</p><p>Do you wish to apply all pending changes and <br/>permanently delete this user?</p>")
          .toSafeHtml());
    } else {
      message = new HTML(new SafeHtmlBuilder()
          .appendHtmlConstant("<p>Proceeding will permanently delete user <b>")
          .appendEscaped(userToDelete.getCanonicalName())
          .appendHtmlConstant("</b> on the server.</p><p>Do you wish to permanently delete this user?</p>")
          .toSafeHtml());
    }
    layout.setWidget(0, 0, message);
    layout.setWidget(2, 0, deleteButton);
    layout.setWidget(2, 1, new ClosePopupButton(this));

    setWidget(layout);
  }
}