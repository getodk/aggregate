package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.constants.common.ExternalServicePublicationOption;

import com.google.gwt.event.dom.client.ChangeEvent;

public class PublishOptionListBox extends AbstractListBox {

  private static final String TOOLTIP_TEXT = "Method data should be published";
  
  public PublishOptionListBox() {
    super(TOOLTIP_TEXT);
    
    for (ExternalServicePublicationOption eso : ExternalServicePublicationOption.values()) {
      addItem(eso.getDescriptionOfOption(), eso.name());
    }
  }
  
  @Override
  public void onChange(ChangeEvent event) {
    super.onChange(event);
  }
 
  public ExternalServicePublicationOption getEsPublishOption() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex > -1) {
          return ExternalServicePublicationOption.valueOf(getValue(selectedIndex));
    } else {
      return null;
    }
  }
}
