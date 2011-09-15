package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ConfirmSubmissionDeletePopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;

public final class DeleteSubmissionButton extends AggregateButton implements ClickHandler {

  private static final String BUTTON_TXT = "<img src=\"images/red_x.png\" />";
  private static final String TOOLTIP_TEXT = "Delete Submission";
  private static final String HELP_BALLOON_TXT = "Remove the submission from the database.";

  private final String submissionKeyAsString;

  public DeleteSubmissionButton(String submissionKeyAsString) {
    super(BUTTON_TXT, TOOLTIP_TEXT, HELP_BALLOON_TXT);
    this.submissionKeyAsString = submissionKeyAsString;
    addStyleDependentName("negative");
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    ConfirmSubmissionDeletePopup popup = new ConfirmSubmissionDeletePopup(submissionKeyAsString);
    popup.setPopupPositionAndShow(popup.getPositionCallBack());
  }

}
