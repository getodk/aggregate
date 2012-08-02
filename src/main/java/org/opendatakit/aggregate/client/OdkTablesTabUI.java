package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;

public class OdkTablesTabUI extends AggregateTabBase {

	  public OdkTablesTabUI(AggregateUI baseUI) {
		    super();
		    
		    // add the subtabs
		    addSubTab(new OdkTablesCurrentTablesSubTab(), SubTabs.CURRENTTABLES);
		    addSubTab(new OdkTablesViewTableSubTab(), SubTabs.VIEWTABLE);
		   
		    // register handler to manage tab selection change (and selecting our tab)
		    registerClickHandlers(Tabs.ODKTABLES, baseUI);
		  }
}
