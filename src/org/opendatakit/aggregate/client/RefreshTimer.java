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
