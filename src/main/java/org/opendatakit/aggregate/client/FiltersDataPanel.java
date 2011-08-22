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
import java.util.HashMap;
import java.util.Map;

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
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;

public class FiltersDataPanel extends ScrollPanel {

  private Tree filtersTree;
  private FilterSubTab parentSubTab;
  private AddFilterButton addFilter;

  private FlowPanel panel;

  private SimplePanel metadataPanel;
  private FlexTable filterGroupButtons;
  private SimplePanel filterHeader;
  
  private FilterGroup previousGroup;

  public FiltersDataPanel(FilterSubTab parentPanel) {
    this.parentSubTab = parentPanel;
    getElement().setId("filters_container");

    panel = new FlowPanel();
    
    metadataPanel = new SimplePanel();
    panel.add(metadataPanel);

    // create filter header
    filterGroupButtons = new FlexTable();
    panel.add(filterGroupButtons);

    filterHeader = new SimplePanel();
    panel.add(filterHeader);
    
    // create tree
    filtersTree = new Tree();
    panel.add(filtersTree);

    // create the root as the new filter button
    addFilter = new AddFilterButton(parentPanel);

    add(panel);
  }

  public void update(FilterGroup group) {
    // check if filter group has changed
    if (!group.equals(previousGroup)) {
      previousGroup = group;
      
      // if new form clear everything so we can regenerate data
      filterGroupButtons.clear();
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

      filterGroupButtons.setWidget(0, 0, new SaveFilterGroupButton(parentSubTab));
      filterGroupButtons.setWidget(0, 1, copyButton);
      filterGroupButtons.setWidget(0, 2, removeButton);

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

      filterHeader.add(filterGroupHeader);
      
      // create filter list
      for (Filter filter : group.getFilters()) {
        TreeItem filterItem = createFilterTreeItem(filter);
        filterItem.setState(true);
        filtersTree.addItem(filterItem);
      }
      
    } else {   // only the filters need to be refreshed
      
      // find if any changes in filters exist
      Map<Filter, TreeItem> filterMap = new HashMap<Filter, TreeItem>();
      for (int i=0; i < filtersTree.getItemCount(); i++) {
        TreeItem filterTreeItem = filtersTree.getItem(i);
        Object obj = filterTreeItem.getUserObject();
        if(obj instanceof Filter) {
          filterMap.put((Filter)obj, filterTreeItem);
        }
      }
      
      filtersTree.removeItems();
      
      // update filter list
      for (Filter filter : group.getFilters()) {
        TreeItem filterItem = createFilterTreeItem(filter);
        
        // get state from previous filter list
        TreeItem oldFilterItem = filterMap.get(filter);
        if(oldFilterItem != null) {
          filterItem.setState(oldFilterItem.getState());
        }
        
        filtersTree.addItem(filterItem);
      }
    }

  }

  private TreeItem createFilterTreeItem(Filter filter) {
    FlexTable title = new FlexTable();
    title.setWidget(0, 0, new DeleteFilterButton(filter, parentSubTab));

    TreeItem filterItem = new TreeItem(title);
    filterItem.setUserObject(filter);
    filterItem.setState(true);
    
    if (filter instanceof RowFilter) {
      RowFilter rowFilter = (RowFilter) filter;
      title.setWidget(0, 1, new Label(rowFilter.getVisibility()
          + rowFilter.getColumn().getDisplayHeader()));
      title.setWidget(1, 1,
          new Label("where column is " + rowFilter.getOperation() + rowFilter.getInput()));
    } else if (filter instanceof ColumnFilter) {
      ColumnFilter columnFilter = (ColumnFilter) filter;
      ArrayList<ColumnFilterHeader> columns = columnFilter.getColumnFilterHeaders();
      title.setWidget(0, 1, new Label(columnFilter.getVisibility().getDisplayText()));
      for (ColumnFilterHeader column : columns) {
        filterItem.addItem(new Label(column.getColumn().getDisplayHeader()));
      }
    }
    return filterItem;
  }


}
