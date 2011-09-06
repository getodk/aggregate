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
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.CreateExportButton;
import org.opendatakit.aggregate.client.widgets.FileTypeListBox;
import org.opendatakit.aggregate.client.widgets.KmlSettingListBox;
import org.opendatakit.aggregate.constants.common.ExportType;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExportPopup extends PopupPanel {

  private static final String GEOPOINT_TOOLTIP = "Geopoint field to map";
  private static final String BINARY_TOOLTIP = "Binary field to display";
  private static final String TITLE_TOOLTIP = "Field to use as Title";
  
  private boolean gotKmlOptions = false;
  private FlexTable layout;
  private FileTypeListBox fileType;

  private String formId;

  private KmlSettingListBox geoPointsDropDown;
  private KmlSettingListBox titleFieldsDropDown;
  private KmlSettingListBox binaryFieldsDropDown;

  private CreateExportButton exportButton;

  public ExportPopup(String formid) {
    super(false);
    this.formId = formid;

    layout = new FlexTable();

    geoPointsDropDown = new KmlSettingListBox(GEOPOINT_TOOLTIP);
    titleFieldsDropDown = new KmlSettingListBox(TITLE_TOOLTIP);
    binaryFieldsDropDown = new KmlSettingListBox(BINARY_TOOLTIP);

    exportButton = new CreateExportButton(this);
    fileType = new FileTypeListBox(this);

 
    SecureGWT.getFormService().getPossibleKmlSettings(formId, new KmlSettingsCallback());

    layout.setWidget(0, 0, new ClosePopupButton(this));
    layout.setWidget(0, 1, new HTML("<h3>Form:<h3>"));
    layout.setWidget(0, 2, new HTML(formId));

    layout.setWidget(0, 3, new HTML("<h3>Type:<h3>"));
    layout.setWidget(0, 4, fileType);
    layout.setWidget(0, 6, exportButton);

    layout.setWidget(1, 1, new HTML("<h4>Geopoint:<h4>"));
    layout.setWidget(1, 2, geoPointsDropDown);

    layout.setWidget(1, 3, new HTML("<h4>Title:<h4>"));
    layout.setWidget(1, 4, titleFieldsDropDown);

    layout.setWidget(1, 5, new HTML("<h4>Picture:<h4>"));
    layout.setWidget(1, 6, binaryFieldsDropDown);

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
    ExportType type = fileType.getExportType();

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
      exportButton.setEnabled(true);
      disableKmlOptions();
      break;
    default: // unknown type
      exportButton.setEnabled(false);
      disableKmlOptions();
      break;
    }
  }

  public void createExport() {
    ExportType type = ExportType.valueOf(fileType.getValue(fileType.getSelectedIndex()));

    if (type.equals(ExportType.CSV)) {
      SecureGWT.getFormService().createCsv(formId, null, new CreateExportCallback());
    } else { // .equals(ExportType.KML.toString())
      String geoPointValue = geoPointsDropDown.getElementKey();     
      String titleValue = titleFieldsDropDown.getElementKey();     
      String binaryValue = binaryFieldsDropDown.getElementKey();
      
      SecureGWT.getFormService().createKml(formId, geoPointValue,
          titleValue, binaryValue, new CreateExportCallback());
    }
  }
  
  private class CreateExportCallback implements AsyncCallback<Boolean> {
    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(Boolean result) {
      if(result) {      
        AggregateUI.getUI().redirectToSubTab(SubTabs.EXPORT);
      } else {
        Window.alert("Either the Geopoint field or your Title field were invalid");
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
  
}
