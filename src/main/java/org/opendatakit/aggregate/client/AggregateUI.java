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
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AggregateUI implements EntryPoint {

  private static final Tabs[] MAIN_MENU = { Tabs.SUBMISSIONS, Tabs.MANAGEMENT };
  private UrlHash hash;
  private VerticalPanel wrappingLayoutPanel;
  private Label errorMsgLabel;
  private HorizontalPanel layoutPanel;
  private VerticalPanel helpPanel;

  private DecoratedTabPanel mainNav;
  private ManageTabUI manageNav;
  private SubmissionTabUI submissionNav;

  private RefreshTimer timer;
  private HTMLPanel login_logout_link = new HTMLPanel("");

  private static AggregateUI singleton = null;

  public static synchronized final AggregateUI getUI() {
    if (singleton == null) {
      // if you get here, you've put something in the AggregateUI() 
      // constructor that should have been put in the onModuleLoad()
      // method.
      GWT.log("AggregateUI.getUI() called before singleton has been initialized");
    }
    return singleton;
  }

  private AggregateUI() {
	  /*
	   * CRITICAL NOTE:
	   * Do not do **anything** in this constructor
	   * that might cause something underneath to 
	   * call AggregateUI.get()
	   * 
	   *  The singleton is not yet assigned!!!
	   */
	singleton = null;
    timer = new RefreshTimer(this);

    wrappingLayoutPanel = new VerticalPanel();
    errorMsgLabel = new Label();
    layoutPanel = new HorizontalPanel();
    helpPanel = new VerticalPanel();

    mainNav = new DecoratedTabPanel();
    manageNav = new ManageTabUI(this);
    submissionNav = new SubmissionTabUI(this);

    // Create sub menu navigation
    mainNav.add(submissionNav, Tabs.SUBMISSIONS.getTabLabel());
    mainNav.add(manageNav, Tabs.MANAGEMENT.getTabLabel());
    mainNav.addStyleName("mainNav");

    // add the error message info...
    errorMsgLabel.setStyleName("error_message");
    errorMsgLabel.setVisible(false);
    wrappingLayoutPanel.add(errorMsgLabel);
    wrappingLayoutPanel.add(layoutPanel);
    // add to layout
    layoutPanel.add(mainNav);
    layoutPanel.getElement().setId("layout_panel");
    login_logout_link.getElement().setId("login_logout_link");

    RootPanel.get("dynamic_content").add(wrappingLayoutPanel);
    RootPanel.get("dynamic_content").add(login_logout_link);
    RootPanel.get("dynamic_content").add(new HTML("<img src=\"images/odk_color.png\" id=\"odk_aggregate_logo\" />"));
    
    updateTogglePane();
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

  static final String LOGOUT_URL_PATH = "j_spring_security_logout";
  static final HTML LOGOUT_LINK = new HTML("<a href=\"" + LOGOUT_URL_PATH + "\">Log Out</a>");
  static final String LOGIN_URL_PATH = "relogin.html";
  static final HTML LOGIN_LINK = new HTML("<a href=\"" + LOGIN_URL_PATH + "\">Log In</a>");

  private long lastUserInfoUpdateAttemptTimestamp = 0L;
  private UserSecurityInfo userInfo = null;
  private long lastRealmInfoUpdateAttemptTimestamp = 0L;
  private RealmSecurityInfo realmInfo = null;

  private void updateTogglePane() {
    if ((userInfo != null) && (userInfo.getType() != UserType.ANONYMOUS)) {
        GWT.log("Setting logout link");
        login_logout_link.clear();
        login_logout_link.add(LOGOUT_LINK);
    } else {
        GWT.log("Setting login link");
        login_logout_link.clear();
        login_logout_link.add(LOGIN_LINK);
    }
  }
  
  public UserSecurityInfo getUserInfo() {
	  if ( userInfo == null ) {
		  GWT.log("AggregateUI.getUserInfo: userInfo is null");
	  }
	  if ( lastUserInfoUpdateAttemptTimestamp + RefreshTimer.SECURITY_REFRESH_INTERVAL 
			  < System.currentTimeMillis() ) {
		  // record the attempt
		  lastUserInfoUpdateAttemptTimestamp = System.currentTimeMillis();
		  GWT.log("AggregateUI.getUserInfo: triggering refresh of userInfo");
		  SecureGWT.getSecurityService().getUserInfo(new AsyncCallback<UserSecurityInfo>() {

		        @Override
		        public void onFailure(Throwable caught) {
		      	  reportError(caught);
		        }

		        @Override
		        public void onSuccess(UserSecurityInfo result) {
		          userInfo = result;
				  if ( realmInfo != null && userInfo != null ) {
					commonUpdateCompleteAction();
				  }
		        }
		      });

	  }
	  return userInfo;
  }
  
  public RealmSecurityInfo getRealmInfo() {
	  if ( realmInfo == null ) {
		  GWT.log("AggregateUI.getRealmInfo: realmInfo is null");
	  }
	  if ( lastRealmInfoUpdateAttemptTimestamp + RefreshTimer.SECURITY_REFRESH_INTERVAL 
			  < System.currentTimeMillis() ) {
		  // record the attempt
		  lastRealmInfoUpdateAttemptTimestamp = System.currentTimeMillis();
		  GWT.log("AggregateUI.getRealmInfo: triggering refresh of realmInfo");
		  SecureGWT.getSecurityService().getRealmInfo(Cookies.getCookie("JSESSIONID"),
					new AsyncCallback<RealmSecurityInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				reportError(caught);
			}
			
			@Override
			public void onSuccess(RealmSecurityInfo result) {
				realmInfo = result;
				if ( realmInfo != null && userInfo != null ) {
					commonUpdateCompleteAction();
				}
			}
		});
	  }
	  return realmInfo;
  }
  
  private void commonUpdateCompleteAction() {
      updateTogglePane();
      Preferences.updatePreferences();
      
      // Select the correct menu item based on url hash.
      int selected = 0;
      String mainMenu = hash.get(UrlHash.MAIN_MENU);
      for (int i = 0; i < MAIN_MENU.length; i++) {
        if (mainMenu.equals(MAIN_MENU[i].getHashString())) {
          selected = i;
        }
      }
      mainNav.selectTab(selected);

      // AND schedule an async operation to 
      // refresh the tabs that are not selected.
      Timer t = new Timer() {
			@Override
			public void run() {
		        // warm up the underlying UI tabs...
		        manageNav.warmUp();
		        submissionNav.warmUp();
			}};
		t.schedule(1000);
  }
  
  private void updateSecurityInfo() {
	  lastUserInfoUpdateAttemptTimestamp = 
		  lastRealmInfoUpdateAttemptTimestamp = System.currentTimeMillis();
    SecureGWT.getSecurityService().getUserInfo(new AsyncCallback<UserSecurityInfo>() {

      @Override
      public void onFailure(Throwable caught) {
    	  reportError(caught);
      }

      @Override
      public void onSuccess(UserSecurityInfo result) {
        userInfo = result;
        if ( realmInfo != null && userInfo != null ) {
        	commonUpdateCompleteAction();
        }
      }
    });
    SecureGWT.getSecurityService().getRealmInfo(Cookies.getCookie("JSESSIONID"),
    											new AsyncCallback<RealmSecurityInfo>() {

        @Override
        public void onFailure(Throwable caught) {
      	  reportError(caught);
        }

        @Override
        public void onSuccess(RealmSecurityInfo result) {
          realmInfo = result;
          if ( realmInfo != null && userInfo != null ) {
          	commonUpdateCompleteAction();
          }
        }
      });
  }

  @Override
  public void onModuleLoad() {
    // Get url hash.
    hash = UrlHash.getHash();
    hash.get();
    errorMsgLabel.setVisible(false);
    userInfo = null;

    // assign the singleton here...
    singleton = this;
    
    // start the refresh timer...
    timer.setInitialized();

    // Update the user security info.
    // This gets the identity and privileges of the 
    // user to the UI and the realm of that user.
    // The success callback then renders the requested
    // page and warms up the various sub-tabs and 
    // displays the highlighted tab.
    updateSecurityInfo();
    
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
  
  SelectionHandler<Integer> getSubMenuSelectionHandler( final Tabs menu, final SubTabs[] subMenus) {
	  // add the mainNav selection handler for this menu...
	  mainNav.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if ( userInfo == null ) {
					GWT.log("getSubMenuSelectionHandler: No userInfo - not setting selection");
					return;
				}
				int selected = event.getSelectedItem();
				String tabHash = menu.getHashString();
				if ( tabHash.equals(MAIN_MENU[selected].getHashString())) {
					// OK: this is the handler instance for the selected top-level tab.
					//
					// if we are not already on this main tab (as indicated by the hash) or if there
					// is no hash specified for the subtab, go to subMenus[0]
					int selectedSubTab = 0;
					String mainHash = hash.get(UrlHash.MAIN_MENU);
					if ( mainHash != null && mainHash.equals(MAIN_MENU[selected].getHashString()) ) {
						// we haven't changed the hash -- see if
						// a specific subpage is specified...
						String subMenuHash = hash.get(UrlHash.SUB_MENU);
						if ( subMenuHash != null ) {
							for ( int i = 0 ; i < subMenus.length ; ++i ) {
								if ( subMenuHash.equals(subMenus[i].getHashString()) ) {
									// found the menu that should be selected...
									selectedSubTab = i;
								}
							}
						}
					}
					// and select the appropriate subtab...
					if ( menu == Tabs.MANAGEMENT ) {
						getManageNav().selectTab(selectedSubTab);
					} else if ( menu == Tabs.SUBMISSIONS ) {
						getSubmissionNav().selectTab(selectedSubTab);
					}
				}
			}
	  });

	  return new SelectionHandler<Integer>() {
		@Override
		public void onSelection(SelectionEvent<Integer> event) {
			if ( userInfo == null ) {
				GWT.log("getSubMenuSelectionHandler: No userInfo - not setting subMenu selection");
				return;
			}
			int selected = event.getSelectedItem();
	        clearError();
	        hash.clear();
	        hash.set(UrlHash.MAIN_MENU, menu.getHashString());
	        hash.set(UrlHash.SUB_MENU, subMenus[selected].getHashString());
	        getTimer().setCurrentSubTab(subMenus[selected]);
	        hash.put();
		}
	  };
  }
}
