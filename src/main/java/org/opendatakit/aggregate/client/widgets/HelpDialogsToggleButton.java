package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.HelpBalloon;

import com.google.gwt.user.client.ui.Image;

public class HelpDialogsToggleButton extends AbstractImageToggleButton {

	private static final String TOOLTIP_TEXT = "Help Balloons";

	private static final Image HELP_DIALOG_ICON = new Image("images/help_dialog.jpg");

	private static final String HELP_BALLOON_TXT = "This will display a more detailed help balloon when" +
			"you hover over an icon.";

	public HelpDialogsToggleButton() {
		super(HELP_DIALOG_ICON, TOOLTIP_TEXT);
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

}
