package org.opendatakit.aggregate.client.table;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.widgets.FetchFormButton;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.ListBox;

// TODO: address possible inconsistent states

public class FormNFilterSelectionTable extends FlexTable {
  // ui elements
  private ListBox formsBox;
  private ListBox filtersBox;
  private FilterSubTab filterSubTab;

  // state
  private FormSummary[] displayedFormList;
  private FormSummary selectedForm;
  private List<FilterGroup> displayedFilterList;

  public FormNFilterSelectionTable(FilterSubTab filterSubTab) {
    this.filterSubTab = filterSubTab;

    formsBox = new ListBox();
    formsBox.addChangeHandler(new ChangeDropDownHandler());
    filtersBox = new ListBox();

    getElement().setId("form_and_goal_selection");
    setWidget(0, 0, formsBox);
    setWidget(0, 1, filtersBox);

    FetchFormButton loadFormAndFilterButton = new FetchFormButton(this);
    setWidget(0, 2, loadFormAndFilterButton);
  }

  public void update() {
    // Set up the callback object.
    AsyncCallback<FormSummary[]> callback = new AsyncCallback<FormSummary[]>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(FormSummary[] forms) {
        AggregateUI.getUI().clearError();
        updateFormDropDown(forms);
      }
    };

    // Make the call to the form service.
    SecureGWT.getFormService().getForms(callback);
  }

  public void fetchClicked() {
    FormSummary form = displayedFormList[formsBox.getSelectedIndex()];
    FilterGroup filterGroup = displayedFilterList.get(filtersBox.getSelectedIndex());
    filterSubTab.switchForm(form, filterGroup);
  }

  private synchronized void updateFormDropDown(FormSummary[] formsFromService) {

    FormSummary currentFormSelected = null;

    FormSummary[] forms = formsFromService;

    if (forms == null || forms.length == 0) {
      forms = new FormSummary[1];
      forms[0] = new FormSummary(UIConsts.FILTER_NONE, null, null, false, false, null);
    } else {
      // get the previously selected form, and verify it matches
      int currentSelectionIndex = formsBox.getSelectedIndex();
      if (currentSelectionIndex >= 0) {
        String currentFormTitle = formsBox.getItemText(currentSelectionIndex);
        currentFormSelected = displayedFormList[currentSelectionIndex];
        if (currentFormSelected != null) {
          // double check that the titles match,
          // otherwise this would be a not fun bug to track down
          if (!currentFormTitle.equals(currentFormSelected.getTitle())) {
            currentFormSelected = null;
          }
        }
      }
    }

    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available

    formsBox.clear();
    
    // populate the form box
    for (int i = 0; i < forms.length; i++) {
      FormSummary form = forms[i];
      formsBox.insertItem(form.getTitle(), i);
      if (form.equals(currentFormSelected)) {
        selectedIndex = i;
      }
    }

    // update the panel to display the right form
    formsBox.setItemSelected(selectedIndex, true);
    selectedForm = forms[selectedIndex];

    // set the class state to the newly created form list
    displayedFormList = forms;

    updateFilterList();
  }

  private synchronized void updateFilterList() {
    AsyncCallback<FilterSet> callback = new AsyncCallback<FilterSet>() {
      @Override
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      @Override
      public void onSuccess(FilterSet filterSet) {
        AggregateUI.getUI().clearError();
        updateFilterDropDown(filterSet);
      }
    };

    // request the update
    if (selectedForm == null) {
      return;
    }
    if (selectedForm.getId() != null) {
    	SecureGWT.getFilterService().getFilterSet(selectedForm.getId(), callback);
    }

  }

  private synchronized void updateFilterDropDown(FilterSet filterSet) {

    FilterGroup currentFilterSelected = null;
    FilterGroup defaultFilterGroup = new FilterGroup(UIConsts.FILTER_NONE, selectedForm.getId(), null);
    
    // create what should be the new filter gorup
    List<FilterGroup> filterGroups  = new ArrayList<FilterGroup>();
    filterGroups.add(defaultFilterGroup);
    filterGroups.addAll(filterSet.getGroups());

    // get the previously selected filter, and verify it matches
    int currentSelectionIndex = filtersBox.getSelectedIndex();
    if (currentSelectionIndex >= 0) {
      String currentFilterTitle = filtersBox.getItemText(currentSelectionIndex);
      currentFilterSelected = displayedFilterList.get(currentSelectionIndex);
      if (currentFilterSelected != null) {
        // double check that the titles match,
        // otherwise this would be a not fun bug to track down
        if (!currentFilterTitle.equals(currentFilterSelected.getName())) {
          currentFilterSelected = null;
        }
      }
    }

    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available
    filtersBox.clear();
    
    // populate the form box
    for (int i = 0; i < filterGroups.size(); i++) {
      FilterGroup filter = filterGroups.get(i);
      filtersBox.insertItem(filter.getName(), i);
      if (filter.equals(currentFilterSelected)) {
        selectedIndex = i;
      }
    }

    // update the panel to display the right filter
    filtersBox.setItemSelected(selectedIndex, true);

    // set the class state to the newly created filter list
    displayedFilterList = filterGroups;
  }

  public ListBox getFormsBox() {
    return formsBox;
  }

  public ListBox getFiltersBox() {
    return filtersBox;
  }

  /**
   * Handler to process the change in the form drop down
   * 
   */
  private class ChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      AggregateUI.getUI().getTimer().restartTimer();
      for (FormSummary form : displayedFormList) {
        if (form.getTitle().compareTo(formsBox.getValue(formsBox.getSelectedIndex())) == 0) {
          selectedForm = form;
          break;
        }
      }
      updateFilterList();
    }
  }
}