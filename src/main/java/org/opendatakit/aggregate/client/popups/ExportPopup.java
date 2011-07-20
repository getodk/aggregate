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
import org.opendatakit.aggregate.client.SubmissionTabUI;
import org.opendatakit.aggregate.client.form.KmlSettingOption;
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.client.widgets.ClosePopupButton;
import org.opendatakit.aggregate.client.widgets.CreateExportButton;
import org.opendatakit.aggregate.constants.common.ExportType;
import org.opendatakit.aggregate.constants.common.SubTabs;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;

public class ExportPopup extends PopupPanel {

  private boolean gotKmlOptions = false;
  private FlexTable layout;
  private ListBox fileType;

  private String formId;

  private ListBox geoPointsDropDown;
  private ListBox titleFieldsDropDown;
  private ListBox binaryFieldsDropDown;

  private CreateExportButton exportButton;

  public ExportPopup(String formid) {
    super(false);
    this.formId = formid;

    layout = new FlexTable();

    geoPointsDropDown = new ListBox();
    titleFieldsDropDown = new ListBox();
    binaryFieldsDropDown = new ListBox();

    exportButton = new CreateExportButton(this);

    fileType = new ListBox();
    fileType.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateUIOptions();
      }
    });

    for (ExportType eT : ExportType.values()) {
      fileType.addItem(eT.getDisplayText(), eT.name());
    }

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

  private void updateUIOptions() {
    ExportType type = ExportType.valueOf(fileType.getValue(fileType.getSelectedIndex()));
    if (type.equals(ExportType.KML)) {
      if (gotKmlOptions) {
        exportButton.setEnabled(true);
      } else {
        exportButton.setEnabled(false);
      }
      enableKmlOptions();

    } else {
      exportButton.setEnabled(true);
      disableKmlOptions();
    }
  }

  public void createExport() {
    ExportType type = ExportType.valueOf(fileType.getValue(fileType.getSelectedIndex()));

    if (type.equals(ExportType.CSV)) {
      SecureGWT.getFormService().createCsv(formId, new CreateExportCallback());
    } else { // .equals(ExportType.KML.toString())
      SecureGWT.getFormService().createKml(formId,
          geoPointsDropDown.getValue(geoPointsDropDown.getSelectedIndex()),
          titleFieldsDropDown.getValue(titleFieldsDropDown.getSelectedIndex()),
          binaryFieldsDropDown.getValue(binaryFieldsDropDown.getSelectedIndex()),
          new CreateExportCallback());
    }
  }
  
  private class CreateExportCallback implements AsyncCallback<Boolean> {
    @Override
    public void onFailure(Throwable caught) {
      AggregateUI.getUI().reportError(caught);
    }

    @Override
    public void onSuccess(Boolean result) {
        SubmissionTabUI subUI = AggregateUI.getUI().getSubmissionNav();
        int tabIndex = subUI.findSubTabIndex(SubTabs.EXPORT);
        subUI.selectTab(tabIndex);
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
      for (KmlSettingOption kSO : result.getGeopointNodes())
        geoPointsDropDown.addItem(kSO.getDisplayName(), kSO.getElementKey());
      for (KmlSettingOption kSO : result.getTitleNodes())
        titleFieldsDropDown.addItem(kSO.getDisplayName(), kSO.getElementKey());
      for (KmlSettingOption kSO : result.getBinaryNodes())
        binaryFieldsDropDown.addItem(kSO.getDisplayName(), kSO.getElementKey());
    }
  }
  
}
