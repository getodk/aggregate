package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.popups.ExportPopup;
import org.opendatakit.aggregate.constants.common.ExportType;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;

public class FileTypeListBox extends AbstractListBox implements ChangeHandler {
  
  private static final String TOOLTIP_TEXT = "Type of File to Generate";
  
  private ExportPopup popup;
  
  public FileTypeListBox(ExportPopup popup) {
    super(TOOLTIP_TEXT);
    this.popup = popup;
    
    for (ExportType eT : ExportType.values()) {
      addItem(eT.getDisplayText(), eT.name());
    }
  }
  
  @Override
  public void onChange(ChangeEvent event) {
    super.onChange(event);
    popup.updateUIOptions();
  }
  
  public ExportType getExportType() {
    return ExportType.valueOf(getValue(getSelectedIndex()));
  }
}
