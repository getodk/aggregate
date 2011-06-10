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
import org.opendatakit.aggregate.constants.common.PageUpdates;
import org.opendatakit.aggregate.constants.common.SubTabs;
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
	
	private static final String TOGGLE_AUTHENTICATION_STATUS = 
		"toggle-authentication-status";
	private static final String[] MAIN_MENU = {
		SubmissionTabUI.SUBMISSIONS, ManageTabUI.MANAGEMENT, 
		TOGGLE_AUTHENTICATION_STATUS};
	private UrlHash hash;
	private VerticalPanel wrappingLayoutPanel = new VerticalPanel();
	private Label errorMsgLabel = new Label();
	private HorizontalPanel layoutPanel = new HorizontalPanel();
	private VerticalPanel helpPanel = new VerticalPanel();
	private DecoratedTabPanel mainNav = new DecoratedTabPanel();
	private ManageTabUI manageNav = new ManageTabUI(this);
	private SubmissionTabUI submissionNav = new SubmissionTabUI(this);
	private SecurityServiceAsync identitySvc;
	private RefreshTimer timer;

	public AggregateUI() {
		SecureGWT sg = SecureGWT.get();
		identitySvc = sg.createSecurityService();
		Preferences.updatePreferences();
		setTimer(new RefreshTimer(this));
	}

	public void reportError(Throwable t) {
		if ( t instanceof 
				org.opendatakit.common.persistence.client.exception.DatastoreFailureException ) {
			errorMsgLabel.setText("Error: " + t.getMessage());
			errorMsgLabel.setVisible(true);
		} else if ( t instanceof
				org.opendatakit.common.security.client.exception.AccessDeniedException ) {
			errorMsgLabel.setText("You do not have permission for this action.\nError: " + t.getMessage());
			errorMsgLabel.setVisible(true);
		} else if ( t instanceof InvocationException ) {
			redirect( GWT.getHostPageBaseURL() + UIConsts.HOST_PAGE_BASE_ADDR);
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

	static final HTML togglePane = new HTML("<div>Selecting tab should toggle authentication status</div>");
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
		if ( userInfo != null ) {
			if ( userInfo.getType() != UserType.ANONYMOUS ) {
				link = LOGOUT_LINK;
			}
		}
		mainNav.add(togglePane,	link, true);
		toggleTabIndex = mainNav.getWidgetCount()-1;
	}
	
	private synchronized void updateUserSecurityInfo() {
		if ( securityService == null ) {
			securityService = SecureGWT.get().createSecurityService();
		}
		securityService.getUserInfo(new AsyncCallback<UserSecurityInfo>(){

			@Override
			public void onFailure(Throwable caught) {
			}

			@Override
			public void onSuccess(UserSecurityInfo result) {
				userInfo = result;
				updateTogglePane();
			}});
	}

	/*
	 * Creates a click handler for a main menu tab.
	 * Defaults to the first sub-menu tab.
	 * Does nothing if we're already on the tab clicked.
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
				if (s.equals(SubmissionTabUI.SUBMISSIONS))
					panel = submissionNav;
				else if (s.equals(ManageTabUI.MANAGEMENT))
					panel = manageNav;
				else if (s.equals(TOGGLE_AUTHENTICATION_STATUS)) {
					if ( userInfo != null && userInfo.getType() != UserType.ANONYMOUS ) {
						redirect( GWT.getHostPageBaseURL() + "/" + LOGOUT_URL_PATH);
					} else {
						redirect( GWT.getHostPageBaseURL() + "/" + LOGIN_URL_PATH);
					}
					return;
				}
				panel.selectTab(0);
				hash.put();
				timer.restartTimer();
			}
		};
	}

	@Override
	public void onModuleLoad() {
		// Get url hash.
		hash = UrlHash.getHash();
		hash.get();

		// Create sub menu navigation
		getTimer().restartTimer();
		update(SubTabs.FORM, PageUpdates.ALL);
		mainNav.add(submissionNav, "Submissions");
		mainNav.add(manageNav, "Management");
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
		//layoutPanel.add(helpContainer);

		// Select the correct menu item based on url hash.
		int selected = 0;
		String mainMenu = hash.get(UrlHash.MAIN_MENU);
		for (int i = 0; i < MAIN_MENU.length; i++)
			if (mainMenu.equals(MAIN_MENU[i]))
				selected = i;
		mainNav.selectTab(selected);

		// Add click handlers for each menu item
		for (int i = 0; i < MAIN_MENU.length; i++)
			mainNav.getTabBar().getTab(i).addClickHandler(getMainMenuClickHandler(MAIN_MENU[i]));

		RootPanel.get("dynamic_content").add(new HTML("<img src=\"images/odk_color.png\" id=\"odk_aggregate_logo\" />"));
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
   
	public void update(SubTabs tabs, PageUpdates update) {
		if(tabs.equals(SubTabs.FORM)) {
			submissionNav.getFormList(update);
			manageNav.getFormList(update);
		} else if (tabs.equals(SubTabs.FILTER)) {
			submissionNav.getFilterList(update);
		} else if (tabs.equals(SubTabs.PREFERENCES)) {
			manageNav.updatePreferencesPanel();
		} else if (tabs.equals(SubTabs.EXPORT)) {
			submissionNav.setupExportPanel();
		} else if (tabs.equals(SubTabs.PUBLISH)) {
			manageNav.setupPublishPanel();
		}
	}

	public void setTimer(RefreshTimer timer) {
		this.timer = timer;
	}
	public RefreshTimer getTimer() {
		return timer;
	}
	
	public ManageTabUI getManageNav() {
		return manageNav;
	}
	
	public SubmissionTabUI getSubmissionNav() {
		return submissionNav;
	}

}
