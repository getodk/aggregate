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

import java.util.ArrayList;
import java.util.HashMap;

import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.HelpSliderConsts;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.logical.shared.BeforeSelectionEvent;
import com.google.gwt.event.logical.shared.BeforeSelectionHandler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AggregateUI implements EntryPoint {

	private UrlHash hash;
	private Label errorMsgLabel;
	
   private VerticalPanel wrappingLayoutPanel;	
	private HorizontalPanel layoutPanel;
	private VerticalPanel helpPanel;
	private TreeItem rootItem;

	private NavLinkBar settingsBar;
	
   private DecoratedTabPanel mainNav;

	// tab datastructures	
	private HashMap<Tabs, AggregateTabBase> tabMap;
	private ArrayList<Tabs> tabPosition;

	private RefreshTimer timer;

	private static AggregateUI singleton = null;

	/***********************************
	 ***** SINGLETON FETCHING ******
	 ***********************************/

	public static synchronized final AggregateUI getUI() {
		if (singleton == null) {
			// if you get here, you've put something in the AggregateUI()
			// constructor that should have been put in the onModuleLoad()
			// method.
			GWT.log("AggregateUI.getUI() called before singleton has been initialized");
		}
		return singleton;
	}

	public RefreshTimer getTimer() {
		return timer;
	}

	/***********************************
	 ***** INITIALIZATION ******
	 ***********************************/

	private AggregateUI() {
		/*
		 * CRITICAL NOTE: Do not do **anything** in this constructor that might
		 * cause something underneath to call AggregateUI.get()
		 * 
		 * The singleton is not yet assigned!!!
		 */
		singleton = null;
		timer = new RefreshTimer(this);

		// create tab datastructures
		tabMap = new HashMap<Tabs, AggregateTabBase>();
		tabPosition = new ArrayList<Tabs>();

		wrappingLayoutPanel = new VerticalPanel();
		errorMsgLabel = new Label();
		layoutPanel = new HorizontalPanel();
		helpPanel = new VerticalPanel();

		mainNav = new DecoratedTabPanel();
		mainNav.addStyleName("mainNav");

		settingsBar = new NavLinkBar();
		
		// Create help panel
		Tree helpTree = new Tree();
		rootItem = new TreeItem();
		helpTree.addItem(rootItem);

		helpPanel.add(helpTree);
		helpPanel.getElement().setId("help_panel");

		// add the error message info...
		errorMsgLabel.setStyleName("error_message");
		errorMsgLabel.setVisible(false);
		wrappingLayoutPanel.add(errorMsgLabel);
		wrappingLayoutPanel.add(layoutPanel);


		// add to layout
		layoutPanel.add(mainNav);
		layoutPanel.getElement().setId("layout_panel");

		RootPanel.get("dynamic_content").add(wrappingLayoutPanel);
		RootPanel.get("dynamic_content").add(settingsBar);
		RootPanel.get("dynamic_content").add(
				new HTML("<img src=\"images/odk_color.png\" id=\"odk_aggregate_logo\" />"));
	}

	private void addTabToDatastructures(AggregateTabBase tabPanel, Tabs tab) {
		int insertIndex = tabPosition.size();

		// add tabPanel into position
		tabPosition.add(insertIndex, tab);
		tabMap.put(tab, tabPanel);
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
	}

	private void updateSecurityInfo() {
		lastUserInfoUpdateAttemptTimestamp = lastRealmInfoUpdateAttemptTimestamp = System
		.currentTimeMillis();
		SecureGWT.getSecurityService().getUserInfo(new AsyncCallback<UserSecurityInfo>() {

			@Override
			public void onFailure(Throwable caught) {
				reportError(caught);
			}

			@Override
			public void onSuccess(UserSecurityInfo result) {
				userInfo = result;
				if (realmInfo != null && userInfo != null) {
					commonUserInfoUpdateCompleteAction();
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
				if (realmInfo != null && userInfo != null) {
					commonUserInfoUpdateCompleteAction();
				}
			}
		});
	}

	private void commonUserInfoUpdateCompleteAction() {
	   settingsBar.update();
		Preferences.updatePreferences();

		SubmissionTabUI submissions = new SubmissionTabUI(this);
		addTabToDatastructures(submissions, Tabs.SUBMISSIONS);

		ManageTabUI management = new ManageTabUI(this);
		addTabToDatastructures(management, Tabs.MANAGEMENT);

		AdminTabUI admin = new AdminTabUI(this);
		addTabToDatastructures(admin, Tabs.ADMIN);

		// Create the only tab that ALL users can see sub menu navigation
		mainNav.add(submissions, Tabs.SUBMISSIONS.getTabLabel());

		if (userInfo != null) {

			if (authorizedForTab(Tabs.MANAGEMENT)) {
				mainNav.add(management, Tabs.MANAGEMENT.getTabLabel());
			}

			if (authorizedForTab(Tabs.ADMIN)) {
				mainNav.add(admin, Tabs.ADMIN.getTabLabel());
			}

			// Select the correct menu item based on url hash.
			int selected = 0;
			String mainMenu = hash.get(UrlHash.MAIN_MENU);
			for (int i = 0; i < tabPosition.size() && i < mainNav.getWidgetCount() ; i++) {
				if (mainMenu.equals(tabPosition.get(i).getHashString())) {
					selected = i;
				}
			}
			mainNav.selectTab(selected); 

		}

		// AND schedule an async operation to
		// refresh the tabs that are not selected.
		Timer t = new Timer() {
			@Override
			public void run() {
				// warm up the underlying UI tabs...
				for (AggregateTabBase tab : tabMap.values()) {
					tab.warmUp();
				}
			}
		};
		t.schedule(1000);

		contentLoaded();
	}

	// Let's JavaScript know that the GWT content has been loaded
	// Currently calls into javascript/resize.js, if we add more JavaScript
	// then that should be changed.
	public native void contentLoaded() /*-{
		$wnd.gwtContentLoaded();
  }-*/;

	/***********************************
	 ****** NAVIGATION ******
	 ***********************************/

	public void redirectToSubTab(SubTabs subTab) {
		for (Tabs tab : tabPosition) {

			AggregateTabBase tabObj = tabMap.get(tab);
			if (tabObj == null) {
				continue;
			}

			SubTabInterface subTabObj = tabObj.getSubTab(subTab);
			if (subTabObj != null) {
				// found the tab
				int index = tabPosition.indexOf(tab);
				mainNav.selectTab(index);
				tabObj.selectTab(tabObj.findSubTabIndex(subTab));

			}
		}
	}

	public SubTabInterface getSubTab(SubTabs subTabType) {
		SubTabInterface subTab = null;

		for (AggregateTabBase tab : tabMap.values()) {
			subTab = tab.getSubTab(subTabType);
			if (subTab != null) {
				return subTab;
			}
		}

		return subTab;
	}

	void setSubMenuSelectionHandler(final TabPanel menuTab, final Tabs menu, final SubTabs[] subMenus) {
		// add the mainNav selection handler for this menu...
		mainNav.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (userInfo == null) {
					GWT.log("getSubMenuSelectionHandler: No userInfo - not setting selection");
					return;
				}

				int selected = event.getSelectedItem();
				String tabHash = menu.getHashString();

				Tabs tab = tabPosition.get(selected);

				if (tab == null) {
					return;
				}

				if (tabHash.equals(tab.getHashString())) {

					if (!authorizedForTab(tab)) {
						return;
					}

					// and select the appropriate subtab...
					AggregateTabBase tabObj = tabMap.get(menu);
					if (tabObj != null) {
						tabObj.selectTab(tabObj.findSubTabIndexFromHash(hash));
					}
				}
			}
		});

		menuTab.addBeforeSelectionHandler(new BeforeSelectionHandler<Integer>() {

			@Override
			public void onBeforeSelection(BeforeSelectionEvent<Integer> event) {
				// allow the currently-selected SubTab to refuse the tab selection.
				// refusal should only occur after user confirmation.
				if (!getTimer().canLeaveCurrentSubTab()) {
					event.cancel();
				}
			}

		});

		menuTab.addSelectionHandler(new SelectionHandler<Integer>() {
			@Override
			public void onSelection(SelectionEvent<Integer> event) {
				if (userInfo == null) {
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
				changeHelpPanel(subMenus[selected]);
			}
		});
	}

	native void redirect(String url)
	/*-{
		$wnd.location.replace(url);

  }-*/;

	/***********************************
	 ****** SECURITY ******
	 ***********************************/

	private boolean authorizedForTab(Tabs tab) {
		switch (tab) {
		case SUBMISSIONS:
			return userInfo.getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_DATA_VIEWER);
		case MANAGEMENT:
			return userInfo.getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_DATA_OWNER);
		case ADMIN:
			return userInfo.getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_SITE_ACCESS_ADMIN);
		default:
			return false;
		}
	}

	private long lastUserInfoUpdateAttemptTimestamp = 0L;
	private UserSecurityInfo userInfo = null;
	private long lastRealmInfoUpdateAttemptTimestamp = 0L;
	private RealmSecurityInfo realmInfo = null;

	public UserSecurityInfo getUserInfo() {
		if (userInfo == null) {
			GWT.log("AggregateUI.getUserInfo: userInfo is null");
		}
		if (lastUserInfoUpdateAttemptTimestamp + RefreshTimer.SECURITY_REFRESH_INTERVAL < System
				.currentTimeMillis()) {
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
				}
			});

		}
		return userInfo;
	}

	public RealmSecurityInfo getRealmInfo() {
		if (realmInfo == null) {
			GWT.log("AggregateUI.getRealmInfo: realmInfo is null");
		}
		if (lastRealmInfoUpdateAttemptTimestamp + RefreshTimer.SECURITY_REFRESH_INTERVAL < System
				.currentTimeMillis()) {
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
				}
			});
		}
		return realmInfo;
	}

	/***********************************
	 ****** HELP STUFF ******
	 ***********************************/

	private void changeHelpPanel(SubTabs subMenu) {
		// change root item
		rootItem.setText(subMenu + " Help");
		rootItem.removeItems();

		SubTabInterface subTabObj = getSubTab(subMenu);
      if (subTabObj != null) {
        HelpSliderConsts[] helpVals = subTabObj.getHelpSliderContent();
        for (int i = 0; i < helpVals.length; i++) {
          TreeItem helpItem = new TreeItem(helpVals[i].getTitle());
          TreeItem content = new TreeItem(helpVals[i].getContent());
          helpItem.setSelected(false);
          content.setSelected(false);
          helpItem.addItem(content);
          rootItem.addItem(helpItem);
       }
      }
	}
	
	  public void displayHelpPanel() {
	    wrappingLayoutPanel.add(helpPanel);
	    contentLoaded();
	  }

	  public void hideHelpPanel() {
	    wrappingLayoutPanel.remove(helpPanel);
	    contentLoaded();
	  }
	  
	  public Boolean displayingHelpBalloons() {
	    return settingsBar.showHelpBalloons();
	  }
	

	/***********************************
	 ****** ERROR STUFF ******
	 ***********************************/
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

}
