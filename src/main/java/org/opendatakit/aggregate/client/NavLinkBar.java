package org.opendatakit.aggregate.client;

import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
import com.google.gwt.user.client.ui.ToggleButton;

public class NavLinkBar extends FlowPanel {
  
  private static final String LOGOUT_URL_PATH = "j_spring_security_logout";
  private static final String LOGIN_URL_PATH = "relogin.html";
  private static final Image HELP_PANEL_ICON = new Image("images/help_icon.png");
  private static final Image HELP_BOOK_ICON = new Image("images/help_book_icon.png");
  private static final Image HELP_DIALOG_ICON = new Image("images/help_dialog.jpg");
  
  private Anchor loginLogoutLink;
  private ToggleButton helpPanelToggleButton;
  private PushButton helpBookButton;
  private ToggleButton helpDialogToggleButton;
  
  public NavLinkBar() {
    getElement().setId("nav_bar_help_login");
    
    loginLogoutLink = new Anchor();
    loginLogoutLink.getElement().setId("nav_bar_help_login_item");

    helpPanelToggleButton = new ToggleButton(HELP_PANEL_ICON);
    helpPanelToggleButton.getElement().setId("nav_bar_help_login_item");
    
    helpBookButton = new PushButton(HELP_BOOK_ICON);
    helpBookButton.getElement().setId("nav_bar_help_login_item");
    
    helpDialogToggleButton = new ToggleButton(HELP_DIALOG_ICON);
    helpDialogToggleButton.getElement().setId("nav_bar_help_login_item");
    
    add(loginLogoutLink);
    add(helpPanelToggleButton);
    add(helpBookButton);
    add(helpDialogToggleButton);
    
  }
  
  public void update() {
     UserSecurityInfo userInfo = AggregateUI.getUI().getUserInfo();
     if ((userInfo != null) && (userInfo.getType() != UserType.ANONYMOUS)) {
        GWT.log("Setting logout link");
        loginLogoutLink.setHref(LOGOUT_URL_PATH);
        loginLogoutLink.setText("Log Out " + userInfo.getCanonicalName());
     } else {
        GWT.log("Setting login link");
        loginLogoutLink.setHref(LOGIN_URL_PATH);
        loginLogoutLink.setText("Log In");
     }
     AggregateUI.getUI().contentLoaded();
  }
}
