package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AdminTabUI;
import org.opendatakit.aggregate.client.AggregateTabBase;
import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.constants.common.Tabs;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;

public class EnableOdkTablesCheckbox extends AbstractCheckBoxBase implements ValueChangeHandler<Boolean> {

	private static final String TOOLTIP_TEXT = "Enable/Disable ODK Tables Sync Functionality";
	private static final String HELP_BALLOON_TXT = "Check this box if you want to manage ODK Tables.  " +
			"Otherwise leave unchecked.";

	public EnableOdkTablesCheckbox(Boolean enabled) {
		super("ODK Tables Syncronization Functionality", TOOLTIP_TEXT);
		setValue(enabled);
		boolean accessable = AggregateUI.getUI().getUserInfo()
		.getGrantedAuthorities().contains(GrantedAuthorityName.ROLE_SITE_ACCESS_ADMIN);
		setEnabled(accessable);
		helpBalloon = new HelpBalloon(this, HELP_BALLOON_TXT);
	}

	public void updateValue(Boolean value) {
		Boolean currentValue = getValue();
		if(currentValue != value) {
			setValue(value);
		}
	}

	@Override
	public void onValueChange(ValueChangeEvent<Boolean> event) {
		super.onValueChange(event);
		final Boolean enabled = event.getValue();
		SecureGWT.getPreferenceService().setOdkTablesEnabled(enabled, new AsyncCallback<Void> () {
			@Override
			public void onFailure(Throwable caught) {
				AggregateUI.getUI().reportError(caught);
				// restore old value
				setValue(Preferences.getOdkTablesEnabled());
			}

			@Override
			public void onSuccess(Void result) {
				AggregateUI.getUI().clearError();
				Preferences.updatePreferences();

				AggregateTabBase possibleAdminTab = AggregateUI.getUI().getTab(Tabs.ADMIN);

				if(possibleAdminTab instanceof AdminTabUI) {
					AdminTabUI adminTab = (AdminTabUI) possibleAdminTab;
					if(enabled) {
						adminTab.displayOdkTablesSubTab();
					} else {
						adminTab.hideOdkTablesSubTab();
					}
				} else {
					AggregateUI.getUI().reportError(new Throwable("ERROR: SOME HOW CAN'T FIND ADMIN TAB"));
				}

			}
		});
	}
}