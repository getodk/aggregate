package org.opendatakit.aggregate.client.table;

import java.util.List;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.UIUtils;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.widgets.FetchFormButton;

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

  public void fetchClicked() {
    FormSummary form = UIUtils.getFormFromSelection(formsBox, displayedFormList);
    FilterGroup filterGroup = UIUtils.getFilterFromSelection(filtersBox, displayedFilterList);
    filterSubTab.switchForm(form, filterGroup);
  }

  public ListBox getFormsBox() {
    return formsBox;
  }

  public ListBox getFiltersBox() {
    return filtersBox;
  }
  
  public void update() {
    // Set up the callback object.
    AsyncCallback<FormSummary[]> callback = new AsyncCallback<FormSummary[]>() {
      public void onFailure(Throwable caught) {
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(FormSummary[] formsFromService) {
        AggregateUI.getUI().clearError();
        
        // setup the display with the latest updates
        // update the class state with the updated form list
        displayedFormList = UIUtils.updateFormDropDown(formsBox, displayedFormList, formsFromService);
        
        // update the class state with the currently displayed form
        selectedForm = UIUtils.getFormFromSelection(formsBox, displayedFormList);
        
        updateFilterList();
      }
    };

    // Make the call to the form service.
    SecureGWT.getFormService().getForms(callback);
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

        // updates the filter dropdown and sets the class state to the newly created filter list
        displayedFilterList = UIUtils.updateFilterDropDown(filtersBox, selectedForm, displayedFilterList, filterSet);
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

  /**
   * Handler to process the change in the form drop down
   * 
   */
  private class ChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      AggregateUI.getUI().getTimer().restartTimer();
      FormSummary form = UIUtils.getFormFromSelection(formsBox, displayedFormList);
      if(form != null) {
        selectedForm = form;
      }

      updateFilterList();
    }
  }
}