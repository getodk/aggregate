package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;

public class AdminTabUI extends AggregateTabBase {
  
  private OdkTablesAdminSubTab odkTablesAdminTab;
  
  public AdminTabUI(AggregateUI baseUI) {
    super();
    
    // build the UI    
    addSubTab(new PermissionsSubTab(), SubTabs.PERMISSIONS);
    addSubTab(new PreferencesSubTab(), SubTabs.PREFERENCES);
    
    odkTablesAdminTab = new OdkTablesAdminSubTab();
    addSubTab(odkTablesAdminTab, SubTabs.TABLES);
    
    // show panel by default, so need to hide it
    if(!Preferences.getOdkTablesEnabled()) {
      hideOdkTablesSubTab();
    }
    
    // register handler to manage tab selection change (and selecting our tab)
    registerClickHandlers(Tabs.ADMIN, baseUI);
  }

  public void displayOdkTablesSubTab() {
    showSubTab(odkTablesAdminTab, SubTabs.TABLES);
  }
  
  public void hideOdkTablesSubTab() {
    hideSubTab(odkTablesAdminTab);
  }
  
}
