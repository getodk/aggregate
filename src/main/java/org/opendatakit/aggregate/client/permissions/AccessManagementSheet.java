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

import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserClassSecurityInfo;
import org.opendatakit.common.security.client.security.admin.SecurityAdminServiceAsync;
import org.opendatakit.common.security.common.GrantedAuthorityNames;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

/**
 * Rewrite of the AccessManagementServlet for GWT
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class AccessManagementSheet extends Composite {

	private static final String MODIFY_GROUP_URL = "access/group-management";
	private static final String MODIFY_USERS_URL = "access/user-management";
	private static AccessManagementSheetUiBinder uiBinder = GWT
			.create(AccessManagementSheetUiBinder.class);

	interface AccessManagementSheetUiBinder extends
			UiBinder<Widget, AccessManagementSheet> {
	}

	public AccessManagementSheet() {
		initWidget(uiBinder.createAndBindUi(this));
	}
	
	@UiField
	Label insecureAlert;
	@UiField
	Label earthHeading;
	@UiField
	Anchor modifyGroup;
	@UiField
	Anchor modifyRegisteredUsers;
	
	SecurityAdminServiceAsync service;
	UserClassSecurityInfo anonymous;
	UserClassSecurityInfo authenticated;

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		if ( isVisible ) {
			if ( service == null ) {
				service = SecureGWT.get().createSecurityAdminService();
			}
			String baseUrl = GWT.getHostPageBaseURL();
			modifyGroup.setHref(baseUrl + MODIFY_GROUP_URL);
			modifyRegisteredUsers.setHref(baseUrl + MODIFY_USERS_URL);
			service.getUserClassPrivileges(GrantedAuthorityNames.USER_IS_ANONYMOUS.toString(), new AsyncCallback<UserClassSecurityInfo>(){
	
				@Override
				public void onFailure(Throwable caught) {
				}
	
				@Override
				public void onSuccess(UserClassSecurityInfo result) {
					anonymous = result;
					earthHeading.setStyleDependentName("problem",
						! anonymous.getGrantedAuthorities().contains(
							new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.toString())));
				}});
			
			service.getUserClassPrivileges(GrantedAuthorityNames.USER_IS_AUTHENTICATED.toString(), new AsyncCallback<UserClassSecurityInfo>(){

				@Override
				public void onFailure(Throwable caught) {
				}

				@Override
				public void onSuccess(UserClassSecurityInfo result) {
					authenticated = result;
					insecureAlert.setVisible( authenticated.getGrantedAuthorities().contains(
							new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_ACCESS_ADMIN.toString())) );
					}
				});
		}
	}
}
