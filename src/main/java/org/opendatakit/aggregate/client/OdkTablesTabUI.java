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

package org.opendatakit.aggregate.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Widget;
import java.util.ArrayList;
import java.util.HashSet;
import org.opendatakit.aggregate.client.odktables.TableEntryClient;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.security.common.GrantedAuthorityName;

public class OdkTablesTabUI extends AggregateTabBase {

  private ArrayList<TableEntryClient> mTables = new ArrayList<TableEntryClient>();
  private HashSet<TablesChangeNotification> mChangeNotifications = new HashSet<TablesChangeNotification>();
  public OdkTablesTabUI(AggregateUI baseUI) {
    super();

    // add the subtabs
    OdkTablesCurrentTablesSubTab ct = new OdkTablesCurrentTablesSubTab(this);
    OdkTablesViewTableSubTab vt = new OdkTablesViewTableSubTab(this);
    OdkTablesManageInstanceFilesSubTab tif = new OdkTablesManageInstanceFilesSubTab(this);
    OdkTablesManageTableFilesSubTab tlf = new OdkTablesManageTableFilesSubTab(this);
    OdkTablesManageAppLevelFilesSubTab alf = new OdkTablesManageAppLevelFilesSubTab(this);

    addSubTab(ct, SubTabs.CURRENTTABLES);
    addSubTab(vt, SubTabs.VIEWTABLE);
    addSubTab(tif, SubTabs.MANAGE_INSTANCE_FILES);
    addSubTab(tlf, SubTabs.MANAGE_TABLE_ID_FILES);
    addSubTab(alf, SubTabs.MANAGE_APP_LEVEL_FILES);

    updateVisibilityOdkTablesSubTabs();

    // register handler to manage tab selection change (and selecting our tab)
    registerClickHandlers(Tabs.ODKTABLES, baseUI);
  }

  /**
   * All the children rely on the top-level tab to maintain and update the
   * set of accessible tables.
   */
  public void update(TablesChangeNotification activeTab) {
    // listeners are cleared once the response comes back...
    mChangeNotifications.add(activeTab);

    if (mChangeNotifications.size() == 1) {
      GWT.log("ServerTableService.getTables() requested");
      // we don't have an outstanding request -- issue one
      if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
          .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
        SecureGWT.getServerTableService().getTables(new AsyncCallback<ArrayList<TableEntryClient>>() {

          @Override
          public void onFailure(Throwable caught) {
            if (caught instanceof AccessDeniedException) {
              // swallow it...
              AggregateUI.getUI().clearError();
              if (!mTables.isEmpty()) {
                // change our values and notify
                mTables = new ArrayList<TableEntryClient>();
                notifyListener(true);
              } else {
                notifyListener(false);
              }
            } else {
              // ignore error and clear pending listeners
              mChangeNotifications.clear();
              AggregateUI.getUI().reportError(caught);
            }
          }

          @Override
          public void onSuccess(ArrayList<TableEntryClient> tables) {
            AggregateUI.getUI().clearError();
            if (mTables.size() != tables.size() ||
                !mTables.containsAll(tables)) {
              mTables = tables;
              notifyListener(true);
            } else {
              notifyListener(false);
            }
          }
        });
      }
    }
  }

  private void notifyListener(boolean tableListChanged) {
    // make a copy...
    ArrayList<TablesChangeNotification> oldSet =
        new ArrayList<TablesChangeNotification>(this.mChangeNotifications);

    // clear listeners!
    this.mChangeNotifications.clear();

    // notify the listeners in the copy...
    GWT.log("ServerTableService.getTables() response received -- call updateTableSet() x " + oldSet.size());
    for (TablesChangeNotification subtab : oldSet) {
      subtab.updateTableSet(tableListChanged);
    }
  }

  public ArrayList<TableEntryClient> getTables() {
    return mTables;
  }

  public void updateVisibilityOdkTablesSubTabs() {

    /**
     * Admin tabs are still visible, but they have all
     * delete and add features disabled.
     */
    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
        .contains(GrantedAuthorityName.ROLE_SYNCHRONIZE_TABLES)) {
      changeVisibilityOdkTablesSyncSubTabs(true);
      changeVisibilityOdkTablesAdminSubTabs(true);
    } else {
      changeVisibilityOdkTablesSyncSubTabs(false);
      changeVisibilityOdkTablesAdminSubTabs(false);
    }

  }

  private void changeVisibilityOdkTablesSyncSubTabs(boolean outcome) {

    // hide the current files sub-tab
    {
      SubTabInterface odkTablesCurrentTables = getSubTab(SubTabs.CURRENTTABLES);
      OdkTablesCurrentTablesSubTab subTab = ((OdkTablesCurrentTablesSubTab) odkTablesCurrentTables);
      if (subTab != null) {
        subTab.setVisible(outcome);
        if (outcome) {
          subTab.update();
        }
      }
    }
    // hide the table data sub-tab
    {
      SubTabInterface odkTablesTableData = getSubTab(SubTabs.VIEWTABLE);
      OdkTablesViewTableSubTab subTab = ((OdkTablesViewTableSubTab) odkTablesTableData);
      if (subTab != null) {
        subTab.setVisible(outcome);
        if (outcome) {
          subTab.update();
        }
      }
    }
    // hide the table attachments sub-tab
    {
      SubTabInterface odkTablesTableAttachments = getSubTab(SubTabs.MANAGE_INSTANCE_FILES);
      OdkTablesManageInstanceFilesSubTab subTab = ((OdkTablesManageInstanceFilesSubTab) odkTablesTableAttachments);
      if (subTab != null) {
        subTab.setVisible(outcome);
        if (outcome) {
          subTab.update();
        }
      }
    }

    for (int i = 0; i < subTabPosition.size(); i++) {
      if (subTabPosition.get(i).equals(SubTabs.CURRENTTABLES) ||
          subTabPosition.get(i).equals(SubTabs.VIEWTABLE) ||
          subTabPosition.get(i).equals(SubTabs.MANAGE_INSTANCE_FILES)) {
        Widget w = ((Widget) this.getTabBar().getTab(i));
        if (w != null) {
          w.setVisible(outcome);
        }
      }
    }
  }

  private void changeVisibilityOdkTablesAdminSubTabs(boolean outcome) {

    // hide the app-level files sub-tab
    {
      SubTabInterface odkTablesAdmin = getSubTab(SubTabs.MANAGE_APP_LEVEL_FILES);
      OdkTablesManageAppLevelFilesSubTab subTab = ((OdkTablesManageAppLevelFilesSubTab) odkTablesAdmin);
      if (subTab != null) {
        subTab.setVisible(outcome);
        if (outcome) {
          subTab.update();
        }
      }
    }
    // hide the app-level files sub-tab
    {
      SubTabInterface odkTablesAdmin = getSubTab(SubTabs.MANAGE_TABLE_ID_FILES);
      OdkTablesManageTableFilesSubTab subTab = ((OdkTablesManageTableFilesSubTab) odkTablesAdmin);
      if (subTab != null) {
        subTab.setVisible(outcome);
        if (outcome) {
          subTab.update();
        }
      }
    }

    for (int i = 0; i < subTabPosition.size(); i++) {
      if (subTabPosition.get(i).equals(SubTabs.MANAGE_APP_LEVEL_FILES) ||
          subTabPosition.get(i).equals(SubTabs.MANAGE_TABLE_ID_FILES)) {
        Widget w = ((Widget) this.getTabBar().getTab(i));
        if (w != null) {
          w.setVisible(outcome);
        }
      }
    }
  }

  interface TablesChangeNotification {
    public void updateTableSet(boolean tableListChanged);
  }

}
