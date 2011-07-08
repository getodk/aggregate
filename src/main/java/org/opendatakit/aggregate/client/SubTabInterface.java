package org.opendatakit.aggregate.client;

public interface SubTabInterface {
	/**
	 * The canLeave predicate returns true if the focus of the app can move
	 * off of the current SubTab.  This is called when navigating between tabs,
	 * before setCurrentSubTab is called, so that a tab with focus may prompt 
	 * the user to see if they really want to move off of that SubTab.
	 * 
	 * @return
	 */
	public boolean canLeave();
	
	public void update();
}
