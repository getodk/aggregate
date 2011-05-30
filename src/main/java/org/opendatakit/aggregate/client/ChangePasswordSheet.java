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

import org.opendatakit.common.security.client.security.SecurityServiceAsync;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;

/**
 * Equivalent to the UserPasswordServlet -- supports changing the logged-in
 * user's Aggregate password. 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class ChangePasswordSheet extends Composite {

	private static ChangePasswordSheetUiBinder uiBinder = GWT
			.create(ChangePasswordSheetUiBinder.class);

	interface ChangePasswordSheetUiBinder extends
			UiBinder<Widget, ChangePasswordSheet> {
	}

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		if ( isVisible ) {
			usernickname.setText("");
			if ( service == null ) {
				this.service = SecureGWT.get().createSecurityService();
			}
			service.getUserDisplayName(new AsyncCallback<String>()
				{
					@Override
					public void onFailure(Throwable caught) {
					}
	
					@Override
					public void onSuccess(String result) {
						usernickname.setText(result);
					}
				});
			password1.setText("unknown");
			password2.setText("setting");
		}
	}

	public ChangePasswordSheet() {
		initWidget(uiBinder.createAndBindUi(this));
	}

	@UiField
	Label usernickname;
	@UiField
	PasswordTextBox password1;
	@UiField
	PasswordTextBox password2;
	@UiField
	Button button;
	
	SecurityServiceAsync service;

	@UiHandler("button")
	void onClick(ClickEvent e) {
		String pw1 = password1.getText();
		String pw2 = password2.getText();
		if ( pw1.equals(pw2) ) {
			service.setUserPassword(pw1, new AsyncCallback<Void>() {

				@Override
				public void onFailure(Throwable caught) {
					Window.alert("Unable to change password: " + caught.getMessage());
				}

				@Override
				public void onSuccess(Void result) {
					Window.alert("Successful password change.");
				}
				
			});
		} else {
			Window.alert("The passwords do not match. Please retype your password.");
		}
	}
}
