package org.opendatakit.aggregate.client;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.ui.ListBox;

public class UIUtils {
  
  private static final String NO_FORM = "none";
  
  public static FormSummary [] updateFormDropDown(ListBox formsBox, FormSummary[] previouslyDisplayedForms, FormSummary[] newFormsToDisplay ) {
    
    FormSummary currentFormSelected = UIUtils.getFormFromSelection(formsBox, previouslyDisplayedForms);
    
    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available
    
    if (newFormsToDisplay == null || newFormsToDisplay.length == 0) {
      newFormsToDisplay = new FormSummary[1];
      newFormsToDisplay[0] = new FormSummary(NO_FORM, null, null, false, false, null);
    } 

    formsBox.clear();
    
    // populate the form box
    for (int i = 0; i < newFormsToDisplay.length; i++) {
      FormSummary form = newFormsToDisplay[i];
      formsBox.addItem(form.getTitle(), form.getId());
      if (form.equals(currentFormSelected)) {
        selectedIndex = i;
      }
    }

    // update the panel to display the right form
    formsBox.setItemSelected(selectedIndex, true);
    
    // return the new populated form list used to update the formsBox
    return newFormsToDisplay;
  }
  
  public static FormSummary getFormFromSelection(ListBox formsListBox, FormSummary [] forms) {
    int selectedIndex = formsListBox.getSelectedIndex();
    if (selectedIndex > -1 && forms != null) {
      String formId = formsListBox.getValue(selectedIndex);
      for (FormSummary form : forms) {
        String formIdToMatch = form.getId();
        // check if we have a form id
        if(formIdToMatch == null) {
          // if there is no formId this should be the 'NO_FORM' form
          if(form.getTitle() != null && form.getTitle().equals(NO_FORM) && formId.equals("")) {
            return null;
          } else {
            throw new IllegalStateException("Some how a form that is not the 'NO_FORM' had a formId of null");
          }
        } else if(formIdToMatch.equals(formId)) { // check if the formId from listbox matches the form
          return form;
        }
      }
    }
    // return null if the form is not found
    return null;
  }
   
  public static List<FilterGroup> updateFilterDropDown(ListBox filtersBox, FormSummary selectedForm, List<FilterGroup> previouslyDisplayedFilter, FilterSet filterSet) {
    FilterGroup currentFilterSelected = getFilterFromSelection(filtersBox, previouslyDisplayedFilter);
    FilterGroup defaultFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, selectedForm.getId(), null);
    
    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available
    
    // create what should be the new filter group
    List<FilterGroup> filterGroups  = new ArrayList<FilterGroup>();
    filterGroups.add(defaultFilterGroup);
    filterGroups.addAll(filterSet.getGroups());

    filtersBox.clear();
    
    // populate the form box
    for (int i = 0; i < filterGroups.size(); i++) {
      FilterGroup filter = filterGroups.get(i);
      // TODO: currently name is the unique identifier for filter, maybe change to avoid problems
      filtersBox.insertItem(filter.getName(), i);
      if (filter.equals(currentFilterSelected)) {
        selectedIndex = i;
      }
    }

    // update the panel to display the right filter
    filtersBox.setItemSelected(selectedIndex, true);
    
    return filterGroups;
  }
  
  public static FilterGroup getFilterFromSelection(ListBox filterListBox, List<FilterGroup> filters) {
    int selectedIndex = filterListBox.getSelectedIndex();
    if (selectedIndex > -1 && filters != null) {
      String filterName = filterListBox.getValue(selectedIndex);
      for (FilterGroup filterGroup : filters) {
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
