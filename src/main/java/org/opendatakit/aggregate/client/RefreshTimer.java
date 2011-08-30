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

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Timer;

/**
 * Background refresh timer that polls the server every 5 seconds for changes to
 * whichever tab is currently selected. If no UI interaction has happened for 5
 * minutes, the refreshes stop.
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class RefreshTimer extends Timer {

  // SECURITY_REFRESH_INTERVAL is the interval between
  // background reloads of the user's security credentials.
  public static final int SECURITY_REFRESH_INTERVAL = 5 * 60 * 1000; // ms

  // REFRESH_INTERVAL is the interval between callbacks.
  // Upon testing, this is the scheduling interval, not the
  // inter-callback interval. So if the computer on which the
  // browser is running is slow, you can get a backlog of
  // callbacks queued up.
  private static final int REFRESH_INTERVAL = 5000; // ms
  // private static final int REFRESH_INTERVAL = 100000; // ms

  // STALL_INTERVALS is the number of intervals of no UI
  // interaction after which the timer will be stopped.
  private static final int UI_STALL_INTERVALS = 60; // 5 min / 5 sec each

  // lastCompletionTime tracks the completion timestamp
  // of the last timer action. Used to detect and skip
  // any timer callback backlog.
  private long lastCompletionTime = 0L;

  // intervalsElapsed counts the intervals since a UI interaction
  private int intervalsElapsed = 0;

  // isActive tracks the active/cancelled state of the timer
  // the GWT timer doesn't provide this information.
  private boolean isActive = false;
  private boolean isInitializing = true;

  private AggregateUI aggregateUI;

  private SubTabs currentSubTab;

  public RefreshTimer(AggregateUI ui) {
    aggregateUI = ui;
  }

  public void setInitialized() {
    isInitializing = false;
  }

  public boolean canLeaveCurrentSubTab() {
    if (currentSubTab != null) {
      SubTabInterface tabPanel = aggregateUI.getSubTab(currentSubTab);
      if (tabPanel != null) {
        return tabPanel.canLeave();
      } else {
        // should not happen
        GWT.log("currentSubTab (" + currentSubTab.getHashString()
            + ") could not be found in RefreshTimer.canLeaveCurrentSubTab()");
      }
    }
    return true;
  }

  public void setCurrentSubTab(SubTabs subtab) {
    currentSubTab = subtab;
    refreshNow();
  }

  public void refreshNow() {
    // reset the ui inactivity counter.
    restartTimer();
    // set the lastCompletionTime to zero
    // this bypasses the backlog check.
    // The zero value is also used to identify
    // when we are inside a refreshNow() call.
    lastCompletionTime = 0L;
    // set the intervalsElapsed to -1
    // this ensures that all less
    // frequent actions (those modulo N)
    // actually run.
    intervalsElapsed = -1;
    // trigger a run of the timer.
    run();
  }

  @Override
  public void cancel() {
    super.cancel();
    isActive = false;
  }

  public void restartTimer() {
    if (isActive) {
      // just reset the intervalsElapsed
      intervalsElapsed = 0;
    } else {
      // reset the ui inactivity counter
      // restart the periodic timer
      // set the isActive flag
      intervalsElapsed = 0;
      scheduleRepeating(REFRESH_INTERVAL);
      isActive = true;
    }
  }

  @Override
  public void run() {
    if (isInitializing)
      return;

    long timeRefreshStart = System.currentTimeMillis();
    if (lastCompletionTime + REFRESH_INTERVAL - (REFRESH_INTERVAL / 10L) > timeRefreshStart) {
      // timer is backed up -- flush the queued callbacks
      GWT.log("timer is backed up -- skipping");
      return;
    }

    if (intervalsElapsed == UI_STALL_INTERVALS) {
      // this appears to be an idle UI - stop all refresh polling.
      cancel();
    }
    intervalsElapsed++;
    
    if (currentSubTab != null) {
      SubTabInterface tabPanel = aggregateUI.getSubTab(currentSubTab);
      if (tabPanel == null) {
        // should not happen
        GWT.log("currentSubTab (" + currentSubTab.getHashString()
            + ")could not be found in RefreshTimer.run()");
      }
      
      switch (currentSubTab) {
      case FORMS:
      case FILTER:
      case TABLES:
        if ((intervalsElapsed % 3) == 0) {
          tabPanel.update();
        }
        break;
      case EXPORT:
      case PUBLISH:
        tabPanel.update();
        break;
      case PREFERENCES:
        if ((intervalsElapsed % 6) == 0) {
          GWT.log("PREFERENCES UPDATE CALLED FROM TIMER");
          tabPanel.update();
        }
        break;
      case PERMISSIONS:
        if (lastCompletionTime == 0L) {
          // update this ONLY if we are forcing a refreshNow().
          // otherwise, let the entries be stale w.r.t. server.
          tabPanel.update();
        }
        break;
      default:
        // should not happen
        GWT.log("currentSubTab (" + currentSubTab.getHashString()
            + ") has no defined action in RefreshTimer.run()");
      }
    }
    // record last completion time...
    lastCompletionTime = System.currentTimeMillis();
    long timerActionDuration = lastCompletionTime - timeRefreshStart;
    if (timerActionDuration > REFRESH_INTERVAL) {
      GWT.log("update time " + Long.toString(timerActionDuration));
    }
  }
}
