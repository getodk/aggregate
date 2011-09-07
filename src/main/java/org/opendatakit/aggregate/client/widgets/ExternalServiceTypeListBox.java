package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ExternalServicePopup;
import org.opendatakit.aggregate.constants.common.ExternalServiceType;

import com.google.gwt.event.dom.client.ChangeEvent;

public class ExternalServiceTypeListBox extends AbstractListBox {

  private static final String TOOLTIP_TEXT = "Type of External Service Connection";
 
  private ExternalServicePopup popup;
  
  public ExternalServiceTypeListBox(ExternalServicePopup popup) {
    super(TOOLTIP_TEXT);
    this.popup = popup;
    
    addItem(ExternalServiceType.GOOGLE_FUSIONTABLES.getDisplayText(), ExternalServiceType.GOOGLE_FUSIONTABLES.name());
    addItem(ExternalServiceType.GOOGLE_SPREADSHEET.getDisplayText(), ExternalServiceType.GOOGLE_SPREADSHEET.name());

  }
  
  @Override
  public void onChange(ChangeEvent event) {
    super.onChange(event);
    popup.updateUIOptions();
  }
 
  public ExternalServiceType getExternalServiceType() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex > -1) {
      return ExternalServiceType.valueOf(getValue(selectedIndex));
    } else {
      return null;
    }
  }
}
