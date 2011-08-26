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
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.user.client.ui.ListBox;

public class UIUtils {
  
  private static final String NO_FORM = "none";
  
  public static ArrayList<FormSummary> updateFormDropDown(ListBox formsBox, ArrayList<FormSummary> previouslyDisplayedForms, ArrayList<FormSummary> forms ) {
    
    FormSummary currentFormSelected = UIUtils.getFormFromSelection(formsBox, previouslyDisplayedForms);
    
    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available
    
    if (forms == null || forms.size() == 0) {
      forms = new ArrayList<FormSummary>();
      forms.add(new FormSummary(NO_FORM, UIConsts.EMPTY_STRING, null, null, false, false, null, 0));
    } 

    formsBox.clear();
    
    // populate the form box
    for (int i = 0; i < forms.size(); i++) {
      FormSummary form = forms.get(i);
      // don't show forms marked for deletion...
      FormActionStatusTimestamp deletionStatus = form.getMostRecentDeletionRequestStatus();
      if ( deletionStatus != null && deletionStatus.getStatus().isActiveRequest()) continue;
      
      formsBox.addItem(form.getTitle(), form.getId());
      if (form.equals(currentFormSelected)) {
        selectedIndex = formsBox.getItemCount()-1;
      }
    }

    if ( formsBox.getItemCount() > 0 ) {
	    // update the panel to display the right form
	    formsBox.setItemSelected(selectedIndex, true);
    }
    
    // return the new populated form list used to update the formsBox
    return forms;
  }
  
  public static FormSummary getFormFromSelection(ListBox formsListBox, ArrayList<FormSummary> displayedFormList) {
    int selectedIndex = formsListBox.getSelectedIndex();
    if (selectedIndex > -1 && displayedFormList != null) {
      String formId = formsListBox.getValue(selectedIndex);
      for (FormSummary form : displayedFormList) {
        String formIdToMatch = form.getId();
        // check if we have a form id
        if(formIdToMatch == null) {
          // if there is no formId this should be the 'NO_FORM' form
          if(form.getTitle() != null && form.getTitle().equals(NO_FORM) && formId.equals(UIConsts.EMPTY_STRING)) {
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
   
  public static ArrayList<FilterGroup> updateFilterDropDown(ListBox filtersBox, FormSummary selectedForm, ArrayList<FilterGroup> previouslyDisplayedFilter, FilterSet filterSet) {
    FilterGroup currentFilterSelected = getFilterFromSelection(filtersBox, previouslyDisplayedFilter);
    FilterGroup defaultFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, selectedForm.getId(), null);
    
    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available
    
    // create what should be the new filter group
    ArrayList<FilterGroup> filterGroups  = new ArrayList<FilterGroup>();
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
  
  public static FilterGroup getFilterFromSelection(ListBox filterListBox, ArrayList<FilterGroup> filters) {
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
