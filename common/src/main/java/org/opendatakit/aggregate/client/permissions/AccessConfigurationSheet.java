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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.TreeSet;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.PermissionsSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.popups.ChangePasswordPopup;
import org.opendatakit.aggregate.client.popups.ConfirmUserDeletePopup;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.widgets.UploadUsersAndPermsServletPopupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.EmailParser.Email;
import org.opendatakit.common.security.common.GrantedAuthorityName;
import org.opendatakit.common.web.client.BooleanValidationPredicate;
import org.opendatakit.common.web.client.StringValidationPredicate;
import org.opendatakit.common.web.client.UIEnabledActionCell;
import org.opendatakit.common.web.client.UIEnabledActionColumn;
import org.opendatakit.common.web.client.UIEnabledPredicate;
import org.opendatakit.common.web.client.UIEnabledValidatingCheckboxColumn;
import org.opendatakit.common.web.client.UIEnabledValidatingSelectionColumn;
import org.opendatakit.common.web.client.UIEnabledValidatingTextInputColumn;
import org.opendatakit.common.web.client.UIVisiblePredicate;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class AccessConfigurationSheet extends Composite {

  private static final String NOT_VALID_EMAIL = "Username is not a valid Email address.\n\n"
      + "Usernames for Google accounts must be\n" + "Email addresses that Google can\n"
      + "authenticate (Google login is not supported at this time).";

  private static final ArrayList<String> userType;
  private static final String ACCOUNT_TYPE_ODK = "ODK";
  private static final String ACCOUNT_TYPE_GOOGLE = "Google";

  static {
    userType = new ArrayList<String>();
    userType.add(ACCOUNT_TYPE_ODK);
    userType.add(ACCOUNT_TYPE_GOOGLE);
  };

  private static TemporaryAccessConfigurationSheetUiBinder uiBinder = GWT
      .create(TemporaryAccessConfigurationSheetUiBinder.class);

  interface TemporaryAccessConfigurationSheetUiBinder extends
      UiBinder<Widget, AccessConfigurationSheet> {
  }

  private final ListDataProvider<UserSecurityInfo> dataProvider = new ListDataProvider<UserSecurityInfo>();
  private final ListHandler<UserSecurityInfo> columnSortHandler = new ListHandler<UserSecurityInfo>(
      dataProvider.getList());

  private boolean anonymousAttachmentBoolean = false;

  private PermissionsSubTab permissionsTab;
  private boolean changesHappened = false;

  private GroupMembershipColumn formsAdmin;
  private GroupMembershipColumn synchronizeTables;
  private GroupMembershipColumn superUserTables;
  private GroupMembershipColumn administerTables;
  private GroupMembershipColumn siteAdmin;

  public boolean isUiOutOfSyncWithServer() {
    return changesHappened;
  }

  private void uiInSyncWithServer() {
    changesHappened = false;
  }

  private void uiOutOfSyncWithServer() {
    changesHappened = true;
  }

  private static final class AuthChangeValidation implements
      BooleanValidationPredicate<UserSecurityInfo> {

    final GrantedAuthorityName auth;

    AuthChangeValidation(GrantedAuthorityName auth) {
      this.auth = auth;
    }

    @Override
    public boolean isValid(boolean prospectiveValue, UserSecurityInfo key) {
      // data collector must be an ODK account
      boolean badCollector = auth.equals(GrantedAuthorityName.GROUP_DATA_COLLECTORS)
          && (key.getUsername() == null);
      // site admin must not be the anonymous user
      boolean badSiteAdmin = auth.equals(GrantedAuthorityName.GROUP_SITE_ADMINS)
          && (key.getType() == UserType.ANONYMOUS);
      // tables admin must not be the anonymous user
      boolean badTablesAdmin = auth.equals(GrantedAuthorityName.GROUP_ADMINISTER_TABLES)
          && (key.getType() == UserType.ANONYMOUS);
      // tables super-user must not be the anonymous user
      boolean badTablesSuperUser = auth.equals(GrantedAuthorityName.GROUP_SUPER_USER_TABLES)
          && (key.getType() == UserType.ANONYMOUS);
      return !(badCollector || badSiteAdmin || badTablesAdmin || badTablesSuperUser);
    }
  }

  private static final class AuthVisiblePredicate implements UIVisiblePredicate<UserSecurityInfo> {

    final GrantedAuthorityName auth;

    AuthVisiblePredicate(GrantedAuthorityName auth) {
      this.auth = auth;
    }

    @Override
    public boolean isVisible(UserSecurityInfo key) {
      if (auth == GrantedAuthorityName.GROUP_SITE_ADMINS) {
        // anonymous user should not be able to be a site admin
        return (key.getType() != UserType.ANONYMOUS);
      }

      if (auth == GrantedAuthorityName.GROUP_ADMINISTER_TABLES) {
        // anonymous user should not be able to be a tables admin
        return (key.getType() != UserType.ANONYMOUS);
      }

      if (auth == GrantedAuthorityName.GROUP_SUPER_USER_TABLES) {
        // anonymous user should not be able to be a tables super-user
        return (key.getType() != UserType.ANONYMOUS);
      }

      if (auth == GrantedAuthorityName.GROUP_DATA_COLLECTORS) {
        // data collectors can only be ODK accounts...
        return (key.getUsername() != null);
      }
      return true;
    }

  }

  private static final class AuthEnabledPredicate implements UIEnabledPredicate<UserSecurityInfo> {

    final GrantedAuthorityName auth;

    AuthEnabledPredicate(GrantedAuthorityName auth) {
      this.auth = auth;
    }

    @Override
    public boolean isEnabled(UserSecurityInfo info) {
      TreeSet<GrantedAuthorityName> assignedGroups = info.getAssignedUserGroups();

      switch (auth) {
      case GROUP_DATA_COLLECTORS:
        // data collectors must be anonymous
        // or an ODK account type
        return (info.getType() == UserType.ANONYMOUS) ||
               (info.getUsername() != null);
      case GROUP_DATA_VIEWERS:
        if (assignedGroups.contains(GrantedAuthorityName.GROUP_FORM_MANAGERS)
            || assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)) {
          return false;
        }
        return true;
      case GROUP_SYNCHRONIZE_TABLES:
        if (assignedGroups.contains(GrantedAuthorityName.GROUP_ADMINISTER_TABLES)
            || assignedGroups.contains(GrantedAuthorityName.GROUP_SUPER_USER_TABLES)
         || assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)) {
          return false;
        }
        return true;
      case GROUP_SUPER_USER_TABLES:
        if (assignedGroups.contains(GrantedAuthorityName.GROUP_ADMINISTER_TABLES)
        	|| assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)) {
          return false;
        }
        return true;
      case GROUP_ADMINISTER_TABLES:
        if (assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)) {
          return false;
        }
        return true;
      case GROUP_FORM_MANAGERS:
        if (assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)) {
          return false;
        }
        return true;
      case GROUP_SITE_ADMINS:
        // don't let the designated super-user un-check their
        // site admin privileges.
        String email = info.getEmail();
        String superUserEmail = AggregateUI.getUI().getRealmInfo().getSuperUserEmail();
        String username = info.getUsername();
        String superUsername = AggregateUI.getUI().getRealmInfo().getSuperUsername();
        if ( ( email != null && superUserEmail != null && superUserEmail.equals(email) ) ||
             ( username != null && superUsername != null && superUsername.equals(username) ) ) {
          return false;
        }
        return true;
      default:
        return false;
      }
    }
  }

  private static final class AuthComparator implements Comparator<UserSecurityInfo> {

    final GrantedAuthorityName auth;

    AuthComparator(GrantedAuthorityName auth) {
      this.auth = auth;
    }

    @Override
    public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
      boolean arg0Contains = arg0.getAssignedUserGroups().contains(auth);
      boolean arg1Contains = arg1.getAssignedUserGroups().contains(auth);

      if (arg0Contains == arg1Contains) {
        // same value. In the case where another
        // assigned granted authority subsumes this one,
        // we want to place the users with subsumed
        // rights above those with no rights.
        arg0Contains = arg0.getGrantedAuthorities().contains(auth);
        arg1Contains = arg1.getGrantedAuthorities().contains(auth);
        if (arg0Contains == arg1Contains)
          return 0;
        if (arg0Contains)
          return -1;
        return 1;
      }
      // checked before unchecked...
      if (arg0Contains)
        return -1;
      return 1;
    }
  }

  private class GroupMembershipColumn extends UIEnabledValidatingCheckboxColumn<UserSecurityInfo> {
    final GrantedAuthorityName auth;

    GroupMembershipColumn(GrantedAuthorityName auth) {
      super(new AuthChangeValidation(auth), new AuthVisiblePredicate(auth),
          new AuthEnabledPredicate(auth), new AuthComparator(auth));
      this.auth = auth;
    }

    @Override
    public void setValue(UserSecurityInfo object, Boolean value) {
      if (value) {
        object.getAssignedUserGroups().add(auth);
      } else {
        object.getAssignedUserGroups().remove(auth);
      }
      if (!auth.equals(GrantedAuthorityName.GROUP_DATA_COLLECTORS)) {
        // we may be disabling or enabling some checkboxes...
        userTable.redraw();
        if (object.getType() == UserType.ANONYMOUS) {
          boolean isGDV = object.getAssignedUserGroups().contains(
              GrantedAuthorityName.GROUP_DATA_VIEWERS);
          if (isGDV) {
            anonymousAttachmentViewers.setValue(true, false);
          } else {
            // restore original value to checkbox...
            anonymousAttachmentViewers.setValue(anonymousAttachmentBoolean, false);
          }
          anonymousAttachmentViewers.setEnabled(!isGDV);
        }
      }
      uiOutOfSyncWithServer();
    }

    @Override
    public Boolean getValue(UserSecurityInfo object) {

      TreeSet<GrantedAuthorityName> assignedGroups = object.getAssignedUserGroups();
      switch (auth) {
      case GROUP_DATA_COLLECTORS:
        return assignedGroups.contains(auth);
      case GROUP_DATA_VIEWERS:
        return assignedGroups.contains(GrantedAuthorityName.GROUP_FORM_MANAGERS)
            || assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)
            || assignedGroups.contains(auth);
      case GROUP_FORM_MANAGERS:
        return assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)
            || assignedGroups.contains(auth);
      case GROUP_SYNCHRONIZE_TABLES:
        return assignedGroups.contains(GrantedAuthorityName.GROUP_SUPER_USER_TABLES)
            || assignedGroups.contains(GrantedAuthorityName.GROUP_ADMINISTER_TABLES)
            || assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)
            || assignedGroups.contains(auth);
      case GROUP_SUPER_USER_TABLES:
        return assignedGroups.contains(GrantedAuthorityName.GROUP_ADMINISTER_TABLES)
            || assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)
            || assignedGroups.contains(auth);
      case GROUP_ADMINISTER_TABLES:
        return assignedGroups.contains(GrantedAuthorityName.GROUP_SITE_ADMINS)
            || assignedGroups.contains(auth);
      case GROUP_SITE_ADMINS:
        return assignedGroups.contains(auth);
      default:
        return false;
      }
    }
  }

  private class UpdateUserDisplay implements AsyncCallback<ArrayList<UserSecurityInfo>> {
    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(" Unable to retrieve users from server: ", caught);
    }

    @Override
    public void onSuccess(ArrayList<UserSecurityInfo> result) {
      dataProvider.getList().clear();
      addedUsers.setText("");
      for (UserSecurityInfo i : result) {
        if (i.getType() == UserType.ANONYMOUS) {
          TreeSet<GrantedAuthorityName> assignedSet = i.getAssignedUserGroups();
          boolean hasAV = assignedSet.contains(GrantedAuthorityName.ROLE_ATTACHMENT_VIEWER);
          anonymousAttachmentBoolean = hasAV;
          boolean isGDV = i.getAssignedUserGroups().contains(
              GrantedAuthorityName.GROUP_DATA_VIEWERS);
          anonymousAttachmentViewers.setValue(anonymousAttachmentBoolean || isGDV, false);
          anonymousAttachmentViewers.setEnabled(!isGDV);
          break;
        }
      }
      dataProvider.getList().addAll(result);
      userTable.setPageSize(Math.max(15, result.size()));
      uiInSyncWithServer();
    }
  };

  public void deleteUser(UserSecurityInfo user) {
    dataProvider.getList().remove(user);
    updateUsersOnServer();
  }

  public void updateUsersOnServer() {
    ArrayList<GrantedAuthorityName> allGroups = new ArrayList<GrantedAuthorityName>();
    allGroups.add(GrantedAuthorityName.GROUP_SITE_ADMINS);
    allGroups.add(GrantedAuthorityName.GROUP_ADMINISTER_TABLES);
    allGroups.add(GrantedAuthorityName.GROUP_SUPER_USER_TABLES);
    allGroups.add(GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES);
    allGroups.add(GrantedAuthorityName.GROUP_FORM_MANAGERS);
    allGroups.add(GrantedAuthorityName.GROUP_DATA_VIEWERS);
    allGroups.add(GrantedAuthorityName.GROUP_DATA_COLLECTORS);
    allGroups.add(GrantedAuthorityName.USER_IS_ANONYMOUS);

    ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>();
    users.addAll(dataProvider.getList());
    for (UserSecurityInfo i : users) {
      if (i.getType() == UserType.ANONYMOUS) {
        if (anonymousAttachmentBoolean) {
          i.getAssignedUserGroups().add(GrantedAuthorityName.ROLE_ATTACHMENT_VIEWER);
        } else {
          i.getAssignedUserGroups().remove(GrantedAuthorityName.ROLE_ATTACHMENT_VIEWER);
        }
        break;
      } else {
        if (i.getUsername() == null) {
          // don't allow Google users to be data collectors
          i.getAssignedUserGroups().remove(GrantedAuthorityName.GROUP_DATA_COLLECTORS);
        }
      }
    }
    SecureGWT.getSecurityAdminService().setUsersAndGrantedAuthorities(
        Cookies.getCookie("JSESSIONID"), users, allGroups, new AsyncCallback<Void>() {

          @Override
          public void onFailure(Throwable caught) {
            AggregateUI.getUI().reportError("Incomplete security update: ", caught);
          }

          @Override
          public void onSuccess(Void result) {
            SecureGWT.getSecurityAdminService().getAllUsers(true, new UpdateUserDisplay());
          }
        });
  }

  private static final class VisibleNotAnonymousPredicate implements
      UIVisiblePredicate<UserSecurityInfo> {

    @Override
    public boolean isVisible(UserSecurityInfo info) {
      // enable only if it is not the anonymous user
      return (info.getType() != UserType.ANONYMOUS);
    }
  }

  private static final class EnableNotAnonymousOrSuperUserPredicate implements
      UIEnabledPredicate<UserSecurityInfo> {
    @Override
    public boolean isEnabled(UserSecurityInfo info) {
      // enable only if it is a registered user
      if (info.getType() != UserType.REGISTERED)
        return false;
      // enable only if the user is not the superUser. 
      String email = info.getEmail();
      String superUserEmail = AggregateUI.getUI().getRealmInfo().getSuperUserEmail();
      String username = info.getUsername();
      String superUsername = AggregateUI.getUI().getRealmInfo().getSuperUsername();
      if ( ( email != null && superUserEmail != null && superUserEmail.equals(email) ) ||
           ( username != null && superUsername != null && superUsername.equals(username) ) ) {
        return false;
      }
      return true;
    }
  };

  private static final class EnableNotAnonymousPredicate implements
      UIEnabledPredicate<UserSecurityInfo> {
    @Override
    public boolean isEnabled(UserSecurityInfo info) {
      // enable only if it is a registered user
      return (info.getType() == UserType.REGISTERED);
    }
  };

  private static final class EnableLocalAccountPredicate implements
      UIEnabledPredicate<UserSecurityInfo> {
    @Override
    public boolean isEnabled(UserSecurityInfo info) {
      return (info.getType() == UserType.REGISTERED && info.getUsername() != null);
    }
  };

  /**
   * Username cannot be null or zero-length. If it is a Google account type (an
   * e-mail address), then it should look like an e-mail address.
   */
  private final class ValidatingUsernamePredicate implements
      StringValidationPredicate<UserSecurityInfo> {

    @Override
    public boolean isValid(String prospectiveValue, UserSecurityInfo key) {
      if (prospectiveValue != null && prospectiveValue.length() != 0) {
        if (prospectiveValue.trim().length() != prospectiveValue.length()) {
          Window.alert("Invalid whitespace before or after the username");
          return false;
        }

        // don't allow an edit to convert this name into an existing
        // one.
        for (UserSecurityInfo i : dataProvider.getList()) {
          if (i == key)
            continue;
          if (i.getCanonicalName().equals(prospectiveValue)) {
            Window.alert("Username is already defined");
            return false;
          }
        }
        if (key.getUsername() == null) {
          // we are setting an e-mail address... verify it...
          if (EmailParser.hasInvalidEmailCharacters(prospectiveValue)) {
            Window.alert("Invalid characters in Email address.\n"
                + "Email address cannot contain whitespace, quotes,\n"
                + "commas, semicolons or other punctuation");
            return false;
          } else if (prospectiveValue.indexOf(EmailParser.K_AT) == -1) {
            Window.alert("Email address is missing the '@domain.org' portion\n"
                + "Email must be of the form 'username@domain.org'");
            return false;
          }
        }
      } else {
        Window.alert("Username cannot be empty");
        return false;
      }
      return true;
    }

  }

  private static class UsernameComparator implements Comparator<UserSecurityInfo> {
    @Override
    public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
      if (arg0 == arg1)
        return 0;

      if (arg0 != null) {
        return (arg1 != null) ? arg0.getCanonicalName()
            .compareToIgnoreCase(arg1.getCanonicalName()) : 1;
      }
      return -1;
    }
  };

  private final class UsernameTextColumn extends
      UIEnabledValidatingTextInputColumn<UserSecurityInfo> {

    UsernameTextColumn() {
      super(new ValidatingUsernamePredicate(), new EnableNotAnonymousOrSuperUserPredicate(),
          new UsernameComparator());
    }

    @Override
    public String getValue(UserSecurityInfo object) {
      String email = object.getEmail();
      if (email != null) {
        return email.substring(EmailParser.K_MAILTO.length());
      } else {
        return object.getUsername();
      }
    }

    @Override
    public void setValue(UserSecurityInfo object, String value) {
      uiOutOfSyncWithServer();
      // validation happens in the validation predicate...
      if (object.getUsername() == null) {
        object.setEmail(EmailParser.K_MAILTO + value);
      } else {
        object.setUsername(value);
      }
    }
  }

  private static class FullNameComparator implements Comparator<UserSecurityInfo> {
    @Override
    public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
      if (arg0 == arg1)
        return 0;

      if (arg0 != null) {
        return (arg1 != null) ? arg0.getFullName().compareToIgnoreCase(arg1.getFullName()) : 1;
      }
      return -1;
    }
  };

  private final class FullNameTextColumn extends
      UIEnabledValidatingTextInputColumn<UserSecurityInfo> {

    FullNameTextColumn() {
      super(null, new EnableNotAnonymousPredicate(), new FullNameComparator());
    }

    @Override
    public String getValue(UserSecurityInfo object) {
      return object.getFullName();
    }

    @Override
    public void setValue(UserSecurityInfo object, String value) {
      uiOutOfSyncWithServer();
      // validation happens in the validation predicate...
      object.setFullName(value);
    }
  }

  /**
   * If a Google account type is chosen, the username should be an e-mail
   * address.
   */
  private static final class ValidatingAccountTypePredicate implements
      StringValidationPredicate<UserSecurityInfo> {

    @Override
    public boolean isValid(String prospectiveValue, UserSecurityInfo key) {
      if (prospectiveValue == null || prospectiveValue.length() == 0) {
        Window.alert("Account Type cannot be empty");
        return false;
      }

      if (prospectiveValue.equals(ACCOUNT_TYPE_GOOGLE) && key.getEmail() == null) {
        // must be changing to a Google account type from an ODK account
        // verify that the username is a well-formed e-mail address...
        String username = key.getUsername();
        if (username == null || username.length() == 0) {
          return false;
        }
        if (EmailParser.hasInvalidEmailCharacters(username) || username.indexOf(EmailParser.K_AT) == -1) {
          Window.alert(NOT_VALID_EMAIL);
          return false;
        }
      }
      return true;
    }
  }

  private class AccountTypeComparator implements Comparator<UserSecurityInfo> {

    @Override
    public int compare(UserSecurityInfo arg0, UserSecurityInfo arg1) {
      if (arg0 == arg1)
        return 0;

      if (arg0 != null) {
        if (arg1 == null)
          return 1;
        if (arg0.getUsername() == null) {
          return (arg1.getUsername() == null) ? 0 : -1;
        } else {
          return (arg1.getUsername() == null) ? 1 : 0;
        }
      }
      return -1;
    }
  };

  private class AccountTypeSelectionColumn extends
      UIEnabledValidatingSelectionColumn<UserSecurityInfo> {

    protected AccountTypeSelectionColumn() {
      super(new ValidatingAccountTypePredicate(), new VisibleNotAnonymousPredicate(),
          new EnableNotAnonymousOrSuperUserPredicate(), new AccountTypeComparator(), userType);
    }

    @Override
    public String getValue(UserSecurityInfo object) {
      if (object.getUsername() == null) {
        return ACCOUNT_TYPE_GOOGLE;
      } else {
        return ACCOUNT_TYPE_ODK;
      }
    }

    @Override
    public void setValue(UserSecurityInfo object, String value) {
      String email = object.getEmail();
      String username = object.getUsername();
      if (value.equals(ACCOUNT_TYPE_ODK)) {
        if (username == null) {
          object.setEmail(null);
          object.setUsername(email.substring(EmailParser.K_MAILTO.length()));
          uiOutOfSyncWithServer();
          userTable.redraw(); // because this changes the Change
          // Password button...
        }
      } else {
        if (email == null) {
          object.setUsername(null);
          object.setEmail(EmailParser.K_MAILTO + username);
          uiOutOfSyncWithServer();
          userTable.redraw(); // because this changes the Change
          // Password button...
        }
      }
    }
  }

  private final class DeleteActionCallback implements
      UIEnabledActionCell.Delegate<UserSecurityInfo> {

    @Override
    public void execute(UserSecurityInfo object) {
      final ConfirmUserDeletePopup popup = new ConfirmUserDeletePopup(object,
          AccessConfigurationSheet.this);
      popup.setPopupPositionAndShow(popup.getPositionCallBack());
    }
  };

  private final class ChangePasswordActionCallback implements
      UIEnabledActionCell.Delegate<UserSecurityInfo> {

    @Override
    public void execute(UserSecurityInfo object) {
      if (isUiOutOfSyncWithServer()) {
        Window
            .alert("Unsaved changes exist. "
                + "\nPlease save changes, or reset the changes by refreshing the screen.\nThen you may change passwords.");
        return;
      }

      final PopupPanel popup = new ChangePasswordPopup(object);
      popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
        @Override
        public void setPosition(int offsetWidth, int offsetHeight) {
          int left = ((Window.getScrollLeft() + Window.getClientWidth() - offsetWidth) / 2);
          int top = ((Window.getScrollTop() + Window.getClientHeight() - offsetHeight) / 2);
          popup.setPopupPosition(left, top);
        }
      });
    }
  };

  public AccessConfigurationSheet(PermissionsSubTab permissionsTab) {
    this.permissionsTab = permissionsTab;
    initWidget(uiBinder.createAndBindUi(this));
    sinkEvents(Event.ONCHANGE | Event.ONCLICK);
    
    downloadCsv.setHref(UIConsts.GET_USERS_AND_PERMS_CSV_SERVLET_ADDR);

    SafeHtmlBuilder sb = new SafeHtmlBuilder();
    sb.appendHtmlConstant("<img src=\"images/red_x.png\" />");
    UIEnabledActionColumn<UserSecurityInfo> deleteMe = new UIEnabledActionColumn<UserSecurityInfo>(
        sb.toSafeHtml(), null, new EnableNotAnonymousOrSuperUserPredicate(),
        new DeleteActionCallback());
    userTable.addColumn(deleteMe, "");

    // Username
    UsernameTextColumn username = new UsernameTextColumn();
    userTable.addColumn(username, "Username");

    // Full Name
    FullNameTextColumn fullname = new FullNameTextColumn();
    userTable.addColumn(fullname, "Full Name");

    // Change Password
    UIEnabledActionColumn<UserSecurityInfo> changePassword = new UIEnabledActionColumn<UserSecurityInfo>(
        "Change Password", new EnableLocalAccountPredicate(), new ChangePasswordActionCallback());
    userTable.addColumn(changePassword, "");

    // Type of User
    AccountTypeSelectionColumn type = new AccountTypeSelectionColumn();
    userTable.addColumn(type, "Account Type");

    GroupMembershipColumn dc = new GroupMembershipColumn(GrantedAuthorityName.GROUP_DATA_COLLECTORS);
    userTable.addColumn(dc, GrantedAuthorityName.GROUP_DATA_COLLECTORS.getDisplayText());

    GroupMembershipColumn dv = new GroupMembershipColumn(GrantedAuthorityName.GROUP_DATA_VIEWERS);
    userTable.addColumn(dv, GrantedAuthorityName.GROUP_DATA_VIEWERS.getDisplayText());

    formsAdmin = new GroupMembershipColumn(GrantedAuthorityName.GROUP_FORM_MANAGERS);
    userTable.addColumn(formsAdmin, GrantedAuthorityName.GROUP_FORM_MANAGERS.getDisplayText());

    columnSortHandler.setComparator(username, username.getComparator());
    columnSortHandler.setComparator(fullname, fullname.getComparator());
    columnSortHandler.setComparator(type, type.getComparator());
    columnSortHandler.setComparator(dc, dc.getComparator());
    columnSortHandler.setComparator(dv, dv.getComparator());
    columnSortHandler.setComparator(formsAdmin, formsAdmin.getComparator());

    synchronizeTables = new GroupMembershipColumn(GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES);
    if ( Preferences.getOdkTablesEnabled() ) {
      userTable.addColumn(synchronizeTables, GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES.getDisplayText());
    }

    superUserTables = new GroupMembershipColumn(GrantedAuthorityName.GROUP_SUPER_USER_TABLES);
    if ( Preferences.getOdkTablesEnabled() ) {
      userTable.addColumn(superUserTables, GrantedAuthorityName.GROUP_SUPER_USER_TABLES.getDisplayText());
    }

    administerTables = new GroupMembershipColumn(GrantedAuthorityName.GROUP_ADMINISTER_TABLES);
    if ( Preferences.getOdkTablesEnabled() ) {
      userTable.addColumn(administerTables, GrantedAuthorityName.GROUP_ADMINISTER_TABLES.getDisplayText());
    }

    columnSortHandler.setComparator(synchronizeTables, synchronizeTables.getComparator());
    columnSortHandler.setComparator(superUserTables, superUserTables.getComparator());
    columnSortHandler.setComparator(administerTables, administerTables.getComparator());

    siteAdmin = new GroupMembershipColumn(GrantedAuthorityName.GROUP_SITE_ADMINS);
    userTable.addColumn(siteAdmin, GrantedAuthorityName.GROUP_SITE_ADMINS.getDisplayText());
    columnSortHandler.setComparator(siteAdmin, siteAdmin.getComparator());

    dataProvider.addDataDisplay(userTable);

    userTable.addColumnSortHandler(columnSortHandler);
  }

  public void changeTablesPrivilegesVisibility(boolean isVisible) {
    int idxNow;

    // insert or remove the synchronizeTables permissions
    idxNow = userTable.getColumnIndex(synchronizeTables);
    if ( isVisible && idxNow == -1) {
      idxNow = userTable.getColumnIndex(formsAdmin);
      if ( idxNow != -1) {
        userTable.insertColumn(idxNow+1, synchronizeTables, GrantedAuthorityName.GROUP_SYNCHRONIZE_TABLES.getDisplayText());
        // make idxNow point to the synchronizeTables column
        ++idxNow; 
      }
    } else if ( !isVisible && idxNow != -1) {
      userTable.removeColumn(idxNow);
    }

    // insert or remove the superUserTables permissions
    int idxPrior = idxNow;
    idxNow = userTable.getColumnIndex(superUserTables);
    if ( isVisible && idxNow == -1) {
      idxNow = idxPrior;
      if ( idxNow != -1) {
        userTable.insertColumn(idxNow+1, superUserTables, GrantedAuthorityName.GROUP_SUPER_USER_TABLES.getDisplayText());
      }
    } else if ( !isVisible && idxNow != -1) {
      userTable.removeColumn(idxNow);
    }

    // insert or remove the administerTables permissions
    idxNow = userTable.getColumnIndex(administerTables);
    if ( isVisible && idxNow == -1) {
      idxNow = userTable.getColumnIndex(siteAdmin);
      if ( idxNow != -1) {
        userTable.insertColumn(idxNow, administerTables, GrantedAuthorityName.GROUP_ADMINISTER_TABLES.getDisplayText());
      }
    } else if ( !isVisible && idxNow != -1) {
      userTable.removeColumn(idxNow);
    }
  }

  @Override
  public void setVisible(boolean isVisible) {
    super.setVisible(isVisible);
    if (isVisible) {
      SecureGWT.getSecurityAdminService().getAllUsers(true, new UpdateUserDisplay());
    }
  }

  @UiField
  TextArea addedUsers;
  @UiField
  UploadUsersAndPermsServletPopupButton uploadCsv;
  @UiField
  Anchor downloadCsv;
  @UiField
  Button addNow;
  @UiField
  CellTable<UserSecurityInfo> userTable;
  @UiField
  CheckBox anonymousAttachmentViewers;
  @UiField
  Button button;

  @UiHandler("anonymousAttachmentViewers")
  void onAnonAttachmentViewerChange(ValueChangeEvent<Boolean> event) {
    anonymousAttachmentBoolean = event.getValue();
    uiOutOfSyncWithServer();
  }

  @UiHandler("uploadCsv")
  void onUploadCsvClick(ClickEvent e) {
    uploadCsv.onClick(permissionsTab, e);
  }
  
  @UiHandler("addNow")
  void onAddUsersClick(ClickEvent e) {
    String text = addedUsers.getText();
    Collection<Email> emails = EmailParser.parseEmails(text);
    HashMap<String, UserSecurityInfo> localUsers = new HashMap<String, UserSecurityInfo>();
    HashMap<String, UserSecurityInfo> googleUsers = new HashMap<String, UserSecurityInfo>();
    List<UserSecurityInfo> list = dataProvider.getList();
    for (UserSecurityInfo u : list) {
      if (u.getUsername() != null) {
        localUsers.put(u.getUsername(), u);
      } else {
        googleUsers.put(u.getEmail(), u);
      }
    }
    int nAdded = 0;
    for (Email email : emails) {
      boolean localUser = (email.getUsername() != null);
      UserSecurityInfo u = localUser ? localUsers.get(email.getUsername()) : googleUsers.get(email
          .getEmail());
      if (u == null) {
        u = new UserSecurityInfo(email.getUsername(), email.getFullName(), email.getEmail(),
                                 UserType.REGISTERED);
        list.add(u);
        if (localUser) {
          localUsers.put(u.getUsername(), u);
        } else {
          googleUsers.put(u.getEmail(), u);
        }
        ++nAdded;
      }
    }
    if (nAdded != 0) {
      userTable.setPageSize(Math.max(15, list.size()));
      uiOutOfSyncWithServer();
    }
  }

  @UiHandler("button")
  void onUpdateClick(ClickEvent e) {
    updateUsersOnServer();
  }
}
