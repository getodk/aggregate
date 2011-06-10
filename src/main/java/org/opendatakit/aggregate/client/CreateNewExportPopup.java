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

import java.util.List;

import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.KmlSettingOption;
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.constants.common.ExportType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;

public class CreateNewExportPopup extends PopupPanel {

  private List<KmlSettingOption> geoPoints = null;
  private List<KmlSettingOption> titleFields = null;
  private List<KmlSettingOption> binaryFields = null;
  private boolean gotKmlOptions = false;
  private ManageTabUI parent;
  private FlexTable layout;
  private ListBox fileType;

  private ListBox geoPointsDropDown;
  private ListBox titleFieldsDropDown;
  private ListBox binaryFieldsDropDown;

  private Button exportButton;

  private void rePopulateExportsAndRedirect() {
    parent.getBase().getSubmissionNav().setupExportPanel();
    parent.selectTab(1);
    hide();
  }

  public CreateNewExportPopup(final String formId, final FormServiceAsync formSvc,
      final ManageTabUI parent, final AggregateUI baseUI) {
    super(false);
    this.parent = parent;
    layout = new FlexTable();
    fileType = new ListBox();

    geoPointsDropDown = new ListBox();
    titleFieldsDropDown = new ListBox();
    binaryFieldsDropDown = new ListBox();

    exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");

    Button closeButton = new Button("<img src=\"images/red_x.png\" />");
    closeButton.addStyleDependentName("close");
    closeButton.addStyleDependentName("negative");
    closeButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        hide();
        baseUI.getTimer().restartTimer();
      }
    });
    layout.setWidget(0, 0, closeButton);

    layout.setWidget(0, 1, new HTML("<h3>Form:<h3>"));
    layout.setWidget(0, 2, new HTML(formId));

    for (ExportType eT : ExportType.values()) {
      fileType.addItem(eT.toString());
    }

    updateUIOptions();

    layout.setWidget(0, 3, new HTML("<h3>Type:<h3>"));
    layout.setWidget(0, 4, fileType);

    layout.setWidget(1, 1, new HTML("<h4>Geopoint:<h4>"));
    layout.setWidget(1, 2, geoPointsDropDown);

    layout.setWidget(1, 3, new HTML("<h4>Title:<h4>"));
    layout.setWidget(1, 4, titleFieldsDropDown);

    layout.setWidget(1, 5, new HTML("<h4>Picture:<h4>"));
    layout.setWidget(1, 6, binaryFieldsDropDown);
    // geoPointsDropDown.setEnabled(false);
    // titleFieldsDropDown.setEnabled(false);
    // binaryFieldsDropDown.setEnabled(false);

    layout.setWidget(0, 6, exportButton);

    fileType.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        updateUIOptions();
      }
    });

    formSvc.getPossibleKmlSettings(formId, new AsyncCallback<KmlSettings>() {
      @Override
      public void onFailure(Throwable caught) {
        // TODO Auto-generated method stub
      }

      @Override
      public void onSuccess(KmlSettings result) {
        // TODO Auto-generated method stub
        gotKmlOptions = true;
        geoPoints = result.getGeopointNodes();
        titleFields = result.getTitleNodes();
        binaryFields = result.getBinaryNodes();
        if (fileType.getItemText(fileType.getSelectedIndex()).equals(ExportType.KML.toString())) {
          geoPointsDropDown.setEnabled(true);
          titleFieldsDropDown.setEnabled(true);
          binaryFieldsDropDown.setEnabled(true);
        }
        for (KmlSettingOption kSO : geoPoints)
          geoPointsDropDown.addItem(kSO.getDisplayName());
        for (KmlSettingOption kSO : titleFields)
          titleFieldsDropDown.addItem(kSO.getDisplayName());
        for (KmlSettingOption kSO : binaryFields)
          binaryFieldsDropDown.addItem(kSO.getDisplayName());
      }
    });

    exportButton.addClickHandler(new ClickHandler() {
      @Override
      public void onClick(ClickEvent event) {
        String selectedFileType = fileType.getItemText(fileType.getSelectedIndex());
        if (selectedFileType.equals(ExportType.CSV.toString())) {
          formSvc.createCsv(formId, new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
              // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(Boolean result) {
              rePopulateExportsAndRedirect();
            }
          });
        } else { // .equals(ExportType.KML.toString())
          formSvc.createKml(formId, geoPoints.get(geoPointsDropDown.getSelectedIndex())
              .getElementKey(), titleFields.get(titleFieldsDropDown.getSelectedIndex())
              .getElementKey(), binaryFields.get(binaryFieldsDropDown.getSelectedIndex())
              .getElementKey(), new AsyncCallback<Boolean>() {
            @Override
            public void onFailure(Throwable caught) {
              // TODO Auto-generated method stub
            }

            @Override
            public void onSuccess(Boolean result) {
              rePopulateExportsAndRedirect();
            }
          });
        }
        baseUI.getTimer().restartTimer();
      }
    });

    setWidget(layout);
  }

  private void updateUIOptions() {
    String selectedFileType = fileType.getItemText(fileType.getSelectedIndex());
    if (selectedFileType.equals(ExportType.KML.toString())) {
      if (gotKmlOptions) {
        exportButton.setEnabled(true);
      } else {
        exportButton.setEnabled(false);
      }
      geoPointsDropDown.setEnabled(true);
      titleFieldsDropDown.setEnabled(true);
      binaryFieldsDropDown.setEnabled(true);
      layout.getRowFormatter().setStyleName(1, "enabledTableRow");
    } else {
      exportButton.setEnabled(true);
      geoPointsDropDown.setEnabled(false);
      titleFieldsDropDown.setEnabled(false);
      binaryFieldsDropDown.setEnabled(false);
      layout.getRowFormatter().setStyleName(1, "disabledTableRow");
    }
  }
}
