package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ChangeHandler;

public class FilterListBox extends AbstractListBox {
  private static final String TOOLTIP_TEXT = "Filter to use";

  private ArrayList<FilterGroup> displayedFilterList;
  
  public FilterListBox() {
    super(TOOLTIP_TEXT, false);
  }
  
  public FilterListBox(ChangeHandler handler) {
    this();
    addChangeHandler(handler);
  }
  
  public ArrayList<FilterGroup> getDisplayedFilterList() {
    return displayedFilterList;
  }
  
  public void updateFilterDropDown(FilterSet filterSet) {
    FilterGroup currentFilterSelected = getSelectedFilter();
    FilterGroup defaultFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, filterSet.getFormId(), null);
    
    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available
    
    // create what should be the new filter group
    ArrayList<FilterGroup> filterGroups  = new ArrayList<FilterGroup>();
    filterGroups.add(defaultFilterGroup);
    filterGroups.addAll(filterSet.getGroups());

    clear();
    
    // populate the form box
    for (int i = 0; i < filterGroups.size(); i++) {
      FilterGroup filter = filterGroups.get(i);
      // TODO: currently name is the unique identifier for filter, maybe change to avoid problems
      insertItem(filter.getName(), i);
      if (filter.equals(currentFilterSelected)) {
        selectedIndex = i;
      }
    }

    // update the panel to display the right filter
    setItemSelected(selectedIndex, true);
    
    displayedFilterList = filterGroups;
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
