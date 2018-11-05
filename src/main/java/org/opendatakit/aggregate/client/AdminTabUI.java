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

import com.google.gwt.user.client.ui.Widget;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;

public class AdminTabUI extends AggregateTabBase {

  private PermissionsSubTab permissionsSubTab;

  public AdminTabUI(AggregateUI baseUI) {
    super();

    // build the UI
    permissionsSubTab = new PermissionsSubTab();
    addSubTab(permissionsSubTab, SubTabs.PERMISSIONS);
    addSubTab(new PreferencesSubTab(), SubTabs.PREFERENCES);

    // show panel by default, so need to hide it
    if (!Preferences.getOdkTablesEnabled()) {
      hideOdkTablesSubTab();
    }

    // register handler to manage tab selection change (and selecting our tab)
    registerClickHandlers(Tabs.ADMIN, baseUI);
  }

  private void changeVisibilityOdkTablesSubTab(boolean outcome) {
    for (int i = 0; i < subTabPosition.size(); i++) {
      if (subTabPosition.get(i).equals(SubTabs.TABLES)) {
        Widget w = ((Widget) this.getTabBar().getTab(i));
        if (w != null) {
          w.setVisible(outcome);
        }
      }
    }
  }

  public void displayOdkTablesSubTab() {
    changeVisibilityOdkTablesSubTab(true);
    permissionsSubTab.changeTablesPrivilegesVisibility(true);
  }

  public void hideOdkTablesSubTab() {
    changeVisibilityOdkTablesSubTab(false);
    permissionsSubTab.changeTablesPrivilegesVisibility(false);
  }

}
