package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class AggregateUI implements EntryPoint {
  
  private static final int REFRESH_INTERVAL = 5000; // ms
  
  private Url url;
  
  // navigation
  private TabPanel mainNav = new TabPanel();
  private Element spacer;
  
  // Report tab
  private VerticalPanel reportContent = new VerticalPanel();
  private HorizontalPanel filtersDataHelp = new HorizontalPanel();
  private FlexTable formAndGoalSelectionTable = new FlexTable();
  private FlexTable uploadTable = new FlexTable();
  
  // Manage tab
  private VerticalPanel manageContent = new VerticalPanel();

  private FormServiceAsync formSvc;
  private ListBox enabledDropDown;
  private ListBox publishDropDown;
  private ListBox exportDropDown;
  private FlexTable listOfForms;
  
  public AggregateUI() {
    formSvc = GWT.create(FormService.class);
    
    enabledDropDown = new ListBox();
    enabledDropDown.addItem("Disabled/Inactive");
    enabledDropDown.addItem("Disabled/Active");
    enabledDropDown.addItem("Enabled/Active");

    publishDropDown = new ListBox();
    publishDropDown.addItem("Google FusionTable");
    publishDropDown.addItem("Google Spreadsheet");
    publishDropDown.addItem("JSON Server");
    
    exportDropDown = new ListBox();
    exportDropDown.addItem("CSV");
    exportDropDown.addItem("KML");
    exportDropDown.addItem("XML");
    
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
    ListBox formsBox = new ListBox();
    formsBox.addItem("form1");
    formsBox.addItem("form2");
    formAndGoalSelectionTable.setWidget(0, 0, formsBox);
    // list of filters
    ListBox filtersBox = new ListBox();
    filtersBox.addItem("filter1");
    filtersBox.addItem("filter2");
    formAndGoalSelectionTable.setWidget(0, 1, filtersBox);
    // load form + filter
    Button loadFormAndFilterButton = new Button("Load");
    formAndGoalSelectionTable.setWidget(0, 2, loadFormAndFilterButton);
    // create filter
    Button createFilterButton = new Button("Create Filter");
    formAndGoalSelectionTable.setWidget(0, 3, createFilterButton);
    formAndGoalSelectionTable.setHTML(0, 4, "&nbsp;&nbsp;");

    // end goals vis, export, publish
    Button visualizeButton = new Button("Visualize");
    formAndGoalSelectionTable.setWidget(0, 5, visualizeButton);
    Button exportButton = new Button("Export");
    formAndGoalSelectionTable.setWidget(0, 6, exportButton);
    Button publishButton = new Button("Publish");
    formAndGoalSelectionTable.setWidget(0, 7, publishButton);

    HorizontalPanel formsAndGoalsPanel = new HorizontalPanel();
    formsAndGoalsPanel.add(formAndGoalSelectionTable);
    formsAndGoalsPanel.getElement().setId("form_and_goals_panel");
    return formsAndGoalsPanel;
  }

  public HorizontalPanel setupFiltersDataHelpPanel() {
    // view filters
    FlexTable filtersTable = new FlexTable();
    filtersTable.setWidget(0, 0, new Button("Filter1"));
    filtersTable.setWidget(0, 1, new Button("-"));
    filtersTable.setWidget(1, 0, new Button("Filter2"));
    filtersTable.setWidget(1, 1, new Button("-"));
    filtersTable.setStyleName("filters_panel");
    filtersDataHelp.add(filtersTable);

    // view data
    FlexTable dataTable = new FlexTable();
    for (int i = 0; i < 4; i++) {
      dataTable.setText(0, i, "Column " + i);
    }
    for (int i = 1; i < 6; i++) {
      for (int j = 0; j < 4; j++) {
        dataTable.setText(i, j, "cell (" + i + ", " + j + ")");
      }
      if (i % 2 == 0)
        dataTable.getRowFormatter().setStyleName(i, "even_table_row");
    }
    dataTable.getRowFormatter().addStyleName(0, "title_bar");
    dataTable.getElement().setId("data_table");
    filtersDataHelp.add(dataTable);

    // view help
    VerticalPanel helpPanel = new VerticalPanel();
    for (int i = 1; i < 5; i++) {
      helpPanel.add(new HTML("Help Content " + i));
    }
    helpPanel.setStyleName("help_panel");
    filtersDataHelp.add(helpPanel);
    filtersDataHelp.getElement().setId("filters_data_help");
    filtersDataHelp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
    return filtersDataHelp;
  }

  public VerticalPanel setupFormManagementPanel() {
    Button uploadFormButton = new Button("Upload Form");
    uploadTable.setWidget(0, 0, uploadFormButton);
    
    listOfForms.setText(0, 0, "Title");
    listOfForms.setText(0, 1, "Form Id");
    listOfForms.setText(0, 2, "User");
    listOfForms.setText(0, 3, "Enabled");
    listOfForms.setText(0, 4, "Publish");
    listOfForms.setText(0, 5, "Export");
    listOfForms.setText(0, 6, "Delete");
    listOfForms.getRowFormatter().addStyleName(0, "title_bar");
    listOfForms.getElement().setId("form_management_table");
    
    getFormList();

    VerticalPanel formManagementPanel = new VerticalPanel();
    formManagementPanel.add(uploadTable);
    formManagementPanel.add(listOfForms);
    return formManagementPanel;
  }
  
  public void onModuleLoad() {
    reportContent.add(setupFormsAndGoalsPanel());
    reportContent.add(setupFiltersDataHelpPanel());

    manageContent.add(setupFormManagementPanel());

    mainNav.add(reportContent, "Report");
    mainNav.add(manageContent, "Manage");
    mainNav.addSelectionHandler(new SelectionHandler<Integer>() {
    	public void onSelection(SelectionEvent<Integer> event) {
    		if (event.getSelectedItem() == 0)
    			url.set("panel", "report");
    		else if (event.getSelectedItem() == 1)
    			url.set("panel", "manage");
    	}
    });
    url = new Url();
    if (!url.contains("panel")) { // default
    	mainNav.selectTab(0);
    } else if (url.contains("panel", "report")) {
    	mainNav.selectTab(0);
    } else if (url.contains("panel", "manage")) {
    	mainNav.selectTab(1);
    } else { // default
    	mainNav.selectTab(0);
    }

    mainNav.addStyleName("mainNav");
    mainNav.getTabBar().addStyleName("mainNavTabBar");
    
    spacer = mainNav.getTabBar().getElement().getFirstChildElement()
    	.getFirstChildElement().getFirstChildElement().getFirstChildElement();
    spacer.setId("spacer_tab");
    
    RootPanel.get("dynamic_content").add(new HTML("<img src=\"images/odk_aggregate.png\" id=\"odk_aggregate_logo\" />"));
    RootPanel.get("dynamic_content").add(mainNav);
    contentLoaded();
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
      }
    };

    // Make the call to the form service.
    formSvc.getForms(callback);
  }
  
  /**
   * Update the list of forms
   * 
   * @param formSummary
   */
  private void updateFormTable(FormSummary [] forms) {
    for (int i = 0; i < forms.length; i++) {
      FormSummary form = forms[i];
      listOfForms.setWidget(i, 0, new Anchor(form.getTitle()));
      listOfForms.setWidget(i, 1, new HTML(form.getId()));
      listOfForms.setWidget(i, 2, new HTML(form.getCreatedUser()));

      listOfForms.setWidget(i, 3, enabledDropDown);
      listOfForms.setWidget(i, 4, publishDropDown);
      listOfForms.setWidget(i, 5, exportDropDown);
      listOfForms.setWidget(i, 6, new Button("Delete"));
      listOfForms.getRowFormatter().addStyleName(i, "table_data");
      if (i % 2 == 0)
        listOfForms.getRowFormatter().addStyleName(i, "even_table_row");
    }
  }
}
