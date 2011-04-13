package org.opendatakit.aggregate.client;

import com.google.gwt.user.client.Timer;

public class RefreshTimer {
	private static final int REFRESH_INTERVAL = 30000; // ms
	private static final int STALL_INTERVALS = 10; // 5 min / 30 sec
	Timer refreshTimer;
	int intervalsElapsed = 0;
	
	public RefreshTimer(final AggregateUI aggregateUI) {
	    // Setup timer to refresh list automatically.
	    refreshTimer = new Timer() {
	       @Override
	       public void run() {
	    	   if(intervalsElapsed == STALL_INTERVALS) {
	    		   this.cancel();
	    	   } else {
	    		   intervalsElapsed++;
		    	   aggregateUI.update();	   
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
