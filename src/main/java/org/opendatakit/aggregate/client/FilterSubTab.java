package org.opendatakit.aggregate.client;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.FilterNavigationTable;
import org.opendatakit.aggregate.client.table.SubmissionTable;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class FilterSubTab extends VerticalPanel implements SubTabInterface {
 
  private FilterNavigationTable navTable;
  private HorizontalPanel filtersNSubmissions;

  private FiltersDataPanel filtersPanel;
  private SubmissionPanel submissionPanel;
  private FilterGroup currentlyDisplayedFilterGroup;
  
  public FilterSubTab() {
    // create Nav Panel
    navTable = new FilterNavigationTable(this);
    navTable.getElement().setId("submission_nav_table");
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
    currentlyDisplayedFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, null, null);
  }
  
  public void switchForm(FormSummary form, FilterGroup filterGroup) {
    navTable.updateNavTable(form);
    currentlyDisplayedFilterGroup = filterGroup;
    filtersPanel.update(currentlyDisplayedFilterGroup);
    submissionPanel.update(currentlyDisplayedFilterGroup);
  }

  public void switchFilterGroupWithinForm(FilterGroup filterGroup) {
    // verify form remained the same, if not need to use switch form API
    if(!currentlyDisplayedFilterGroup.getFormId().equals(filterGroup.getFormId())) {
      return;
    }
    currentlyDisplayedFilterGroup = filterGroup;
    filtersPanel.update(currentlyDisplayedFilterGroup);
    submissionPanel.update(currentlyDisplayedFilterGroup);
  }
  
  public void update() {
    navTable.update();
    filtersPanel.update(currentlyDisplayedFilterGroup);
    submissionPanel.update(currentlyDisplayedFilterGroup);
  }
  
  public FilterGroup getDisplayedFilterGroup() {
    return currentlyDisplayedFilterGroup;
  }

  public ListBox getListOfPossibleFilterGroups() {
    return navTable.getCurrentFilterList();
  }
  
  public SubmissionTable getSubmissionTable() {
    return submissionPanel.getSubmissionTable();
  }
}
