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
	private static final String ES_SERVICEOPTIONS_BALLOON = "Choose whether you would like only old data, only new data, or all data to be published.";
	private static final String ES_TYPE_TOOLTIP = "Type of External Service Connection";
	private static final String ES_TYPE_BALLOON = "Select the application where you want your data to be published.";

	// this is the main flex table for the popup
	private final FlexTable layout;
	// this is the header
	private final FlexTable topBar;
	// to hold the options
	private final FlexTable optionsBar;
	// to hold the google spreadsheet only options
	private final FlexTable gsBar;
	private final String formId;
	private final TextBox name;
	// this is the default text in the blankNameText when it is not form id
	private final String blankNameText;
	// this will hold the last entered tablename
	private String enteredName;
	// saves whether or not anything has been input by the user in the name box
	private boolean didInput;
	
	private final EnumListBox<ExternalServiceType> serviceType;
	private final EnumListBox<ExternalServicePublicationOption> esOptions;

	public PublishPopup(String formId) {
		super();

		this.formId = formId;
		AggregateButton deleteButton = new AggregateButton(BUTTON_TXT,
				TOOLTIP_TXT, HELP_BALLOON_TXT);
		deleteButton.addClickHandler(new CreateExernalServiceHandler());

		name = new TextBox();
		enteredName = formId;
		blankNameText = "";
		name.setText(blankNameText);
		didInput = false;

		ExternalServiceType[] valuesToShow = {
				ExternalServiceType.GOOGLE_FUSIONTABLES,
				ExternalServiceType.GOOGLE_SPREADSHEET };
				// ExternalServiceType.OHMAGE_JSON_SERVER };
		serviceType = new EnumListBox<ExternalServiceType>(valuesToShow,
				ES_TYPE_TOOLTIP, ES_TYPE_BALLOON);
		serviceType.addChangeHandler(new ExternalServiceTypeChangeHandler());

		esOptions = new EnumListBox<ExternalServicePublicationOption>(
				ExternalServicePublicationOption.values(),
				ES_SERVICEOPTIONS_TOOLTIP, ES_SERVICEOPTIONS_BALLOON);

		// Set up the tables in the popup
		layout = new FlexTable();	
		
		topBar = new FlexTable();
		topBar.addStyleName("stretch_header");
		topBar.setWidget(0, 0, new HTML("<h2>Form: </h2>"));
		topBar.setWidget(0, 1, new HTML(formId));
		topBar.setWidget(0, 2, new HTML("<h2>Publish to: </h2>"));
		topBar.setWidget(0, 3, serviceType);
		topBar.setWidget(0, 4, deleteButton);
		topBar.setWidget(0, 5, new ClosePopupButton(this));
		
		optionsBar = new FlexTable();
		optionsBar.addStyleName("flexTableBorderTopStretchWidth");
		optionsBar.setWidget(1, 0, new HTML("<h3>Data to Publish:</h3>"));
		optionsBar.setWidget(1, 1, esOptions);
		
		// this is only for google spreadsheets
		gsBar = new FlexTable();
		gsBar.addStyleName("stretch_header");
		gsBar.setWidget(1, 0, new HTML("<h3>Workbook Name (Spreadsheet Only):</h3>"));
		// make the name textbox an appropriate size
		name.setVisibleLength(35);
		gsBar.setWidget(1, 1, name);
		
		optionsBar.setWidget(2, 0, gsBar);
		optionsBar.getFlexCellFormatter().setColSpan(2, 0, 2);
		
		layout.setWidget(0, 0, topBar);
		layout.setWidget(1, 0, optionsBar);
		// set the options to fill the table as well
		layout.getFlexCellFormatter().setColSpan(1, 0, 6);
		setWidget(layout);
		
		updateUIOptions();
		
	}

	public void updateUIOptions() {
		ExternalServiceType type = serviceType.getSelectedValue();
		
		if (type == null) {
			name.setEnabled(false);
			name.setReadOnly(true);
			return;
		}

		// This checks to see if the input has been changed while on spreadsheet,
		// and if it has then it saves the information so you can switch back
		// easily without losing what was entered.
		if (!name.getText().equals(formId) && !name.getText().equals(blankNameText)) {
			didInput = true;
			enteredName = name.getText();
		}

		switch (type) {
		case GOOGLE_SPREADSHEET:
			// this complicated looking thing just sets the previously entered table name
			// if it's already been set.
			name.setText( (didInput) ? enteredName : formId );
			name.setEnabled(true);
			name.setReadOnly(false);
			optionsBar.getRowFormatter().setStyleName(2, "enabledTableRow");
			break;
		case OHMAGE_JSON_SERVER:
			name.setText("http://localhost");
			name.setEnabled(true);
			name.setReadOnly(false);
			optionsBar.getRowFormatter().setStyleName(2, "enabledTableRow");
			break;
		case GOOGLE_FUSIONTABLES:
			name.setText("");
			name.setEnabled(false);
			name.setReadOnly(true);
			optionsBar.getRowFormatter().setStyleName(2, "disabledTableRow");
			break;
		default: // unknown type
			name.setText("Spreadsheet Name");
			name.setEnabled(false);
			name.setReadOnly(true);
			optionsBar.getRowFormatter().setStyleName(2, "disabledTableRow");			
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
			case OHMAGE_JSON_SERVER:
				SecureGWT.getServicesAdminService().createOhmageJsonServer(formId, name.getText(), serviceOp, new AsyncCallback<String>() {

					@Override
					public void onFailure(Throwable caught) {
						// no-op
					}

					@Override
					public void onSuccess(String result) {
						// no-op
					}
				});
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
			SecureGWT.getServicesAdminService().generateOAuthUrl(result,
					new AsyncCallback<String>() {
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
