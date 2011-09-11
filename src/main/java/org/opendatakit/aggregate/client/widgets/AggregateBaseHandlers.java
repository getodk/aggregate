package org.opendatakit.aggregate.client.widgets;

import org.opendatakit.aggregate.client.AggregateUI;
import org.opendatakit.aggregate.client.popups.HelpBalloon;
import org.opendatakit.aggregate.constants.common.UIConsts;

import com.google.gwt.event.dom.client.MouseOutEvent;
import com.google.gwt.event.dom.client.MouseOutHandler;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.user.client.ui.Widget;

public final class AggregateBaseHandlers implements MouseOverHandler, MouseOutHandler {
  
  private final Widget widget;
  private final String tooltipText;
  private final HelpBalloon helpBalloon;
  
  public AggregateBaseHandlers(Widget uiwidget, String tooltipTxt, String helpBalloonTxt) {
    this.widget = uiwidget;
    this.tooltipText = tooltipTxt;
    
    // setup help system
    if(helpBalloonTxt == null) {
      this.helpBalloon = null;
    } else {
      this.helpBalloon = new HelpBalloon(widget, helpBalloonTxt);
    }
    
    widget.setTitle(tooltipText);
    
  }
  
  public void userAction() {
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
      widget.setTitle(UIConsts.EMPTY_STRING);
      
      // show the help balloon;
      helpBalloon.display();
    }
  }

  public void onMouseOut(MouseOutEvent event) {
    if(helpBalloon != null) {
      // restore the tool tip
      widget.setTitle(tooltipText);
      
      // hide the help balloon;
      helpBalloon.hide();
    }
  }

}
