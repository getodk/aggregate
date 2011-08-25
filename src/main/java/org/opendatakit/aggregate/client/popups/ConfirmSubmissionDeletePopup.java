package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecuteDeleteSubmissionButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmSubmissionDeletePopup extends PopupPanel {

  public ConfirmSubmissionDeletePopup(String submissionKeyAsString) {
    super(false);
    setModal(true);
    FlexTable layout = new FlexTable();
   
    HTML message = new HTML(
        "Are you sure you want to Delete the submission?");
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, new ExecuteDeleteSubmissionButton(submissionKeyAsString, this));
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }
}
