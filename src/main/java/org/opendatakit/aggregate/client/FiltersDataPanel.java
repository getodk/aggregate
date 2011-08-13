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

import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.ColumnFilterHeader;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.widgets.AddFilterButton;
import org.opendatakit.aggregate.client.widgets.CopyFilterGroupButton;
import org.opendatakit.aggregate.client.widgets.DeleteFilterButton;
import org.opendatakit.aggregate.client.widgets.RemoveFilterGroupButton;
import org.opendatakit.aggregate.client.widgets.SaveFilterGroupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class FiltersDataPanel extends ScrollPanel {

	private Tree filtersTree;
	private TreeItem root;
	private FilterSubTab parentSubTab;
	private AddFilterButton addFilter;

	public FiltersDataPanel(FilterSubTab panel) {
		this.parentSubTab = panel;
		getElement().setId("filters_container");

		// create tree
		filtersTree = new Tree();
		add(filtersTree);

		// create the root as the new filter button
		addFilter = new AddFilterButton(panel);   
		root = new TreeItem("No Filter Group");
		filtersTree.addItem(root);

	}

	public void update(FilterGroup group) {
		// clear the current filters being displayed so we don't get duplicates
		root.removeItems();

		// create filter header
		FlexTable filterHeader = new FlexTable();
		String filterName = group.getName();
		if(filterName.equals(UIConsts.FILTER_NONE)) {
			filterName = "No Filter Group";
		}
		root.setText(filterName);

		if(group.getFormId() == null) {
			return;
		}

		filterHeader.setWidget(0, 0, new SaveFilterGroupButton(parentSubTab));

		if (group.getName() != null) {
			if (!group.getName().equals(UIConsts.FILTER_NONE)) {
				filterHeader.setWidget(0, 1, new CopyFilterGroupButton(parentSubTab));
				filterHeader.setWidget(0, 2, new RemoveFilterGroupButton(parentSubTab));
			}
		}

		TreeItem currentFilterGroup = new TreeItem(filterHeader);

		// recreate filter list
		FlexTable filters = new FlexTable();
		int row = 0;
		for (Filter filter : group.getFilters()) {
			DeleteFilterButton removeFilter = new DeleteFilterButton(filter, parentSubTab);
			filters.setWidget(row, 0, removeFilter);
			if (filter instanceof RowFilter) {
				RowFilter rowFilter = (RowFilter) filter;
				filters.setWidget(row, 1,
						new Label(rowFilter.getVisibility() + rowFilter.getColumn().getDisplayHeader()));
				filters.setWidget(++row, 1, 
						new Label("where columns are " + rowFilter.getOperation() + rowFilter.getInput()));
			} else if (filter instanceof ColumnFilter) {
				ColumnFilter columnFilter = (ColumnFilter) filter;
				ArrayList<ColumnFilterHeader> columns = columnFilter.getColumnFilterHeaders();
				filters.setWidget(row, 1, new Label(""+columnFilter.getVisibility()));
				for (ColumnFilterHeader column : columns) {
					filters.setWidget(++row, 1, new Label(column.getColumn().getDisplayHeader()));
				}
			}
			row++;
		}
		currentFilterGroup.addItem(filters);
		currentFilterGroup.addItem(addFilter);
		currentFilterGroup.setState(true);

		// set system auto drops the information under root
		root.addItem(currentFilterGroup);
		root.setState(true);
	}
}
