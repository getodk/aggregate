package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.KmlSettingOption;
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.client.submission.UIGeoPoint;
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
  
  private void rePopulateExportsAndRedirect() {
    parent.getExportList();
    parent.selectTab(1);
    hide();
  }
  
  public CreateNewExportPopup(final String formId, final FormServiceAsync formSvc, final ManageTabUI parent) {
    super(true);
    this.parent = parent;
    FlexTable layout = new FlexTable();
    
    layout.setWidget(0, 0, new HTML("Form: " + formId + " "));
    
    final ListBox fileType = new ListBox();
    for (ExportType eT : ExportType.values()) {
      fileType.addItem(eT.toString());
    }
    final ListBox geoPointsDropDown = new ListBox();
    layout.setWidget(0, 2, geoPointsDropDown);
    final ListBox titleFieldsDropDown = new ListBox();
    layout.setWidget(0, 3, titleFieldsDropDown);
    final ListBox binaryFieldsDropDown = new ListBox();
    layout.setWidget(0, 4, binaryFieldsDropDown);
    geoPointsDropDown.setEnabled(false);
    titleFieldsDropDown.setEnabled(false);
    binaryFieldsDropDown.setEnabled(false);
    
    final Button exportButton = new Button("<img src=\"images/green_right_arrow.png\" /> Export");
    layout.setWidget(0, 5, exportButton);
    
    exportButton.addClickHandler(new ClickHandler () {
      @Override
      public void onClick(ClickEvent event) {
        String selectedFileType = fileType.getItemText(fileType.getSelectedIndex());
        if (selectedFileType.equals(ExportType.CSV.toString())) {
          formSvc.createCsv(formId, new AsyncCallback<Boolean> () {
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
          formSvc.createKml(formId,
              geoPoints.get(geoPointsDropDown.getSelectedIndex()).getElementKey(),
              titleFields.get(titleFieldsDropDown.getSelectedIndex()).getElementKey(),
              binaryFields.get(binaryFieldsDropDown.getSelectedIndex()).getElementKey(),
              new AsyncCallback<Boolean> () {
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
      }
    });
    
    
    fileType.addChangeHandler(new ChangeHandler() {
      @Override
      public void onChange(ChangeEvent event) {
        String selectedFileType = fileType.getItemText(fileType.getSelectedIndex());
        if (selectedFileType.equals(ExportType.CSV.toString())) {
          exportButton.setEnabled(true);
          geoPointsDropDown.setEnabled(false);
          titleFieldsDropDown.setEnabled(false);
          binaryFieldsDropDown.setEnabled(false);
        } else { // .equals(ExportType.KML.toString())
          if (gotKmlOptions) {
            exportButton.setEnabled(true);
          } else {
            exportButton.setEnabled(false);
          }
          geoPointsDropDown.setEnabled(true);
          titleFieldsDropDown.setEnabled(true);
          binaryFieldsDropDown.setEnabled(true);          
        }
      }
    });
    layout.setWidget(0, 1, fileType);
    
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
        if (fileType.getItemText(fileType.getSelectedIndex()).equals(
            ExportType.KML.toString())) {
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
    
    setWidget(layout);
  }
}
