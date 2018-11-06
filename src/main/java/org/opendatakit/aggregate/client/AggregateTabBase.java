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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.TabPanel;
import java.util.ArrayList;
import java.util.HashMap;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.Tabs;

public class AggregateTabBase extends TabPanel {

  // Navigation
  protected ArrayList<SubTabs> subTabPosition;

  protected HashMap<SubTabs, SubTabInterface> subTabMap;

  public AggregateTabBase() {
    super();

    subTabPosition = new ArrayList<SubTabs>();
    subTabMap = new HashMap<SubTabs, SubTabInterface>();

    getElement().addClassName("second_level_menu");
    getElement().getFirstChildElement().getFirstChildElement().addClassName("tab_measure_2");

  }

  protected void addSubTab(AggregateSubTabBase panel, SubTabs subTab) {
    int insertIndex = subTabPosition.size();

    // add panel into position
    subTabPosition.add(insertIndex, subTab);
    insert(panel, subTab.getTabLabel(), insertIndex);
    subTabMap.put(subTab, panel);
  }

  /**
   * register handler to manage tab selection change (and selecting our tab)
   *
   * @param tab
   * @param baseUI
   */
  protected void registerClickHandlers(Tabs tab, AggregateUI baseUI) {
    baseUI.setSubMenuSelectionHandler(this, tab, subTabPosition.toArray(new SubTabs[subTabPosition.size()]));
  }

  /**
   * warm up any tabs that are not selected.
   * this is done in a timer that runs asynchronously
   * so that the initial page render should be fast.
   */
  public void warmUp() {
    for (int i = 0; i < subTabPosition.size(); ++i) {
      boolean isVisible = this.isVisible();
      boolean isSelectedTab = this.getTabBar().getSelectedTab() == i;
      if (!isVisible || !isSelectedTab) {
        GWT.log("background update " + subTabPosition.get(i).getHashString());
        subTabMap.get(subTabPosition.get(i)).update();
      }
    }
  }

  public int findSubTabIndex(SubTabs subTab) {
    int index = subTabPosition.indexOf(subTab);

    // if object is not found java sets value to -1
    if (index < 0) {
      return 0;
    }

    return index;
  }

  public int findSubTabIndexFromHash(UrlHash hash) {
    int selectedSubTab = 0;

    String subMenuHash = hash.get(UrlHash.SUB_MENU);
    if (subMenuHash != null) {
      for (int i = 0; i < subTabPosition.size(); ++i) {
        if (subMenuHash.equals(subTabPosition.get(i).getHashString())) {
          // found the menu that should be selected...
          selectedSubTab = i;
        }
      }
    }
    return selectedSubTab;
  }

  public SubTabInterface getSubTab(SubTabs subTab) {
    return subTabMap.get(subTab);
  }
}
