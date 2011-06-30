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

package org.opendatakit.aggregate.client.widgets;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.permissions.CredentialsInfoBuilder;
import org.opendatakit.aggregate.client.popups.ChangePasswordPopup;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.security.admin.SecurityAdminServiceAsync;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.PasswordTextBox;

public class ExecuteChangePasswordButton extends AButtonBase implements ClickHandler {

	private ChangePasswordPopup popup;
	  
	  public ExecuteChangePasswordButton(ChangePasswordPopup popup) {
	    super("<img src=\"images/green_right_arrow.png\" /> Change Password");
	    this.popup = popup;
	    addClickHandler(this);
	  }
	  
	  @Override
	  public void onClick(ClickEvent event) {
	    super.onClick(event);
	    PasswordTextBox password1 = popup.getPassword1();
	    PasswordTextBox password2 = popup.getPassword2();
	    UserSecurityInfo userInfo = popup.getUser();
	    RealmSecurityInfo realmInfo = AggregateUI.getUI().getRealmInfo();
	    SecurityAdminServiceAsync service = SecureGWT.getSecurityAdminService();
	    
	    
		String pw1 = password1.getText();
		String pw2 = password2.getText();
		if ( pw1 == null || pw2 == null || pw1.length() == 0 ) {
			Window.alert("Password cannot be blank");
		} else if ( pw1.equals(pw2) ) {
			if ( realmInfo == null || userInfo == null ) {
				Window.alert("Unable to obtain required information from server");
			} else {
				try {
					ArrayList<CredentialsInfo> credentials = new ArrayList<CredentialsInfo>();
					credentials.add(CredentialsInfoBuilder.build(userInfo.getUsername(), realmInfo, pw1));
					// TODO: change to an https post 
					service.setUserPasswords(Cookies.getCookie("JSESSIONID"), credentials , new AsyncCallback<Void>() {
							
							@Override
							public void onFailure(Throwable caught) {
								Window.alert("Incomplete password update: " + caught.getMessage());
							}
				
							@Override
							public void onSuccess(Void result) {
								popup.hide();
							}
						});
				} catch (NoSuchAlgorithmException e1) {
					Window.alert("Unable to create encrypted password");
				}
			}
		} else {
			Window.alert("The passwords do not match. Please retype the password.");
		}

	  }
}
