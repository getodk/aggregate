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

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.table.FilterNavigationTable;
import org.opendatakit.aggregate.client.table.SubmissionTable;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.ListBox;

public class FilterSubTab extends AggregateSubTabBase {

	private FilterNavigationTable navTable;
	private HorizontalPanel filtersNSubmissions;

	private FiltersDataPanel filtersPanel;
	private SubmissionPanel submissionPanel;
	private FilterGroup currentlyDisplayedFilterGroup;
	private FormSummary form;

	public FilterSubTab() {
		getElement().setId("filter_sub_tab");
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

	public void switchForm(FormSummary formSwitch, FilterGroup filterGroup) {
		form = formSwitch;
		navTable.updateNavTable(form, filterGroup);
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

	public void removeFilterGroupWithinForm() {
		currentlyDisplayedFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, form.getId(), null);
		navTable.updateNavTable(form, currentlyDisplayedFilterGroup);
		navTable.update();
		filtersPanel.update(currentlyDisplayedFilterGroup);
		submissionPanel.update(currentlyDisplayedFilterGroup);
	}


	@Override
	public boolean canLeave() {
		return true;
	}

	@Override
	public void update() {
		navTable.update();
		if(form != null) {
			navTable.updateNavTable(form, currentlyDisplayedFilterGroup);
		}
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
