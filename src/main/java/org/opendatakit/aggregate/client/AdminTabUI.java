package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;

public class AdminTabUI extends AggregateTabBase {
  
  public AdminTabUI(AggregateUI baseUI) {
    super();
    
    // build the UI    
    addSubTab(new PermissionsSubTab(), SubTabs.PERMISSIONS);
    addSubTab(new PreferencesSubTab(), SubTabs.PREFERENCES);
    addSubTab(new OdkTablesAdminSubTab(), SubTabs.TABLES);   
    
    // register handler to manage tab selection change (and selecting our tab)
    registerClickHandlers(Tabs.ADMIN, baseUI);
  }

}
