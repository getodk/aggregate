package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.ui.TextBox;

public class AggregateTextBox extends TextBox implements ValueChangeHandler<String>{
  
  private final AggregateBaseHandlers handlers;
  
  public AggregateTextBox(String tooltipText, String helpBalloonText) {
    super();
   
    addValueChangeHandler(this);

    handlers = new AggregateBaseHandlers(this, tooltipText, helpBalloonText);
    addMouseOverHandler(handlers);
    addMouseOutHandler(handlers);
  }
  
  @Override
  public void onValueChange(ValueChangeEvent<String> event) {
    handlers.userAction();
  }
}
