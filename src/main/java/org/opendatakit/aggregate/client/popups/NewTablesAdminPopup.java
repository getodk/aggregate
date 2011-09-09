package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.widgets.BasicButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

public class NewTablesAdminPopup extends AbstractPopupBase {

  private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Create User";
  private static final String TOOLTIP_TXT = "Create a new user";
  private static final String HELP_BALLOON_TXT = "Create a new administrative user to edit data.";

  
  private TextBox name;
  private TextBox externalUid;

  public NewTablesAdminPopup() {
    super();

    name = new TextBox();
    externalUid = new TextBox();

    BasicButton deleteButton = new BasicButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
    deleteButton.addClickHandler(new CreateUser());
    
    FlexTable layout = new FlexTable();
    layout.setWidget(0, 0, new ClosePopupButton(this));
    layout.setWidget(0, 1, new HTML("Create a New User"));
    layout.setWidget(1, 0, new HTML("Name:"));
    layout.setWidget(1, 1, name);
    layout.setWidget(2, 0, new HTML("User ID:"));
    layout.setWidget(2, 1, externalUid);
    layout.setWidget(3, 1, deleteButton);

    setWidget(layout);
  }

  private class CreateUser implements ClickHandler {
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
            Window.alert("Successfully added the user");
          } else {
            Window.alert("Error: unable to add the user!");
          }
          AggregateUI.getUI().getTimer().refreshNow();
        }
      };

      // Make the call to the odk tables user admin service.
      OdkTablesAdmin admin = new OdkTablesAdmin(name.getValue(), externalUid.getValue());
      SecureGWT.getOdkTablesAdminService().addAdmin(admin, callback);
      hide();
    }
  }

}