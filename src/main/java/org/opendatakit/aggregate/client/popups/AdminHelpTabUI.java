package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AdminHelpTabUI extends VerticalPanel {
	
	public AdminHelpTabUI() {
		add(new HTML("<b>" + SubTabs.PERMISSIONS.getTabLabel() + "<b>"));
		add(new HTML("This is the page where you can view and edit settings for users that you " +
				"manage. <br><br>"));
		add(new HTML("<b>" + SubTabs.PREFERENCES.getTabLabel() + "<b>"));
		add(new HTML("This is the page where you can obtain or upgrade a Google Maps API key, " +
				"and include ODK Tables synchronization. <br><br>"));
		add(new HTML("<b>" + SubTabs.TABLES.getTabLabel() + "<b>"));
		add(new HTML("This is the page where you can add and remove administrative users to " +
				"edit the form data."));
	}

}
