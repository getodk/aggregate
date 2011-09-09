package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.ListBox;

public abstract class AbstractListBox extends ListBox implements ChangeHandler, MouseOverHandler, MouseOutHandler {

  private String tooltipText;
  protected HelpBalloon helpBalloon;
  
  public AbstractListBox(String tooltipText, boolean multipleValueSelection) {
    super(multipleValueSelection);
  
    addChangeHandler(this);
    
    // setup help system
    this.helpBalloon = null;
    this.tooltipText = tooltipText;
    setTitle(tooltipText);
    
    addMouseOverHandler(this);
    addMouseOutHandler(this);
  }
  
  public void onChange(ChangeEvent event) {
    AggregateUI baseUI = AggregateUI.getUI();
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
