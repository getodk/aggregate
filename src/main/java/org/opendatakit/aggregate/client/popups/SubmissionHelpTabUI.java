package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SubmissionHelpTabUI extends VerticalPanel {
	
	public SubmissionHelpTabUI() {
		add(new HTML("<b>" + SubTabs.FILTER.getTabLabel() + "<b>"));
		add(new HTML("This is the page where you can view and filter your data and " +
				"have options to Visualize, Export, or Publish your data. <br><br>"));
		add(new HTML("<b>" + SubTabs.EXPORT.getTabLabel() + "<b>"));
		add(new HTML("This is the page where you can view the status of exported data " +
				"and view the completed file."));	
	}
}
