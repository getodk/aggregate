package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionService;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.constants.common.FormOrFilter;
import org.opendatakit.aggregate.constants.common.PageUpdates;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AggregateUI implements EntryPoint {
	private static final int REFRESH_INTERVAL = 5000; // ms

	// Main Navigation
	private static final String[] MAIN_MENU = {
		SubmissionTabUI.SUBMISSIONS, ManageTabUI.MANAGEMENT};
	private List<FilterGroup> view = new ArrayList<FilterGroup>();
	private FlexTable dataTable = new FlexTable(); //contains the data
	private FilterGroup def; //the default filter group
	private UrlHash hash;

	// layout
	private HorizontalPanel layoutPanel = new HorizontalPanel();
	private VerticalPanel helpPanel = new VerticalPanel();

	// navigation
	private DecoratedTabPanel mainNav = new DecoratedTabPanel();
	ManageTabUI manageNav;
	private SubmissionTabUI submissionNav;

	// Report tab
	FormServiceAsync formSvc;
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
	private RefreshTimer timer;
	private String lastFormUsed = "";

	public AggregateUI() {
		formSvc = GWT.create(FormService.class);
		listOfForms = new FlexTable();

		// Setup timer to refresh list automatically.
		setTimer(new RefreshTimer(this));
	}

	public void requestUpdatedSubmissionData(List<FilterGroup> groups) {

		// Initialize the service proxy.
		if (submissionSvc == null) {
			submissionSvc = GWT.create(SubmissionService.class);
		}

		// Set up the callback object.
		AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
			public void onFailure(Throwable caught) {
				// TODO: deal with error
			}

			public void onSuccess(SubmissionUISummary summary) {
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

	/*
	 * Creates a click handler for a main menu tab.
	 * Defaults to the first sub-menu tab.
	 * Does nothing if we're already on the tab clicked.
	 */
	private ClickHandler getMainMenuClickHandler(final String s) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (hash.get(UrlHash.MAIN_MENU).equals(s))
					return;
				hash.clear();
				hash.set(UrlHash.MAIN_MENU, s);
				TabPanel panel = null;
				if (s.equals(SubmissionTabUI.SUBMISSIONS))
					panel = submissionNav;
				else if (s.equals(ManageTabUI.MANAGEMENT))
					panel = manageNav;
				panel.selectTab(0);
				hash.put();
			}
		};
	}

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
				dataTable, def, this, allGroups);
		mainNav.add(submissionNav, "Submissions");
		mainNav.add(manageNav, "Management");
		mainNav.addStyleName("mainNav");

		// create help panel
		for (int i = 1; i < 5; i++) {
			helpPanel.add(new HTML("Help Content " + i));
		}
		helpPanel.setStyleName("help_panel");

		// add to layout
		layoutPanel.add(mainNav);
		layoutPanel.getElement().setId("layout_panel");
		FlowPanel helpContainer = new FlowPanel();
		helpContainer.add(helpPanel);
		helpContainer.getElement().setId("help_container");
		layoutPanel.add(helpContainer);

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
		RootPanel.get("dynamic_content").add(layoutPanel);

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
			formSvc = GWT.create(FormService.class);
		}

		// Set up the callback object.
		AsyncCallback<FormSummary []> callback = new AsyncCallback<FormSummary []>() {
			public void onFailure(Throwable caught) {
				// TODO: deal with error
			}

			public void onSuccess(FormSummary[] forms) {
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
					formsBox.addItem(form.getTitle());
					if (hash.get(UrlHash.FORM).equals(form.getTitle()))
						formsBox.setItemSelected(formsBox.getItemCount() - 1, true);
				}
			}
		} else if (formsBox.getItemCount() == 0) {
			formsBox.addItem("none");
		}
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
		filtersBox.clear();
		allGroups.clear();
		filtersBox.addItem("none");

		//if you are sick and tired of groups populating... uncomment this code to clean all of your groups
		//	  for(FilterGroup group : set.getGroups()) {
		//		  removeFilterGroup(group);
		//		  if(set.getGroups().size() == 0)
		//			  break;
		//	  }
		for(FilterGroup group : set.getGroups()) {
			filtersBox.addItem(group.getName());
			allGroups.add(group);
		}
		if(selected == -1)
			filtersBox.setSelectedIndex(0);
		else
			filtersBox.setSelectedIndex(selected);
	}

	private void removeFilterGroup(FilterGroup group) {
		if (filterSvc == null) {
			filterSvc = GWT.create(FilterService.class);
		}

		AsyncCallback<Boolean> callback = 
			new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(Boolean result) {
				// TODO Auto-generated method stub

			}
		};

		filterSvc.deleteFilterGroup(group, callback);
	}

	private void getFilterList(final String id) {
		// Initialize the service proxy.
		if (filterSvc == null) {
			filterSvc = GWT.create(FilterService.class);
		}

		// Set up the callback object.
		AsyncCallback<FilterSet> callback = 
			new AsyncCallback<FilterSet>() {
			public void onFailure(Throwable caught) {

			}

			@Override
			public void onSuccess(FilterSet result) {
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
			listOfForms.setWidget(i, 2, new Anchor(user.substring(user.indexOf(":") + 1, user.indexOf("@")), user));

			ListBox enabledDropDown = new ListBox();
			enabledDropDown.addItem("Disabled/Inactive");
			enabledDropDown.addItem("Disabled/Active");
			enabledDropDown.addItem("Enabled/Active");

			Button deleteButton = new Button();
			deleteButton.setHTML("<img src=\"images/red_x.png\" /> Delete");
			deleteButton.addStyleDependentName("negative");

			listOfForms.setWidget(i, 3, enabledDropDown);
			Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Publish");
			publishButton.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					final PopupPanel popup = new CreateNewExternalServicePopup(form.getId(), formSvc, manageNav);
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
			listOfForms.setWidget(i, 4, publishButton);

			Button exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");
			exportButton.addClickHandler(new ClickHandler () {
				@Override
				public void onClick(ClickEvent event) {
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
			listOfForms.setWidget(i, 5, exportButton);
			listOfForms.setWidget(i, 6, deleteButton);
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
