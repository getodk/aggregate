package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.FilterNavigationTable;
import org.opendatakit.aggregate.client.table.SubmissionTable;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FilterSubTab extends VerticalPanel implements SubTabInterface {
 
  private FilterNavigationTable navTable;
  private HorizontalPanel filtersNSubmissions;

  private FiltersDataPanel filtersPanel;
  private SubmissionPanel submissionPanel;
  private FilterGroup currentFilterToDisplay;
  
  public FilterSubTab() {
    // create Nav Panel
    navTable = new FilterNavigationTable(this);
    add(navTable);
    
    // Create Filters ande Submissions Panel
    filtersNSubmissions = new HorizontalPanel();
    
    filtersPanel = new FiltersDataPanel(this);
    filtersNSubmissions.add(filtersPanel);
    
    submissionPanel = new SubmissionPanel();
    filtersNSubmissions.add(submissionPanel);
    
    filtersNSubmissions.getElement().setId("filters_data");
    filtersNSubmissions.getElement().getFirstChildElement().getFirstChildElement().getFirstChildElement()
        .setId("filters_panel");
    
    filtersNSubmissions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);
    
    add(filtersNSubmissions);
    currentFilterToDisplay = new FilterGroup("Default", null, null);
  }
  
  public void switchForm(FormSummary form, FilterGroup filterGroup) {
    navTable.updateNavTable(form);
    currentFilterToDisplay = filterGroup;
    submissionPanel.update(filterGroup);
  }

  public void update() {
    navTable.update();
    submissionPanel.update(currentFilterToDisplay);
  }
  
  public FilterGroup getCurrentlyDisplayedFilter() {
    return currentFilterToDisplay;
  }

  public ListBox getCurrentFilterList() {
    return navTable.getCurrentFilterList();
  }
  
  public SubmissionTable getSubmissionTable() {
    return submissionPanel.getSubmissionTable();
  }
}
