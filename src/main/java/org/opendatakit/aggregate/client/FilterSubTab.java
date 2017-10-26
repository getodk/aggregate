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

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.table.FilterNavigationTable;
import org.opendatakit.aggregate.client.table.SubmissionTable;
import org.opendatakit.aggregate.constants.common.FilterConsts;
import org.opendatakit.aggregate.constants.common.HelpSliderConsts;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.security.common.GrantedAuthorityName;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class FilterSubTab extends AggregateSubTabBase {

  public static final int DEFAULT_FETCH_LIMIT = 100;

  private FilterNavigationTable navTable;

  private FiltersDataPanel filtersPanel;
  private SubmissionPanel submissionPanel;

  private FilterGroup currentlyDisplayedFilterGroup;

  public FilterSubTab() {
    // vertical
    setStylePrimaryName(UIConsts.VERTICAL_FLOW_PANEL_STYLENAME);

    getElement().setId("filter_sub_tab");

    // create Nav Panel
    navTable = new FilterNavigationTable(this);
    navTable.getElement().setId("submission_nav_table");
    add(navTable);

    // Create Filters and Submissions Panel
    HorizontalPanel filtersNSubmissions = new HorizontalPanel();

    filtersPanel = new FiltersDataPanel(this);
    filtersNSubmissions.add(filtersPanel);
    filtersNSubmissions.getElement().getFirstChildElement().getFirstChildElement()
    .getFirstChildElement().setId("filters_panel"); // TODO: improve this

    submissionPanel = new SubmissionPanel();

    filtersNSubmissions.add(submissionPanel);
    filtersNSubmissions.getElement().setId("filters_data");
    filtersNSubmissions.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_JUSTIFY);

    add(filtersNSubmissions);
  }

  private void setCurrentlyDisplayedFilterGroup(FilterGroup newFilterGroup) {
    // preserve the fetch limit as we switch filters...
    newFilterGroup.setQueryFetchLimit(getQueryFetchLimit());
    currentlyDisplayedFilterGroup = newFilterGroup;
    navTable.updateNavTable(newFilterGroup);
  }

  public FilterGroup getDisplayedFilterGroup() {
    if(currentlyDisplayedFilterGroup == null) {
      return new FilterGroup(UIConsts.FILTER_NONE, null, null);
    }
    return currentlyDisplayedFilterGroup;
  }

  public void updateAfterSave(FilterGroup filterGroup) {
    currentlyDisplayedFilterGroup = filterGroup;
    navTable.updateNavAfterSave(filterGroup);
    update();
  }

  public void switchFilterGroup(FilterGroup filterGroup) {
    // check if filter group is changed, if the same no need to do anything
    if(getDisplayedFilterGroup().equals(filterGroup)) {
      return;
    }
    setCurrentlyDisplayedFilterGroup(filterGroup);
    update();
  }

  public void removeFilterGroupWithinForm() {
    String formId = getDisplayedFilterGroup().getFormId();
    FilterGroup blankFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, formId, null);
    setCurrentlyDisplayedFilterGroup(blankFilterGroup);
    update();
  }

  @Override
  public boolean canLeave() {
    return true;
  }

  @Override
  public void update() {
    if (AggregateUI.getUI().getUserInfo().getGrantedAuthorities()
            .contains(GrantedAuthorityName.ROLE_DATA_VIEWER)) {
		navTable.update();
		
		FilterGroup filterGroup = getDisplayedFilterGroup();
		filtersPanel.update(filterGroup);
		submissionPanel.update(filterGroup);
    }
  }

  public ArrayList<FilterGroup> getListOfPossibleFilterGroups() {
    return navTable.getCurrentFilters();
  }

  public SubmissionTable getSubmissionTable() {
    return submissionPanel.getSubmissionTable();
  }

  public void setDisplayMetaData(Boolean displayMetaData) {
    this.currentlyDisplayedFilterGroup.setIncludeMetadata(displayMetaData);
  }

  public int getQueryFetchLimit() {
    if (this.currentlyDisplayedFilterGroup != null) {
      return this.currentlyDisplayedFilterGroup.getQueryFetchLimit();
    }
    return FilterGroup.DEFAULT_FETCH_LIMIT;
  }

  public void setQueryFetchLimit(int fetchLimit) {
    this.currentlyDisplayedFilterGroup.setQueryFetchLimit(fetchLimit);
  }

  @Override
  public HelpSliderConsts[] getHelpSliderContent() {
    return FilterConsts.values();
  }
}
