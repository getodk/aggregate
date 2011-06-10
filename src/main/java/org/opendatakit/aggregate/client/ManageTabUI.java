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

package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.form.admin.FormAdminServiceAsync;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.services.admin.ExternServSummary;
import org.opendatakit.aggregate.client.services.admin.ServicesAdminServiceAsync;
import org.opendatakit.aggregate.constants.common.PageUpdates;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class ManageTabUI extends TabPanel {
	// Universal
	private UrlHash hash;
	private AggregateUI baseUI;
	ServicesAdminServiceAsync servicesAdminSvc;
	private FormServiceAsync formSvc;

	// Management Navigation
	private static final String FORMS = "forms";
	static final String PUBLISH = "publish";
	static final String PERMISSIONS = "permissions";
	private static final String UTILITIES = "utilities";
	private static final String[] MANAGEMENT_MENU = {FORMS, PUBLISH, PERMISSIONS, UTILITIES};
	static final String MANAGEMENT = "management";

	// Forms tab
	private FlexTable uploadTable = new FlexTable();
	private FlexTable listOfForms = new FlexTable();
	private static final String K_MAILTO = "mailto:";
	FormAdminServiceAsync formAdminSvc;

	// Publish tab
	private FlexTable publishTable;
	private ListBox formsBox = new ListBox();
	private List<FormSummary> allForms = new ArrayList<FormSummary>();
	private String lastFormUsed = "";
	private FlexTable navTable = new FlexTable();

	// Permissions tab
	private PermissionsSheet permissionsSheet;

	// Preferences tab
	private static final String GOOGLE_MAPS_API_KEY_LABEL = 
		"<h2>Google Maps API Key</h2> To obtain a key signup at <a href=\"http://code.google.com/apis/maps/signup.html\"> Google Maps </a>";
	private TextBox mapsApiKey = new TextBox();

	public ManageTabUI(AggregateUI baseUI) {
		super();
		SecureGWT sg = SecureGWT.get();
		formAdminSvc = sg.createFormAdminService();
		servicesAdminSvc = sg.createServicesAdminService();
		formSvc = sg.createFormService();
		this.hash = UrlHash.getHash();
		this.baseUI = baseUI;
		
		publishTable = new PublishSheet(this);
		permissionsSheet = new PermissionsSheet(this);
		publishTable.setWidget(0, 0, formsBox);

		this.add(setupFormManagementPanel(), "Forms");

		this.add(publishTable, "Publish");
		this.add(permissionsSheet, "Permissions");
		this.add(setupUtilitiesPanel(), "Utilities");
		this.add(setupPreferencesPanel(), "Preferences");

		int selected = 0;
		String subMenu = hash.get(UrlHash.SUB_MENU);
		for (int i = 0; i < MANAGEMENT_MENU.length; i++)
			if (subMenu.equals(MANAGEMENT_MENU[i]))
				selected = i;
		this.selectTab(selected);

		for (int i = 0; i < MANAGEMENT_MENU.length; i++)
			this.getTabBar().getTab(i)
			.addClickHandler(getSubMenuClickHandler(
					MANAGEMENT, MANAGEMENT_MENU[i]));
	}

	public VerticalPanel setupFormManagementPanel() {
		Button uploadFormButton = new Button("<img src=\"images/yellow_plus.png\" /> New Form");
		uploadFormButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				baseUI.clearError();
				hash.goTo("../ui/upload");
				baseUI.getTimer().restartTimer();
			}
		});
		uploadTable.setWidget(0, 0, uploadFormButton);
		Button uploadSubmissionsButton = new Button("<img src=\"images/blue_up_arrow.png\" /> Upload Data");
		uploadSubmissionsButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				baseUI.clearError();
				hash.goTo("../ui/submission");
				baseUI.getTimer().restartTimer();
			}
		});
		uploadTable.setWidget(0, 1, uploadSubmissionsButton);

		listOfForms.setText(0, 0, "Title");
		listOfForms.setText(0, 1, "Form Id");
		listOfForms.setText(0, 2, "User");
		listOfForms.setText(0, 3, "Downloadable");
		listOfForms.setText(0, 4, "Accept Submissions");
		listOfForms.setText(0, 5, "Publish");
		listOfForms.setText(0, 6, "Export");
		listOfForms.setText(0, 7, "Delete");
		listOfForms.getRowFormatter().addStyleName(0, "titleBar");
		listOfForms.addStyleName("dataTable");
		listOfForms.getElement().setId("form_management_table");

		VerticalPanel formManagementPanel = new VerticalPanel();
		formManagementPanel.add(uploadTable);
		formManagementPanel.add(listOfForms);
		return formManagementPanel;
	}

	/**
	 * Update the list of forms
	 * 
	 * @param formSummary
	 */
	private void updateFormTable(FormSummary [] forms) {
		for (int j = 0; j < forms.length; j++) {
			int i = j + 1;
			final FormSummary form = forms[j];
			listOfForms.setWidget(i, 0, new HTML(form.getViewableURL()));
			listOfForms.setWidget(i, 1, new HTML(form.getId()));
			String user = form.getCreatedUser();
			String displayName;
			if ( user.startsWith(K_MAILTO) ) {
				displayName =user.substring(K_MAILTO.length());
			} else if ( user.startsWith("uid:") ) {
				displayName = user.substring("uid:".length(),user.indexOf("|"));
			} else {
				displayName = user;
			}
			listOfForms.setText(i, 2, displayName);

			CheckBox downloadableCheckBox = new CheckBox();
			downloadableCheckBox.setValue(form.isDownloadable());
			downloadableCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					final String formId = form.getId();
					formAdminSvc.setFormDownloadable(formId, event.getValue(), new AsyncCallback<Boolean> () {
						@Override
						public void onFailure(Throwable caught) {
							baseUI.reportError(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							baseUI.clearError();
						}}
					);
				}
			});
			listOfForms.setWidget(i, 3, downloadableCheckBox);

			CheckBox acceptSubmissionCheckBox = new CheckBox();
			acceptSubmissionCheckBox.setValue(form.receiveSubmissions());
			acceptSubmissionCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
				@Override
				public void onValueChange(ValueChangeEvent<Boolean> event) {
					final String formId = form.getId();
					formAdminSvc.setFormAcceptSubmissions(formId, event.getValue(), new AsyncCallback<Boolean> () {
						@Override
						public void onFailure(Throwable caught) {
							baseUI.reportError(caught);
						}

						@Override
						public void onSuccess(Boolean result) {
							baseUI.clearError();
						}}
					);
				}
			});
			listOfForms.setWidget(i, 4, acceptSubmissionCheckBox);

			Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Publish");
			publishButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					baseUI.clearError();
					final PopupPanel popup = new CreateNewExternalServicePopup(form.getId(), servicesAdminSvc, hash, baseUI);
					popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							int left = ((Window.getClientWidth() - offsetWidth) / 2);
							int top = ((Window.getClientHeight() - offsetHeight) / 2);
							popup.setPopupPosition(left, top);
						}
					});
					baseUI.getTimer().restartTimer();
				}
			});
			listOfForms.setWidget(i, 5, publishButton);

			Button exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");
			exportButton.addClickHandler(new ClickHandler () {
				@Override
				public void onClick(ClickEvent event) {
					baseUI.clearError();
					final PopupPanel popup = new CreateNewExportPopup(form.getId(), formSvc, baseUI.getManageNav(), baseUI);
					popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							int left = ((Window.getClientWidth() - offsetWidth) / 2);
							int top = ((Window.getClientHeight() - offsetHeight) / 2);
							popup.setPopupPosition(left, top);
						}
					});
					baseUI.getTimer().restartTimer();
				}
			});
			listOfForms.setWidget(i, 6, exportButton);

			Button deleteButton = new Button();
			deleteButton.setHTML("<img src=\"images/red_x.png\" /> Delete");
			deleteButton.addStyleDependentName("negative");
			deleteButton.addClickHandler(new ClickHandler () {
				@Override
				public void onClick(ClickEvent event) {
					// TODO: display pop-up with text from b...
					final ConfirmFormDeletePopup popup = new ConfirmFormDeletePopup(form.getId());
					popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							int left = ((Window.getClientWidth() - offsetWidth) / 2);
							int top = ((Window.getClientHeight() - offsetHeight) / 2);
							popup.setPopupPosition(left, top);
						}
					});
					baseUI.getTimer().restartTimer();
				}
			});

			listOfForms.setWidget(i, 7, deleteButton);
			if (i % 2 == 0)
				listOfForms.getRowFormatter().addStyleName(i, "evenTableRow");
		}
	}

	public void setupPublishPanel() {
		if (hash.get(UrlHash.FORM) != null && !hash.get(UrlHash.FORM).equals("")) {
			if (servicesAdminSvc == null) {
				servicesAdminSvc = SecureGWT.get().createServicesAdminService();
			}

			AsyncCallback<ExternServSummary[] > callback = new AsyncCallback<ExternServSummary []>() {
				@Override
				public void onFailure(Throwable caught) {
					baseUI.reportError(caught);
				}

				@Override
				public void onSuccess(ExternServSummary[] result) {
					((PublishSheet) publishTable).updatePublishPanel(lastFormUsed, result);
				}
			};

			servicesAdminSvc.getExternalServices(lastFormUsed, callback);
		}
	}

	public VerticalPanel  setupPreferencesPanel() {
		HTML labelMapsKey = new HTML(GOOGLE_MAPS_API_KEY_LABEL); 
		String key = Preferences.getGoogleMapsApiKey();
		mapsApiKey.setText(key);

		Button updateMapsApiKeyButton = new Button("Update");
		updateMapsApiKeyButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				baseUI.clearError();
				Preferences.setGoogleMapsApiKey(mapsApiKey.getText()); 
				baseUI.getTimer().restartTimer();
			}

		});

		VerticalPanel  preferencesPanel = new VerticalPanel();
		preferencesPanel.add(labelMapsKey);
		preferencesPanel.add(mapsApiKey);
		preferencesPanel.add(updateMapsApiKeyButton);
		return preferencesPanel;
	}

	public void updatePreferencesPanel() {
		mapsApiKey.setText(Preferences.getGoogleMapsApiKey());
	}

	public HTML setupUtilitiesPanel() {
		return new HTML("Content Forthcoming");
	}

	ClickHandler getSubMenuClickHandler(
			final String menu, final String subMenu) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				baseUI.clearError();
				baseUI.getTimer().restartTimer();
				baseUI.update(SubTabs.FORM, PageUpdates.FORMTABLE);
				hash.clear();
				hash.set(UrlHash.MAIN_MENU, menu);
				hash.set(UrlHash.SUB_MENU, subMenu);
				hash.put();
				baseUI.getTimer().restartTimer();
			}
		};
	}

	public void setSubSelection(String subMenu, String subSubMenu) {
		hash.clear();
		hash.set(UrlHash.MAIN_MENU, MANAGEMENT);
		hash.set(UrlHash.SUB_MENU, subMenu);
		hash.set(UrlHash.FORM, subSubMenu);
		hash.put();
	}

	void getFormList(final PageUpdates update) {
		// Initialize the service proxy.
		if (formSvc == null) {
			formSvc = SecureGWT.get().createFormService();
		}

		// Set up the callback object.
		AsyncCallback<FormSummary []> callback = new AsyncCallback<FormSummary []>() {
			public void onFailure(Throwable caught) {
				baseUI.reportError(caught);
			}

			public void onSuccess(FormSummary[] forms) {
				baseUI.clearError();
				if(update.equals(PageUpdates.FORMDROPDOWN))
					fillFormDropDown(forms);
				else if(update.equals(PageUpdates.FORMTABLE))
					updateFormTable(forms);
				else if(update.equals(PageUpdates.ALL)) {
					fillFormDropDown(forms);
					updateFormTable(forms);
				}
			}
		};

		// Make the call to the form service.
		formSvc.getForms(callback);
	}

	private class ConfirmFormDeletePopup  extends PopupPanel implements ClickHandler {
		final String formId;

		ConfirmFormDeletePopup(String formId) {
			super(false);
			this.formId = formId;
			FlexTable layout = new FlexTable();

			layout.setWidget(0, 0, new HTML("Delete all data and the form definition for <b>" + formId + 
			"</b>?<br/>Do you wish to delete all uploaded data and the form definition for this form?"));

			Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Delete Data and Form");
			publishButton.addClickHandler(this);
			layout.setWidget(0, 1, publishButton);

			Button closeButton = new Button("<img src=\"images/red_x.png\" />");
			closeButton.addStyleDependentName("close");
			closeButton.addStyleDependentName("negative");
			closeButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					hide();
					baseUI.getTimer().restartTimer();
				}
			});
			layout.setWidget(0, 2, closeButton);

			setWidget(layout);
		}

		@Override
		public void onClick(ClickEvent event) {
			// OK -- we are to proceed.
			// Set up the callback object.
			AsyncCallback<Boolean> callback = new AsyncCallback<Boolean>() {
				@Override
				public void onFailure(Throwable caught) {
					baseUI.reportError(caught);
				}

				@Override
				public void onSuccess(Boolean result) {
					baseUI.clearError();
					Window.alert("Successfully scheduled this form's deletion.\n" +
							"It may take several minutes to delete all the " +
							"data submissions\nfor this form -- after which the " +
					"form definition itself will be deleted.");
					baseUI.update(SubTabs.FILTER, PageUpdates.NEWFORM);
				}
			};
			// Make the call to the form service.
			formAdminSvc.deleteForm(formId, callback);
			hide();
			baseUI.getTimer().restartTimer();
		}
	}

	private void fillFormDropDown(final FormSummary [] forms) {
		Set<String> existingForms = new HashSet<String>();
		for (int i = 0; i < formsBox.getItemCount(); i++) {
			existingForms.add(formsBox.getItemText(i));
		}
		if(forms.length > 0) {
			for (int i = 0; i < forms.length; i++) {
				FormSummary form = forms[i];
				if (!existingForms.contains(form.getTitle())) {
					allForms.add(form);
					formsBox.addItem(form.getTitle());
					if (hash.get(UrlHash.FORM).equals(form.getTitle())) {
						formsBox.setItemSelected(formsBox.getItemCount() - 1, true);
					}
				}
			}
		} else if (formsBox.getItemCount() == 0) {
			formsBox.addItem("none");
		}
		int formIdx = formsBox.getSelectedIndex();
		if ( formIdx == -1 ) {
			setTitleString("");
		} else {
			setTitleString(formsBox.getItemText(formIdx));
		}
		formsBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				for (FormSummary form : forms) {
					if (form.getTitle().compareTo(formsBox.getValue(formsBox.getSelectedIndex())) == 0) {
						lastFormUsed = form.getId();
						break;
					}
				}
				setupPublishPanel();
				setTitleString(formsBox.getValue(formsBox.getSelectedIndex()));
			}
		});

		//Have it begin on filter list as well
		String formId = "";
		for (FormSummary form : forms) {
			if (form.getTitle().compareTo(formsBox.getValue(formsBox.getSelectedIndex())) == 0) {
				lastFormUsed = form.getId();
				break;
			}
		}
		setupPublishPanel();
		setTitleString(formsBox.getValue(formsBox.getSelectedIndex()));
	}

	public void setTitleString(String title) {
		publishTable.setHTML(0, 1, "<h1 id=\"form_name\">" + title + "</h1>");
	}
	
	public AggregateUI getBase() {
		return baseUI;
	}
}