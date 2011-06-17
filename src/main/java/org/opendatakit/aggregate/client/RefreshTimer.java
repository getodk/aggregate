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

import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.user.client.Timer;

public class RefreshTimer extends Timer {
  private static final int REFRESH_INTERVAL = 1000; // ms

//  private static final int REFRESH_INTERVAL = 5000; // ms
  private static final int STALL_INTERVALS = 60; // 5 min / 5 sec
  private int intervalsElapsed = 0;
  private AggregateUI aggregateUI;

  private SubTabs currentSubTab;

  public RefreshTimer(AggregateUI ui) {
    aggregateUI = ui;

    // Setup timer to refresh list automatically.
    scheduleRepeating(REFRESH_INTERVAL);
  }

  public void setCurrentSubTab(SubTabs subtab) {
    currentSubTab = subtab;
    restartTimer();
  }

  public void refreshNow() {
    // cause an update
    run();
  }
  
  public void restartTimer() {
    // stop the auto refresh
    cancel();

    // Restart timer to refresh list automatically.
    scheduleRepeating(REFRESH_INTERVAL);
    intervalsElapsed = 0;
  }

  @Override
  public void run() {
    if (intervalsElapsed == STALL_INTERVALS) {
      this.cancel();
    }
    intervalsElapsed++;
    
    if(currentSubTab == null) {
      return;
    }
    SubTabInterface tabPanel;
    
    switch (currentSubTab) {
    case FORMS:
      if ((intervalsElapsed % 3) == 0) {
        tabPanel = aggregateUI.getManageNav().getSubTab(currentSubTab);
        tabPanel.update();
      }
      break;
    case FILTER:
      if ((intervalsElapsed % 3) == 0) {
        tabPanel = aggregateUI.getSubmissionNav().getSubTab(currentSubTab);
        tabPanel.update();
      }
      break;
    case PUBLISH:
      tabPanel = aggregateUI.getManageNav().getSubTab(currentSubTab);
      tabPanel.update();
      break;
    case EXPORT:
      tabPanel = aggregateUI.getSubmissionNav().getSubTab(currentSubTab);
      tabPanel.update();
      break;
    case PREFERENCES:
      if ((intervalsElapsed % 6) == 0) {
        tabPanel = aggregateUI.getManageNav().getSubTab(currentSubTab);
        tabPanel.update();
      }
      break;
    default:
      // should not happen

    }

  }
}
