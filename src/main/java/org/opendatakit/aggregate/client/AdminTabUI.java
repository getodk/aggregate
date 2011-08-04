package org.opendatakit.aggregate.client;

import java.util.HashMap;
import java.util.Map;

import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabPanel;

public class AdminTabUI extends TabPanel {

  // Management Navigation
  public static final SubTabs[] ADMIN_MENU = {SubTabs.PERMISSIONS, SubTabs.PREFERENCES, SubTabs.TABLES};

  // Sub tabs
  private PreferencesSubTab preferencesSubTab;
  private PermissionsSubTab permissionsSubTab;
  private OdkTablesAdminSubTab odkTablesSubTab;
  
  private Map<SubTabs, SubTabInterface> subTabMap;
  
  public AdminTabUI(AggregateUI baseUI) {
    super();

    subTabMap = new HashMap<SubTabs,SubTabInterface>();
    
    // build the UI    
    permissionsSubTab = new PermissionsSubTab();
    this.add(permissionsSubTab, SubTabs.PERMISSIONS.getTabLabel());
    subTabMap.put(SubTabs.PERMISSIONS, permissionsSubTab);

    preferencesSubTab = new PreferencesSubTab();
    this.add(preferencesSubTab, SubTabs.PREFERENCES.getTabLabel());
    subTabMap.put(SubTabs.PREFERENCES, preferencesSubTab);
    
    odkTablesSubTab = new OdkTablesAdminSubTab();
    this.add(odkTablesSubTab, SubTabs.TABLES.getTabLabel());
    subTabMap.put(SubTabs.TABLES, odkTablesSubTab);
    
    getElement().setId("second_level_menu");
    
    // register handler to manage tab selection change (and selecting our tab)
    baseUI.setSubMenuSelectionHandler(this, Tabs.ADMIN, ADMIN_MENU);
  }

  public void warmUp() {
     // warm up any tabs that are not selected.
     // this is done in a timer that runs asynchronously
     // so that the initial page render should be fast.
     for ( int i = 0 ; i < ADMIN_MENU.length ; ++i ) {
        boolean isVisible = this.isVisible();
        boolean isSelectedTab = this.getTabBar().getSelectedTab() == i;
        if ( !isVisible || !isSelectedTab ) {
           GWT.log("background update " + ADMIN_MENU[i].getHashString());
           subTabMap.get(ADMIN_MENU[i]).update();
        }
     }
  }
  
  public SubTabInterface getSubTab(SubTabs subTab) {
    return subTabMap.get(subTab);
  }
}
