package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.ExecuteDeleteTablesAdmin;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ConfirmDeleteTablesAdmin extends PopupPanel {

  public ConfirmDeleteTablesAdmin(String aggregateUid) {
    super(false);
    setModal(true);
    FlexTable layout = new FlexTable();

    HTML message = new HTML("Delete the ODK Tables user?");
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, new ExecuteDeleteTablesAdmin(aggregateUid, this));
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }
}