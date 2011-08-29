package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.client.popups.NewTablesAdminPopup;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class ExecuteNewTablesUser extends AbstractButtonBase implements ClickHandler {

	private static final String TOOLTIP_TEXT = "Create a new user";

	private static final String HELP_BALLOON_TXT = "Create a new administrative user to edit data.";

	private NewTablesAdminPopup popup;

	public ExecuteNewTablesUser(NewTablesAdminPopup popup) {
		super("<img src=\"images/green_right_arrow.png\" /> Create User", TOOLTIP_TEXT);
		this.popup = popup;
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	@Override
	public void onClick(ClickEvent event) {
		super.onClick(event);

		// OK -- we are to proceed.
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
		OdkTablesAdmin admin = new OdkTablesAdmin(popup.getName(), popup.getExternalUid());
		SecureGWT.getOdkTablesAdminService().addAdmin(admin, callback);
		popup.hide();
	} 
}
