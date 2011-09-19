package org.opendatakit.aggregate.client.widgets;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;

public class HelpVideoButton extends AggregateButton implements ClickHandler {
  
  private final String url;

  public HelpVideoButton(String buttonText, String url, String tooltipText) {
    super(buttonText, tooltipText);
    this.url = url;
  }

  @Override
  public void onClick(ClickEvent event) {
    super.onClick(event);
    
    Window.Location.assign(url);
  }
}