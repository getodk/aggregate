package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.widgets.BasicButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class ConfirmDeleteTablesAdminPopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Delete User";
  private static final String TOOLTIP_TXT = "Remove this user";
  private static final String HELP_BALLOON_TXT = "Remove this administrative user from editing data.";

  private String aggregateUid;

  public ConfirmDeleteTablesAdminPopup(String aggregateUid) {
    super();
    this.aggregateUid = aggregateUid;

    BasicButton deleteButton = new BasicButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new ExecuteDelete());

    FlexTable layout = new FlexTable();

    HTML message = new HTML("Delete the ODK Tables user?");
    layout.setWidget(0, 0, message);
    layout.setWidget(0, 1, deleteButton);
    layout.setWidget(0, 2, new ClosePopupButton(this));

    setWidget(layout);
  }

  private class ExecuteDelete implements ClickHandler {

    @Override
    public void onClick(ClickEvent event) {
      // Set up the callback object.
      AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
        @Override
        public void onFailure(Throwable caught) {
          AggregateUI.getUI().reportError(caught);
        }

        @Override
        public void onSuccess(Boolean result) {
          AggregateUI.getUI().clearError();
          if (result) {
            Window.alert("Successfully deleted the user");
          } else {
            Window.alert("Error: unable to delete the user!");
          }
          AggregateUI.getUI().getTimer().refreshNow();
        }
      };
      // Make the call to the form service.
      SecureGWT.getOdkTablesAdminService().deleteAdmin(aggregateUid, callback);
      hide();
    }
  }
}