package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.widgets.HelpBookToggleButton;
import org.opendatakit.aggregate.client.widgets.HelpDialogsToggleButton;
import org.opendatakit.aggregate.client.widgets.HelpSlidePanelToggleButton;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.FlowPanel;

public class NavLinkBar extends FlowPanel {
  
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
    
    add(loginLogoutLink);
    add(helpPanelToggleButton);
    add(helpBookButton);
    add(helpBalloonsToggleButton);
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
  
  public Boolean showHelpBalloons() {
    return helpBalloonsToggleButton.getValue();
  }
}
