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

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.permissions.CredentialsInfoBuilder;
import org.opendatakit.aggregate.client.popups.ChangePasswordPopup;
import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.PasswordTextBox;

/**
 * Uses whatever the secure channel is (https: if available; http: if not) and
 * GWT RequestBuilder to POST back to ODK Aggregate to change a user's password.
 * The password is sent as a hash back to the server, so even if it is sent over
 * http: (which happens if the server does not have SSL configured), it would 
 * take a while to compromise the password.  
 * 
 * @author mitchellsundt@gmail.com
 *
 */
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
	    
		String pw1 = password1.getText();
		String pw2 = password2.getText();
		if ( pw1 == null || pw2 == null || pw1.length() == 0 ) {
			Window.alert("Password cannot be blank");
		} else if ( pw1.equals(pw2) ) {
			if ( realmInfo == null || userInfo == null ) {
				Window.alert("Unable to obtain required information from server");
			} else {
				try {
					CredentialsInfo credential = CredentialsInfoBuilder.build(userInfo.getUsername(), realmInfo, pw1);
					
					String url = realmInfo.getChangeUserPasswordURL();
					String postData = credential.getPostBody();
					
					RequestBuilder builder = new RequestBuilder(RequestBuilder.POST, url);
					builder.setHeader("Content-type", "application/x-www-form-urlencoded");
					builder.setRequestData(postData);
					builder.setCallback(new RequestCallback() {

						@Override
						public void onResponseReceived(Request request,
								Response response) {
							int status = response.getStatusCode();
							if ( status != 204 /* NO_CONTENT */ ) {
								String error = response.getStatusText();
								if ( error == null || error.length() == 0 ) {
									error = "Server request failed";
								}
								Window.alert("Incomplete password update (bad response received): " + error);
							} else {
								popup.hide();
							}
						}

						@Override
						public void onError(Request request, Throwable exception) {
							Window.alert("Incomplete password update (request error): " + exception.getMessage());
						}
						
					});
					try {
						builder.send();
					} catch (RequestException e) {
						Window.alert("Incomplete password update (send exception): " + e.getMessage());
						popup.hide();
					} 
				} catch (NoSuchAlgorithmException e1) {
					Window.alert("Unable to create encrypted password");
					popup.hide();
				}
			}
		} else {
			Window.alert("The passwords do not match. Please retype the password.");
		}

	  }
}
