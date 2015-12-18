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

package org.opendatakit.aggregate.client.table;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.exception.FormNotAvailableException;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.client.preferences.Preferences;
import org.opendatakit.aggregate.client.widgets.EnketoWebformButton;
import org.opendatakit.aggregate.client.widgets.ExportButton;
import org.opendatakit.aggregate.client.widgets.FilterListBox;
import org.opendatakit.aggregate.client.widgets.FormListBox;
import org.opendatakit.aggregate.client.widgets.PublishButton;
import org.opendatakit.aggregate.client.widgets.VisualizationButton;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;

public class FilterNavigationTable extends FlexTable {

  // ui elements
  private FormListBox formsBox;
  private FilterListBox filtersBox;
  private FilterSubTab filterSubTab;

  // state
  private FormSummary selectedForm;
  private boolean showEnketoIntegration;

  public FilterNavigationTable(FilterSubTab filterSubTab) {
    this.filterSubTab = filterSubTab;
    // invert this because we have not yet initialized the actionTable...
    showEnketoIntegration = !Preferences.showEnketoIntegration();

    formsBox = new FormListBox(new FormChangeDropDownHandler());
    filtersBox = new FilterListBox(new FilterChangeDropDownHandler());

    FlexTable formNFilterTable = new FlexTable();
    formNFilterTable.getElement().setId("form_and_goal_selection");
    formNFilterTable.setHTML(0, 0, "<h2 id=\"form_name\"> Form </h2>");
    formNFilterTable.setWidget(0, 1, formsBox);
    formNFilterTable.setHTML(0, 2, "<h2 id=\"filter_name\"> Filter </h2>");
    formNFilterTable.setWidget(0, 3, filtersBox);

    setWidget(0, 0, formNFilterTable);
  }

  public void updateNavTable(FilterGroup filterGroup) {
    FlexTable actionTable = new FlexTable();
    actionTable.getElement().setAttribute("align", "right");

    // end goals vis, export, publish
    int columnIndex = 0;
    VisualizationButton visualizeButton = new VisualizationButton(filterSubTab);
    actionTable.setWidget(0, columnIndex, visualizeButton);

    if (Preferences.showEnketoIntegration()) {
      columnIndex++;
      actionTable.setWidget(0, columnIndex, new EnketoWebformButton(formsBox.getSelectedForm()
          .getId()));
    }
    columnIndex++;
    ExportButton exportButton = new ExportButton(filterGroup.getFormId(), filterGroup);
    actionTable.setWidget(0, columnIndex, exportButton);

    columnIndex++;
    PublishButton publishButton = new PublishButton(filterGroup.getFormId());
    actionTable.setWidget(0, columnIndex, publishButton);

    if ( getCellCount(0) == 2 ) {
      removeCell(0, 1);
    }

    setWidget(0, 1, actionTable);
  }

  public void updateNavAfterSave(FilterGroup filterGroup) {
    filtersBox.updateSelectedFilterAfterSave(filterGroup);
  }

  public ArrayList<FilterGroup> getCurrentFilters() {
    return filtersBox.getDisplayedFilterList();
  }

  public void update() {
    GWT.log("inside FilterNavigationTable.update");

    // Set up the callback object.
    AsyncCallback<ArrayList<FormSummary>> callback = new AsyncCallback<ArrayList<FormSummary>>() {
      public void onFailure(Throwable caught) {
        // something failed...
        selectedForm = null;

        updateFilterList();
        AggregateUI.getUI().reportError(caught);
      }

      public void onSuccess(ArrayList<FormSummary> formsFromService) {
        AggregateUI.getUI().clearError();

        // setup the display with the latest updates
        formsBox.updateFormDropDown(formsFromService);

        // update the class state with the currently displayed form
        selectedForm = formsBox.getSelectedForm();

        updateFilterList();
      }
    };

    // Make the call to the form service.
    SecureGWT.getFormService().getForms(callback);
  }

  private synchronized void updateFilterList() {
    GWT.log("inside FilterNavigationTable.updateFilterList");

    if (selectedForm == null || selectedForm.getId().equals(BasicConsts.EMPTY_STRING)) {
      // no form
      // therefore no filters ... update filter box
      filtersBox.updateFilterDropDown(null);
      // update the submissions display
      updateSelectedFormNFilter();
      return;
    }

    // otherwise, request the filters appropriate for this form...
    AsyncCallback<FilterSet> callback = new AsyncCallback<FilterSet>() {
      @Override
      public void onFailure(Throwable caught) {
        if (caught instanceof FormNotAvailableException) {
          // the form is now not available, restart the update process
          GWT.log("form not available - restarting form/filter update FilterNavigationTable");
          update();
        } else {
          // no filters... update filter box
          filtersBox.updateFilterDropDown(null);
          // update the submissions display
          updateSelectedFormNFilter();
          AggregateUI.getUI().reportError(caught);
        }
      }

      @Override
      public void onSuccess(FilterSet filterSet) {
        AggregateUI.getUI().clearError();
        // updates the filter dropdown and sets the class state to the newly
        // created filter list
        filtersBox.updateFilterDropDown(filterSet);
        // update the submissions display
        updateSelectedFormNFilter();
      }
    };

    // request the filters for the form...
    SecureGWT.getFilterService().getFilterSet(selectedForm.getId(), callback);

  }

  private void updateSelectedFormNFilter() {
    GWT.log("inside FilterNavigationTable.updateSelectedFormNFilter");
    FormSummary form = formsBox.getSelectedForm();
    FilterGroup filterGroup = filtersBox.getSelectedFilter();

    // verify a form and filter group exist
    if (form == null || filterGroup == null) {
      return;
    }

    boolean newShowEnketoIntegration = Preferences.showEnketoIntegration();
    if ( newShowEnketoIntegration != showEnketoIntegration ) {
      showEnketoIntegration = newShowEnketoIntegration;
      updateNavTable(filterGroup);
    }

    filterSubTab.switchFilterGroup(filterGroup);
  }

  /**
   * Handler to process the change in the form drop down
   *
   */
  private class FormChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      AggregateUI.getUI().getTimer().restartTimer();
      FormSummary form = formsBox.getSelectedForm();
      if (form != null) {
        if (selectedForm == null || !selectedForm.equals(form)) {
          selectedForm = form;
          // update filter list based on new form
          // NOTE: the filter list MUST be updated BEFORE the selected
          // updateSelectedFormNFilter() is called
          updateFilterList();
        }
        // otherwise no-op (form unchanged)...
      } else {
        // no selected form...
        selectedForm = null;
        updateFilterList();
      }
    }
  }

  /**
   * Handler to process the change in the filter drop down
   *
   */
  private class FilterChangeDropDownHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      AggregateUI.getUI().getTimer().restartTimer();
      updateSelectedFormNFilter();
    }
  }

}
