package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.ui.ListBox;

public class AggregateListBox extends ListBox implements ChangeHandler {

  private final AggregateBaseHandlers handlers;
  
  public AggregateListBox(String tooltipText, boolean multipleValueSelection) {
    this(tooltipText, multipleValueSelection, null);    
  }
    
  public AggregateListBox(String tooltipText, boolean multipleValueSelection, String helpBalloonText) {
    super(multipleValueSelection);
  
    addChangeHandler(this);
    
    // setup help system
    handlers = new AggregateBaseHandlers(this, tooltipText, helpBalloonText);  
    addMouseOverHandler(handlers);
    addMouseOutHandler(handlers);  
  }
  
  @Override
  public void onChange(ChangeEvent event) {
    handlers.userAction();
  }
  
}
