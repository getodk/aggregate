package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.CreateNewFilterPopup;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
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
	private static final String VISUALIZE = "visualize";
	private static final String[] SUBMISSION_MENU = {FILTER, VISUALIZE};
	static final String SUBMISSIONS = "submissions";
	private HorizontalPanel filterPanel = new HorizontalPanel();
	private FlexTable formAndGoalSelectionTable = new FlexTable();
	private CreateNewFilterPopup filterPopup = new CreateNewFilterPopup();
	private HorizontalPanel filtersData = new HorizontalPanel();
	private UrlHash hash;
	private List<FilterGroup> view;
	private ListBox formsBox;
	private ListBox filtersBox;
	private FlexTable dataTable;
	private FilterGroup def;
	
	public SubmissionTabUI(List<FilterGroup> view,
			ListBox formsBox, ListBox filtersBox, FlexTable dataTable, 
			FilterGroup def) {
		super();
		this.hash = UrlHash.getHash();
		this.view = view;
		this.formsBox = formsBox;
		this.filtersBox = filtersBox;
		this.dataTable = dataTable;
		this.def = def;
		this.add(setupSubmissionsPanel(), "Filter");
		this.add(setupVisualizePanel(), "Visualize");
		
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
		reportContent.add(setupFormsAndGoalsPanel());
		filterPanel = setupFiltersDataPanel(view);
		reportContent.add(filterPanel);
		return reportContent;
	}
	
	public HTML setupVisualizePanel() {
		return new HTML("Content Forthcoming");
	}
	
	public HorizontalPanel setupFormsAndGoalsPanel() {
		// list of forms
		formAndGoalSelectionTable.setWidget(0, 0, formsBox);
		// list of filters
		formAndGoalSelectionTable.setWidget(0, 1, filtersBox);
		// load form + filter
		Button loadFormAndFilterButton = new Button("Load Filter");
		formAndGoalSelectionTable.setWidget(0, 2, loadFormAndFilterButton);
		formAndGoalSelectionTable.setHTML(0, 3, "&nbsp;&nbsp;");

		// end goals vis, export, publish
		Button exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");
		formAndGoalSelectionTable.setWidget(0, 4, exportButton);
		Button publishButton = new Button("<img src=\"images/green_right_arrow.png\" /> Publish");
		formAndGoalSelectionTable.setWidget(0, 5, publishButton);

		HorizontalPanel formsAndGoalsPanel = new HorizontalPanel();
		formsAndGoalsPanel.add(formAndGoalSelectionTable);
		formsAndGoalsPanel.getElement().setId("form_and_goals_panel");
		return formsAndGoalsPanel;
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
	
	public HorizontalPanel setupFiltersDataPanel(
			List<FilterGroup> groups) {
		//create filter tree
		Tree activeFilters = new Tree();
		TreeItem title = new TreeItem(new Label("Active Filters"));
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
				filterPopup = new CreateNewFilterPopup(dataTable, def);
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
					  filterPanel.clear();
					  setupFiltersDataPanel(view);
				  }
				
			  });
			}
		  });
		  
		  activeFilters.add(newFilter);
		  filtersData.add(activeFilters);
		  
	    // view data
	    dataTable.getRowFormatter().addStyleName(0, "titleBar");
	    dataTable.addStyleName("dataTable");
	    filtersData.add(dataTable);
	    
	    filtersData.getElement().setId("filters_data");
		  filtersData.getElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().setId("filters_panel");
	    filtersData.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
	    return filtersData;
	  }
	  
	  public TreeItem loadFilterGroup(final FilterGroup group) {
		  TreeItem filterGroup = new TreeItem(
				  new Label(group.getName()));
		  
		  final FlexTable filters = new FlexTable();
		  
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
			  removeFilter.getElement().setPropertyObject(
					  "filter", filter);
			  removeFilter.addClickHandler(new ClickHandler() {

				@Override
				public void onClick(ClickEvent event) {
					Filter remove = (Filter)removeFilter.getElement()
						.getPropertyObject("filter");
					group.removeFilter(remove);
					filterPanel.clear();
					setupFiltersDataPanel(view);
				} 
			  });
			  row++;
		  }
		  filterGroup.addItem(filters);
		  filterGroup.setState(true);
		  return filterGroup;
	  }
}