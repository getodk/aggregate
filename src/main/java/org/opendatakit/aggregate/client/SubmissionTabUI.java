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

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.CreateNewFilterPopup;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.form.ExportSummary;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;
import org.opendatakit.aggregate.client.visualization.CreateNewVisualizationPopup;
import org.opendatakit.aggregate.constants.common.SubTabs;
import org.opendatakit.aggregate.constants.common.PageUpdates;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SubmissionTabUI extends TabPanel {
	//Universal
	private UrlHash hash;
	FormServiceAsync formSvc;

	// Submission Navigation
	private static final String FILTER = "filter";
	private static final String EXPORT = "export";
	private static final String[] SUBMISSION_MENU = {FILTER, EXPORT};
	static final String SUBMISSIONS = "submissions";

	//Filter tab
	private List<FilterGroup> view = new ArrayList<FilterGroup>();
	private ListBox formsBox = new ListBox();
	private ListBox filtersBox = new ListBox();
	private FlexTable navTable;
	private FlexTable dataTable = new FlexTable(); //contains the data
	private FilterGroup def; //the default filter group
	private AggregateUI baseUI;
	private FilterServiceAsync filterSvc;
	private List<FilterGroup> allGroups = new ArrayList<FilterGroup>();
	private List<FormSummary> allForms = new ArrayList<FormSummary>();
	private TreeItem title;
	private FilterGroup currentGroup;
	private String lastFormUsed = "";
	SubmissionServiceAsync submissionSvc;
	private List<SubmissionUI> submissions;
	public List<SubmissionUI> getSubmissions() { return submissions; }
	private List<Column> headers;
	public List<Column> getHeaders() { return headers; }

	//Export tab
	private ExportSheet exportTable = new ExportSheet();

	public SubmissionTabUI(AggregateUI baseUI) {
		super();
		SecureGWT sg = SecureGWT.get();
		formSvc = sg.createFormService();
		this.hash = UrlHash.getHash();
		this.baseUI = baseUI;
		setupExportPanel();
		this.add(setupSubmissionsPanel(), "Filter");
		this.add(exportTable, "Export");
		this.getElement().setId("second_level_menu");

		def = new FilterGroup("Default", "", new ArrayList<Filter>());
		view.add(def);

		int selected = 0;
		String subMenu = hash.get(UrlHash.SUB_MENU);
		for (int i = 0; i < SUBMISSION_MENU.length; i++)
			if (subMenu.equals(SUBMISSION_MENU[i]))
				selected = i;
		this.selectTab(selected);

		for (int i = 0; i < SUBMISSION_MENU.length; i++)
			this.getTabBar().getTab(i)
			.addClickHandler(getSubMenuClickHandler(
					SUBMISSIONS, SUBMISSION_MENU[i]));
	}

	public VerticalPanel setupSubmissionsPanel() {
		VerticalPanel reportContent = new VerticalPanel();
		setupFormsAndGoalsPanel();
		reportContent.add(navTable);
		reportContent.add(setupFiltersDataPanel(view));
		return reportContent;
	}

	public void setTitleString(String title) {
		navTable.setHTML(0, 1, "<h1 id=\"form_name\">" + title + "</h1>");
	}

	public HTML setupVisualizePanel() {
		return new HTML("Content Forthcoming");
	}

	public void setupFormsAndGoalsPanel() {
		navTable = new FlexTable();
		navTable.getElement().setId("submission_nav_table");

		FlexTable formAndGoalSelectionTable = new FlexTable();
		formAndGoalSelectionTable.getElement().setId("form_and_goal_selection");
		// list of forms
		formAndGoalSelectionTable.setWidget(0, 0, formsBox);
		// list of filters
		formAndGoalSelectionTable.setWidget(0, 1, filtersBox);
		// load form + filter
		Button loadFormAndFilterButton = new Button("Fetch Form with Filter");
		currentGroup = def;

		loadFormAndFilterButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				String formName = formsBox.getValue(formsBox.getSelectedIndex());
				String groupName = filtersBox.getValue(filtersBox.getSelectedIndex());
				String formID = "";
				for(FormSummary form : allForms) {
					if(formName.compareTo(form.getTitle()) == 0) {
						formID = form.getId();
					}
				}
				view.clear();
				for(FilterGroup group : allGroups) {
					if(groupName.compareTo(group.getName()) == 0) {
						group.setFormId(formID);
						currentGroup = enterEditMode(group);
						break;
					}
				}
				if(view.size() == 0) {
					def.setFormId(formID);
					view.add(def);
					currentGroup = def;
				}
				updateFiltersDataPanel(view);
				baseUI.getTimer().restartTimer();
				baseUI.update(SubTabs.FORM, PageUpdates.SUBMISSIONDATA);
			}

		});
		formAndGoalSelectionTable.setWidget(0, 2, loadFormAndFilterButton);

		navTable.setWidget(0, 0, formAndGoalSelectionTable);
		navTable.setHTML(0, 1, "<h2 id=\"form_name\"></h2>");
		navTable.getElement().getFirstChildElement().getNextSiblingElement().getFirstChildElement()
		.getFirstChildElement().getNextSiblingElement().setId("form_title_cell");

		FlexTable actionTable = new FlexTable();
		// end goals vis, export, publish
		Button visualizeButton = new Button("<img src=\"images/bar_chart.png\" /> Visualize");
		visualizeButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel vizPopup = new CreateNewVisualizationPopup(getHeaders(),
						getSubmissions(),
						currentGroup.getFormId(),
						formSvc,
						submissionSvc,
						baseUI);
				vizPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						int left = (Window.getClientWidth() - offsetWidth) / 2;
						int top = (Window.getClientHeight() - offsetHeight) / 2;
						vizPopup.setPopupPosition(left, top);
					}
				});
				baseUI.getTimer().restartTimer();
			}
		});
		actionTable.setWidget(0, 0, visualizeButton);
		Button exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");
		exportButton.addClickHandler(new ClickHandler () {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new CreateNewExportPopup(currentGroup.getFormId(), formSvc, baseUI.getManageNav(), baseUI);
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
		actionTable.setWidget(0, 1, exportButton);
		Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Publish");
		publishButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new CreateNewExternalServicePopup(currentGroup.getFormId(), 
						baseUI.getManageNav().servicesAdminSvc, hash, baseUI);
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
		actionTable.setWidget(0, 2, publishButton);
		navTable.setWidget(0, 2, actionTable);
		navTable.getElement().getFirstChildElement().getNextSiblingElement().getFirstChildElement()
		.getFirstChildElement().getNextSiblingElement().getNextSiblingElement().setAttribute("align", "right");
	}

	ClickHandler getSubMenuClickHandler(
			final String menu, final String subMenu) {
		return new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				hash.clear();
				hash.set(UrlHash.MAIN_MENU, menu);
				hash.set(UrlHash.SUB_MENU, subMenu);
				hash.put();
				baseUI.getTimer().restartTimer();
			}
		};
	}

	public void updateFiltersDataPanel(List<FilterGroup> groups) {
		title.removeItems();
		for (FilterGroup group : groups) {
			TreeItem itemGroup = loadFilterGroup(group);
			title.addItem(itemGroup);
			title.setState(true);
		}
	}

	public HorizontalPanel setupFiltersDataPanel(List<FilterGroup> groups) {
		HorizontalPanel filterPanel = new HorizontalPanel();
		//create filter tree
		Tree activeFilters = new Tree();
		title = new TreeItem(new Label("Active Filters"));
		activeFilters.addItem(title);

		for (FilterGroup group : groups) {
			TreeItem itemGroup = loadFilterGroup(group);
			title.addItem(itemGroup);
			title.setState(true);
		}

		//add new filter button
		Button newFilter = new Button();
		newFilter.setHTML(
		"<img src=\"images/yellow_plus.png\" /> New Filter");
		newFilter.addClickHandler(new ClickHandler(){
			@Override
			public void onClick(ClickEvent event) {
				final CreateNewFilterPopup filterPopup = new CreateNewFilterPopup(dataTable, currentGroup, baseUI);
				filterPopup.setPopupPositionAndShow(
						new PopupPanel.PositionCallback() {
							@Override
							public void setPosition(int offsetWidth, int offsetHeight) {
								int left = (Window.getClientWidth() - offsetWidth) / 2;
								int top = (Window.getClientHeight() - offsetHeight) / 2;
								filterPopup.setPopupPosition(left, top);
							}
						}
				);
				filterPopup.addCloseHandler(new CloseHandler<PopupPanel>() {

					@Override
					public void onClose(CloseEvent<PopupPanel> event) {
						updateFiltersDataPanel(view);
					}

				});
				baseUI.getTimer().restartTimer();
			}
		});

		activeFilters.add(newFilter);
		FlowPanel filtersContainer = new FlowPanel();
		filtersContainer.add(activeFilters);
		filtersContainer.getElement().setId("filters_container");
		filterPanel.add(filtersContainer);

		// view data
		dataTable.getRowFormatter().addStyleName(0, "titleBar");
		dataTable.addStyleName("dataTable");
		FlowPanel submissionContainer = new FlowPanel();
		submissionContainer.getElement().setId("submission_container");
		submissionContainer.add(dataTable);
		filterPanel.add(submissionContainer);

		filterPanel.getElement().setId("filters_data");
		filterPanel.getElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().setId("filters_panel");
		filterPanel.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
		return filterPanel;
	}

	public TreeItem loadFilterGroup(final FilterGroup group) {
		final FlexTable filterBox = new FlexTable();
		Label groupName = new Label(group.getName());
		final Button saveFilterGroup = new Button("Save");
		saveFilterGroup.getElement().setPropertyObject("group", group);
		final FlexTable filters = new FlexTable();
		saveFilterGroup.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent event) {
				if(filters.getRowCount() == 0) {
					Window.alert(
					"You need at least one filter to save a group.");
				} else {
					boolean filterSet = false;
					boolean firstTime = true;
					boolean match = false;
					String newFilter = "";
					while(!filterSet) {
						if(firstTime) {
							newFilter = 
								Window.prompt("Please enter a name for this group", 
										"FilterGroup" + (filtersBox.getItemCount()+1));
						} else {
							match = false;
							newFilter = Window.prompt(
									"That group already exists.  " +
									"Please enter a new name", 
									"FilterGroup" + 
									(filtersBox.getItemCount()+1));
						}
						firstTime = false;
						if(newFilter != null) {
							for(int i = 0; i < filtersBox.getItemCount(); i++) {
								if((filtersBox.getValue(i))
										.compareTo(newFilter) == 0 &&
										newFilter.compareTo(currentGroup.getName()) != 0) {
									match = true;
								}
							}
							if(!match) {
								filterSet = true;
							}
						} else {
							filterSet = true;
						}
					}
					//Save the new filter
					addFilterGroup(newFilter, currentGroup);
				}
				baseUI.getTimer().restartTimer();
			}
		});
		filterBox.setWidget(0, 0, groupName);
		filterBox.setWidget(0, 1, saveFilterGroup);

		TreeItem filterGroup = new TreeItem(filterBox);

		int row = 0;
		for (Filter filter: group.getFilters()) {
			if(filter instanceof RowFilter) {
				RowFilter rowFilter = (RowFilter) filter;
				filters.setWidget(row, 0, new Label(
						rowFilter.getVisibility() + rowFilter.getColumn().getDisplayHeader() + 
						"where columns are " + rowFilter.getOperation() + 
						rowFilter.getInput()));	  
			} else if (filter instanceof ColumnFilter){
				ColumnFilter columnFilter = (ColumnFilter) filter;
				List<ColumnFilterHeader> columns = columnFilter.getColumnFilterHeaders();
				String columnNames = "";
				for(ColumnFilterHeader column: columns) {
					columnNames += " " + column.getColumn().getDisplayHeader();
				}
				filters.setWidget(row, 0, new Label(
						columnFilter.getVisibility() + columnNames));
			}

			final Button removeFilter = new Button("-");
			filters.setWidget(row, 1, removeFilter);
			removeFilter.getElement().setPropertyObject("filter", filter);

			removeFilter.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					Filter remove = (Filter)removeFilter.getElement().getPropertyObject("filter");
					currentGroup.removeFilter(remove);
					updateFiltersDataPanel(view);
					baseUI.getTimer().restartTimer();
					baseUI.update(SubTabs.FORM, PageUpdates.SUBMISSIONDATA);
				} 
			});
			row++;
		}
		filterGroup.addItem(filters);
		filterGroup.setState(true);
		return filterGroup;
	}

	private void addFilterGroup(final String id, FilterGroup group) {
		// Initialize the service proxy.
		if (filterSvc == null) {
			filterSvc = SecureGWT.get().createFilterService();
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = 
			new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {			

			}
			@Override
			public void onSuccess(Boolean result) {
				baseUI.update(SubTabs.FILTER, PageUpdates.NEWFORM);
				updateFiltersDataPanel(view);
			}
		};
		List<Filter> filters = new ArrayList<Filter>();
		filters.addAll(currentGroup.getFilters());
		FilterGroup newGroup = new FilterGroup(id, group.getFormId(), filters);
		currentGroup = enterEditMode(newGroup);

		// Make the call to the form service.
		filterSvc.updateFilterGroup(newGroup, callback);
	}

	//we can only edit one filter group at a time
	//so we are making a temporary filter group
	//that will take all of the user changes
	//these won't be reflected permanently unless a save is done
	//we don't need to do this with the default group however
	public FilterGroup enterEditMode(FilterGroup group) {
		view.clear();
		ArrayList<Filter> filters = new ArrayList<Filter>();
		filters.addAll(group.getFilters());
		FilterGroup tempGroup = new FilterGroup(group.getName(), group.getFormId(), filters);
		view.add(tempGroup);
		return tempGroup;
	}

	public void setupExportPanel() {
		if (formSvc == null) {
			formSvc = SecureGWT.get().createFormService();
		}

		AsyncCallback<ExportSummary[] > callback = new AsyncCallback<ExportSummary []>() {
			@Override
			public void onFailure(Throwable caught) {
				// TODO Auto-generated method stub
			}

			@Override
			public void onSuccess(ExportSummary[] result) {
				exportTable.updateExportPanel(result);
			}
		};

		formSvc.getExports(callback);
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
				else if(update.equals(PageUpdates.SUBMISSIONDATA) && forms.length > 0)
					requestUpdatedSubmissionData(view);
				else if(update.equals(PageUpdates.ALL)) {
					fillFormDropDown(forms);
					if(forms.length > 0)
						requestUpdatedSubmissionData(view);
				}
			}
		};

		// Make the call to the form service.
		formSvc.getForms(callback);

		// TODO: refactor properly to the new update
		//manageNav.getExportList();
		//		manageNav.getExternalServicesList(formId)
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
				baseUI.update(SubTabs.FILTER, PageUpdates.NEWFORM);
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
		baseUI.update(SubTabs.FILTER, PageUpdates.SAMEFORM);
	}

	public void requestUpdatedSubmissionData(List<FilterGroup> groups) {

		// Initialize the service proxy.
		if (submissionSvc == null) {
			submissionSvc = SecureGWT.get().createSubmissionService();
		}

		// Set up the callback object.
		AsyncCallback<SubmissionUISummary> callback = new AsyncCallback<SubmissionUISummary>() {
			public void onFailure(Throwable caught) {
				baseUI.reportError(caught);
			}

			public void onSuccess(SubmissionUISummary summary) {
				baseUI.clearError();
				submissions = summary.getSubmissions();
				updateSubmissionTable(dataTable, summary);
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

	/**
	 * NOTE: This formatting function is called by several places, should not be used to update member variables
	 * 
	 * NEED to refactor code so that submissionSvc comes from a global context
	 * 
	 * @param table
	 * @param summary
	 */
	public void updateSubmissionTable(FlexTable table, SubmissionUISummary summary) {
		List<Column> tableHeaders = summary.getHeaders();
		List<SubmissionUI> tableSubmissions = summary.getSubmissions();

		int headerIndex = 0;
		table.removeAllRows();
		table.getRowFormatter().addStyleName(0, "titleBar");
		table.addStyleName("dataTable");
		for(Column column : tableHeaders) {
			table.setText(0, headerIndex++, column.getDisplayHeader());
		}

		int i = 1;
		for(SubmissionUI row : tableSubmissions) {
			int j = 0;
			for(final String values : row.getValues()) {
				switch (tableHeaders.get(j).getUiDisplayType()) {
				case BINARY:
					Image image = new Image(values + UIConsts.PREVIEW_SET);     
					image.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							final PopupPanel popup = new ImagePopup(values);
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

					table.setWidget(i, j, image);
					break;
				case REPEAT:
					Button repeat = new Button("View");
					final AggregateUI tmp = baseUI; // fix after refactoring of the function and global services
					repeat.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							final PopupPanel popup = new RepeatPopup(values, submissionSvc, tmp);
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

					table.setWidget(i, j, repeat);
					break;
				default:
					table.setText(i, j, values);            
				}
				j++;
			}
			if (i % 2 == 0) {
				table.getRowFormatter().setStyleName(i, "evenTableRow");
			}
			i++;
		}
	}

	private void removeFilterGroup(FilterGroup group) {
		if (filterSvc == null) {
			filterSvc = SecureGWT.get().createFilterService();
		}

		AsyncCallback<Boolean> callback = 
			new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {
				baseUI.reportError(caught);
			}

			@Override
			public void onSuccess(Boolean result) {
				baseUI.clearError();
			}
		};

		filterSvc.deleteFilterGroup(group, callback);
	}

	void getFilterList(final PageUpdates formChange) {
		// Initialize the service proxy.
		if (filterSvc == null) {
			filterSvc = SecureGWT.get().createFilterService();
		}

		// Set up the callback object.
		AsyncCallback<FilterSet> callback = 
			new AsyncCallback<FilterSet>() {
			public void onFailure(Throwable caught) {
				baseUI.reportError(caught);
			}

			@Override
			public void onSuccess(FilterSet result) {
				baseUI.clearError();
				fillFilterDropDown(result, formChange);
			}
		};

		// Make the call to the form service.
		filterSvc.getFilterSet(lastFormUsed, callback);
	}

	private void fillFilterDropDown(FilterSet set, PageUpdates formChange) {
		if(formChange.equals(PageUpdates.SAMEFORM)) {
			int selected = filtersBox.getSelectedIndex();
			allGroups.clear();
			if(filtersBox.getItemCount() == 0)
				filtersBox.addItem("none");

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
		} else if (formChange.equals(PageUpdates.NEWFORM)) {
			filtersBox.clear();
			allGroups.clear();
			filtersBox.addItem("none");
			for(FilterGroup group : set.getGroups()) {
				filtersBox.addItem(group.getName());
				allGroups.add(group);
			}
		}
	}
}