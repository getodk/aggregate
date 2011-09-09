package org.opendatakit.aggregate.client.widgets;

import java.util.List;

import org.opendatakit.aggregate.client.form.KmlSettingOption;

public class KmlSettingListBox extends AbstractListBox {

  public KmlSettingListBox(String tooltipText) {
    super(tooltipText, false);
  }

  public void updateValues(List<KmlSettingOption> options) {
    clear();
    for (KmlSettingOption kSO : options) {
      addItem(kSO.getDisplayName(), kSO.getElementKey());
    }
  }
  
  public String getElementKey() {
    int index = getSelectedIndex();
    int size = getItemCount();
    
    String geoPointValue = null;
    if(size > index && size > 0) {
      geoPointValue = getValue(index);
    }
    return geoPointValue;
  }
}
