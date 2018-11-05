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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.SimplePanel;
import org.opendatakit.aggregate.client.widgets.HelpBookToggleButton;
import org.opendatakit.aggregate.client.widgets.HelpDialogsToggleButton;
import org.opendatakit.aggregate.client.widgets.HelpSlidePanelToggleButton;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.web.constants.HtmlConsts;

public class NavLinkBar extends SimplePanel {

  private static final String LOGOUT_URL_PATH = "j_spring_security_logout";
  private static final String LOGIN_URL_PATH = "relogin.html";

  private Anchor loginLogoutLink;
  private HelpSlidePanelToggleButton helpPanelToggleButton;
  private HelpBookToggleButton helpBookButton;
  private HelpDialogsToggleButton helpBalloonsToggleButton;

  public NavLinkBar() {
    getElement().setId("nav_bar_help_login");

    loginLogoutLink = new Anchor();
    loginLogoutLink.getElement().setId("nav_bar_help_login_item");

    helpPanelToggleButton = new HelpSlidePanelToggleButton();
    helpPanelToggleButton.getElement().setId("nav_bar_help_login_item");

    helpBookButton = new HelpBookToggleButton();
    helpBookButton.getElement().setId("nav_bar_help_login_item");

    helpBalloonsToggleButton = new HelpDialogsToggleButton();
    helpBalloonsToggleButton.getElement().setId("nav_bar_help_login_item");

    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, loginLogoutLink);
    layout.setWidget(0, 1, helpPanelToggleButton);
    layout.setWidget(0, 2, helpBookButton);
    layout.setWidget(0, 3, helpBalloonsToggleButton);
    layout.setWidget(0, 4, new HTML(HtmlConsts.TAB));
    add(layout);
  }

  public void update() {
    UserSecurityInfo userInfo = AggregateUI.getUI().getUserInfo();
    if ((userInfo != null) && (userInfo.getType() != UserType.ANONYMOUS)) {
      GWT.log("Setting logout link");
      loginLogoutLink.setHref(LOGOUT_URL_PATH);
      loginLogoutLink.setText("Log Out " + userInfo.getCanonicalName());
    } else {
      GWT.log("Setting login link");
      String login = LOGIN_URL_PATH;
      // preserve any query string (aids in GWT debugging)
      login += Window.Location.getQueryString();
      loginLogoutLink.setHref(login);
      loginLogoutLink.setText("Log In");
    }
    AggregateUI.resize();
  }

  public Boolean showHelpBalloons() {
    return helpBalloonsToggleButton.getValue();
  }
}
