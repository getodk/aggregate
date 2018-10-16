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

import static org.opendatakit.aggregate.client.LayoutUtils.*;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.VerticalPanel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.opendatakit.aggregate.client.filter.ColumnFilter;
import org.opendatakit.aggregate.client.filter.Filter;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.RowFilter;
import org.opendatakit.aggregate.client.submission.Column;
import org.opendatakit.aggregate.client.widgets.AddFilterButton;
import org.opendatakit.aggregate.client.widgets.DeleteFilterButton;
import org.opendatakit.aggregate.client.widgets.MetadataCheckBox;
import org.opendatakit.aggregate.client.widgets.PaginationNumTextBox;
import org.opendatakit.aggregate.client.widgets.RemoveFilterGroupButton;
import org.opendatakit.aggregate.client.widgets.SaveAsFilterGroupButton;
import org.opendatakit.aggregate.client.widgets.SaveFilterGroupButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

public class FiltersDataPanel extends ScrollPanel {

  private Tree filtersTree;
  private FilterSubTab parentSubTab;

  private AddFilterButton addFilter;
  private SaveAsFilterGroupButton copyButton;
  private RemoveFilterGroupButton removeButton;

  private FlowPanel filterHeader;

  private FilterGroup previousGroup;
  private String previousName;

  public FiltersDataPanel(FilterSubTab parentPanel) {
    this.parentSubTab = parentPanel;
    getElement().setId("filters_container");

    FlowPanel panel = new FlowPanel();

    FlexTable filterGroupButtons = new FlexTable();
    filterGroupButtons.setWidget(0, 0, new SaveFilterGroupButton(parentSubTab));
    copyButton = new SaveAsFilterGroupButton(parentSubTab);
    filterGroupButtons.setWidget(0, 1, copyButton);
    removeButton = new RemoveFilterGroupButton(parentSubTab);
    filterGroupButtons.setWidget(0, 2, removeButton);
    panel.add(filterGroupButtons);

    HTML filterText = new HTML("<h2 id=\"filter_header\">Filters Applied</h2>");
    filterText.getElement().setId("filter_desc_title");

    VerticalPanel filterGlobal = new VerticalPanel();
    FlexTable paginationTable = new FlexTable();
    paginationTable.setHTML(0, 0, "<p id=\"filter_header\">Submissions per page</p>");
    paginationTable.setWidget(0, 1, new PaginationNumTextBox(parentSubTab));
    filterGlobal.add(paginationTable);
    filterGlobal.add(filterText);
    panel.add(filterGlobal);

    // Filters applied header
    filterHeader = new FlowPanel();
    panel.add(filterHeader);

    // create tree
    filtersTree = new Tree();
    panel.add(filtersTree);

    panel.add(buildVersionNote());

    // create the root as the new filter button
    addFilter = new AddFilterButton(parentPanel);

    add(panel);
  }

  public void update(FilterGroup group) {
    // check if filter group has changed
    if (!group.equals(previousGroup)) {
      previousGroup = group;
      previousName = group.getName();

      // if new form clear everything so we can regenerate data
      filtersTree.removeItems();
      filterHeader.clear();

      // verify form exists, then update
      if (group.getFormId() == null) {
        return;
      }

      // update filter header
      updateFilterHeader(group);

      // create filter list
      for (Filter filter : group.getFilters()) {
        TreeItem filterItem = createFilterTreeItem(filter);
        filterItem.setState(true);
        filtersTree.addItem(filterItem);
      }

    } else {
      // only the filters need to be refreshed (unless a save or save as
      // happens)
      String name = group.getName();

      // if save/saveAs happen the UI needs to detect the change and update the
      // filter header information
      // Note: the group.equals(previousGorup) check does not catch this case
      // as the filterGroup's name will update in the reference do it does not
      // detect the difference with the change in the global object. To detect
      // the change keeping a local copy of name to compare with
      if (name != null) {
        // check for name change
        if (!name.equals(previousName)) {
          previousName = name;
          filterHeader.clear();

          if (group.getFormId() == null) {
            return;
          }
          updateFilterHeader(group);
        }
      }

      // find if any changes in filters exist, then put them back to ensure
      // filters don't jump around
      Map<Filter, TreeItem> filterMap = new HashMap<Filter, TreeItem>();
      for (int i = 0; i < filtersTree.getItemCount(); i++) {
        TreeItem filterTreeItem = filtersTree.getItem(i);
        Object obj = filterTreeItem.getUserObject();
        if (obj instanceof Filter) {
          filterMap.put((Filter) obj, filterTreeItem);
        }
      }

      filtersTree.removeItems();

      // update filter list
      for (Filter filter : group.getFilters()) {
        TreeItem filterItem = createFilterTreeItem(filter);

        // get state from previous filter list
        TreeItem oldFilterItem = filterMap.get(filter);
        if (oldFilterItem != null) {
          filterItem.setState(oldFilterItem.getState());
        }

        filtersTree.addItem(filterItem);
      }
    }

  }

  private void updateFilterHeader(FilterGroup group) {
    copyButton.setEnabled(false);
    removeButton.setEnabled(false);

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
    filterHeader.add(new MetadataCheckBox(group, parentSubTab));
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
      title.setWidget(1, 1, new Label(" where column is " + rowFilter.getOperation() + " "
          + rowFilter.getInput()));
    } else if (filter instanceof ColumnFilter) {
      ColumnFilter columnFilter = (ColumnFilter) filter;
      ArrayList<Column> columns = columnFilter.getColumnFilterHeaders();
      if (columns.size() == 1) {
        title.setWidget(0, 1, new Label(columnFilter.getVisibility().getDisplayText() + " "
            + columns.get(0).getDisplayHeader()));
      } else {
        title.setWidget(0, 1, new Label(columnFilter.getVisibility().getDisplayText() + " ..."));
        for (Column column : columns) {
          filterItem.addItem(new Label(column.getDisplayHeader()));
        }
      }
    }
    return filterItem;
  }

}
