package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.client.popups.NewTablesAdminPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class AddTablesAdmin extends AbstractButtonBase implements ClickHandler {

	private static final String TOOLTIP_TEXT = "Add administrative user";
	private static final String HELP_BALLOON_TXT = "Add an administrative user with their phone id.";

	public AddTablesAdmin() {
		super("<img src=\"images/green_right_arrow.png\" /> Add User", TOOLTIP_TEXT);
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	@Override
	public void onClick(ClickEvent event) {
		super.onClick(event);

		NewTablesAdminPopup popup = new NewTablesAdminPopup();
		popup.setPopupPositionAndShow(popup.getPositionCallBack());
	}
}