package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;
import org.opendatakit.aggregate.client.externalserv.GeoPointElement;

public class GeoPointListBox extends AggregateListBox {

  public GeoPointListBox(String tooltipText, String balloonText) {
    super(tooltipText, false, balloonText);
  }

  public void updateValues(ArrayList<GeoPointElement> geopoints) {
    clear();
    for (GeoPointElement geo : geopoints) {
      addItem(geo.getDisplayName(), geo.getElementKey());
    }
  }

  public String getElementKey() {
    int index = getSelectedIndex();
    int size = getItemCount();

    String geoPointValue = null;
    if (size > index && size > 0) {
      geoPointValue = getValue(index);
    }
    return geoPointValue;
  }

}
