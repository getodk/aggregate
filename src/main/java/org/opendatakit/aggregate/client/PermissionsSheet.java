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

	private HTML asIFrame(String url) {
		HTML panel = new HTML(
		"<iframe src=\"" + url + "\" width=\"1500\" height=\"3000\" />");
		panel.setStyleName("embedded_iframe");
		return panel;
	}
	
	private static class ChangePasswordLazyPanel extends LazyPanel {
		
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
		}
		
		@Override
		protected Widget createWidget() {
			return new ChangePasswordSheet();
		}
	}
	public LazyPanel changePassword = new ChangePasswordLazyPanel();
	
	private static class AccessConfigurationLazyPanel extends LazyPanel {
		
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
		}
		
		@Override
		protected Widget createWidget() {
			return new AccessConfigurationSheet();
		}
	}
	public LazyPanel accessConfiguration = new AccessConfigurationLazyPanel();
	
	
	private static class ManageUserPasswordsLazyPanel extends LazyPanel {
		
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
		}
		
		@Override
		protected Widget createWidget() {
			return new ManageUserPasswordsSheet();
		}
	}
	public LazyPanel manageUserPasswords = new ManageUserPasswordsLazyPanel();

	public boolean isConfigured = false;
	
	public PermissionsSheet() {
		super();
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
					if ( result.getType() == UserType.REGISTERED ) {
						PermissionsSheet.this.add( changePassword, "Change AggregatePassword");
					}
					
					if ( result.getGrantedAuthorities().contains(
							new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.toString())) ) {
						PermissionsSheet.this.add( accessConfiguration, "Manage Website Access");
						
						PermissionsSheet.this.add( manageUserPasswords, "Manage User Passwords");
				
						PermissionsSheet.this.add(asIFrame("access/access-management"),
											"Advanced Website Access Management");
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
