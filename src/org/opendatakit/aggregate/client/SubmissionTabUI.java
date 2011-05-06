package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.CreateNewFilterPopup;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterService;
import org.opendatakit.aggregate.client.filter.FilterServiceAsync;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.visualization.CreateNewVisualizationPopup;
import org.opendatakit.aggregate.constants.common.FormOrFilter;
import org.opendatakit.aggregate.constants.common.PageUpdates;

import com.google.gwt.core.client.GWT;
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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SubmissionTabUI extends TabPanel {

	// Submission Navigation
	private static final String FILTER = "filter";
	private static final String[] SUBMISSION_MENU = {FILTER};
	static final String SUBMISSIONS = "submissions";
	private UrlHash hash;
	private List<FilterGroup> view;
	private ListBox formsBox;
	private ListBox filtersBox;
	private FlexTable navTable;
	private FlexTable dataTable;
	private FilterGroup def;
	private AggregateUI parent;
	private FilterServiceAsync filterSvc;
	private List<FilterGroup> allGroups;
	private List<FormSummary> allForms;
	private TreeItem title;
	private FilterGroup currentGroup;

	public SubmissionTabUI(List<FilterGroup> view,
			ListBox formsBox, ListBox filtersBox, FlexTable dataTable, 
			FilterGroup def, AggregateUI parent, 
			List<FilterGroup> allGroups, List<FormSummary> allForms) {
		super();
		this.hash = UrlHash.getHash();
		this.view = view;
		this.formsBox = formsBox;
		this.filtersBox = filtersBox;
		this.dataTable = dataTable;
		this.def = def;
		this.parent = parent;
		this.allGroups = allGroups;
		this.allForms = allForms;
		this.add(setupSubmissionsPanel(), "Filter");
		this.getElement().setId("second_level_menu");

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
				parent.getTimer().restartTimer(parent);
				parent.update(FormOrFilter.FORM, PageUpdates.SUBMISSIONDATA);
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
				final PopupPanel vizPopup = new CreateNewVisualizationPopup(parent.getHeaders(),
						parent.getSubmissions(),
						currentGroup.getFormId(),
						parent.formSvc,
						parent.submissionSvc);
				vizPopup.setPopupPositionAndShow(new PopupPanel.PositionCallback() {
					@Override
					public void setPosition(int offsetWidth, int offsetHeight) {
						int left = (Window.getClientWidth() - offsetWidth) / 2;
						int top = (Window.getClientHeight() - offsetHeight) / 2;
						vizPopup.setPopupPosition(left, top);
					}
				});
			}
		});
		actionTable.setWidget(0, 0, visualizeButton);
		Button exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");
		exportButton.addClickHandler(new ClickHandler () {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new CreateNewExportPopup(currentGroup.getFormId(), parent.formSvc, parent.manageNav);
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
		actionTable.setWidget(0, 1, exportButton);
		Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Publish");
		publishButton.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				final PopupPanel popup = new CreateNewExternalServicePopup(currentGroup.getFormId(), parent.formSvc, parent.manageNav);
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
				final CreateNewFilterPopup filterPopup = new CreateNewFilterPopup(dataTable, currentGroup, parent);
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
					parent.getTimer().restartTimer(parent);
					parent.update(FormOrFilter.FORM, PageUpdates.SUBMISSIONDATA);
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
			filterSvc = GWT.create(FilterService.class);
		}

		// Set up the callback object.
		AsyncCallback<Boolean> callback = 
			new AsyncCallback<Boolean>() {
			public void onFailure(Throwable caught) {			

			}
			@Override
			public void onSuccess(Boolean result) {
				parent.update(FormOrFilter.FILTER, PageUpdates.ALL);
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
}