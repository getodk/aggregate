package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;

public class AggregateImageToggleButton extends ToggleButton implements ClickHandler{
  
  private final AggregateBaseHandlers handlers;
  
  public AggregateImageToggleButton(Image img, String tooltipText) {
    this(img, tooltipText, null);
  }

  public AggregateImageToggleButton(Image img, String tooltipText, String helpBalloonText) {
    super(img);
    
    addClickHandler(this);
    
    handlers = new AggregateBaseHandlers(this, tooltipText, helpBalloonText);  
    addMouseOverHandler(handlers);
    addMouseOutHandler(handlers); 
  }
  
  @Override
  public void onClick(ClickEvent event) {
    handlers.userAction();
  }

  
}
