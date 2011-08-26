package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.FilterSubTab;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class MetadataCheckBox extends AbstractCheckBoxBase implements ValueChangeHandler<Boolean> {
  
  private static final String TOOLTIP_TEXT = "Display or hide metadata";
  
  private FilterSubTab filterSubTab;
  
  public MetadataCheckBox(FilterSubTab filterSubTab) {
    super("Display Metadata", TOOLTIP_TEXT);
    this.filterSubTab = filterSubTab;
    
    Boolean inlcudeMetaData = filterSubTab.getDisplayMetaData();
    
    setValue(inlcudeMetaData);
    setEnabled(true);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    super.onValueChange(event);
    
    filterSubTab.setDisplayMetaData(event.getValue());
    filterSubTab.update();
  }

}