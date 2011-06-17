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

import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.security.SecurityServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AggregateUI implements EntryPoint {

  private static final String TOGGLE_AUTHENTICATION_STATUS = "toggle-authentication-status";
  private static final String[] MAIN_MENU = { Tabs.SUBMISSIONS.getTabLabel(), Tabs.MANAGEMENT.getTabLabel(),
      TOGGLE_AUTHENTICATION_STATUS };
  private UrlHash hash;
  private VerticalPanel wrappingLayoutPanel;
  private Label errorMsgLabel;
  private HorizontalPanel layoutPanel;
  private VerticalPanel helpPanel;
 
  private DecoratedTabPanel mainNav;
  private ManageTabUI manageNav;
  private SubmissionTabUI submissionNav;
 
  private RefreshTimer timer;

  private static AggregateUI singleton = null;

  public static synchronized final AggregateUI getUI() {
    if (singleton == null) {
      singleton = new AggregateUI();
    }
    return singleton;
  }

  private AggregateUI() {
    timer = new RefreshTimer(this);

    wrappingLayoutPanel = new VerticalPanel();
    errorMsgLabel = new Label();
    layoutPanel = new HorizontalPanel();
    helpPanel = new VerticalPanel();
    
    mainNav = new DecoratedTabPanel();
    manageNav = new ManageTabUI(this);
    submissionNav = new SubmissionTabUI(this);
    
    Preferences.updatePreferences();
  }

  public void reportError(Throwable t) {
    if (t instanceof org.opendatakit.common.persistence.client.exception.DatastoreFailureException) {
      errorMsgLabel.setText("Error: " + t.getMessage());
      errorMsgLabel.setVisible(true);
    } else if (t instanceof org.opendatakit.common.security.client.exception.AccessDeniedException) {
      errorMsgLabel
          .setText("You do not have permission for this action.\nError: " + t.getMessage());
      errorMsgLabel.setVisible(true);
    } else if (t instanceof InvocationException) {
      redirect(GWT.getHostPageBaseURL() + UIConsts.HOST_PAGE_BASE_ADDR);
    } else {
      errorMsgLabel.setText("Error: " + t.getMessage());
      errorMsgLabel.setVisible(true);
    }
  }

  public void clearError() {
    errorMsgLabel.setVisible(false);
    errorMsgLabel.setText("");
  }

  native void redirect(String url)
  /*-{
		$wnd.location.replace(url);

  }-*/;

  static final HTML togglePane = new HTML(
      "<div>Selecting tab should toggle authentication status</div>");
  static final String LOGOUT_URL_PATH = "j_spring_security_logout";
  static final String LOGOUT_LINK = "<a href=\"" + LOGOUT_URL_PATH + "\">Log Out</a>";
  static final String LOGIN_URL_PATH = "relogin.html";
  static final String LOGIN_LINK = "<a href=\"" + LOGIN_URL_PATH + "\">Log In</a>";
  private int toggleTabIndex = -1;
  private SecurityServiceAsync securityService = null;
  private UserSecurityInfo userInfo = null;

  private synchronized void updateTogglePane() {
    int idx = toggleTabIndex;
    toggleTabIndex = -1;
    if (idx != -1) {
      mainNav.remove(idx);
    }
    String link = LOGIN_LINK;
    if (userInfo != null) {
      if (userInfo.getType() != UserType.ANONYMOUS) {
        link = LOGOUT_LINK;
      }
    }
    mainNav.add(togglePane, link, true);
    toggleTabIndex = mainNav.getWidgetCount() - 1;
  }

  private synchronized void updateUserSecurityInfo() {
    if (securityService == null) {
      securityService = SecureGWT.get().createSecurityService();
    }
    securityService.getUserInfo(new AsyncCallback<UserSecurityInfo>() {

      @Override
      public void onFailure(Throwable caught) {
      }

      @Override
      public void onSuccess(UserSecurityInfo result) {
        userInfo = result;
        updateTogglePane();
      }
    });
  }

  /*
   * Creates a click handler for a main menu tab. Defaults to the first sub-menu
   * tab. Does nothing if we're already on the tab clicked.
   */
  private ClickHandler getMainMenuClickHandler(final String s) {
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        clearError();
        if (hash.get(UrlHash.MAIN_MENU).equals(s))
          return;
        hash.clear();
        hash.set(UrlHash.MAIN_MENU, s);
        TabPanel panel = null;
        if (s.equals(Tabs.SUBMISSIONS.getTabLabel())) {
          panel = submissionNav;
          panel.selectTab(0);
          timer.setCurrentSubTab(SubmissionTabUI.SUBMISSION_MENU[0]);
          hash.put();
        } else if (s.equals(Tabs.MANAGEMENT.getTabLabel())) {
          panel = manageNav;
          panel.selectTab(0);
          timer.setCurrentSubTab(ManageTabUI.MANAGEMENT_MENU[0]);
          hash.put();
        } else if (s.equals(TOGGLE_AUTHENTICATION_STATUS)) {
          if (userInfo != null && userInfo.getType() != UserType.ANONYMOUS) {
            redirect(GWT.getHostPageBaseURL() + "/" + LOGOUT_URL_PATH);
          } else {
            redirect(GWT.getHostPageBaseURL() + "/" + LOGIN_URL_PATH);
          }
        }
      }
    };
  }

  @Override
  public void onModuleLoad() {
    // Get url hash.
    hash = UrlHash.getHash();
    hash.get();

    // Create sub menu navigation
    mainNav.add(submissionNav, Tabs.SUBMISSIONS.getTabLabel());
    mainNav.add(manageNav, Tabs.MANAGEMENT.getTabLabel());
    updateTogglePane();
    mainNav.addStyleName("mainNav");

    // create help panel
    for (int i = 1; i < 5; i++) {
      helpPanel.add(new HTML("Help Content " + i));
    }
    helpPanel.setStyleName("help_panel");

    // add the error message info...
    errorMsgLabel.setStyleName("error_message");
    errorMsgLabel.setVisible(false);
    wrappingLayoutPanel.add(errorMsgLabel);
    wrappingLayoutPanel.add(layoutPanel);
    // add to layout
    layoutPanel.add(mainNav);
    layoutPanel.getElement().setId("layout_panel");
    FlowPanel helpContainer = new FlowPanel();
    helpContainer.add(helpPanel);
    helpContainer.getElement().setId("help_container");
    // layoutPanel.add(helpContainer);

    // Select the correct menu item based on url hash.
    int selected = 0;
    String mainMenu = hash.get(UrlHash.MAIN_MENU);
    for (int i = 0; i < MAIN_MENU.length; i++) {
      if (mainMenu.equals(MAIN_MENU[i])) {
        selected = i;
      }
    }
    mainNav.selectTab(selected);

    // Add click handlers for each menu item
    for (int i = 0; i < MAIN_MENU.length; i++) {
      mainNav.getTabBar().getTab(i).addClickHandler(getMainMenuClickHandler(MAIN_MENU[i]));
    }
    
    RootPanel.get("dynamic_content").add(
        new HTML("<img src=\"images/odk_color.png\" id=\"odk_aggregate_logo\" />"));
    RootPanel.get("dynamic_content").add(wrappingLayoutPanel);

    updateUserSecurityInfo();

    contentLoaded();
  }

  // Let's JavaScript know that the GWT content has been loaded
  // Currently calls into javascript/resize.js, if we add more JavaScript
  // then that should be changed.
  private native void contentLoaded() /*-{
		$wnd.gwtContentLoaded();
  }-*/;


  public RefreshTimer getTimer() {
    return timer;
  }

  public ManageTabUI getManageNav() {
    return manageNav;
  }

  public SubmissionTabUI getSubmissionNav() {
    return submissionNav;
  }

  
  ClickHandler getSubMenuClickHandler(final Tabs menu, final SubTabs subMenu) {
    return new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        clearError();
        getTimer().restartTimer();
        getTimer().setCurrentSubTab(subMenu);
        hash.clear();
        hash.set(UrlHash.MAIN_MENU, menu.getTabLabel());
        hash.set(UrlHash.SUB_MENU, subMenu.getTabLabel());
        hash.put();
      }
    };
  }
}
