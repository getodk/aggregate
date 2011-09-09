package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ConfirmSubmissionDeletePopup;
import org.opendatakit.aggregate.client.popups.HelpBalloon;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public class DeleteSubmissionButton extends AbstractButtonBase implements ClickHandler {

	private static final String TOOLTIP_TEXT = "Delete Submission";

	private static final String HELP_BALLOON_TXT = "Remove the submission from the database.";

	private String submissionKeyAsString;

	public DeleteSubmissionButton(String submissionKeyAsString) {
		super("<img src=\"images/red_x.png\" />", TOOLTIP_TEXT);
		this.submissionKeyAsString = submissionKeyAsString;
		addStyleDependentName("negative");
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	@Override
	public void onClick(ClickEvent event) {
		super.onClick(event);

		ConfirmSubmissionDeletePopup popup = new ConfirmSubmissionDeletePopup(submissionKeyAsString);
		popup.setPopupPositionAndShow(popup.getPositionCallBack());
	}

}
