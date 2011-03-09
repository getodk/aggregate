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
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.submission.SubmissionService;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.aggregate.client.submission.SubmissionUI;
import org.opendatakit.aggregate.client.submission.SubmissionUISummary;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.CloseEvent;
import com.google.gwt.event.logical.shared.CloseHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AggregateUI implements EntryPoint {
  
  private static final int REFRESH_INTERVAL = 5000; // ms
  
  // Main Navigation
  private static final String SUBMISSIONS = "submissions";
  private static final String MANAGEMENT = "management";
  private static final String[] MAIN_MENU = {SUBMISSIONS, MANAGEMENT};
  // Submission Navigation
  private static final String FILTER = "filter";
  private static final String VISUALIZE = "visualize";
  private static final String[] SUBMISSION_MENU = {FILTER, VISUALIZE};
  // Management Navigation
  private static final String FORMS = "forms";
  private static final String EXPORT = "export";
  private static final String PERMISSIONS = "permissions";
  private static final String UTILITIES = "utilities";
  private static final String[] MANAGEMENT_MENU = {FORMS, EXPORT, PERMISSIONS, UTILITIES};

  private List<FilterGroup> view = new ArrayList<FilterGroup>();
  private FlexTable dataTable; //contains the data
  private FilterGroup def; //the default filter group
  private HorizontalPanel filterPanel = new HorizontalPanel();
  private CreateNewFilterPopup filterPopup = new CreateNewFilterPopup();
  private UrlHash hash;
  
  // navigation
  private DecoratedTabPanel mainNav = new DecoratedTabPanel();
  private TabPanel manageNav = new TabPanel();
  private TabPanel submissionNav = new TabPanel();
  
  // Report tab
  private HorizontalPanel filtersDataHelp = new HorizontalPanel();
  private FlexTable formAndGoalSelectionTable = new FlexTable();
  private FlexTable uploadTable = new FlexTable();

  private FormServiceAsync formSvc;
  private SubmissionServiceAsync submissionSvc;
  
  private FlexTable listOfForms;
  private ListBox formsBox = new ListBox();
  
  public AggregateUI() {
    formSvc = GWT.create(FormService.class);
    listOfForms = new FlexTable();
    
    // Setup timer to refresh list automatically.
    Timer refreshTimer = new Timer() {
       @Override
       public void run() {
         getFormList();
       }
    };
    refreshTimer.scheduleRepeating(REFRESH_INTERVAL);
    
  }
  
  public HorizontalPanel setupFormsAndGoalsPanel() {
    // list of forms
    formAndGoalSelectionTable.setWidget(0, 0, formsBox);
    // list of filters
    ListBox filtersBox = new ListBox();
    filtersBox.addItem("filter1");
    filtersBox.addItem("filter2");
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
				setupFiltersDataHelpPanel(view);
			} 
		  });
		  row++;
	  }
	  filterGroup.addItem(filters);
	  filterGroup.setState(true);
	  return filterGroup;
  }
  
  public HorizontalPanel setupFiltersDataHelpPanel(
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
	  newFilter.setHTML("<img src=\"images/yellow_plus.png\" /> New Filter");
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
				});
		  filterPopup.addCloseHandler(new CloseHandler<PopupPanel>() {
			  
			  @Override
			  public void onClose(CloseEvent<PopupPanel> event) {
				  filterPanel.clear();
				  setupFiltersDataHelpPanel(view);
			  }
			
		  });
		}
	  });
	  
	  activeFilters.add(newFilter);
	  filtersDataHelp.add(activeFilters);

	  
    // view data
    dataTable = new FlexTable();
    requestUpdatedData();
    dataTable.getRowFormatter().addStyleName(0, "titleBar");
    dataTable.addStyleName("dataTable");
    filtersDataHelp.add(dataTable);

    // view help
    VerticalPanel helpPanel = new VerticalPanel();
    for (int i = 1; i < 5; i++) {
      helpPanel.add(new HTML("Help Content " + i));
    }
    helpPanel.setStyleName("help_panel");
    filtersDataHelp.add(helpPanel);
    filtersDataHelp.getElement().setId("filters_data_help");
	  filtersDataHelp.getElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().setId("filters_panel");
	  filtersDataHelp.getElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().getNextSiblingElement().getNextSiblingElement().setId("help_panel");
    filtersDataHelp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
    return filtersDataHelp;
  }

  public void requestUpdatedData() {
    
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

    FilterGroup testFilterGroup = new FilterGroup("TESTING", "widgets", null);
    
    // Make the call to the form service.
    submissionSvc.getSubmissions(testFilterGroup, callback);

  }
  
  public void updateDataTable(SubmissionUISummary summary) {

    int headerIndex = 0;
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
  
  public VerticalPanel setupFormManagementPanel() {
    Button uploadFormButton = new Button();
    uploadFormButton.setHTML("<img src=\"images/blue_up_arrow.png\" /> Upload Form");
    uploadTable.setWidget(0, 0, uploadFormButton);
    
    listOfForms.setText(0, 0, "Title");
    listOfForms.setText(0, 1, "Form Id");
    listOfForms.setText(0, 2, "User");
    listOfForms.setText(0, 3, "Enabled");
    listOfForms.setText(0, 4, "Publish");
    listOfForms.setText(0, 5, "Export");
    listOfForms.setText(0, 6, "Delete");
    listOfForms.getRowFormatter().addStyleName(0, "titleBar");
    listOfForms.addStyleName("dataTable");
    
    getFormList();

    VerticalPanel formManagementPanel = new VerticalPanel();
    formManagementPanel.add(uploadTable);
    formManagementPanel.add(listOfForms);
    return formManagementPanel;
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
			  if (s.equals(SUBMISSIONS))
				  panel = submissionNav;
			  else if (s.equals(MANAGEMENT))
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
	
	// Create sub menu navigation
	setupSubmissionNav();
	setupManageNav();
	
    mainNav.add(submissionNav, "Submissions");
    mainNav.add(manageNav, "Management");
    mainNav.addStyleName("mainNav");
    
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
    RootPanel.get("dynamic_content").add(mainNav);
    contentLoaded();
  }
  
  private ClickHandler getSubMenuClickHandler(
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
  
  public void setupManageNav() {
	  manageNav.add(setupFormManagementPanel(), "Forms");
	  manageNav.add(setupExportsPanel(), "Export");
	  manageNav.add(setupPermissionsPanel(), "Permissions");
	  manageNav.add(setupUtilitiesPanel(), "Utilities");
	  
	  int selected = 0;
	  String subMenu = hash.get(UrlHash.SUB_MENU);
	  for (int i = 0; i < MANAGEMENT_MENU.length; i++)
		  if (subMenu.equals(MANAGEMENT_MENU[i]))
			  selected = i;
	  manageNav.selectTab(selected);

	  for (int i = 0; i < MANAGEMENT_MENU.length; i++)
		  manageNav.getTabBar().getTab(i).addClickHandler(getSubMenuClickHandler(MANAGEMENT, MANAGEMENT_MENU[i]));
  }
  
  public FlexTable setupExportsPanel() {
	  return new FlexTable();
  }
  
  public HTML setupPermissionsPanel() {
	  return new HTML("Content Forthcoming");
  }
  
  public HTML setupUtilitiesPanel() {
	  return new HTML("Content Forthcoming");
  }
  
  public void setupSubmissionNav() {
	  submissionNav.add(setupSubmissionsPanel(), "Filter");
	  submissionNav.add(setupVisualizePanel(), "Visualize");
	  
	  int selected = 0;
	  String subMenu = hash.get(UrlHash.SUB_MENU);
	  for (int i = 0; i < SUBMISSION_MENU.length; i++)
		  if (subMenu.equals(SUBMISSION_MENU[i]))
			  selected = i;
	  submissionNav.selectTab(selected);
	  
	  for (int i = 0; i < SUBMISSION_MENU.length; i++)
		  submissionNav.getTabBar().getTab(i).addClickHandler(getSubMenuClickHandler(SUBMISSIONS, SUBMISSION_MENU[i]));
  }
  
  public VerticalPanel setupSubmissionsPanel() {
	  	VerticalPanel reportContent = new VerticalPanel();
	    reportContent.add(setupFormsAndGoalsPanel());
	    def = new FilterGroup(
	    		"Default", "def", new ArrayList<Filter>());
	    view.add(def);
	    filterPanel = setupFiltersDataHelpPanel(view);
	    reportContent.add(filterPanel);
	    return reportContent;
  }
  
  public HTML setupVisualizePanel() {
	  return new HTML("Content Forthcoming");
  }
  
  // Let's JavaScript know that the GWT content has been loaded
  // Currently calls into javascript/resize.js, if we add more JavaScript
  // then that should be changed.
  private native void contentLoaded() /*-{
  	$wnd.gwtContentLoaded();
  }-*/;

  private void getFormList() {
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
        updateFormTable(forms);
        fillFormDropDown(forms);
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
	  for (int i = 0; i < forms.length; i++) {
		  FormSummary form = forms[i];
		  if (!existingForms.contains(form.getTitle())) {
			  formsBox.addItem(form.getTitle());
			  if (hash.get(UrlHash.FORM).equals(form.getTitle()))
				  formsBox.setItemSelected(formsBox.getItemCount() - 1, true);
		  }
		  // TODO: Kyle - need to fix... once form is loaded then set the id
		  def.setFormId(form.getId());
	  }
  }
  
  /**
   * Update the list of forms
   * 
   * @param formSummary
   */
  private void updateFormTable(FormSummary [] forms) {
    for (int j = 0; j < forms.length; j++) {
    	int i = j + 1;
    	FormSummary form = forms[j];
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
        listOfForms.setWidget(i, 4, new Button("<img src=\"images/green_right_arrow.png\" /> Publish"));
        listOfForms.setWidget(i, 5, new Button("<img src=\"images/green_right_arrow.png\" /> Export"));
        listOfForms.setWidget(i, 6, deleteButton);
        if (i % 2 == 0)
            listOfForms.getRowFormatter().addStyleName(i, "evenTableRow");
    }
  }
}
