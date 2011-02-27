package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.form.FormService;
import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.FormSummary;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
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
  
  private TabPanel mainNav = new TabPanel();

  private FormServiceAsync formSvc;
  private ListBox enabledDropDown;
  private ListBox publishDropDown;
  private ListBox exportDropDown;
  private FlexTable formManagementTable;
  
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
    
    formManagementTable = new FlexTable();
    

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
    // select data to display
    FlexTable formAndGoalSelectionTable = new FlexTable();
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
    HorizontalPanel filtersDataHelp = new HorizontalPanel();
    FlexTable filtersTable = new FlexTable();
    filtersTable.setWidget(0, 0, new Button("Filter1"));
    filtersTable.setWidget(0, 1, new Button("-"));
    filtersTable.setWidget(1, 0, new Button("Filter2"));
    filtersTable.setWidget(1, 1, new Button("-"));
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
    dataTable.setStyleName("filters_data_help_cell");
    filtersDataHelp.add(dataTable);

    // view help
    VerticalPanel helpPanel = new VerticalPanel();
    for (int i = 1; i < 5; i++) {
      helpPanel.add(new HTML("Help Content " + i));
    }
    helpPanel.setStyleName("filters_data_help_cell");
    filtersDataHelp.add(helpPanel);
    filtersDataHelp.getElement().setId("filters_data_help");
    filtersDataHelp.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
    return filtersDataHelp;
  }

  public VerticalPanel setupFormManagementPanel() {
    Button uploadFormButton = new Button("Upload Form");
    
    formManagementTable.setText(0, 0, "Title");
    formManagementTable.setText(0, 1, "Form Id");
    formManagementTable.setText(0, 2, "User");
    formManagementTable.setText(0, 3, "Enabled");
    formManagementTable.setText(0, 4, "Publish");
    formManagementTable.setText(0, 5, "Export");
    formManagementTable.setText(0, 6, "Delete");
    formManagementTable.getRowFormatter().addStyleName(0, "title_bar");
    formManagementTable.getElement().setId("form_management_table");
    
    getFormList();

    VerticalPanel formManagementPanel = new VerticalPanel();
    formManagementPanel.add(uploadFormButton);
    formManagementPanel.add(formManagementTable);
    return formManagementPanel;
  }

  public void onModuleLoad() {
    VerticalPanel reportContent = new VerticalPanel();
    reportContent.add(setupFormsAndGoalsPanel());
    reportContent.add(setupFiltersDataHelpPanel());

    VerticalPanel manageContent = new VerticalPanel();
    manageContent.add(setupFormManagementPanel());

    mainNav.add(reportContent, "Report");
    mainNav.add(manageContent, "Manage");
    mainNav.selectTab(0);

    mainNav.getTabBar().addStyleName("mainNav");
    mainNav.getTabBar().getElement().getFirstChildElement().getFirstChildElement()
        .getFirstChildElement().getFirstChildElement().setId("spacer_tab");

    RootPanel.get("dynamic_content").add(mainNav);
  }

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
      formManagementTable.setWidget(i, 0, new Anchor(form.getTitle()));
      formManagementTable.setWidget(i, 1, new HTML(form.getId()));
      formManagementTable.setWidget(i, 2, new HTML(form.getCreatedUser()));

      formManagementTable.setWidget(i, 3, enabledDropDown);
      formManagementTable.setWidget(i, 4, publishDropDown);
      formManagementTable.setWidget(i, 5, exportDropDown);
      formManagementTable.setWidget(i, 6, new Button("Delete"));
      formManagementTable.getRowFormatter().addStyleName(i, "table_data");
      if (i % 2 == 0)
        formManagementTable.getRowFormatter().addStyleName(i, "even_table_row");
    }
  }
}
