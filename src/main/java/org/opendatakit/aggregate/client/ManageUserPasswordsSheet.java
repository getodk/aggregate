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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.TreeSet;

import org.opendatakit.common.security.client.CredentialsInfo;
import org.opendatakit.common.security.client.RealmSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.security.SecurityServiceAsync;
import org.opendatakit.common.security.client.security.admin.SecurityAdminServiceAsync;
import org.opendatakit.common.security.common.EmailParser;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

/**
 * Equivalent to the UserManagePasswordsServlet -- allows an administrator
 * to bulk-change the passwords for a set of users on the system.
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class ManageUserPasswordsSheet extends Composite {

	private static ManageUserPasswordsSheetUiBinder uiBinder = GWT
			.create(ManageUserPasswordsSheetUiBinder.class);

	interface ManageUserPasswordsSheetUiBinder extends
			UiBinder<Widget, ManageUserPasswordsSheet> {
	}
	
	TreeSet<UserSecurityInfo> changePasswordSet = new TreeSet<UserSecurityInfo>();
	
	private class ChangePasswordCheckbox extends Column<UserSecurityInfo,Boolean> 
						implements FieldUpdater<UserSecurityInfo, Boolean> {
		
		ChangePasswordCheckbox() {
			super(new CheckboxCell(true, false));
			this.setFieldUpdater(this);
			this.setSortable(true);
		}

		@Override
		public Boolean getValue(UserSecurityInfo object) {
			return changePasswordSet.contains(object);
		}

		@Override
		public void update(int index, UserSecurityInfo object, Boolean value) {
			if ( value ) {
				changePasswordSet.add(object);
			} else {
				changePasswordSet.remove(object);
			}
		}
	}

	ListDataProvider<UserSecurityInfo> dataProvider = new ListDataProvider<UserSecurityInfo>();

	public ManageUserPasswordsSheet() {
		initWidget(uiBinder.createAndBindUi(this));
		
		// ChangePasswordCheckbox
		Column<UserSecurityInfo,Boolean> checkbox = new ChangePasswordCheckbox();
		userTable.addColumn(checkbox, "");
		
		// Username
		TextColumn<UserSecurityInfo> username = new TextColumn<UserSecurityInfo>() {
			@Override
			public String getValue(UserSecurityInfo object) {
				return object.getUsername();
			}
			
		};
		username.setSortable(true);
		userTable.addColumn(username, "Username");
		
		// Nickname
		TextColumn<UserSecurityInfo> nickname = new TextColumn<UserSecurityInfo>() {
			@Override
			public String getValue(UserSecurityInfo object) {
				return object.getNickname();
			}
			
		};
		nickname.setSortable(true);
		userTable.addColumn(nickname, "Nickname");
		
		// Email
		Column<UserSecurityInfo,String> email = new TextColumn<UserSecurityInfo>() {
			@Override
			public String getValue(UserSecurityInfo object) {
				String email = object.getEmail();
				if ( email != null ) {
					email = email.substring(EmailParser.K_MAILTO.length());
				}
				return object.getEmail();
			}
			
		};
		email.setSortable(true);
		userTable.addColumn(email, "Email");
		dataProvider.addDataDisplay(userTable);
		
		ListHandler<UserSecurityInfo> columnSortHandler =
				new ListHandler<UserSecurityInfo>(dataProvider.getList());
		
		columnSortHandler.setComparator(username, new Comparator<UserSecurityInfo>() {

			@Override
			public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
				if ( arg0 == arg1 ) return 0;
				
				if ( arg0 != null ) {
					return (arg1 != null) ? 
							arg0.getUsername().compareToIgnoreCase(arg1.getUsername()) : 1;
				}
				return -1;
			}
		});
		columnSortHandler.setComparator(nickname, new Comparator<UserSecurityInfo>() {

			@Override
			public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
				if ( arg0 == arg1 ) return 0;
				
				if ( arg0 != null && arg0.getNickname() != null) {
					return (arg1 != null && arg1.getNickname() != null) ? 
							arg0.getNickname().compareToIgnoreCase(arg1.getNickname()) : 1;
				}
				return -1;
			}
		});
		columnSortHandler.setComparator(email, new Comparator<UserSecurityInfo>() {

			@Override
			public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
				if ( arg0 == arg1 ) return 0;
				
				if ( arg0 != null && arg0.getEmail() != null ) {
					return (arg1 != null && arg1.getEmail() != null) ? 
							arg0.getEmail().compareToIgnoreCase(arg1.getEmail()) : 1;
				}
				return -1;
			}
		});
		userTable.addColumnSortHandler(columnSortHandler);
	}

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		if ( isVisible ) {
			if ( service == null ) {
				this.service = SecureGWT.get().createSecurityAdminService();
			}
			if ( userSecurityService == null ) {
				this.userSecurityService = SecureGWT.get().createSecurityService();
			}
			
			service.getAllUsers(false, new AsyncCallback<ArrayList<UserSecurityInfo>>() {

				@Override
				public void onFailure(Throwable caught) {
					Window.alert("Unable to access server: " + caught.getMessage());
				}

				@Override
				public void onSuccess(ArrayList<UserSecurityInfo> result) {
					dataProvider.getList().clear();
					dataProvider.getList().addAll(result);
				}
				
			});
			if ( realmInfo == null ) {
				userSecurityService.getRealmInfo(Cookies.getCookie("JSESSIONID"), new AsyncCallback<RealmSecurityInfo>(){

					@Override
					public void onFailure(Throwable caught) {
					}

					@Override
					public void onSuccess(RealmSecurityInfo result) {
						realmInfo = result;
					}});
			}
			
			password1.setText("unknown");
			password2.setText("setting");
		}
	}

	@UiField
	PasswordTextBox password1;
	@UiField
	PasswordTextBox password2;
	@UiField
	CellTable<UserSecurityInfo> userTable;
	@UiField
	Button button;

	SecurityServiceAsync userSecurityService;
	SecurityAdminServiceAsync service;

	RealmSecurityInfo realmInfo;
	
	@UiHandler("button")
	void onUpdateClick(ClickEvent e) {
		String pw1 = password1.getText();
		String pw2 = password2.getText();
		if ( pw1.equals(pw2) ) {
			if ( realmInfo == null ) {
				Window.alert("Unable to obtain required information from server");
			}

			ArrayList<CredentialsInfo> credentials = new ArrayList<CredentialsInfo>();
			try {
				for ( UserSecurityInfo user : changePasswordSet) {
					credentials.add(CredentialsInfoBuilder.build(user.getUsername(), realmInfo, pw1));
				}
				service.setUserPasswords(Cookies.getCookie("JSESSIONID"), credentials, new AsyncCallback<Void>() {
		
					@Override
					public void onFailure(Throwable caught) {
						Window.alert("Incomplete security update: " + caught.getMessage());
					}
		
					@Override
					public void onSuccess(Void result) {
						Window.alert("Successful bulk update of selected users' passwords");
						changePasswordSet.clear();
						dataProvider.refresh();
					}
				});
			} catch (NoSuchAlgorithmException e1) {
				Window.alert("Unable to create encrypted password");
			}
			
		} else {
			Window.alert("The passwords do not match. Please retype the password.");
		}
	}
}
