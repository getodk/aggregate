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

import org.opendatakit.aggregate.client.permissions.AccessConfigurationSheet;
import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.common.security.client.UserSecurityInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExecuteDeleteUserButton extends AbstractButtonBase implements ClickHandler {

	private static final String TOOLTIP_TEXT = "Remove this user";

	private static final String HELP_BALLOON_TXT = "Remove this user from the server.";

	private UserSecurityInfo user;
	private AccessConfigurationSheet sheet;
	private PopupPanel popup;

	public ExecuteDeleteUserButton(UserSecurityInfo user, AccessConfigurationSheet sheet, PopupPanel popup) {
		super("<img src=\"images/green_right_arrow.png\" /> Delete User", TOOLTIP_TEXT);
		this.user = user;
		this.sheet = sheet;
		this.popup = popup;
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	@Override
	public void onClick(ClickEvent event) {
		super.onClick(event);

		sheet.deleteUser(user);
		popup.hide();
	}  
}
