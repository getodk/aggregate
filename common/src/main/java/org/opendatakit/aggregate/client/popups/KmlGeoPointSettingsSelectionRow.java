package org.opendatakit.aggregate.client.popups;

import org.opendatakit.aggregate.client.form.KmlGeopointOption;
import org.opendatakit.aggregate.client.form.KmlOptionSetting;
import org.opendatakit.aggregate.client.form.KmlSelection;
import org.opendatakit.aggregate.client.widgets.AggregateCheckBox;
import org.opendatakit.aggregate.client.widgets.KmlSettingListBox;

import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;

public class KmlGeoPointSettingsSelectionRow extends FlexTable implements KmlSelectionGeneration {

  private static final String BINARY_TOOLTIP = "Binary field to display";
  private static final String TITLE_TOOLTIP = "Field to use as Title";
  private static final String INCLUDE_TOOLTIP = "Whether to included in KML";
  
  private static final String TITLE_BALLOON = "Choose the field for the title.";
  private static final String BINARY_BALLOON = "Choose the binary field to display.";
  private static final String INCLUDE_BALLON = "When checked the geo element will be included in KML";
  
  private final String formId;
  private final AggregateCheckBox include;
  
  private final KmlOptionSetting geoPoint;
  private final KmlSettingListBox titleFieldsDropDown;
  private final KmlSettingListBox binaryFieldsDropDown;
  
  KmlGeoPointSettingsSelectionRow(String formID, KmlGeopointOption geopointNode) {
    formId = formID;
    geoPoint = geopointNode.getGeoElement();
    
    include = new AggregateCheckBox(null, false, INCLUDE_TOOLTIP, INCLUDE_BALLON);
    include.setValue(true);
    
    titleFieldsDropDown = new KmlSettingListBox(TITLE_TOOLTIP, TITLE_BALLOON);
    binaryFieldsDropDown = new KmlSettingListBox(BINARY_TOOLTIP, BINARY_BALLOON);

    titleFieldsDropDown.updateValues(geopointNode.getTitleNodes(), true);
    binaryFieldsDropDown.updateValues(geopointNode.getBinaryNodes(), true);

    addStyleName("dataTable");
    setWidget(0, 0, include);
    setWidget(0, 1, new HTML("<h2>Geopoint:<h2>"));
    setWidget(0, 2, new HTML(geoPoint.getDisplayName()));
    setWidget(0, 3, new HTML("<h4>Title:<h4>"));
    setWidget(0, 4, titleFieldsDropDown);
    setWidget(0, 5, new HTML("<h4>Picture:<h4>"));
    setWidget(0, 6, binaryFieldsDropDown);
  }

  @Override
  public KmlSelection generateKmlSelection() {
    // if not checked do not generate the information
    if(!include.getValue())
      return null;
    
    String geoPointValue = geoPoint.getElementKey();
    String titleValue = titleFieldsDropDown.getElementKey();
    String binaryValue = binaryFieldsDropDown.getElementKey();
    
    KmlSelection settings = new KmlSelection(formId);    
    settings.setGeoPointSelections(geoPointValue, titleValue, binaryValue);
    return settings;
  }
  
}
