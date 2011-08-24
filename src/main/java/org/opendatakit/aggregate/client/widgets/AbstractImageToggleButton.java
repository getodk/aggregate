package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.ToggleButton;

public abstract class AbstractImageToggleButton extends ToggleButton implements ClickHandler, MouseOverHandler, MouseOutHandler {
  
  private String tooltipText;
  protected HelpBalloon helpBalloon;
  
  public AbstractImageToggleButton(Image img, String tooltipText) {
    super(img);
    
    addClickHandler(this);
    
    // setup help system
    this.helpBalloon = null;
    this.tooltipText = tooltipText;
    setTitle(tooltipText);
    
    addMouseOverHandler(this);
    addMouseOutHandler(this);
  }

  @Override
  public void onClick(ClickEvent event) {
    AggregateUI baseUI = AggregateUI.getUI();
    baseUI.clearError();
    baseUI.getTimer().restartTimer();
  }

  public void onMouseOver(MouseOverEvent event) {
    if(!AggregateUI.getUI().displayingHelpBalloons()) {
      return;
    }
    
    if(helpBalloon != null) {
      // hide the tool tip
      setTitle(UIConsts.EMPTY_STRING);
      
      // show the help balloon;
      helpBalloon.display();
    }
  }

  public void onMouseOut(MouseOutEvent event) {
    if(helpBalloon != null) {
      // restore the tool tip
      setTitle(tooltipText);
      
      // hide the help balloon;
      helpBalloon.hide();
    }
  }
  
}
