package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.CheckBox;

public class ACheckBoxBase extends CheckBox implements ValueChangeHandler<Boolean> {
  
  public ACheckBoxBase() {
    super();
  }
  
  @Override
  public void onValueChange(ValueChangeEvent<Boolean> event) {
    AggregateUI baseUI = AggregateUI.getUI();
    baseUI.clearError();
    baseUI.getTimer().restartTimer();
  }
  


}
