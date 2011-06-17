package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;

public class AButtonBase extends Button implements ClickHandler {
  
  public AButtonBase(String buttonText) {
    super(buttonText);
  }

  @Override
  public void onClick(ClickEvent event) {
    AggregateUI baseUI = AggregateUI.getUI();
    baseUI.clearError();
    baseUI.getTimer().restartTimer();
  }

}
