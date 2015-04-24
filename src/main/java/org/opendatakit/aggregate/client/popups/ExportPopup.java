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

package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.SecureGWT;
import org.opendatakit.aggregate.client.filter.FilterGroup;
import org.opendatakit.aggregate.client.filter.FilterSet;
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.client.widgets.AggregateButton;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.EnumListBox;
import org.opendatakit.aggregate.client.widgets.FilterListBox;
import org.opendatakit.aggregate.client.widgets.KmlSettingListBox;
import org.opendatakit.aggregate.constants.common.ExportType;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public final class ExportPopup extends AbstractPopupBase {

  private static final String FILE_TYPE_TOOLTIP = "Type of File to Generate";
  private static final String FILE_TYPE_BALLOON = "Select the type of file you wish to create.";
  private static final String GEOPOINT_TOOLTIP = "Geopoint field to map";
  private static final String BINARY_TOOLTIP = "Binary field to display";
  private static final String TITLE_TOOLTIP = "Field to use as Title";

  private static final String CREATE_BUTTON_TXT = "<img src=\"images/green_right_arrow.png\" /> Export";
  private static final String CREATE_BUTTON_TOOLTIP = "Create Export File";
  private static final String CREATE_BUTTON_HELP_BALLOON = "This creates either a CSV or KML file of your data.";

  private static final String PROBLEM_NULL_FILTER_GROUP = "Filter group is invalid";
  private static final String EXPORT_ERROR_MSG = "Either the Geopoint field or your Title field were invalid";
  private static final String GEOPOINT_BALLOON = "Choose the geopoint field to map.";
  private static final String TITLE_BALLOON = "Choose the field for the title.";
  private static final String BINARY_BALLOON = "Choose the binary field to display.";

  private boolean gotKmlOptions = false;
  // this will be the main flex table for the popups
  private final FlexTable layout;
  // this will be the standard header across the top
  private final FlexTable topBar;
  // this is the bottom bar that houses the KML options
  private final FlexTable bottomBar;
  private final EnumListBox<ExportType> fileType;

  private final FilterListBox filtersBox;

  private final KmlSettingListBox geoPointsDropDown;
  private final KmlSettingListBox titleFieldsDropDown;
  private final KmlSettingListBox binaryFieldsDropDown;

  private final AggregateButton exportButton;

  private final String formId;

  public ExportPopup(String formid, FilterGroup selectedFilterGroup) {
    super();
    this.formId = formid;

    // ensure the filter group passed in is for the correct form
    if (selectedFilterGroup != null && formid.equals(selectedFilterGroup.getFormId())) {
      filtersBox = new FilterListBox(selectedFilterGroup);
    } else {
      filtersBox = new FilterListBox();
    }

    geoPointsDropDown = new KmlSettingListBox(GEOPOINT_TOOLTIP, GEOPOINT_BALLOON);
    titleFieldsDropDown = new KmlSettingListBox(TITLE_TOOLTIP, TITLE_BALLOON);
    binaryFieldsDropDown = new KmlSettingListBox(BINARY_TOOLTIP, BINARY_BALLOON);

    SecureGWT.getFormService().getPossibleKmlSettings(formId, new KmlSettingsCallback());
    SecureGWT.getFilterService().getFilterSet(formId, new FiltersCallback());

    exportButton = new AggregateButton(CREATE_BUTTON_TXT, CREATE_BUTTON_TOOLTIP,
        CREATE_BUTTON_HELP_BALLOON);
    exportButton.addClickHandler(new CreateExportHandler());

    fileType = new EnumListBox<ExportType>(ExportType.values(), FILE_TYPE_TOOLTIP,
        FILE_TYPE_BALLOON);
    fileType.addChangeHandler(new ExportTypeChangeHandler());
    
    // set the standard header widgets
    topBar = new FlexTable();
    topBar.addStyleName("stretch_header");
    topBar.setWidget(0, 0, new HTML("<h2> Form:</h2>"));
    topBar.setWidget(0, 1, new HTML(formId));
    topBar.setWidget(0, 2, new HTML("<h2>Type:</h2>"));
    topBar.setWidget(0, 3, fileType);
    topBar.setWidget(0, 4, new HTML("<h2>Filter:</h2>"));
    topBar.setWidget(0, 5, filtersBox);
    topBar.setWidget(0, 6, exportButton);
    topBar.setWidget(0, 7, new ClosePopupButton(this));
    
    // set the widgets for the kml option bar
    bottomBar = new FlexTable();
    bottomBar.addStyleName("flexTableBorderTopStretchWidth");
    bottomBar.setWidget(0, 0, new HTML("<h4>Geopoint:<h4>"));
    bottomBar.setWidget(0, 1, geoPointsDropDown);
    bottomBar.setWidget(0, 2, new HTML("<h4>Title:<h4>"));
    bottomBar.setWidget(0, 3, titleFieldsDropDown);
    bottomBar.setWidget(0, 4, new HTML("<h4>Picture:<h4>"));
    bottomBar.setWidget(0, 5, binaryFieldsDropDown);

    layout = new FlexTable();
    layout.setWidget(0, 0, topBar);
    layout.setWidget(1, 0, bottomBar);

    updateUIOptions();

    setWidget(layout);
  }

  private void enableKmlOptions() {
    geoPointsDropDown.setEnabled(true);
    titleFieldsDropDown.setEnabled(true);
    binaryFieldsDropDown.setEnabled(true);
    layout.getRowFormatter().setStyleName(1, "enabledTableRow");
  }

  private void disableKmlOptions() {
    geoPointsDropDown.setEnabled(false);
    titleFieldsDropDown.setEnabled(false);
    binaryFieldsDropDown.setEnabled(false);
    layout.getRowFormatter().setStyleName(1, "disabledTableRow");
  }

  public void updateUIOptions() {
    ExportType type = fileType.getSelectedEnumValue();

    if (type == null) {
      exportButton.setEnabled(false);
      disableKmlOptions();
      return;
    }

    switch (type) {
    case KML:
      if (gotKmlOptions) {
        exportButton.setEnabled(true);
      } else {
        exportButton.setEnabled(false);
      }
      enableKmlOptions();
      break;
    case CSV:
    case JSONFILE:
      exportButton.setEnabled(true);
      disableKmlOptions();
      break;
    default: // unknown type
      exportButton.setEnabled(false);
      disableKmlOptions();
      break;
    }
  }

  private class CreateExportCallback implements AsyncCallback<Boolean> {

    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(Boolean result) {
      if (result) {
        AggregateUI.getUI().redirectToSubTab(SubTabs.EXPORT);
      } else {
        Window.alert(EXPORT_ERROR_MSG);
      }

      hide();
    }
  }

  private class KmlSettingsCallback implements AsyncCallback<KmlSettings> {
    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(KmlSettings result) {
      gotKmlOptions = true;
      geoPointsDropDown.updateValues(result.getGeopointNodes());
      titleFieldsDropDown.updateValues(result.getTitleNodes());
      binaryFieldsDropDown.updateValues(result.getBinaryNodes());
    }
  }

  private class FiltersCallback implements AsyncCallback<FilterSet> {

    private static final String PROBLEM_NULL_FILTER_SET = "PROBLEM: got a NULL for a filterSet from server";

    @Override
    public void onFailure(Throwable caught) {
      filtersBox.updateFilterDropDown(null);
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(FilterSet filterSet) {
      if (filterSet == null) {
        AggregateUI.getUI().reportError(new Throwable(PROBLEM_NULL_FILTER_SET));
      }

      // updates the filter dropdown and sets the class state to the newly
      // created filter list
      filtersBox.updateFilterDropDown(filterSet);
    }
  };

  private class ExportTypeChangeHandler implements ChangeHandler {
    @Override
    public void onChange(ChangeEvent event) {
      updateUIOptions();
    }
  }

  private class CreateExportHandler implements ClickHandler {
    @Override
    public void onClick(ClickEvent event) {
      ExportType type = ExportType.valueOf(fileType.getValue(fileType.getSelectedIndex()));

      FilterGroup filterGroup = filtersBox.getSelectedFilter();
      
      if ( filterGroup == null ) {
        AggregateUI.getUI().reportError(new Throwable(PROBLEM_NULL_FILTER_GROUP));
        return;
      }

      if (type == ExportType.CSV) {
        SecureGWT.getFormService().createCsvFromFilter(filterGroup, new CreateExportCallback());
      } else if (type == ExportType.JSONFILE) {
        SecureGWT.getFormService().createJsonFileFromFilter(filterGroup, new CreateExportCallback());
      } else if( type == ExportType.KML) {
        String geoPointValue = geoPointsDropDown.getElementKey();
        String titleValue = titleFieldsDropDown.getElementKey();
        String binaryValue = binaryFieldsDropDown.getElementKey();

        SecureGWT.getFormService().createKmlFromFilter(filterGroup, geoPointValue, titleValue, binaryValue,
            new CreateExportCallback());
      } else {
        new ErrorDialog().show();
      }

    }
  }
  
  private static class ErrorDialog extends DialogBox {

    public ErrorDialog() {
      setText("Error Unknown Export Type!! Please file an Issue on ODK website!");
      Button ok = new Button("OK");
      ok.addClickHandler(new ClickHandler() {
        public void onClick(ClickEvent event) {
          ErrorDialog.this.hide();
        }
      });
      setWidget(ok);
    }
  }
}
