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
import org.opendatakit.aggregate.client.widgets.MetadataCheckBox;
import org.opendatakit.aggregate.client.widgets.RemoveFilterGroupButton;
import org.opendatakit.aggregate.client.widgets.SaveFilterGroupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class FiltersDataPanel extends ScrollPanel {

  private Tree filtersTree;
  private FilterSubTab parentSubTab;
  private AddFilterButton addFilter;
  
  private FlowPanel panel;

  private FlowPanel metadataPanel;
  private FlexTable filterHeader;

  
  public FiltersDataPanel(FilterSubTab parentPanel) {
    this.parentSubTab = parentPanel;
    getElement().setId("filters_container");

    panel = new FlowPanel();
    metadataPanel = new FlowPanel();
    
    panel.add(metadataPanel);
    
    // create filter header
    filterHeader = new FlexTable();
    panel.add(filterHeader);

    // create tree
    filtersTree = new Tree();
    panel.add(filtersTree);

    // create the root as the new filter button
    addFilter = new AddFilterButton(parentPanel);

    add(panel);
  }

  public void update(FilterGroup group) {
    // clear the current filters being displayed so we don't get duplicates
    filterHeader.clear();
    filtersTree.removeItems();
    metadataPanel.clear();
    
    if (group.getFormId() == null) {
      return;
    }
    
    // set the header information
    
    metadataPanel.add(new MetadataCheckBox(parentSubTab));
    
    CopyFilterGroupButton copyButton = new CopyFilterGroupButton(parentSubTab);
    copyButton.setEnabled(false);
    
    RemoveFilterGroupButton removeButton = new RemoveFilterGroupButton(parentSubTab);
    removeButton.setEnabled(false);
    
    filterHeader.setWidget(0, 0, new SaveFilterGroupButton(parentSubTab));
    filterHeader.setWidget(0, 1, copyButton);
    filterHeader.setWidget(0, 2, removeButton);
    
    if (group.getName() != null) {
      if (!group.getName().equals(UIConsts.FILTER_NONE)) {
        copyButton.setEnabled(true);
        removeButton.setEnabled(true);
      }
    }

    // create the filter group information
    
    String filterName = group.getName();
    if (filterName.equals(UIConsts.FILTER_NONE)) {
      filterName = "";
    }
    
    FlexTable filterGroupHeader = new FlexTable();    
    filterGroupHeader.setWidget(0, 0, new Label(filterName));
    filterGroupHeader.setWidget(0, 1, addFilter);

    TreeItem currentFilterGroup = new TreeItem(filterGroupHeader);

    // recreate filter list
    int row = 0;
    for (Filter filter : group.getFilters()) {
      FlexTable title = new FlexTable();
      title.setWidget(0, 0, new DeleteFilterButton(filter, parentSubTab));

      TreeItem filterItem = new TreeItem(title);
      if (filter instanceof RowFilter) {
        RowFilter rowFilter = (RowFilter) filter;
        title.setWidget(0, 1, new Label(rowFilter.getVisibility()
            + rowFilter.getColumn().getDisplayHeader()));
        title.setWidget(1, 1,
            new Label("where column is " + rowFilter.getOperation() + rowFilter.getInput()));
      } else if (filter instanceof ColumnFilter) {
        ColumnFilter columnFilter = (ColumnFilter) filter;
        ArrayList<ColumnFilterHeader> columns = columnFilter.getColumnFilterHeaders();
        title.setWidget(row, 1, new Label(columnFilter.getVisibility().getDisplayText()));
        for (ColumnFilterHeader column : columns) {
          filterItem.addItem(new Label(column.getColumn().getDisplayHeader()));
        }
      }
      currentFilterGroup.addItem(filterItem);
    }
    currentFilterGroup.setState(true);
    filtersTree.addItem(currentFilterGroup);

  }
}
