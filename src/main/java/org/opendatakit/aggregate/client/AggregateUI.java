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

import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.form.admin.FormAdminServiceAsync;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.services.admin.ServicesAdminServiceAsync;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.FormOrFilter;
import org.opendatakit.aggregate.constants.common.PageUpdates;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.client.security.SecurityServiceAsync;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.InvocationException;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AggregateUI implements EntryPoint {
	private static final int REFRESH_INTERVAL = 5000; // ms

	private static final String LOGOUT = "logout";
	// Main Navigation
	private static final String[] MAIN_MENU = {
		SubmissionTabUI.SUBMISSIONS, ManageTabUI.MANAGEMENT, LOGOUT};
	private List<FilterGroup> view = new ArrayList<FilterGroup>();
	private FlexTable dataTable = new FlexTable(); //contains the data
	private FilterGroup def; //the default filter group
	private UrlHash hash;

	// layout
	private VerticalPanel wrappingLayoutPanel = new VerticalPanel();
	private Label errorMsgLabel = new Label(); 
	// layout
	private HorizontalPanel layoutPanel = new HorizontalPanel();
	private VerticalPanel helpPanel = new VerticalPanel();

	// navigation
	private DecoratedTabPanel mainNav = new DecoratedTabPanel();
	ManageTabUI manageNav;
	private SubmissionTabUI submissionNav;

	// Top tab
	SecurityServiceAsync identitySvc;
	// Report tab
	FormServiceAsync formSvc;
	FormAdminServiceAsync formAdminSvc;
	ServicesAdminServiceAsync servicesAdminSvc;
	SubmissionServiceAsync submissionSvc;
	private FilterServiceAsync filterSvc;

	// Visualization
	private List<Column> headers;
	private List<SubmissionUI> submissions;
	public List<Column> getHeaders() { return headers; }
	public List<SubmissionUI> getSubmissions() { return submissions; }

	private FlexTable listOfForms;
	private ListBox formsBox = new ListBox();
	private ListBox filtersBox = new ListBox();
	private List<FilterGroup> allGroups = new ArrayList<FilterGroup>();
	private List<FormSummary> allForms = new ArrayList<FormSummary>();
	private RefreshTimer timer;
	private String lastFormUsed = "";

	public AggregateUI() {
		SecureGWT sg = SecureGWT.get();
		identitySvc = sg.createSecurityService();
		formSvc = sg.createFormService();
		formAdminSvc = sg.createFormAdminService();
		servicesAdminSvc = sg.createServicesAdminService();
		listOfForms = new FlexTable();
		
		Preferences.updatePreferences();
		
		// Setup timer to refresh list automatically.
		setTimer(new RefreshTimer(this));
	}

	public void reportError(Throwable t) {
		if ( t instanceof 
				org.opendatakit.common.persistence.client.exception.DatastoreFailureException ) {
			errorMsgLabel.setText("Error: " + t.getMessage());
			errorMsgLabel.setVisible(true);
		} else if ( t instanceof
				org.opendatakit.common.security.client.exception.AccessDeniedException ) {
			errorMsgLabel.setText("You do not have permission for this action.\nError: " + t.getMessage());
			errorMsgLabel.setVisible(true);
		} else if ( t instanceof InvocationException ) {
			redirect( GWT.getHostPageBaseURL() + UIConsts.HOST_PAGE_BASE_ADDR);
		} else {
			errorMsgLabel.setText("Error: " + t.getMessage());
			errorMsgLabel.setVisible(true);
		}
	}
	
	public void clearError() {
		errorMsgLabel.setVisible(false);
		errorMsgLabel.setText("");
	}
	
	public void requestUpdatedSubmissionData(List<FilterGroup> groups) {

		// Initialize the service proxy.
		if (submissionSvc == null) {
			submissionSvc = SecureGWT.get().createSubmissionService();
		}

		// Set up the callback object.
		AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
			public void onFailure(Throwable caught) {
				reportError(caught);
			}

			public void onSuccess(SubmissionUISummary summary) {
				clearError();
				updateDataTable(summary);
			}
		};


		for(FilterGroup group : groups) {
			boolean allEmpty = true;
			if(group.getFilters().size() != 0) {
				// Make the call to the form service.
				allEmpty = false;
				submissionSvc.getSubmissions(group, callback);
			}
			if(allEmpty) {
				submissionSvc.getSubmissions(def, callback);
			}
		}

	}

	public void updateDataTable(SubmissionUISummary summary) {
		// for viz
		headers = summary.getHeaders();
		submissions = summary.getSubmissions();

		int headerIndex = 0;
		dataTable.removeAllRows();
		dataTable.getRowFormatter().addStyleName(0, "titleBar");
		dataTable.addStyleName("dataTable");
		for(Column column : summary.getHeaders()) {
			dataTable.setText(0, headerIndex++, column.getDisplayHeader());
		}

		int i = 1;
		for(SubmissionUI row : summary.getSubmissions()) {
			int j = 0;
			for(String values : row.getValues()) {
				dataTable.setText(i, j++, values);
			}
			if (i % 2 == 0) {
				dataTable.getRowFormatter().setStyleName(i, "evenTableRow");
			}
			i++;
		}
	}


	native void redirect(String url)
	/*-{
	        $wnd.location.replace(url);

	}-*/;

	/*
	 * Creates a click handler for a main menu tab.
	 * Defaults to the first sub-menu tab.
	 * Does nothing if we're already on the tab clicked.
	 */
	private ClickHandler getMainMenuClickHandler(final String s) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				clearError();
				if (hash.get(UrlHash.MAIN_MENU).equals(s))
					return;
				hash.clear();
				hash.set(UrlHash.MAIN_MENU, s);
				TabPanel panel = null;
				if (s.equals(SubmissionTabUI.SUBMISSIONS))
					panel = submissionNav;
				else if (s.equals(ManageTabUI.MANAGEMENT))
					panel = manageNav;
				else if (s.equals(LOGOUT)) {
					redirect( GWT.getHostPageBaseURL() + "/j_spring_security_logout");
					return;
				}
				panel.selectTab(0);
				hash.put();
			}
		};
	}

	@Override
	public void onModuleLoad() {
		// Get url hash.
		hash = UrlHash.getHash();
		hash.get();

		def = new FilterGroup(
				"Default", "", new ArrayList<Filter>());
		view.add(def);

		// Create sub menu navigation
		getTimer().restartTimer(this);
		update(FormOrFilter.FORM, PageUpdates.ALL);
		manageNav = new ManageTabUI(listOfForms, this);
		submissionNav = new SubmissionTabUI(view, formsBox, filtersBox, 
				dataTable, def, this, allGroups, allForms);
		mainNav.add(submissionNav, "Submissions");
		mainNav.add(manageNav, "Management");
		mainNav.add(new HTML("<div>Selecting tab should log out</div>"),
					"<a href=\"j_spring_security_logout\">Log Out</a>", true);
		mainNav.addStyleName("mainNav");

		// create help panel
		for (int i = 1; i < 5; i++) {
			helpPanel.add(new HTML("Help Content " + i));
		}
		helpPanel.setStyleName("help_panel");

		// add the error message info...
		errorMsgLabel.setStyleName("error_message");
		errorMsgLabel.setVisible(false);
		wrappingLayoutPanel.add(errorMsgLabel);
		wrappingLayoutPanel.add(layoutPanel);
		// add to layout
		layoutPanel.add(mainNav);
		layoutPanel.getElement().setId("layout_panel");
		FlowPanel helpContainer = new FlowPanel();
		helpContainer.add(helpPanel);
		helpContainer.getElement().setId("help_container");
		//layoutPanel.add(helpContainer);

		// Select the correct menu item based on url hash.
		int selected = 0;
		String mainMenu = hash.get(UrlHash.MAIN_MENU);
		for (int i = 0; i < MAIN_MENU.length; i++)
			if (mainMenu.equals(MAIN_MENU[i]))
				selected = i;
		mainNav.selectTab(selected);

		// Add click handlers for each menu item
		for (int i = 0; i < MAIN_MENU.length; i++)
			mainNav.getTabBar().getTab(i).addClickHandler(getMainMenuClickHandler(MAIN_MENU[i]));

		RootPanel.get("dynamic_content").add(new HTML("<img src=\"images/odk_color.png\" id=\"odk_aggregate_logo\" />"));
		RootPanel.get("dynamic_content").add(wrappingLayoutPanel);
		contentLoaded();
	}

	// Let's JavaScript know that the GWT content has been loaded
	// Currently calls into javascript/resize.js, if we add more JavaScript
	// then that should be changed.
	private native void contentLoaded() /*-{
  	$wnd.gwtContentLoaded();
  }-*/;

	void getFormList(final PageUpdates update) {
		// Initialize the service proxy.
		if (formSvc == null) {
			formSvc = SecureGWT.get().createFormService();
		}

		// Set up the callback object.
		AsyncCallback<FormSummary []> callback = new AsyncCallback<FormSummary []>() {
			public void onFailure(Throwable caught) {
				reportError(caught);
			}

			public void onSuccess(FormSummary[] forms) {
				clearError();
				if(update.equals(PageUpdates.FORMDROPDOWN))
					fillFormDropDown(forms);
				else if(update.equals(PageUpdates.FORMTABLE))
					updateFormTable(forms);
				else if(update.equals(PageUpdates.SUBMISSIONDATA) && forms.length > 0)
					requestUpdatedSubmissionData(view);
				else if(update.equals(PageUpdates.ALL)) {
					fillFormDropDown(forms);
					updateFormTable(forms);
					if(forms.length > 0)
						requestUpdatedSubmissionData(view);
				}
			}
		};

		// Make the call to the form service.
		formSvc.getForms(callback);
	}

	private void fillFormDropDown(FormSummary [] forms) {
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
		submissionNav.setTitleString(formsBox.getItemText(formsBox.getSelectedIndex()));
		formsBox.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent event) {
				lastFormUsed = formsBox.getValue(formsBox.getSelectedIndex());
				update(FormOrFilter.FILTER, PageUpdates.ALL);
			}

		});
		
		//Have it begin on filter list as well
		String formId = "";
		for (FormSummary form : forms) {
			if (form.getTitle().compareTo(formsBox.getValue(formsBox.getSelectedIndex())) == 0) {
				formId = form.getId();
				break;
			}
		}
		def.setFormId(formId);
		lastFormUsed = formId;
		update(FormOrFilter.FILTER, PageUpdates.ALL);
	}

	private void fillFilterDropDown(FilterSet set) {
		int selected = filtersBox.getSelectedIndex();
		allGroups.clear();
		if(filtersBox.getItemCount() == 0)
			filtersBox.addItem("none");

		//if you are sick and tired of groups populating... uncomment this code to clean all of your groups
		//	  for(FilterGroup group : set.getGroups()) {
		//		  removeFilterGroup(group);
		//		  if(set.getGroups().size() == 0)
		//			  break;
		//	  }
		for(FilterGroup group : set.getGroups()) {
			int i = 0;
			for(i = 0; i < filtersBox.getItemCount(); i++) {
				if(group.getName().compareTo(filtersBox.getItemText(i)) == 0) {
					allGroups.add(group);
					break;
				}
			}
			if(i == filtersBox.getItemCount()) {
				filtersBox.addItem(group.getName());
				allGroups.add(group);
			}
		}
		if(selected == -1)
			filtersBox.setSelectedIndex(0);
		else
			filtersBox.setSelectedIndex(selected);
	}

	private void removeFilterGroup(FilterGroup group) {
		if (filterSvc == null) {
			filterSvc = SecureGWT.get().createFilterService();
		}

		AsyncCallback<Boolean> callback = 
			new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				reportError(caught);
			}

			@Override
			public void onSuccess(Boolean result) {
				clearError();
			}
		};

		filterSvc.deleteFilterGroup(group, callback);
	}

	private void getFilterList(final String id) {
		// Initialize the service proxy.
		if (filterSvc == null) {
			filterSvc = SecureGWT.get().createFilterService();
		}

		// Set up the callback object.
		AsyncCallback<FilterSet> callback = 
			new AsyncCallback<FilterSet>() {
			public void onFailure(Throwable caught) {
				reportError(caught);
			}

			@Override
			public void onSuccess(FilterSet result) {
				clearError();
				fillFilterDropDown(result);
			}
		};

		// Make the call to the form service.
		filterSvc.getFilterSet(id, callback);
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
			listOfForms.setWidget(i, 0, new Anchor(form.getTitle()));
			listOfForms.setWidget(i, 1, new HTML(form.getId()));
			String user = form.getCreatedUser();
			listOfForms.setWidget(i, 2, new Anchor(user, user));

			CheckBox downloadableCheckBox = new CheckBox();
			downloadableCheckBox.setValue(form.isDownloadable());
			downloadableCheckBox.addValueChangeHandler(new ValueChangeHandler<Boolean>(){
            @Override
            public void onValueChange(ValueChangeEvent<Boolean> event) {
              final String formId = form.getId();
              formAdminSvc.setFormDownloadable(formId, event.getValue(), new AsyncCallback<Boolean> () {
                @Override
                public void onFailure(Throwable caught) {
    				reportError(caught);
                }

                @Override
                public void onSuccess(Boolean result) {
                	clearError();
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
            	   reportError(caught);
               }

               @Override
               public void onSuccess(Boolean result) {
            	   clearError();
               }}
             );
           }
        });
         listOfForms.setWidget(i, 4, acceptSubmissionCheckBox);
			
			Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Publish");
			publishButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					clearError();
					final PopupPanel popup = new CreateNewExternalServicePopup(form.getId(), servicesAdminSvc, manageNav);
					popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							int left = ((Window.getClientWidth() - offsetWidth) / 2);
							int top = ((Window.getClientHeight() - offsetHeight) / 2);
							popup.setPopupPosition(left, top);
						}
					});
				}
			});
			listOfForms.setWidget(i, 5, publishButton);

			Button exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");
			exportButton.addClickHandler(new ClickHandler () {
				@Override
				public void onClick(ClickEvent event) {
					clearError();
					final PopupPanel popup = new CreateNewExportPopup(form.getId(), formSvc, manageNav);
					popup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
						@Override
						public void setPosition(int offsetWidth, int offsetHeight) {
							int left = ((Window.getClientWidth() - offsetWidth) / 2);
							int top = ((Window.getClientHeight() - offsetHeight) / 2);
							popup.setPopupPosition(left, top);
						}
					});
				}
			});
			listOfForms.setWidget(i, 6, exportButton);

         Button deleteButton = new Button();
         deleteButton.setHTML("<img src=\"images/red_x.png\" /> Delete");
         deleteButton.addStyleDependentName("negative");

			listOfForms.setWidget(i, 7, deleteButton);
			if (i % 2 == 0)
				listOfForms.getRowFormatter().addStyleName(i, "evenTableRow");
		}
	}

	public void update(FormOrFilter ff, PageUpdates update) {
		if(ff.equals(FormOrFilter.FORM))
			getFormList(update);
		else if (ff.equals(FormOrFilter.FILTER))
			getFilterList(lastFormUsed);
		else if (ff.equals(FormOrFilter.BOTH)) { 
			getFormList(update);
			getFilterList(lastFormUsed);
		}
	}

	public void setTimer(RefreshTimer timer) {
		this.timer = timer;
	}
	public RefreshTimer getTimer() {
		return timer;
	}

}
