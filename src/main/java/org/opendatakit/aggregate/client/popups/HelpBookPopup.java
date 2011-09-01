package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.constants.common.Tabs;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class HelpBookPopup extends PopupPanel {
	private VerticalPanel panel;
	private DecoratedTabPanel items;
	private SubmissionHelpTabUI submission;
	private ManagementHelpTabUI management;
	private AdminHelpTabUI siteadmin;

	public HelpBookPopup() {
		super(false);
		panel = new VerticalPanel();
		items = new DecoratedTabPanel();
		submission = new SubmissionHelpTabUI();
		management = new ManagementHelpTabUI();
		siteadmin = new AdminHelpTabUI();

		// populate the panel
		panel.add(new ClosePopupButton(this));
		panel.add(items);
		items.add(submission, Tabs.SUBMISSIONS.getTabLabel());
		if(AggregateUI.manageVisible) {
			items.add(management, Tabs.MANAGEMENT.getTabLabel());
		}
		if(AggregateUI.adminVisible) {
			items.add(siteadmin, Tabs.ADMIN.getTabLabel());
		}
		items.selectTab(0);

		ScrollPanel scroll = new ScrollPanel(panel);
		scroll.setPixelSize((Window.getClientWidth() / 2),(Window.getClientHeight() / 2));
		setWidget(scroll);
	}
}
