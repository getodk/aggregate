/*
 * Copyright (C) 2011 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.UrlHash;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.EnumListBox;
import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.TextBox;

public final class PublishPopup extends AbstractPopupBase {

	private static final String BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Publish";  
	private static final String TOOLTIP_TXT = "Publish the data";
	private static final String HELP_BALLOON_TXT = "This will publish the data to Google Fusion Tables "
		+ "or Google Spreadsheets.";

	private static final String ES_SERVICEOPTIONS_TOOLTIP = "Method data should be published";
	private static final String ES_SERVICEOPTIONS_BALLOON = 
		"Choose whether you would like only old data, only new data, or all data to be published.";
	private static final String ES_TYPE_TOOLTIP = "Type of External Service Connection";
	private static final String ES_TYPE_BALLOON = 
		"Select the application where you want your data to be published.";

	private final String formId;
	private final TextBox name;
	private final EnumListBox<ExternalServiceType> serviceType;
	private final EnumListBox<ExternalServicePublicationOption> esOptions;

	public PublishPopup(String formId) {
		super();

		this.formId = formId;
		AggregateButton deleteButton = new AggregateButton(BUTTON_TXT, TOOLTIP_TXT, HELP_BALLOON_TXT);
		deleteButton.addClickHandler(new CreateExernalServiceHandler());

		name = new TextBox();

		ExternalServiceType[] valuesToShow = { ExternalServiceType.GOOGLE_FUSIONTABLES, ExternalServiceType.GOOGLE_SPREADSHEET };
		serviceType = new EnumListBox<ExternalServiceType>(valuesToShow,
				ES_TYPE_TOOLTIP, ES_TYPE_BALLOON);
		serviceType.addChangeHandler(new ExternalServiceTypeChangeHandler());

		esOptions = new EnumListBox<ExternalServicePublicationOption>(ExternalServicePublicationOption.values(),
				ES_SERVICEOPTIONS_TOOLTIP, ES_SERVICEOPTIONS_BALLOON);

		updateUIOptions();

		FlexTable layout = new FlexTable();
		layout.setWidget(0, 0, new HTML("Form: " + formId + " "));
		layout.setWidget(0, 1, serviceType);
		layout.setWidget(0, 2, name);

		layout.setWidget(0, 3, esOptions);
		layout.setWidget(0, 4, deleteButton);
		layout.setWidget(0, 5, new ClosePopupButton(this));

		setWidget(layout);
	}

	public void updateUIOptions() {
		ExternalServiceType type = serviceType.getSelectedValue();

		if (type == null) {
			name.setEnabled(false);
			return;
		}

		switch (type) {
		case GOOGLE_SPREADSHEET:
			name.setText("");
			name.setEnabled(true);
			break;
		case GOOGLE_FUSIONTABLES:
		default: // unknown type
			name.setText("Spreadsheet Name");
			name.setEnabled(false);
			break;
		}
	}

	private class CreateExernalServiceHandler implements ClickHandler {

		@Override
		public void onClick(ClickEvent event) {

			ExternalServiceType type = serviceType.getSelectedValue();
			ExternalServicePublicationOption serviceOp = esOptions.getSelectedValue();

			switch (type) {
			case GOOGLE_SPREADSHEET:
				SecureGWT.getServicesAdminService().createGoogleSpreadsheet(formId, name.getText(),
						serviceOp, new OAuthCallback());
				break;
			case GOOGLE_FUSIONTABLES:
				SecureGWT.getServicesAdminService().createFusionTable(formId, serviceOp,
						new OAuthCallback());
				break;
			default: // unknown type
			break;
			}
			
			hide();
		}

	}

	private class OAuthCallback implements AsyncCallback<String> {

		public void onFailure(Throwable caught) {
			AggregateUI.getUI().reportError(caught);
		}

		public void onSuccess(String result) {
			SecureGWT.getServicesAdminService().generateOAuthUrl(result, new AsyncCallback<String>() {
				@Override
				public void onFailure(Throwable caught) {
					AggregateUI.getUI().reportError(caught);
				}

				@Override
				public void onSuccess(String result) {
					UrlHash.getHash().goTo(result);
				}
			});
		}
	}

	private class ExternalServiceTypeChangeHandler implements ChangeHandler {
		@Override
		public void onChange(ChangeEvent event) {
			updateUIOptions();
		}
	}

}
