package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManagementHelpTabUI extends VerticalPanel {
	
	public ManagementHelpTabUI() {
		add(new HTML("<b>" + SubTabs.FORMS.getTabLabel() + "<b>"));
		add(new HTML("This is the page where you can view your current forms, edit settings, " +
				"publish or export the form data, and delete the form. <br><br>"));
		add(new HTML("<b>" + SubTabs.PUBLISH.getTabLabel() + "<b>"));
		add(new HTML("This is the page where you can view the status of published data, " +
				"view the published data, purge the data, and delete the published table."));
	}

}
