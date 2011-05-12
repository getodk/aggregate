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

import org.opendatakit.aggregate.constants.common.FormOrFilter;
import org.opendatakit.aggregate.constants.common.PageUpdates;

import com.google.gwt.user.client.Timer;

public class RefreshTimer {
	private static final int REFRESH_INTERVAL = 10000; // ms
	private static final int STALL_INTERVALS = 30; // 5 min / 10 sec
	Timer refreshTimer;
	int intervalsElapsed = 0;
	
	public RefreshTimer(final AggregateUI aggregateUI) {
	    // Setup timer to refresh list automatically.
	    refreshTimer = new Timer() {
	       @Override
	       public void run() {
	    	   if(intervalsElapsed == STALL_INTERVALS) {
	    		   this.cancel();
	    	   } else if (intervalsElapsed % 3 == 0){
	    		   intervalsElapsed++;
		    	   aggregateUI.update(FormOrFilter.BOTH, PageUpdates.ALL);	   
	    	   } else {
	    		   intervalsElapsed++;
	    		   aggregateUI.update(FormOrFilter.FORM, PageUpdates.FORMTABLE);
	    	   }
	       }
	    };
	    refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
	}
	
	public void restartTimer(final AggregateUI aggregateUI) {
		// Restart timer to refresh list automatically.
		refreshTimer.cancel();
		refreshTimer.run();
		refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
		intervalsElapsed = 0;
	}
}
