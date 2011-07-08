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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabPanel;

public class SubmissionTabUI extends TabPanel { 
  // Submission Navigation
  public static final SubTabs[] SUBMISSION_MENU = { SubTabs.FILTER, SubTabs.EXPORT};
  
  // Sub tabs
  private FilterSubTab filterSubTab;
  private ExportSubTab exportSubTab;

  private Map<SubTabs, SubTabInterface> subTabMap;
  
  public SubmissionTabUI(AggregateUI baseUI) {
    super();
 
    subTabMap = new HashMap<SubTabs,SubTabInterface>();
    
    // build the UI
    filterSubTab = new FilterSubTab();
    add(filterSubTab, SubTabs.FILTER.getTabLabel());
    subTabMap.put(SubTabs.FILTER, filterSubTab);
    
    exportSubTab = new ExportSubTab();
    add(exportSubTab, SubTabs.EXPORT.getTabLabel());
    subTabMap.put(SubTabs.EXPORT, exportSubTab);
    
    getElement().setId("second_level_menu");

    // register handler to manage tab selection change (and selecting our tab)
    baseUI.setSubMenuSelectionHandler(this, Tabs.SUBMISSIONS, SUBMISSION_MENU);
  } 
  
  public void warmUp() {
	  // warm up any tabs that are not selected.
	  // this is done in a timer that runs asynchronously
	  // so that the initial page render should be fast.
	  for ( int i = 0 ; i < SUBMISSION_MENU.length ; ++i ) {
		  boolean isVisible = this.isVisible();
		  boolean isSelectedTab = this.getTabBar().getSelectedTab() == i;
		  if ( !isVisible || !isSelectedTab ) {
			  GWT.log("background update " + SUBMISSION_MENU[i].getHashString());
			  subTabMap.get(SUBMISSION_MENU[i]).update();
		  }
	  }
  }
  
  public int findSubTabIndex(SubTabs subTab) {
    for (int i = 0; i < SUBMISSION_MENU.length; i++) {
      if (subTab.equals(SUBMISSION_MENU[i])) {
        return i;
      }
    }
    return 0;
  }
  
  public SubTabInterface getSubTab(SubTabs subTab) {
    return subTabMap.get(subTab);
  }
}