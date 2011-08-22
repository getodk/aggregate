package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.FilterSubTab;
import org.opendatakit.aggregate.client.filter.FilterGroup;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;

public class MetadataCheckBox extends ACheckBoxBase implements ValueChangeHandler<Boolean> {
  
  private FilterSubTab filterSubTab;
  
  public MetadataCheckBox(FilterSubTab filterSubTab) {
    super("Display Metadata");
    this.filterSubTab = filterSubTab;
    
    FilterGroup filterGroup = filterSubTab.getDisplayedFilterGroup();
    
    setValue(filterGroup.getIncludeMetadata());
    setEnabled(true);
    addValueChangeHandler(this);
  }

  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    super.onValueChange(event);
    
    FilterGroup filterGroup = filterSubTab.getDisplayedFilterGroup();
    filterGroup.setIncludeMetadata(event.getValue());
    filterSubTab.update();
  }

}