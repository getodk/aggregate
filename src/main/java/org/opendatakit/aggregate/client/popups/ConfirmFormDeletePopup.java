package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecuteDeleteFormButton;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmFormDeletePopup extends PopupPanel {

  public ConfirmFormDeletePopup(String formId) {
    super(false);

    FlexTable layout = new FlexTable();
   
    HTML message = new HTML(
        "Delete all data and the form definition for <b>"
        + formId
        + "</b>?<br/>Do you wish to delete all uploaded data and the form definition for this form?");
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, new ExecuteDeleteFormButton(formId, this));
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }
}