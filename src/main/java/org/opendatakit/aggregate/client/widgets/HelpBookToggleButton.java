package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.client.popups.HelpBookPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.ui.Image;

public class HelpBookToggleButton extends AbstractImageToggleButton {

	private static final String TOOLTIP_TEXT = "Show Detailed Help";

	private static final Image HELP_BOOK_ICON = new Image("images/help_book_icon.png");

	private static final String HELP_BALLOON_TXT = "This will display a more detailed help popup.";

	public HelpBookToggleButton() {
		super(HELP_BOOK_ICON, TOOLTIP_TEXT);
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	@Override
	public void onClick(ClickEvent event) {
		super.onClick(event);

		final HelpBookPopup helpPopup = new HelpBookPopup();
		helpPopup.show();
	}
}
