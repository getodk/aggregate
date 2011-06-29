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

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;

import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.TabPanel;

public class ManageTabUI extends TabPanel {

  // Management Navigation
  public static final SubTabs[] MANAGEMENT_MENU = { SubTabs.FORMS,
      SubTabs.PUBLISH, SubTabs.PERMISSIONS, SubTabs.PREFERENCES};

  // Sub tabs
  private FormsSubTab formsSubTab;
  private PublishSubTab publishSubTab;
  private PreferencesSubTab preferencesSubTab;
  private PermissionsSubTab permissionsSubTab;
  
  private Map<SubTabs, SubTabInterface> subTabMap;
  
  public ManageTabUI(AggregateUI baseUI) {
    super();

    subTabMap = new HashMap<SubTabs,SubTabInterface>();
    
    // build the UI
    formsSubTab = new FormsSubTab(baseUI);
    formsSubTab.update();
    this.add(formsSubTab, SubTabs.FORMS.getTabLabel());
    subTabMap.put(SubTabs.FORMS, formsSubTab);

    publishSubTab = new PublishSubTab(baseUI);
    publishSubTab.update();
    this.add(publishSubTab, SubTabs.PUBLISH.getTabLabel());
    subTabMap.put(SubTabs.PUBLISH, publishSubTab);
    
    permissionsSubTab = new PermissionsSubTab();
    permissionsSubTab.configure();
    this.add(permissionsSubTab, SubTabs.PERMISSIONS.getTabLabel());
    subTabMap.put(SubTabs.PERMISSIONS, permissionsSubTab);

    preferencesSubTab = new PreferencesSubTab();
    preferencesSubTab.update();
    this.add(preferencesSubTab, SubTabs.PREFERENCES.getTabLabel());
    subTabMap.put(SubTabs.PREFERENCES, preferencesSubTab);
    

    
    getElement().setId("second_level_menu");
    
    // navigate to proper subtab on creation based on the URL
    UrlHash hash = UrlHash.getHash();
    int selected = 0;
    String subMenu = hash.get(UrlHash.SUB_MENU);
    for (int i = 0; i < MANAGEMENT_MENU.length; i++) {
      if (subMenu.equals(MANAGEMENT_MENU[i].getHashString())) {
        selected = i;
      }
    }

    // creating the sub tab click handlers
    for (int i = 0; i < MANAGEMENT_MENU.length; i++) {
      ClickHandler handler = baseUI.getSubMenuClickHandler(Tabs.MANAGEMENT, MANAGEMENT_MENU[i]);
      this.getTabBar().getTab(i).addClickHandler(handler);
    }

    this.selectTab(selected);
    baseUI.getTimer().setCurrentSubTab(MANAGEMENT_MENU[selected]);
  }

  public SubTabInterface getSubTab(SubTabs subTab) {
    return subTabMap.get(subTab);
  }
}