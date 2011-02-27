package org.opendatakit.aggregate.client;

import com.google.gwt.core.client.EntryPoint;
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
	private TabPanel mainNav = new TabPanel();
	
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
		
		FlexTable formManagementTable = new FlexTable();
		formManagementTable.setText(0, 0, "Title");
		formManagementTable.setText(0, 1, "Form Id");
		formManagementTable.setText(0, 2, "User");
		formManagementTable.setText(0, 3, "Enabled");
		formManagementTable.setText(0, 4, "Publish");
		formManagementTable.setText(0, 5, "Export");
		formManagementTable.setText(0, 6, "Delete");
		formManagementTable.getRowFormatter().addStyleName(0, "title_bar");
		for (int i = 1; i <= 10; i++) {
			formManagementTable.setWidget(i, 0, new Anchor("Form" + i));
			formManagementTable.setWidget(i, 1, new HTML("Id" + i));
			formManagementTable.setWidget(i, 2, new HTML("User" + i));
			
			ListBox enabledDropDown = new ListBox();
			enabledDropDown.addItem("Disabled/Inactive");
			enabledDropDown.addItem("Disabled/Active");
			enabledDropDown.addItem("Enabled/Active");
			formManagementTable.setWidget(i, 3, enabledDropDown);
			
			ListBox publishDropDown = new ListBox();
			publishDropDown.addItem("Google FusionTables");
			publishDropDown.addItem("Google Spreadsheet");
			publishDropDown.addItem("Rhiza Insight");
			formManagementTable.setWidget(i, 4, publishDropDown);
			
			ListBox exportDropDown = new ListBox();
			exportDropDown.addItem("CSV");
			exportDropDown.addItem("KML");
			exportDropDown.addItem("XML");
			formManagementTable.setWidget(i, 5, exportDropDown);
			
			formManagementTable.setWidget(i, 6, new Button("Delete"));
			formManagementTable.getRowFormatter().addStyleName(i, "table_data");
			if (i % 2 == 0)
				formManagementTable.getRowFormatter().addStyleName(i, "even_table_row");
		}
		formManagementTable.getElement().setId("form_management_table");
		
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
		mainNav.getTabBar().getElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().getFirstChildElement().setId("spacer_tab");
		
		RootPanel.get("dynamic_content").add(mainNav);
	}
}
