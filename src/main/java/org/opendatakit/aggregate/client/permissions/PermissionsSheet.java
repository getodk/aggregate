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

package org.opendatakit.aggregate.client.permissions;

import org.opendatakit.aggregate.client.ManageTabUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.security.SecurityServiceAsync;
import org.opendatakit.common.security.common.GrantedAuthorityNames;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.LazyPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * Sub-tab under the Management tab for managing passwords and permissions.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class PermissionsSheet extends TabPanel {

	private static final String MANAGE_WEBSITE_ACCESS = "Manage Website Access";

	private HTML asIFrame(String url) {
		HTML panel = new HTML(
		"<iframe src=\"" + url + "\" width=\"1500\" height=\"3000\" />");
		panel.setStyleName("embedded_iframe");
		return panel;
	}

	static final String CONFIGURE_ACCESS = "access-configuration";
	static final String MANAGE_PASSWORDS = "manage-passwords";
	static final String CHANGE_PASSWORD = "change-user-password";
	static final String MANAGE_ACCESS = "access-management";
		
	private class ChangePasswordLazyPanel extends LazyPanel {
		
		ChangePasswordLazyPanel() {
		}

		@Override
		public void setVisible(boolean isVisible) {
			super.setVisible(isVisible);
			Widget widget = getWidget();
			if ( widget != null ) {
				// trigger update of UI...
				widget.setVisible(isVisible);
			}
			if ( isVisible ) {
				managementTab.setSubSelection(SubTabs.PERMISSIONS.getTabLabel(), CHANGE_PASSWORD);
			}
		}
		
		@Override
		protected Widget createWidget() {
			// return asIFrame(ssl/user-password");
			return new ChangePasswordSheet();
		}
	}
	public LazyPanel changePassword = new ChangePasswordLazyPanel();
	
	private class AccessConfigurationLazyPanel extends LazyPanel {
		
		AccessConfigurationLazyPanel() {
			
		}

		@Override
		public void setVisible(boolean isVisible) {
			super.setVisible(isVisible);
			Widget widget = getWidget();
			if ( widget != null ) {
				// trigger update of UI...
				widget.setVisible(isVisible);
			}
			if ( isVisible ) {
				managementTab.setSubSelection(SubTabs.PERMISSIONS.getTabLabel(), CONFIGURE_ACCESS);
			}
		}
		
		@Override
		protected Widget createWidget() {
			return new AccessConfigurationSheet();
		}
	}
	public LazyPanel accessConfiguration = new AccessConfigurationLazyPanel();
	
	
	private class ManageUserPasswordsLazyPanel extends LazyPanel {
		
		ManageUserPasswordsLazyPanel() {
		}

		@Override
		public void setVisible(boolean isVisible) {
			super.setVisible(isVisible);
			Widget widget = getWidget();
			if ( widget != null ) {
				// trigger update of UI...
				widget.setVisible(isVisible);
			}
			if ( isVisible ) {
				managementTab.setSubSelection(SubTabs.PERMISSIONS.getTabLabel(), MANAGE_PASSWORDS);
			}
		}
		
		@Override
		protected Widget createWidget() {
			// return asIFrame("ssl/user-manage-passwords");
			return new ManageUserPasswordsSheet();
		}
	}
	public LazyPanel manageUserPasswords = new ManageUserPasswordsLazyPanel();

	
	private class ManageConfigurationLazyPanel extends LazyPanel {
		
		ManageConfigurationLazyPanel() {
		}

		@Override
		public void setVisible(boolean isVisible) {
			super.setVisible(isVisible);
			Widget widget = getWidget();
			if ( widget != null ) {
				// trigger update of UI...
				widget.setVisible(isVisible);
			}
			if ( isVisible ) {
				managementTab.setSubSelection(SubTabs.PERMISSIONS.getTabLabel(), MANAGE_ACCESS);
			}
		}
		
		@Override
		protected Widget createWidget() {
			// return asIFrame("access/access-management");
			return new AccessManagementSheet();
		}
	}
	
	public LazyPanel manageConfiguration = new ManageConfigurationLazyPanel();

	public boolean isConfigured = false;
	
	ManageTabUI managementTab;
	
	public PermissionsSheet(ManageTabUI managementTab) {
		super();
		this.managementTab = managementTab;
	}

	private void lazyConfigure() {
		if ( !isConfigured ) {
			isConfigured = true;
			
			SecurityServiceAsync service = SecureGWT.get().createSecurityService();
			service.getUserInfo(new AsyncCallback<UserSecurityInfo>() {

				@Override
				public void onFailure(Throwable caught) {
					Window.alert("Unable to contact server");
					PermissionsSheet.this.isConfigured = false;
				}

				@Override
				public void onSuccess(UserSecurityInfo result) {
					if ( result.getGrantedAuthorities().contains(
							new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.toString())) ) {
						PermissionsSheet.this.add( accessConfiguration, MANAGE_WEBSITE_ACCESS);
						
						PermissionsSheet.this.add( manageUserPasswords, "Manage User Passwords");
				
						PermissionsSheet.this.add(manageConfiguration, "Advanced Website Access Management");
					}

					if ( result.getType() == UserType.REGISTERED ) {
						PermissionsSheet.this.add( changePassword, "Change Aggregate Password");
					}
					
					if ( PermissionsSheet.this.getWidgetCount() != 0 ) {
						PermissionsSheet.this.selectTab(0);
					}
				}
			});
		} else {
			if ( PermissionsSheet.this.getWidgetCount() != 0 ) {
				PermissionsSheet.this.selectTab(0);
			}
		}
	}
	
	@Override
	public void setVisible(boolean isVisible) {
		if ( isVisible ) {
			lazyConfigure();
		}
		super.setVisible(isVisible);
	}
}
