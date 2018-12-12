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

package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.dom.client.ChangeHandler;
import java.util.ArrayList;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.constants.common.UIConsts;
import org.opendatakit.common.web.constants.BasicConsts;

public final class FilterListBox extends AggregateListBox {
  private static final String TOOLTIP_TEXT = "Filter to use";

  private static final String BALLOON_TEXT = "Select the filter group you want to work with.";

  private ArrayList<FilterGroup> displayedFilterList;

  public FilterListBox() {
    super(TOOLTIP_TEXT, false, BALLOON_TEXT);
  }

  public FilterListBox(ChangeHandler handler) {
    this();
    addChangeHandler(handler);
  }

  public FilterListBox(FilterGroup initiallySelectedGroup) {
    this();

    // verify we should proceed in creating the initial filter list based on
    // passed filter, if it's null no point
    if (initiallySelectedGroup == null) {
      return;
    }

    // create a default filter list
    FilterGroup defaultFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, initiallySelectedGroup.getFormId(),
        null);

    ArrayList<FilterGroup> filterGroups = new ArrayList<FilterGroup>();
    filterGroups.add(defaultFilterGroup);
    insertItem(defaultFilterGroup.getName(), 0);
    filterGroups.add(initiallySelectedGroup);
    insertItem(initiallySelectedGroup.getName(), 1);

    // update the panel to display the correct filter
    setItemSelected(1, true);
    displayedFilterList = filterGroups;
  }

  public ArrayList<FilterGroup> getDisplayedFilterList() {
    return displayedFilterList;
  }

  public void updateSelectedFilterAfterSave(FilterGroup filterGroup) {
    if (filterGroup == null) {
      return;
    }

    ArrayList<FilterGroup> tmp = new ArrayList<FilterGroup>();
    tmp.add(filterGroup);

    insertItem(filterGroup.getName(), 0);
    setItemSelected(0, true);

    displayedFilterList = tmp;
  }

  public void updateFilterDropDown(FilterSet filterSet) {
    FilterGroup currentFilterSelected = getSelectedFilter();

    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available

    // create what should be the new filter group
    ArrayList<FilterGroup> filterGroups = new ArrayList<FilterGroup>();
    if (filterSet != null) {
      FilterGroup defaultFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, filterSet.getFormId(),
          null);
      filterGroups.add(defaultFilterGroup);
      filterGroups.addAll(filterSet.getGroups());
    } else {
      // this case is for the UI to look pretty with NO FORM
      FilterGroup defaultFilterGroup = new FilterGroup(UIConsts.FILTER_NONE,
          BasicConsts.EMPTY_STRING, null);
      filterGroups.add(defaultFilterGroup);
    }

    clear();

    // populate the form box
    for (int i = 0; i < filterGroups.size(); i++) {
      FilterGroup filter = filterGroups.get(i);
      // TODO: currently name is the unique identifier for filter, maybe change
      // to avoid problems
      insertItem(filter.getName(), i);
      if (filter.equals(currentFilterSelected)) {
        selectedIndex = i;
      }
    }

    // set the displayed list before we set the item that was selected
    displayedFilterList = filterGroups;

    // update the panel to display the right filter
    setItemSelected(selectedIndex, true);
  }

  public FilterGroup getSelectedFilter() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex > -1 && displayedFilterList != null) {
      String filterName = getValue(selectedIndex);
      for (FilterGroup filterGroup : displayedFilterList) {
        String filterNameToMatch = filterGroup.getName();
        if (filterNameToMatch != null && filterNameToMatch.equals(filterName)) {
          return filterGroup;
        }
      }
    }
    // return null if the form is not found
    return null;
  }
}
