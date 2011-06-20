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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.opendatakit.common.security.client.GrantedAuthorityInfo;
import org.opendatakit.common.security.client.UserClassSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo;
import org.opendatakit.common.security.client.UserSecurityInfo.UserType;
import org.opendatakit.common.security.client.security.admin.SecurityAdminServiceAsync;
import org.opendatakit.common.security.common.EmailParser;
import org.opendatakit.common.security.common.EmailParser.Email;
import org.opendatakit.common.security.common.GrantedAuthorityNames;

import com.google.gwt.cell.client.ActionCell;
import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextInputCell;
import com.google.gwt.cell.client.ValueUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.InputElement;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Cookies;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class TemporaryAccessConfigurationSheet extends Composite implements ActionCell.Delegate<UserSecurityInfo> {

   private static final int STATIC_USER_TABLE_COLUMNS = 4;
   private static final int ERROR_MESSAGE_RETENTION_INTERVAL_MILLISECONDS = 2000;
   private static final String K_INVALID_EMAIL_CHARACTERS = " \t\n\r\",;()<>?/{}'[]";
   private static TemporaryAccessConfigurationSheetUiBinder uiBinder = GWT
         .create(TemporaryAccessConfigurationSheetUiBinder.class);

   interface TemporaryAccessConfigurationSheetUiBinder extends
         UiBinder<Widget, TemporaryAccessConfigurationSheet> {
   }
   
   ListDataProvider<UserSecurityInfo> dataProvider = new ListDataProvider<UserSecurityInfo>();
   long errorTimestamp = 0;
   
   private class GroupMembership extends Column<UserSecurityInfo,Boolean> 
                  implements FieldUpdater<UserSecurityInfo, Boolean> {
      final GrantedAuthorityInfo auth;
      
      GroupMembership(GrantedAuthorityInfo auth) {
         super(new CheckboxCell(true, false));
         this.auth = auth;
         this.setFieldUpdater(this);
         this.setSortable(true);
      }

      @Override
      public Boolean getValue(UserSecurityInfo object) {
         return object.getAssignedUserGroups().contains(auth);
      }

      @Override
      public void update(int index, UserSecurityInfo object, Boolean value) {
         if ( value ) {
            object.getAssignedUserGroups().add(auth);
         } else {
            object.getAssignedUserGroups().remove(auth);
         }
      }
   }

   @Override
   public void execute(UserSecurityInfo object) {
      dataProvider.getList().remove(object);
      // dataProvider.refresh();
   }

   private static boolean hasInvalidEmailCharacters( String email ) {
      for ( int i = 0 ; i < K_INVALID_EMAIL_CHARACTERS.length(); ++i) {
         char ch = K_INVALID_EMAIL_CHARACTERS.charAt(i);
         if ( email.indexOf(ch) != -1 ) {
            return true;
         }
      }
      return false;
   }
   
   private void setError(String error) {
      errorMessage.setText(error);
      errorMessage.setVisible(true);
      errorTimestamp = System.currentTimeMillis();
   }
   
   private void clearError() {
      if ( errorTimestamp != 0 && System.currentTimeMillis() > 
         ERROR_MESSAGE_RETENTION_INTERVAL_MILLISECONDS + errorTimestamp ) {
         errorMessage.setVisible(false);
         errorMessage.setText("");
         errorTimestamp = 0;
      }
   }

   @Override
   public void onBrowserEvent(Event event) {
      clearError();
      super.onBrowserEvent(event);
   }
   
   class ValidatingTextInputCell extends TextInputCell {
      ValidatingTextInputCell() {
         super();
      };


      /**
       * Because of various scoping issues, we need to override the 
       * onBrowserEvent method.
       */
      @Override
      public void onBrowserEvent(Context context, Element parent, String value,
            NativeEvent event, ValueUpdater<String> valueUpdater) {

         // all events other than the value-change event are handled normally
          String eventType = event.getType();
          if (! "change".equals(eventType)) {
               super.onBrowserEvent(context, parent, value, event, valueUpdater);
              return;
          }
          
          // Ignore events that don't target the input.
          InputElement input = getInputElement(parent);
          Element target = event.getEventTarget().cast();
          if (!input.isOrHasChild(target)) {
            super.onBrowserEvent(context, parent, value, event, valueUpdater);
           return;
          }

          Object key = context.getKey();
           String newValue = super.getInputElement(parent).getValue();

          // Get the view data.
          ViewData vd = super.getViewData(key);
          if (vd == null) {
            vd = new ViewData(value);
            setViewData(key, vd);
          }

          String updateValue = null;
         if ( newValue != null ) {
            updateValue = newValue.trim();
         }
         
         String errorMessage = null;
         if ( updateValue != null && updateValue.length() != 0 ) {
            if ( hasInvalidEmailCharacters(updateValue) ) {
               errorMessage = "Invalid characters in Email address.\n" +
                     "Email address cannot contain whitespace, quotes,\n" +
                     "commas, semicolons or other punctuation";
               updateValue = vd.getLastValue();
            } else if ( newValue.indexOf(EmailParser.K_AT) == -1) {
               errorMessage = "Email address is missing the '@domain.org' portion\n" +
                     "Email must be of the form 'username@domain.org'";
               updateValue = vd.getLastValue();
            }
         }
         
         // possibly trim the incoming value or restore it if there was an error.
         super.getInputElement(parent).setValue(updateValue);
         // handle the change event 'normally', perhaps restoring the value
         super.onBrowserEvent(context, parent, updateValue, event, valueUpdater);
         if ( errorMessage != null ) {
            // set the error message
            setError(errorMessage);
         }
      }

   }

   private class ValidatingTextInputColumn extends Column<UserSecurityInfo,String> {

      class ValidatingFieldUpdater implements FieldUpdater<UserSecurityInfo,String> {

         @Override
         public void update(int index, UserSecurityInfo object, String value) {
            if ( value != null ) {
               value = value.trim();
            }
            
            if ( value != null && value.length() != 0 ) {
               if ( hasInvalidEmailCharacters(value) || value.indexOf(EmailParser.K_AT) == -1) {
                  throw new IllegalStateException("Invalid value should have been caught earlier.");
               } else if ( value.indexOf(EmailParser.K_AT) != -1) {
                  object.setEmail(EmailParser.K_MAILTO + value);
               }
            } else {
               object.setEmail(null);
               userTable.redraw();
            }
         }
      };
      
      ValidatingFieldUpdater updater = new ValidatingFieldUpdater();
      
      ValidatingTextInputColumn() {
         super(new ValidatingTextInputCell());
         setSortable(true);
         setFieldUpdater(updater);
      }

      @Override
      public String getValue(UserSecurityInfo object) {
         String email = object.getEmail();
         if ( email != null ) {
            return email.substring(EmailParser.K_MAILTO.length());
         }
         return null;
      }
   };
   
   private PermissionsSubTab permissionsTab;
   
   public TemporaryAccessConfigurationSheet(PermissionsSubTab permissionsTab) {
     this.permissionsTab = permissionsTab; 
     initWidget(uiBinder.createAndBindUi(this));
      sinkEvents(Event.ONCHANGE | Event.ONCLICK);

      Column<UserSecurityInfo,UserSecurityInfo> deleteMe = new Column<UserSecurityInfo,UserSecurityInfo>
                  (new ActionCell<UserSecurityInfo>("Remove", this){}) {

                     @Override
                     public UserSecurityInfo getValue(UserSecurityInfo object) {
                        return object;
                     }
      };
      userTable.addColumn(deleteMe, "");
      
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
      Column<UserSecurityInfo,String> nickname = new Column<UserSecurityInfo,String>
                  (new TextInputCell()) {
         @Override
         public String getValue(UserSecurityInfo object) {
            return object.getNickname();
         }
         
      };
      nickname.setSortable(true);
      nickname.setFieldUpdater(new FieldUpdater<UserSecurityInfo,String>() {

         @Override
         public void update(int index, UserSecurityInfo object, String value) {
            object.setNickname(value);
            userTable.redraw();
         }});
      userTable.addColumn(nickname, "Nickname");
      
      // Email
      Column<UserSecurityInfo,String> email = new ValidatingTextInputColumn();
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

   protected void reviseCellTableColumns(ArrayList<UserSecurityInfo> allUsersList) {
      TreeSet<GrantedAuthorityInfo> uniqueAuths = new TreeSet<GrantedAuthorityInfo>();
      uniqueAuths.add(new GrantedAuthorityInfo(GrantedAuthorityNames.GROUP_SITE_ADMINS));
      uniqueAuths.add(new GrantedAuthorityInfo(GrantedAuthorityNames.GROUP_FORM_ADMINS));
      uniqueAuths.add(new GrantedAuthorityInfo(GrantedAuthorityNames.GROUP_SUBMITTERS));
      for ( int i = 0 ; i < allUsersList.size() ; ++i ) {
         UserSecurityInfo u = allUsersList.get(i);
         uniqueAuths.addAll(u.getAssignedUserGroups());
      }
      while ( userTable.getColumnCount() > STATIC_USER_TABLE_COLUMNS ) {
         userTable.removeColumn(userTable.getColumnCount()-1);
      }
      for ( GrantedAuthorityInfo auth : uniqueAuths ) {
         userTable.addColumn(new GroupMembership(auth), auth.getName());
      }
   }

   @Override
   public void setVisible(boolean isVisible) {
      super.setVisible(isVisible);
      if ( isVisible ) {
         if ( service == null ) {
            this.service = SecureGWT.get().createSecurityAdminService();
         }
         clearError(); // because navigating off the page might not have sent a mouse event...
         service.getAllUsers(true, new AsyncCallback<ArrayList<UserSecurityInfo> > () 
            {
               @Override
               public void onFailure(Throwable caught) {
                  Window.alert("Unable to access server: " + caught.getMessage());
               }
   
               @Override
               public void onSuccess(ArrayList<UserSecurityInfo> result) {
                  reviseCellTableColumns(result);
                  dataProvider.getList().clear();
                  dataProvider.getList().addAll(result);
                  addedUsers.setText("");
               }
            });
         service.getUserClassPrivileges(GrantedAuthorityNames.USER_IS_ANONYMOUS.toString(), new AsyncCallback<UserClassSecurityInfo>()
            {
               @Override
               public void onFailure(Throwable caught) {
                  Window.alert("Unable to access server: " + caught.getMessage());
               }

               @Override
               public void onSuccess(UserClassSecurityInfo result) {
                  anonymousSubmitters.setValue( 
                        result.getGrantedAuthorities().contains( 
                              new GrantedAuthorityInfo(
                                    GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD.toString())) );
                  anonymousAttachmentViewers.setValue(
                        result.getGrantedAuthorities().contains(
                              new GrantedAuthorityInfo(
                                    GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.toString())));
               }
            });
      }
   }

   @UiField
   TextArea addedUsers;
   @UiField
   Button addNow;
   @UiField
   Label errorMessage;
   @UiField
   CellTable<UserSecurityInfo> userTable;
   @UiField
   CheckBox anonymousSubmitters;
   @UiField
   CheckBox anonymousAttachmentViewers;
   @UiField
   Button button;
   @UiField
   Button gotoAdvanced;
   
   SecurityAdminServiceAsync service;
   
   
   @UiHandler("gotoAdvanced")
   void onGotoAdvancedClick(ClickEvent e) {
     clearError();
     permissionsTab.createAdvanced();
   }
   
   @UiHandler("addNow")
   void onAddUsersClick(ClickEvent e) {
      String text = addedUsers.getText();
      Collection<Email> emails = EmailParser.parseEmails(text);
      Map<String, UserSecurityInfo> users = new HashMap<String, UserSecurityInfo>();
      List<UserSecurityInfo> list = dataProvider.getList();
      for ( UserSecurityInfo u : list ) {
         users.put(u.getUsername(), u);
      }
      int nAdded = 0;
      int nUnchanged = 0;
      for ( Email email : emails ) {
         String username = email.getUsername();
         UserSecurityInfo u = users.get(username);
         if ( u != null ) {
            ++nUnchanged;
         } else {
            u = new UserSecurityInfo(email.getUsername(), 
                  email.getNickname(), email.getEmail(), UserType.REGISTERED);
            list.add(u);
            users.put(u.getUsername(), u);
            ++nAdded;
         }
      }
      Window.alert("Added " + Integer.toString(nAdded) + " users.");
   }
   
   @UiHandler("button")
   void onUpdateClick(ClickEvent e) {
      ArrayList<GrantedAuthorityInfo> allGroups = new ArrayList<GrantedAuthorityInfo>();
      allGroups.add(new GrantedAuthorityInfo(GrantedAuthorityNames.GROUP_SITE_ADMINS));
      allGroups.add(new GrantedAuthorityInfo(GrantedAuthorityNames.GROUP_FORM_ADMINS));
      allGroups.add(new GrantedAuthorityInfo(GrantedAuthorityNames.GROUP_SUBMITTERS));
      allGroups.add(new GrantedAuthorityInfo(GrantedAuthorityNames.USER_IS_ANONYMOUS.toString()));
      
      ArrayList<GrantedAuthorityInfo> anonGrants = new ArrayList<GrantedAuthorityInfo>();
      if ( anonymousSubmitters.getValue() ) {
         anonGrants.add(new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_FORM_DOWNLOAD.toString()));
         anonGrants.add(new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_FORM_LIST.toString()));
         anonGrants.add(new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_SUBMISSION_UPLOAD.toString()));
      }
      if ( anonymousAttachmentViewers.getValue() ) {
         anonGrants.add(new GrantedAuthorityInfo(GrantedAuthorityNames.ROLE_ATTACHMENT_VIEWER.toString()));
      }
      
      ArrayList<UserSecurityInfo> users = new ArrayList<UserSecurityInfo>();
      users.addAll(dataProvider.getList());
      service.setUsersAndGrantedAuthorities(Cookies.getCookie("JSESSIONID"), 
                           users, anonGrants, allGroups, new AsyncCallback<Void>() {

         @Override
         public void onFailure(Throwable caught) {
            Window.alert("Incomplete security update: " + caught.getMessage());
         }

         @Override
         public void onSuccess(Void result) {
            Window.alert("Successful update of site access configuration");
         }
      });
   }
}
