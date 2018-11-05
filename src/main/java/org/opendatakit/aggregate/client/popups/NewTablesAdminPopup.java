/*
 * Copyright (C) 2013 University of Washington
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

package org.opendatakit.aggregate.client.popups;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.AggregateListBox;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.common.GrantedAuthorityName;

public final class NewTablesAdminPopup extends AbstractPopupBase {

  private static final String LABEL_TXT = "Grant ODK Tables Admin Rights to User";
  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Grant Admin Rights";
  private static final String TOOLTIP_TXT = "Grant administrative rights to ODK Tables data to a user with Synchronize Tables privileges";
  private static final String HELP_BALLOON_TXT = "Enable a user with Synchronize Tables privileges to perform administrative actions on that data.";
  private final UserListBox users;

  ;
  private ArrayList<UserSecurityInfo> userList;
  public NewTablesAdminPopup() {
    super();

    users = new UserListBox();
    AggregateButton addButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    addButton.addClickHandler(new CreateUser());

    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new ClosePopupButton(this));
    layout.setWidget(0, 1, new HTML(LABEL_TXT));
    layout.setWidget(1, 0, new HTML("Users:"));
    layout.setWidget(1, 1, users);
    layout.setWidget(3, 1, addButton);

    SecureGWT.getSecurityAdminService().getAllUsers(true, new ODKTablesAdminPopupCallback());
    setWidget(layout);
  }

  public class UserListBox extends AggregateListBox {

    public UserListBox() {
      super(TOOLTIP_TXT, true, HELP_BALLOON_TXT);
    }

  }

  ;

  public class ODKTablesAdminPopupCallback implements AsyncCallback<ArrayList<UserSecurityInfo>> {
    public ODKTablesAdminPopupCallback() {
    }

    @Override
    public void onFailure(Throwable caught) {
      users.clear();
      userList.clear();
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(ArrayList<UserSecurityInfo> result) {
      AggregateUI.getUI().clearError();

      ArrayList<UserSecurityInfo> filteredResult = new ArrayList<UserSecurityInfo>();
      for (UserSecurityInfo user : result) {
        if (user.getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
          filteredResult.add(user);
        }
      }
      Collections.sort(filteredResult, new Comparator<UserSecurityInfo>() {

        @Override
        public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
          String name0 = arg0.getFullName();
          String name1 = arg1.getFullName();
          if (name0 == null) {
            name0 = arg0.getCanonicalName();
          }
          if (name1 == null) {
            name1 = arg1.getCanonicalName();
          }
          return name0.compareTo(name1);
        }
      });

      users.clear();
      userList = filteredResult;
      for (int i = 0; i < userList.size(); ++i) {
        UserSecurityInfo user = userList.get(i);
        String displayName = user.getFullName();
        if (displayName == null) {
          displayName = user.getCanonicalName();
        }
        users.addItem(displayName, Integer.toString(i));
        users.setItemSelected(i,
            user.getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_ADMINISTER_TABLES));
      }
    }

  }

  private class CreateUser implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      // Set up the callback object.
      AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
        }

        @Override
        public void onSuccess(Boolean result) {
          AggregateUI.getUI().clearError();
          if (result) {
            Window.alert("Successfully added the user");
          } else {
            Window.alert("Error: unable to add the user!");
          }
          AggregateUI.getUI().getTimer().refreshNow();
        }
      };

      ArrayList<UserSecurityInfo> enabledUsers = new ArrayList<UserSecurityInfo>();
      for (int i = 0; i < userList.size(); ++i) {
        if (users.isItemSelected(i)) {
          UserSecurityInfo user = userList.get(i);
          enabledUsers.add(user);
        }
      }
      // Make the call to the odk tables user admin service.
      SecureGWT.getOdkTablesAdminService().setAdmins(enabledUsers, callback);
      hide();
    }
  }

}