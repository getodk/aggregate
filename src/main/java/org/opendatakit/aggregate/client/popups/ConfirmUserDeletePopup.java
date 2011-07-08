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

import org.opendatakit.aggregate.client.TemporaryAccessConfigurationSheet;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecuteDeleteUserButton;
import org.opendatakit.common.security.client.UserSecurityInfo;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmUserDeletePopup extends PopupPanel {

  public ConfirmUserDeletePopup(UserSecurityInfo userToDelete, TemporaryAccessConfigurationSheet sheet) {
    super(false);
    setModal(true);

    FlexTable layout = new FlexTable();
   
    HTML message;
    if ( sheet.isUiOutOfSyncWithServer() ) {
    	message = new HTML("Unsaved changes exist.<br/>"
						+ "<p>Proceeding will save all pending changes and<br/>permanently delete user <b>"
        + userToDelete.getCanonicalName()
        + "</b> on the server.</p>"
        + "<p>Do you wish to apply all pending changes and <br/>permanently delete this user?</p>");
    } else {
    	message = new HTML(
            "<p>Proceeding will permanently delete user <b>"
            + userToDelete.getCanonicalName()
            + "</b> on the server.</p><p>Do you wish to permanently delete this user?</p>");
    }
    layout.setWidget(0, 0, message);
    layout.setWidget(2, 0, new ExecuteDeleteUserButton(userToDelete, sheet, this));
    layout.setWidget(2, 1, new ClosePopupButton(this));

    setWidget(layout);
  }
}